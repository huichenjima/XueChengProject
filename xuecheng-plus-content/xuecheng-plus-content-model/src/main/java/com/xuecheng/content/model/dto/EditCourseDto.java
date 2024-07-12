package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * ClassName: EditCourseDto
 * Package: com.xuecheng.content.model.dto
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/11 14:34
 * @Version 1.0
 */
@Data
@ApiModel(value="EditCourseDto", description="更新课程基本信息")
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "课程id",required = true)
    @NotNull(message = "courseId不能为空")
    private Long id;
}
