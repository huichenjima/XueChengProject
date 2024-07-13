package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-07-03
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> queryTeacherByCourseId(Long courseId);

    CourseTeacher addCourseTeacher(CourseTeacherDto courseTeacherDto);

    CourseTeacher updateCourseTeacher(CourseTeacherDto courseTeacherDto);

    void deleteCourseTeacher(Long courseId, Long teacherId);
}
