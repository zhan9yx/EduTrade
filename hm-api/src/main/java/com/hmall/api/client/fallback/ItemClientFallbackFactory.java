package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {
    // 需要注册为Bean，但是hm-api没有启动类，所以只能注册为配置类，在DefaultFeignConfig中注册为Bean（也可以另new一个config文件）
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品信息失败", cause);
                // 兜底方案
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品业务失败", cause);
                throw new RuntimeException(cause);
            }
        };
    }
}
