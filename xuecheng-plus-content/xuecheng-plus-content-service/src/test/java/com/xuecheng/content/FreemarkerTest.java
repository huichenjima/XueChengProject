package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * ClassName: FreemarkerTest
 * Package: com.xuecheng.content
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/13 21:05
 * @Version 1.0
 */
@SpringBootTest
public class FreemarkerTest {

    @Autowired
    private CoursePublishService coursePublishService;

    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {

        Configuration configuration = new Configuration(Configuration.getVersion());

        //得到模板
        //指定classpath路径
        String classpath = this.getClass().getResource("/").getPath();
        //设置模板的目录
        configuration.setDirectoryForTemplateLoading(new File(classpath+"/templates/"));
        configuration.setDefaultEncoding("utf-8");
        Template template = configuration.getTemplate("course_template.ftl");

        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);

        HashMap<String, Object> map = new HashMap<>();

        map.put("model",coursePreviewInfo);


        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");

        FileOutputStream outputStream = new FileOutputStream(new File("D:\\ev\\XueChengProject\\codemy\\upload\\1.html"));
        //使用流将html写入文件
        IOUtils.copy(inputStream,outputStream);


    }
}
