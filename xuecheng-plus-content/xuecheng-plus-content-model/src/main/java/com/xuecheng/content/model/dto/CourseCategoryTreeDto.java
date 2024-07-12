package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName: CourseCategoryTreeDto
 * Package: com.xuecheng.content.model.dto
 * Description: 课程分类树 1 是根节点
 *
 * @Author 何琛
 * @Create 2024/7/7 16:58
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;


}
