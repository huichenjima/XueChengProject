package com.xuecheng.content.service.jobhandler;

import cn.hutool.core.bean.BeanUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * ClassName: CoursePublishTask
 * Package: com.xuecheng.content.service.jobhandler
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/13 13:24
 * @Version 1.0
 * 课程发布任务类
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }
    //执行发布任务的逻辑
    @Override
    public boolean execute(MqMessage mqMessage) {
        //从mqMessage中拿课程id 即从bussinesskey1
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);

        //向elastsearch写索引数据
        saveCourseIndex(mqMessage,courseId);

        //向redis写缓存




        //返回true表示任务完成

        return true;
    }
    //静态页面保存处理
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        //做任务幂等性处理
        Long taskId=mqMessage.getId();
        //取出该阶段的执行状态
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne>0)
        {
            log.debug("课程静态化任务完成，无需处理");
            return;
        }
        // 开始进行课程静态化生成html页面
        File file = coursePublishService.generateCourseHtml(courseId);

        if (file==null)
        {
            XueChengPlusException.cast("生成的静态页面为空");
        }


        // 将html上传到minio

        coursePublishService.uploadCourseHtml(courseId,file);



        //静态化任务处理完成，任务状态设置为1
        mqMessageService.completedStageOne(taskId);


    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        //做任务幂等性处理
        Long taskId=mqMessage.getId();
        //取出该阶段的执行状态
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo>0)
        {
            log.debug("课程索引信息写入，无需处理");
            return;
        }

        // 开始进行索引信息写入，调用搜索服务 TODO

        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = BeanUtil.copyProperties(coursePublish, CourseIndex.class);
        Boolean add = searchServiceClient.add(courseIndex);

        if (!add){
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
        }

        //静态化任务处理完成，任务状态设置为1
        mqMessageService.completedStageTwo(taskId);

    }
}
