package com.hmall.trade.listener;

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
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        // 查询订单
        Order order = orderService.getById(orderId);
        // 判断订单状态是否为未支付（才需要标记）
        if (order == null || order.getStatus() != 1) {
            // 订单不存在/订单状态不是未支付(说明发生异常，不应该操作，否则会覆盖状态） 确保业务幂等性
            return;
        }
        // 标记订单状态为已支付
        orderService.markOrderPaySuccess(orderId);
    }
}
