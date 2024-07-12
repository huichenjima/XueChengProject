package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
    private final CourseCategoryMapper courseCategoryMapper;
    //下面这两个方法是自己写的，需要太多次的sql执行，不行
    @Override
    public List<CourseCategoryTreeDto> categoryTree() {
        CourseCategory root = this.lambdaQuery().eq(CourseCategory::getParentid, 0).one();
        CourseCategoryTreeDto courseCategoryTreeDto = BeanUtil.copyProperties(root, CourseCategoryTreeDto.class);
        queryChildrenNodes(courseCategoryTreeDto);
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();
        courseCategoryTreeDtos.add(courseCategoryTreeDto);
        return courseCategoryTreeDtos;

    }

    private void queryChildrenNodes(CourseCategoryTreeDto courseCategoryTreeDto) {
        //查找子节点并设指针
        String id = courseCategoryTreeDto.getId();
        List<CourseCategory> courseCategories = this.lambdaQuery().eq(CourseCategory::getParentid, id).list();
        if (CollUtil.isEmpty(courseCategories))
            return;
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategories.stream().map(courseCategory -> BeanUtil.copyProperties(courseCategory, CourseCategoryTreeDto.class)).collect(Collectors.toList());
        courseCategoryTreeDtos.forEach(this::queryChildrenNodes);
        courseCategoryTreeDto.setChildrenTreeNodes(courseCategoryTreeDtos);


    }
    //
//    @Override
//    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
//        //查找id的所有子节点,以及本身
//        List<CourseCategoryTreeDto> courseCategoryTreeDtos=courseCategoryMapper.selectTreeNodes(id);
//        CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
//        for (int i = 0; i < courseCategoryTreeDtos.size(); i++) {
//            //查找第i个节点的子节点
//            CourseCategoryTreeDto courseCategoryTreeDto1 = courseCategoryTreeDtos.get(i);
//            List<CourseCategoryTreeDto> list= new ArrayList<>();
//            for (int j = i+1; j < courseCategoryTreeDtos.size(); j++) {
//                if (courseCategoryTreeDtos.get(j).getParentid().equals(courseCategoryTreeDto1.getId()))
//                    list.add(courseCategoryTreeDtos.get(j));
//
//            }
//            courseCategoryTreeDto1.setChildrenTreeNodes(list);
//
//        }
//
//        List<CourseCategoryTreeDto> courseCategoryTreeDtoList=new ArrayList<>();
//        courseCategoryTreeDtoList.add(courseCategoryTreeDtos.get(0));
//        return courseCategoryTreeDtoList;
//    }
//这是教的方法 ,上面自己的方法用了两层for循环，因为没有map可以直接找父节点，这里妙在用了stream流构造map
@Override
public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
    //查找id的所有子节点,以及本身
    List<CourseCategoryTreeDto> courseCategoryTreeDtos=courseCategoryMapper.selectTreeNodes(id);
    Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
    List<CourseCategoryTreeDto> courseCategoryTreeDtos1=new ArrayList<>();
    courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
        //这个if是判断是否是根节点的儿节点来放入list
        if (item.getParentid().equals(id)){
            courseCategoryTreeDtos1.add(item);
        }
        //找到节点的父节点 ,请注意根节点已经被排除掉了
        CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
        if (courseCategoryTreeDto!=null)
        {
            if (courseCategoryTreeDto.getChildrenTreeNodes()==null) {
                courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
            }
            courseCategoryTreeDto.getChildrenTreeNodes().add(item);
        }





    });
    return courseCategoryTreeDtos1;


}

}
