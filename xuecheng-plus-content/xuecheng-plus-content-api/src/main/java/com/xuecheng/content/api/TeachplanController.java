package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: TeachplanController
 * Package: com.xuecheng.content.api
 * Description:
 *
 * @Author 何琛
 * @Create 2024/7/11 21:32
 * @Version 1.0
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
@RequiredArgsConstructor
public class TeachplanController {
    private final TeachplanService teachplanService;


    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){

        return teachplanService.getTreeNodes(courseId);
    }


    @ApiOperation("新增或修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto)
    {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }


    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable(value = "id") Long id){
        teachplanService.deleteTeachplan(id);
    }

//    Request URL: http://localhost:8601/api/content/teachplan/movedown/43
//    Request Method: POST
    
    @ApiOperation("修改排序向下移动")
    @PostMapping("/teachplan/movedown/{id}")
    public void movedown(@PathVariable(value = "id") Long id){
        teachplanService.movedown(id);
    }

    @ApiOperation("修改排序向上移动")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveup(@PathVariable(value = "id") Long id){
        teachplanService.moveup(id);
    }
}
