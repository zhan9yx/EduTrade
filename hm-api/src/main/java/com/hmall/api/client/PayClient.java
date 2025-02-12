package com.hmall.api.client;

import com.hmall.api.client.fallback.PayClientFallbackFactory;
import com.hmall.api.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pay-service", fallbackFactory = PayClientFallbackFactory.class)
public interface PayClient {
    /**
     * 根据业务订单号查询支付订单
     * @param id
     * @return 业务订单传输实体
     */
    @GetMapping("/pay-orders/biz/{id}")
    PayOrderDTO queryPayOrderByBizOrderNo(@PathVariable("id") Long id);
}
