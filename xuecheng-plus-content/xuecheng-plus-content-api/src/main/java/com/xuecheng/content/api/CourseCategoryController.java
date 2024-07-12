package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: CourseCategoryController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/7 17:02
 * @Version 1.0
 */
@RestController
@Api(tags = "课程分类接口")
@Slf4j
@RequiredArgsConstructor
public class CourseCategoryController {
    private final CourseCategoryService courseCategoryService;

//    : http://localhost:8601/api/content/course-category/tree-nodes
//    请求方法: GET
    @ApiOperation("课程分类树查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> categoryTree()
    {
        log.info("开始查询课程分类树");
        // 这是自己写的java代码，用的多次sql查询，这样耗时太久 ，应该用sql的内连接查询或者嵌套查询
//        return courseCategoryService.categoryTree();
        return   courseCategoryService.queryTreeNodes("1");
    }

}
