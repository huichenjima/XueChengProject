package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    private final  CourseBaseMapper courseBaseMapper;

    private final CourseMarketMapper courseMarketMapper;

    private final CourseCategoryMapper courseCategoryMapper;

    private final TeachplanMapper teachplanMapper;

    private final TeachplanMediaMapper teachplanMediaMapper;

    private final CourseTeacherMapper courseTeacherMapper;




    @Override
    public PageResult<CourseBase> pageQuery(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        log.info("开始查询课程信息，参数为{}",queryCourseParamsDto);
        long pageNum=pageParams.getPageNo(),pageSize=pageParams.getPageSize();
        Page<CourseBase> page =new Page(pageNum,pageSize);
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>();
        courseBasePageResult.setPage(pageNum);
        courseBasePageResult.setPageSize(pageSize);
        // TODO 根据培训机构id拼装查询条件


        if (queryCourseParamsDto==null)
        {
            Page<CourseBase> courseBasePage = lambdaQuery()
                    .eq(CourseBase::getCompanyId,companyId)
                    .page(page);
            courseBasePageResult.setItems(courseBasePage.getRecords());
            courseBasePageResult.setCounts(courseBasePage.getTotal());

        }
        else
        {
            Page<CourseBase> courseBasePage = lambdaQuery()
                    .eq(StrUtil.isNotBlank(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus())
                    .eq(StrUtil.isNotBlank(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                    .like(StrUtil.isNotBlank(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                    .eq(CourseBase::getCompanyId,companyId)
                    .page(page);
            courseBasePageResult.setItems(courseBasePage.getRecords());
            courseBasePageResult.setCounts(courseBasePage.getTotal());

        }

        return courseBasePageResult;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto dto) {
//        //合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
////            throw new RuntimeException("课程名称为空");
//            XueChengPlusException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
////            throw new RuntimeException("课程分类为空");
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
////            throw new RuntimeException("课程分类为空");
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
////            throw new RuntimeException("课程等级为空");
//            XueChengPlusException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
////            throw new RuntimeException("教育模式为空");
//            XueChengPlusException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
////            throw new RuntimeException("适应人群为空");
//            XueChengPlusException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
////            throw new RuntimeException("收费规则为空");
//            XueChengPlusException.cast("收费规则为空");
//        }
        CourseBase courseBase = BeanUtil.copyProperties(dto, CourseBase.class);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert<=0)
            throw new RuntimeException("添加课程失败");
        CourseMarket courseMarket = BeanUtil.copyProperties(dto, CourseMarket.class);
        courseMarket.setId(courseBase.getId());

        int i = saveCourseMarket(courseMarket);

        if (i<=0)
        {
            XueChengPlusException.cast("保存课程营销信息失败");
            return null;
        }
        else
        {
            //正常结束
            return getCourseBaseInfo(courseBase.getId());



        }
//            throw new RuntimeException("保存课程营销信息失败");

    }



    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        CourseBase courseBase = courseBaseMapper.selectById(dto.getId());
        if (courseBase==null)
            XueChengPlusException.cast("课程不存在");
        if (!courseBase.getCompanyId().equals(companyId))
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
//        courseBase.setChangePeople();
        int i = courseBaseMapper.updateById(courseBase);
        if (i<=0)
            XueChengPlusException.cast("更新课程失败");
        CourseMarket courseMarket = courseMarketMapper.selectById(dto.getId());
        if (courseMarket==null)
            XueChengPlusException.cast("课程营销信息不存在");
        BeanUtils.copyProperties(dto,courseMarket);

        i = saveCourseMarket(courseMarket);
        if (i<=0)
            XueChengPlusException.cast("更新营销信息失败");
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseBase.getId());
        return courseBaseInfo;

    }

    //查询课程信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null)
            return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if (courseMarket!=null)
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategory.getName());
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategory1.getName());
        return courseBaseInfoDto;


    }
    //这里这么多删除有并发问题
    @Override
    @Transactional
    public void deleteCourse(Long companyId,Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase==null)
            XueChengPlusException.cast("要删除的课程课程不存在");
        if (!courseBase.getCompanyId().equals(companyId))
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        if (!courseBase.getAuditStatus().equals("202002"))
            XueChengPlusException.cast("不允许删除已审核的课程");

        //先删除其他的最后删除base
//        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //先删除老师
        LambdaQueryWrapper<CourseTeacher> courseTeacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseTeacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        int delete = courseTeacherMapper.delete(courseTeacherLambdaQueryWrapper);


        //再删除课程计划和媒体
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId,courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(teachplanMediaLambdaQueryWrapper);

        //再删除营销信息
        courseMarketMapper.deleteById(courseId);

        //最后删除base

        courseBaseMapper.deleteById(courseId);


    }

    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge=courseMarket.getCharge();
        if (StrUtil.isEmpty(charge))
            throw new RuntimeException("收费规则为空");
        if (charge.equals("201001"))
        {
            if (courseMarket.getPrice()==null||courseMarket.getPrice().floatValue()<=0)
                XueChengPlusException.cast("课程的价格不能为空");
//                throw new RuntimeException("课程的价格不能为空");
        }
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarket1==null)
        {
            //进行插入
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        }
        else
        {
            //更新
            Long id = courseMarket1.getId();
            BeanUtils.copyProperties(courseMarket,courseMarket1);
            courseMarket1.setId(id);

            int i = courseMarketMapper.updateById(courseMarket1);
            return i;
        }


    }

}
