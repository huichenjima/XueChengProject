package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * ClassName: CoursePublishService
 * Package: com.xuecheng.content.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/11 13:51
 * @Version 1.0
 */
public interface CoursePublishService {
    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author hechen
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);


    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上传课程静态化页面
     * @param file  静态化文件
     * @return void
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public void  uploadCourseHtml(Long courseId,File file);

    CoursePublish getCoursePublish(Long courseId);
}
