package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类 课程信息管理
 * </p>
 *
 * @author itcast
 * @since 2024-07-03
 */
public interface CourseBaseService extends IService<CourseBase> {

    PageResult<CourseBase> pageQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto);


    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);
}
