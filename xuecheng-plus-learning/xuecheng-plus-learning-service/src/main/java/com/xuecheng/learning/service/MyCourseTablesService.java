package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * ClassName: MyCourseTablesService
 * Package: com.xuecheng.learning.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/9/30 13:20
 * @Version 1.0
 * 选课相关接口
 */
public interface MyCourseTablesService {

    /**
     * 添加选课接口
     * @param userId
     * @param courseId
     * @return
     */
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    /**
     * 判断学习资格
     * @param userId
     * @param courseId
     * @return
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @author Mr.M
     * @date 2022/10/27 9:24
     */
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params);
}
