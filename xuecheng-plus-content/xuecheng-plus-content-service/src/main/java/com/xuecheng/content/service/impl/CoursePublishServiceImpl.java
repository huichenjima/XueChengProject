package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.sun.xml.internal.bind.v2.TODO;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * ClassName: CoursePublishServiceImpl
 * Package: com.xuecheng.content.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/11 13:52
 * @Version 1.0
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseService courseBaseService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;





    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(treeNodes);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //提交审核，将课程基本信息，营销信息，课程计划等写入课程预发布表中
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        if (courseBaseInfo==null)
            XueChengPlusException.cast("课程找不到");
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (auditStatus.equals("202003"))
            XueChengPlusException.cast("课程已提交请等待");
        //TODO 本机构只能提交本机构课程
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic))
            XueChengPlusException.cast("请上传课程图片");

        List<TeachplanDto> teachplanDtoList = teachplanService.getTreeNodes(courseId);
        if (teachplanDtoList==null||teachplanDtoList.size()==0)
            XueChengPlusException.cast("请编写课程计划");
//        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        String teachplanDtoListJson = JSON.toJSONString(teachplanDtoList);
        coursePublishPre.setMarket(courseMarketJson);
        coursePublishPre.setTeachplan(teachplanDtoListJson);
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //插入预发布表,请注意预发布表中有了就不能插入，变成更新
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre1==null) //插入情况
            coursePublishPreMapper.insert(coursePublishPre);
        else//更新
            coursePublishPreMapper.updateById(coursePublishPre);
        //插入成功后更新原课程信息表的审计状态
        courseBaseService.lambdaUpdate().eq(CourseBase::getId,courseId).set(CourseBase::getAuditStatus,"202003").update();


    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        //查询预发布表数据
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null)
            XueChengPlusException.cast("请提交审核审核通过后再进行发布");

        //没有审核通过不允许发布
        String status = coursePublishPre.getStatus();

        if (!status.equals("202004"))
            XueChengPlusException.cast("课程没有审核通过不允许发布");

        CoursePublish coursePublish = new CoursePublish();

        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //预发布表的状态是审核状态，发布表的状态的发布状态
        //发布状态
        coursePublish.setStatus("203002");

        //需要先查询课程发布表，如果已经有了课程发布信息，更新
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj==null)
            coursePublishMapper.insert(coursePublish);
        else
        {
            coursePublishMapper.updateById(coursePublish);
        }

        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseService.getById(courseId);
        courseBase.setStatus("203002");
        courseBaseService.updateById(courseBase);




        // TODO 向消息表写数据
//        mqMessageService.addMessage("course_publish",courseId,null,null);
        saveCoursePublishMessage(courseId);

        //将预发布表数据删除

        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     * @return void
     * @author Mr.M
     * @date 2022/9/20 16:32
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    @Override
    public File generateCourseHtml(Long courseId) {
        Configuration configuration = new Configuration(Configuration.getVersion());
        File htmlFile=null;

       try {
           //得到模板
           //指定classpath路径
           String classpath = this.getClass().getResource("/").getPath();
           //设置模板的目录
           configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
           configuration.setDefaultEncoding("utf-8");
           Template template = configuration.getTemplate("course_template.ftl");

           CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

           HashMap<String, Object> map = new HashMap<>();

           map.put("model",coursePreviewInfo);


           String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

           InputStream inputStream = IOUtils.toInputStream(html, "utf-8");

           htmlFile= File.createTempFile("coursepublish",".html");

           FileOutputStream outputStream = new FileOutputStream(htmlFile);
           //使用流将html写入文件
           IOUtils.copy(inputStream,outputStream);
       }
       catch (Exception e){
           log.error("页面静态化出问题，课程id：{}",courseId,e);
           e.printStackTrace();

       }
       return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {

        String upload = null;
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String objectName="course/"+courseId+".html";
            upload = mediaServiceClient.upload(multipartFile, objectName);
            if (upload==null) {
                log.debug("远程调用走降级逻辑得到上传的结果为null，课程id：{}", courseId);
                XueChengPlusException.cast("上传静态文件出错");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件出错");
        }



    }

    /**
     * 根据课程id查询课程发布信息
     * @param courseId
     * @return
     */
    public CoursePublish getCoursePublish(Long courseId){
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }

//    public CoursePublish getCoursePublishCache(Long courseId){
//        //查询缓存
//        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if(jsonObj!=null){
//            String jsonString = jsonObj.toString();
//            System.out.println("=================从缓存查=================");
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        } else {
//            System.out.println("从数据库查询...");
//            //从数据库查询
//            CoursePublish coursePublish = getCoursePublish(courseId);
//            if(coursePublish!=null){
//                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish));
//            }
//            return coursePublish;
//        }
//    }


//    public CoursePublish getCoursePublishCache(Long courseId) {
//
//        //查询缓存
//        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if(jsonObj!=null){
//            String jsonString = jsonObj.toString();
//            if(jsonString.equals("null"))
//                return null;
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        } else {
//            //从数据库查询
//            System.out.println("从数据库查询数据...");
//            CoursePublish coursePublish = getCoursePublish(courseId);
//            //设置过期时间300秒
//            //设置过期时间300秒
//            redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),300+new Random().nextInt(100), TimeUnit.SECONDS);
//            return coursePublish;
//        }
//    }

//    public  CoursePublish getCoursePublishCache(Long courseId){
//
//        //查询缓存
//        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if(jsonObj!=null){
//            String jsonString = jsonObj.toString();
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        }else{
//            synchronized(this){
//                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//                if(jsonObj!=null){
//                    String jsonString = jsonObj.toString();
//                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//                    return coursePublish;
//                }
//                System.out.println("=========从数据库查询==========");
//                //从数据库查询
//                CoursePublish coursePublish = getCoursePublish(courseId);
//                //设置过期时间300秒
//                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),30, TimeUnit.SECONDS);
//                return coursePublish;
//            }
//        }
//
//
//    }
    //使用setnx写分布式锁
//    public  CoursePublish getCoursePublishCache(Long courseId){
//
//        //查询缓存
//        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        if(jsonObj!=null){
//            String jsonString = jsonObj.toString();
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        }else{
//
//            while(true)
//            {
//                Boolean lock = redisTemplate.opsForValue().setIfAbsent("coursequerylock:"+courseId, "01", 1L, TimeUnit.SECONDS);
//                if (lock){
//                    try {
//                        jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//                        if(jsonObj!=null){
//                            String jsonString = jsonObj.toString();
//                            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//                            return coursePublish;
//                        }
//                        System.out.println("=========从数据库查询==========");
//                        //从数据库查询
//                        CoursePublish coursePublish = getCoursePublish(courseId);
//                        //设置过期时间300秒
//                        redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),30, TimeUnit.SECONDS);
//                        return coursePublish;
//                    } finally {
//                        //删除redis的锁
//                    }
//                }
//                else
//                {
//                    //获取锁失败等待一会再获取
//                    // 获取锁失败，等待一段时间后重试
//                    try {
//                        Thread.sleep(100); // 随机等待 100 毫秒，可以避免冲突
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        break; // 中断异常时退出循环
//                    }
//                }
//            }
//
//
//        }
//
//
//    }
        public  CoursePublish getCoursePublishCache(Long courseId){

        //查询缓存
        Object  jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if(jsonObj!=null){
            String jsonString = jsonObj.toString();
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        }else{
            //用redisson获取锁
//            RLock 是一种 可重入锁，意味着同一个线程可以多次获取该锁而不会被阻塞。
            //可以简单理解为一个while true循环一直获取锁
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取分布式锁
            lock.lock();

            try {
                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
                if(jsonObj!=null){
                    String jsonString = jsonObj.toString();
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("=========从数据库查询==========");
                //测试看门狗机制
//                try {
//                    Thread.sleep(60000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                //设置过期时间300秒
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish),30, TimeUnit.SECONDS);
                return coursePublish;
            }
            finally {
                //手动去释放锁
                lock.unlock();
            }



        }


    }


}
