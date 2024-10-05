package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: LearningServiceImpl
 * Package: com.xuecheng.learning.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/10/5 13:35
 * @Version 1.0
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    ContentServiceClient contentServiceClient;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null)
            return RestResponse.validfail("课程不存在");

        //根据课程id 去查询课程计划信息teachplan，如果is_preview值为1表示支持试学

        String teachplan = coursepublish.getTeachplan();
        List<TeachplanDto> list = JSON.parseArray(teachplan, TeachplanDto.class);
        if (list == null) {
            XueChengPlusException.cast("该章节没有视频");
        }
        List<Teachplan> list1=new ArrayList<>();

        for (TeachplanDto teachplanDto : list) {
            List<Teachplan> teachPlanTreeNodes = teachplanDto.getTeachPlanTreeNodes();
            teachPlanTreeNodes.forEach(teachplan1 -> list1.add(teachplan1));
        }
        Map<Long, String> map = list1.stream().collect(Collectors.toMap(Teachplan::getId, Teachplan::getIsPreview));
        String s = map.get(teachplanId);
        //支持试学则直接返回地址
        if ("1".equals(s))
        {
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }








        //判断用户
        if (StringUtils.isNotEmpty(userId)){
            //        先判断是否有资格
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = learningStatus.getLearnStatus();
            if (learnStatus.equals("702001")) {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702002")) {
                return RestResponse.validfail("无法观看，由于没有选课或选课后没有支付");
            } else if (learnStatus.equals("702003")) {
                return RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }

        }

        //如果用户没有登录，则要查询课程信息
        //未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)) {//免费可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");




    }
}
