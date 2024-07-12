package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class TeachplanDto extends Teachplan {
    private TeachplanMedia teachplanMedia;
    List<Teachplan> teachPlanTreeNodes;
}
