package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * ClassName: FreemarkerController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/9 13:57
 * @Version 1.0
 */
@Controller
public class FreemarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test(){
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("name","何琛");
        //指定模板
        modelAndView.setViewName("test");//根据视图名称拼接

        return modelAndView;


    }
}
