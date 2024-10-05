package com.xuecheng.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: MyCourseTablesServiceImpl
 * Package: com.xuecheng.learning.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/30 13:21
 * @Version 1.0
 */
@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    XcChooseCourseMapper chooseCourseMapper;

    @Autowired
    XcCourseTablesMapper courseTablesMapper;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //调用内容服务查询课程发布基本信息,查询课程的收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null)
            XueChengPlusException.cast("课程不存在");

        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse=null;
        if ("201000".equals(charge)) //免费
        {
            //向两张表都写入数据
            xcChooseCourse = addFreeCourse(userId, coursepublish); //向选课记录表写入数据

            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse); //向我的课程表写入数据


        }
        else//收费情况
        {
            //收费课程只向选课记录表写入数据

            xcChooseCourse = addChargeCoruse(userId, coursepublish);

        }

        // 判断学生的学习资格
        //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);

        //构造返回值
        XcChooseCourseDto xcChooseCourseDto = BeanUtil.copyProperties(xcChooseCourse, XcChooseCourseDto.class);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());


        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表，如果查不到说明没有选课或者没有支付
//        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId)
//                .eq(XcCourseTables::getCourseId, courseId);
//        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(lambdaQueryWrapper);
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if (xcCourseTables==null) {
            xcCourseTablesDto.setLearnStatus("702002"); //没有选课成功或者支付
            xcCourseTablesDto.setCourseId(courseId);
            xcCourseTablesDto.setUserId(userId);
            return xcCourseTablesDto;
        }
        //学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
        xcCourseTablesDto = BeanUtil.copyProperties(xcCourseTables, XcCourseTablesDto.class);
        if (xcCourseTables.getValidtimeStart().isBefore(LocalDateTime.now()))
            xcCourseTablesDto.setLearnStatus("702003"); //过期了
        else
            xcCourseTablesDto.setLearnStatus("702001");//正常学习
        return xcCourseTablesDto;
    }

    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursepublish) {
        Long courseId = coursepublish.getId();
        //如果存在免费的选课记录且选课状态为成功，直接返回
        LambdaQueryWrapper<XcChooseCourse> xcChooseCourseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        xcChooseCourseLambdaQueryWrapper.eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getOrderType,"700001")// 免费课程
                .eq(XcChooseCourse::getStatus,"701001");//选课成功
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(xcChooseCourseLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(xcChooseCourses))
        {
            return xcChooseCourses.get(0);
        }
        //向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001"); //设置选课成功
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期的开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365L));
        int insert = chooseCourseMapper.insert(xcChooseCourse);

        if (insert<=0)
            XueChengPlusException.cast("添加选课记录失败");

        return xcChooseCourse;


    }

    //添加收费课程
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        Long courseId = coursepublish.getId();
        //如果存在免费的选课记录且选课状态为成功，直接返回
        LambdaQueryWrapper<XcChooseCourse> xcChooseCourseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        xcChooseCourseLambdaQueryWrapper.eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getOrderType,"700002")// 收费课程
                .eq(XcChooseCourse::getStatus,"701002");//选课记录为待支付直接返回
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(xcChooseCourseLambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(xcChooseCourses))
        {
            return xcChooseCourses.get(0);
        }
        //向选课记录表写数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");  //收费类型
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002"); //设置为待支付
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());//有效期的开始时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365L));
        int insert = chooseCourseMapper.insert(xcChooseCourse);

        if (insert<=0)
            XueChengPlusException.cast("添加选课记录失败");

        return xcChooseCourse;

    }
    //添加到我的课程表
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){

        //选课成功了才可以往我的课程表里添加数据
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status))
        {
            XueChengPlusException.cast("选课没有成功，无法添加到课程表");
        }
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables!=null)
            return xcCourseTables;
        //添加课程
        xcCourseTables = BeanUtil.copyProperties(xcChooseCourse, XcCourseTables.class);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId()); //记录选课表的主键
//        xcCourseTables.setCourseType(xcChooseCourse.getOrderType()); //选课类型
        xcCourseTables.setUpdateDate(LocalDateTime.now());

        int insert = courseTablesMapper.insert(xcCourseTables);
        if (insert<=0)
            XueChengPlusException.cast("添加我的课程表失败");

        return xcCourseTables;
    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(
                                new LambdaQueryWrapper<XcCourseTables>().
                        eq(XcCourseTables::getUserId, userId).
                        eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }


}
