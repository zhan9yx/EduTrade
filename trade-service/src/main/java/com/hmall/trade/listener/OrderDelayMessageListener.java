package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.Constants.MqConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {
    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.DELAY_QUEUE_NAME),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE_NAME, delayed = "true"), // 交换机要设置为delayed
            key = MqConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        // 1. 查询订单
        Order order = orderService.getById(orderId);
        // 2. 检查订单状态是否为已支付（23456）
        if (order == null || order.getStatus() != 1) {
            return;
        }
        // 3. 订单状态未支付，查询支付流水状态
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
        // 4. 判断是否支付
        if (payOrder != null && payOrder.getStatus() == 3) {
            // 4.1 已支付（3） 更新本地订单状态，保持一致
            orderService.markOrderPaySuccess(orderId);
        } else {
            // 4.2 未支付 取消订单，恢复库存
            orderService.cancelOrder(orderId);
        }



    }
}
