package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.rabbitmq.client.Channel;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * ClassName: ReceivePayNotifyService
 * Package: com.xuecheng.learning.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2024/10/5 11:32
 * @Version 1.0
 * 接受消息通知类
 */
@Service
@Slf4j
public class ReceivePayNotifyService {

    @Autowired
    MyCourseTablesService myCourseTablesService;


    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message, Channel channel) {
        //保证重试有一定间隔
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        byte[] body = message.getBody();
        String jsonString = new String(body);
        //转成自己的消息对象
        MqMessage mqMessage = JSON.parseObject(jsonString, MqMessage.class);

        //解析消息内容拿到选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        String orderType = mqMessage.getBusinessKey2();

        //学习中心服务只处理购买课程的支付订单的结果，还有一个学习资源因为
        if ("60201".equals(orderType))
        {
            //根据消息内容更新选课内容
            boolean b = myCourseTablesService.saveChooseCourseSuccess(chooseCourseId);
            if (!b)
                XueChengPlusException.cast("保存选课记录状态失败");//抛异常消息会重发
        }







    }
}
