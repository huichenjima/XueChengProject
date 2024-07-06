package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto)
    {

        return courseBaseService.pageQuery(pageParams,queryCourseParamsDto);
    }


}
