package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: CourseBaseInfoController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/4 13:17
 * @Version 1.0
 */
@Controller
@RestController
@Slf4j
@Api(tags = "课程管理相关接口")
@RequiredArgsConstructor
public class CourseBaseInfoController {
    private final CourseBaseService courseBaseService;


    @ApiOperation("课程查询相关接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") //指定权限标识符，拥有此权限才可以访问此方法
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto)
    {
        //当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        //用户所属的机构id
        Long companyId=null;
        if (StringUtils.isNotEmpty(user.getCompanyId()))
        {
            companyId = Long.parseLong(user.getCompanyId());
        }




        return courseBaseService.pageQuery(companyId,pageParams,queryCourseParamsDto);
    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Inster.class}) AddCourseDto addCourseDto){
        // TODO 获取机构id
        Long companyId=1232141425L;
        return courseBaseService.createCourseBase(companyId,addCourseDto);


    }
    @ApiOperation("根据id" +
            "查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto queryCourse(@PathVariable("courseId") Long id)
    {
        return courseBaseService.getCourseBaseInfo(id);
    }

    @ApiOperation("修改课程接口")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated({ValidationGroups.Update.class}) EditCourseDto editCourseDto){
        // TODO 获取机构id
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //底层是threadlocal
//        System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user.getUsername());
        Long companyId=1232141425L;
        return courseBaseService.updateCourseBase(companyId,editCourseDto);


    }

    @ApiOperation("删除课程接口")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable("courseId") Long courseId)
    {
        // TODO 获取机构id
        Long companyId=1232141425L;
        courseBaseService.deleteCourse(companyId,courseId);;
    }




    


}
