package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseBaseMapper courseBaseMapper;
    @Override
    public List<CourseTeacher> queryTeacherByCourseId(Long courseId) {
        List<CourseTeacher> courseTeachers = this.lambdaQuery().eq(CourseTeacher::getCourseId, courseId).orderBy(true,true,CourseTeacher::getCreateDate).list();
        if (CollUtil.isEmpty(courseTeachers))
            return Collections.emptyList();
        return courseTeachers;
    }

    @Override
    public CourseTeacher addCourseTeacher(Long companyId,CourseTeacherDto courseTeacherDto) {
        if (courseTeacherDto.getId()==null)//添加
        {
            CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
            if (courseBase==null)
                XueChengPlusException.cast("该课程不存在添加老师失败");
            if (!courseBase.getCompanyId().equals(companyId))
                XueChengPlusException.cast("本机构只能添加本机构的课程老师");

            CourseTeacher courseTeacher = BeanUtil.copyProperties(courseTeacherDto, CourseTeacher.class);
            courseTeacher.setCreateDate(LocalDateTime.now());
            boolean save = this.save(courseTeacher);
            if (!save)
                XueChengPlusException.cast("插入老师信息失败");
            return courseTeacher;
        }
        else
            return updateCourseTeacher(companyId,courseTeacherDto);


    }

    @Override
    public CourseTeacher updateCourseTeacher(Long companyId,CourseTeacherDto courseTeacherDto) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
        if (courseBase==null)
            XueChengPlusException.cast("该课程查不到");
        if (!courseBase.getCompanyId().equals(companyId))
            XueChengPlusException.cast("本机构只能修改本机构的课程老师");
        CourseTeacher courseTeacher = this.getById(courseTeacherDto.getId());
        if (courseTeacher==null)
            XueChengPlusException.cast("该老师id查不到");

        BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
        boolean b = this.updateById(courseTeacher);
        if (!b)
            XueChengPlusException.cast("更新老师信息失败");
        return courseTeacher;

    }

    @Override
    public void deleteCourseTeacher( Long companyId,Long courseId, Long teacherId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null)
            XueChengPlusException.cast("该课程查不到");
        if (!courseBase.getCompanyId().equals(companyId))
            XueChengPlusException.cast("本机构只能删除本机构的课程老师");
        CourseTeacher courseTeacher = this.getById(teacherId);
        if (courseTeacher==null)
            XueChengPlusException.cast("该老师id查不到，删除失败");
        boolean remove = lambdaUpdate().eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId).remove();
        if (!remove)
            XueChengPlusException.cast("删除老师信息失败");



    }
}
