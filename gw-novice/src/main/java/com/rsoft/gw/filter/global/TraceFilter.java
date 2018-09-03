package com.rsoft.gw.filter.global;

import java.util.Date;
import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class TraceFilter implements GlobalFilter, Ordered {
	private static final String TRACE_ID = "_trace_";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    	log.info("start trace : {}",new Date());
    	ServerHttpRequest request = exchange.getRequest().mutate().header(TRACE_ID, UUID.randomUUID().toString()).build();
    	return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}