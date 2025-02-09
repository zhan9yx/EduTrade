package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager configManager;
    private final RouteDefinitionWriter writer;

    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routerIds = new HashSet<>();


    @PostConstruct

    public void initRouteConfigListener() throws NacosException {
        // 1 项目启动先拉取一次配置，并添加监听器
        String configInfo = configManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        // 定义线程池，异步处理回调，这里不用
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String s) {
                        // 2 监听到配置变更，需要更新路由表
                        updateConfigInfo(s);
                    }
                });
        // 3 第一次读取到的配置需要更新到路由表
        updateConfigInfo(configInfo);

    }

    public void updateConfigInfo(String configInfo) {
        log.debug("监听到路由配置信息：{}", configInfo);
        // 解析配置文件 转为routeDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        // 删除旧路由表（否则若传递的路由表与源路由表相比，删减了，则直接进行新增操作是有问题的
        for (String routerId : routerIds) {
            writer.delete(Mono.just(routerId)).subscribe();
        }
        // 清空路由ID表
        routerIds.clear();
        // 更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions) {
            // 更新路由表（增）
            writer.save(Mono.just(routeDefinition)).subscribe();
            // 记录路由id，便于下一次更新时删除路由表
            routerIds.add(routeDefinition.getId());
        }
    }
}
