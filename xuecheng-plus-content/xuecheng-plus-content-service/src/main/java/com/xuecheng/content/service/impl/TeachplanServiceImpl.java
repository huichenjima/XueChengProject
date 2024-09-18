package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.object.SqlFunction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {
    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;



    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {

        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = new Teachplan();
//        Teachplan teachplan = teachplanMapper.selectById(id);
        int i;
        if(id==null)
        {
            //为插入操作
            teachplan=new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //排序,这里直接使用计数是不合理的，因为会存在删除的情况，这样会出现order不是按顺序排列的情况
            //这里应该取order的最大值加一
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getCourseId,saveTeachplanDto.getCourseId());
            queryWrapper.eq(Teachplan::getParentid,saveTeachplanDto.getParentid()).orderByDesc(Teachplan::getOrderby);
            Teachplan biggestOrderTeaplan = this.getOne(queryWrapper,false);
            int orderBy;
            if (biggestOrderTeaplan==null)
                orderBy=1;
            else
                orderBy=biggestOrderTeaplan.getOrderby()+1;



//            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplan.setOrderby(orderBy);
            i = teachplanMapper.insert(teachplan);
            if (i<=0)
                XueChengPlusException.cast("插入失败");

            return;


        }


        teachplan = teachplanMapper.selectById(id);
        if (teachplan==null)
            XueChengPlusException.cast("课程计划不存在");
        BeanUtils.copyProperties(saveTeachplanDto,teachplan);
        i = teachplanMapper.updateById(teachplan);
        if (i<=0)
            XueChengPlusException.cast("更新失败");

    }
    // 插入计算排序值不使用此函数了
    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count+1;
    }

    @Override
    @Transactional
    public void deleteTeachplan(Long id) {
        // 检测是否是大章节,如果是小章节可以直接删除但是要同时删除对应的媒体信息
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan==null)
            XueChengPlusException.cast("不存在该课程计划");
        if (teachplan.getGrade().equals(1)){
            //检测发现是大章节
            Integer count = lambdaQuery().eq(Teachplan::getParentid, id).count();
            if (count>0)
            {
                //发现大章节下有子课程删除失败
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作","120409");
            }
            else
            {
                //大章节下没有子课程了可以进行删除
                boolean delete = this.removeById(id);
            }
        }
        else
        {
            //反之则为小章节可以直接进行删除，要删除课程计划和对应的媒体信息
            boolean b = this.removeById(id);
            if(!b)
                XueChengPlusException.cast("该课程计划不存在");
            LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);





        }




    }

    @Override
    @Transactional
    public void movedown(Long id) {
        Teachplan teachplan = this.getById(id);
        if (teachplan==null)
            XueChengPlusException.cast("该课程计划不存在");
        Integer orderby = teachplan.getOrderby();
        // 寻找当前课程计划排名后一位的排序序号
        List<Teachplan> teachplanList = this.lambdaQuery().eq(Teachplan::getParentid,teachplan.getParentid()).gt(Teachplan::getOrderby, orderby).orderByAsc(Teachplan::getOrderby).list();
        if (CollUtil.isEmpty(teachplanList))
            XueChengPlusException.cast("该课程已经在末尾，不能再下移了");
        // 取出后一位的课程计划，交换
        Teachplan teachplan1 = teachplanList.get(0);
        teachplan.setOrderby(teachplan1.getOrderby());
        teachplan1.setOrderby(orderby);
        List<Teachplan> list=new ArrayList<>();
        list.add(teachplan);
        list.add(teachplan1);
        boolean b = updateBatchById(list);
        if (!b)
            XueChengPlusException.cast("交换失败");



    }

    @Override
    @Transactional
    public void moveup(Long id) {

        Teachplan teachplan = this.getById(id);
        if (teachplan==null)
            XueChengPlusException.cast("该课程计划不存在");
        Integer orderby = teachplan.getOrderby();
        // 寻找当前课程计划排名前一位的排序序号
        List<Teachplan> teachplanList = this.lambdaQuery().eq(Teachplan::getParentid,teachplan.getParentid()).lt(Teachplan::getOrderby, orderby).orderByDesc(Teachplan::getOrderby).list();
        if (CollUtil.isEmpty(teachplanList))
            XueChengPlusException.cast("该课程已经在顶端，不能再上移了");
        // 取出后一位的课程计划，交换
        Teachplan teachplan1 = teachplanList.get(0);
        teachplan.setOrderby(teachplan1.getOrderby());
        teachplan1.setOrderby(orderby);
        List<Teachplan> list=new ArrayList<>();
        list.add(teachplan);
        list.add(teachplan1);
        boolean b = updateBatchById(list);
        if (!b)
            XueChengPlusException.cast("交换失败");

    }

    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            XueChengPlusException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //先删除原有课程
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));
        TeachplanMedia teachplanMedia = BeanUtil.copyProperties(bindTeachplanMediaDto, TeachplanMedia.class);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if (insert<=0)
            throw new RuntimeException("插入失败");

        return teachplanMedia;

    }

    @Override
    public void deleteAssociationMedia(Long teachPlanId, Long mediaId) {
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,teachPlanId).eq(TeachplanMedia::getMediaId,mediaId);
        int delete = teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);
        if(delete<=0)
            XueChengPlusException.cast("删除媒资与课程计划关联失败");

        

    }
}
