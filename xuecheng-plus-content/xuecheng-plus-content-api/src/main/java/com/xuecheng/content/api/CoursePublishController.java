package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
//import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @description 课程预览，发布
 * @author Mr.M
 * @date 2022/9/16 14:48
 * @version 1.0
 */
//不返回json，返回的是静态资源注意使用controller就行
@Controller
@Slf4j
public class CoursePublishController {

    @Autowired
    private CoursePublishService coursePublishService;


 @GetMapping("/coursepreview/{courseId}")
 public ModelAndView preview(@PathVariable("courseId") Long courseId){

      ModelAndView modelAndView = new ModelAndView();
      //查询课程的数据
     CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);


      modelAndView.addObject("model",coursePreviewInfo);
      modelAndView.setViewName("course_template");
   return modelAndView;
  }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        // TODO 获取机构id
        Long companyId=1232141425L;
        coursePublishService.commitAudit(companyId,courseId);

    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){

        // TODO 获取机构id
        Long companyId=1232141425L;
        coursePublishService.publish(companyId,courseId);

    }



}