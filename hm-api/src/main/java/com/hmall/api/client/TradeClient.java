package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("trade-service")
public interface TradeClient {


    @PutMapping("/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
