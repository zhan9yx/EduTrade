package com.hmall.trade.Constants;

import io.swagger.models.auth.In;

public interface MqConstants {

    String DELAY_EXCHANGE_NAME = "trade.delay.direct";
    String DELAY_QUEUE_NAME = "trade.delay.order.queue";
    String DELAY_ORDER_KEY = "delay.order.query";
    Integer DELAY_TIME = 10000;
}
