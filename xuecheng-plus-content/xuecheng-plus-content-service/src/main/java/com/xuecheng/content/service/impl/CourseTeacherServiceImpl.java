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
        List<CourseTeacher> courseTeachers = this.lambdaQuery().eq(CourseTeacher::getCourseId, courseId).list();
        if (CollUtil.isEmpty(courseTeachers))
            XueChengPlusException.cast("查不到此课程老师");
        return courseTeachers;
    }

    @Override
    public CourseTeacher addCourseTeacher(CourseTeacherDto courseTeacherDto) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
        if (courseBase==null)
            XueChengPlusException.cast("该课程不存在添加失败");
        CourseTeacher courseTeacher = BeanUtil.copyProperties(courseTeacherDto, CourseTeacher.class);
        courseTeacher.setCreateDate(LocalDateTime.now());
        boolean save = this.save(courseTeacher);
        if (!save)
            XueChengPlusException.cast("插入老师信息失败");
        return courseTeacher;

    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto) {
        CourseTeacher courseTeacher = this.getById(courseTeacherDto.getId());
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
        if (courseBase==null)
            XueChengPlusException.cast("该课程查不到");
        if (courseTeacher==null)
            XueChengPlusException.cast("该老师id查不到");

        BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
        boolean b = this.updateById(courseTeacher);
        if (!b)
            XueChengPlusException.cast("更新老师信息失败");
        return courseTeacher;

    }

    @Override
    public void deleteCourseTeacher( Long courseId, Long teacherId) {
        boolean remove = lambdaUpdate().eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId).remove();
        if (!remove)
            XueChengPlusException.cast("删除老师信息失败");



    }
}
