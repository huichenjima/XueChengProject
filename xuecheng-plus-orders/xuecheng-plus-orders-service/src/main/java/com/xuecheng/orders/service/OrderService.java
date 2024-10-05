package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * ClassName: OrderService
 * Package: com.xuecheng.orders.service
 * Description:
 *
 * @Author 何琛
 * @Create 2024/10/2 17:41
 * @Version 1.0
 */
public interface OrderService {
    /**
     *
     * @param userId
     *  @description 创建商品订单
     *  * @param addOrderDto 订单信息
     *  * @return PayRecordDto 支付记录(包括二维码)
     * @param addOrderDto
     * @return
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * @description 查询支付记录
     * @param payNo  交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @author hechen
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo);

    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);

}
