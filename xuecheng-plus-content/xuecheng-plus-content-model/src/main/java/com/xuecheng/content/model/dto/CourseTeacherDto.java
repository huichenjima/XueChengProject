package com.xuecheng.content.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: TeachPlanDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/11 21:29
 * @Version 1.0
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CourseTeacherDto  {

    /**
     * 主键
     */
    @NotNull(message = "修改老师id不能为空",groups = {ValidationGroups.Update.class})
    private Long id;

    /**
     * 课程标识
     */
    @NotNull(message = "新增老师课程id不能为空",groups = {ValidationGroups.Inster.class})
    @NotNull(message = "修改老师课程id不能为空",groups = {ValidationGroups.Update.class})
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(message = "新增老师姓名不能为空",groups = {ValidationGroups.Inster.class})
    @NotEmpty(message = "修改老师姓名不能为空",groups = {ValidationGroups.Update.class})
    private String teacherName;

    /**
     * 教师职位
     */
    private String position;

    /**
     * 教师简介
     */
    private String introduction;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;
}
