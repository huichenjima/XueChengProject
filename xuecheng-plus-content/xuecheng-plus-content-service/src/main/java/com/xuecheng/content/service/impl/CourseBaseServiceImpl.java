package com.xuecheng.content.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    @Override
    public PageResult<CourseBase> pageQuery(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        log.info("开始查询课程信息，参数为{}",queryCourseParamsDto);
        long pageNum=pageParams.getPageNo(),pageSize=pageParams.getPageSize();
        Page<CourseBase> page =new Page(pageNum,pageSize);
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>();
        courseBasePageResult.setPage(pageNum);
        courseBasePageResult.setPageSize(pageSize);

        if (queryCourseParamsDto==null)
        {
            Page<CourseBase> courseBasePage = lambdaQuery().page(page);
            courseBasePageResult.setItems(courseBasePage.getRecords());
            courseBasePageResult.setCounts(courseBasePage.getTotal());

        }
        else
        {
            Page<CourseBase> courseBasePage = lambdaQuery()
                    .eq(StrUtil.isNotBlank(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus())
                    .eq(StrUtil.isNotBlank(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                    .like(StrUtil.isNotBlank(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                    .page(page);
            courseBasePageResult.setItems(courseBasePage.getRecords());
            courseBasePageResult.setCounts(courseBasePage.getTotal());

        }

        return courseBasePageResult;
    }
}
