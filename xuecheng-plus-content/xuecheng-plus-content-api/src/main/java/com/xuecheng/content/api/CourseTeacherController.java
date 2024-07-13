package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * ClassName: CourseTeacherController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/13 18:04
 * @Version 1.0
 */
@RestController
@Slf4j
@Api(tags = "教师管理相关接口")
@RequiredArgsConstructor
public class CourseTeacherController {
    private  final CourseTeacherService courseTeacherService;
//    get /courseTeacher/list/75
//            75为课程id，请求参数为课程id

    @ApiOperation("根据课程id查询老师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher>  queryTeacherByCourseId(@PathVariable(value = "courseId") Long courseId){
        return courseTeacherService.queryTeacherByCourseId(courseId);
    }

    @ApiOperation("添加老师")
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(@RequestBody @Validated(value = {ValidationGroups.Inster.class}) CourseTeacherDto courseTeacherDto)
    {
        return courseTeacherService.addCourseTeacher(courseTeacherDto);
    }
//    put /courseTeacher

    @ApiOperation("修改老师信息")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateCourseTeacher(@RequestBody @Validated(value = {ValidationGroups.Update.class}) CourseTeacherDto courseTeacherDto)
    {
        return courseTeacherService.updateCourseTeacher(courseTeacherDto);
    }

//    delete /ourseTeacher/course/75/26

//            75:课程id
//26:教师id，即course_teacher表的主键
    @ApiOperation("删除老师信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable(value = "courseId") @NotNull Long courseId, @PathVariable(value = "teacherId") @NotNull Long teacherId)
    {
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }



}
