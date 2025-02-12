package com.hmall.api.client.fallback;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

public class PayClientFallbackFactory implements FallbackFactory<PayClient> {
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient(){
            @Override
            public PayOrderDTO queryPayOrderByBizOrderNo(Long id) {
                // 查询支付流水失败的处理策略
                return null;
            }
        };
    }
}
