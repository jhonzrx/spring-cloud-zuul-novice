package com.rsoft.gw.filter.factory;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ServerWebExchange;

import com.rsoft.gw.filter.factory.ApiKeyGatewayFilterFactory.ApiKeyConfig.Algorithm;
import com.rsoft.gw.util.EncryptUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientResponse;

@Slf4j
@Component
public class ApiKeyGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiKeyGatewayFilterFactory.ApiKeyConfig> {
	public static final String API_KEY_REDIS_NS = "zuul:apikey:";
	private static final String FAILURE_BACK_PATH = "/gateway/401";
	private PathMatcher pathMatcher = new AntPathMatcher();
	private final DispatcherHandler dispatcherHandler;
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	public ApiKeyGatewayFilterFactory(DispatcherHandler dispatcherHandler) {
		super(ApiKeyConfig.class);
		this.dispatcherHandler = dispatcherHandler;
	}

	@Override
	public GatewayFilter apply(ApiKeyConfig cfg) {
		Assert.notNull(cfg, "A config must be supplied for the ApiKeyGatewayFilter ");
		
		return (exchange, chain) -> {
            //pre filter
			ServerHttpRequest request = exchange.getRequest();
			
			//generate apikey
			if(cfg.authUrl !=null && !request.getURI().getPath().startsWith(cfg.authUrl)){
				List<String> ignoreUrls = cfg.getIgnoreUrls();
	        	if(ignoreUrls!=null && ignoreUrls.size()>0){
	        		final String routePath = request.getPath().value();
	        		for(String uri : ignoreUrls){
	        			if(pathMatcher.match(uri, routePath)){
	        				//continue request for next
	        				return chain.filter(exchange);
	        			}
	        		}
	        	}
	        	
	            log.info(">>> ApiKeyGatewayFilter {},{}", request.getMethod(), request.getURI().toString());
	            String key = request.getHeaders().get(cfg.getHeader()).get(0);
	            
	            if(StringUtils.isBlank(key)){
	            	return writeResponse(exchange, cfg, false);
	            }
	            
	            String uid = redisTemplate.opsForValue().get(API_KEY_REDIS_NS+key);
	            if(StringUtils.isBlank(uid)){
	            	return writeResponse(exchange, cfg, false);
	            }
	            
	            boolean success = false;
	            Algorithm algr =  cfg.getAlg();
				switch (algr) {
				case MD5:
					String md5Generated = EncryptUtil.md5(uid+cfg.getSalt());
					log.debug("apikey={},generate md5={}",key, md5Generated);
					if (md5Generated.equals(key)) {
						success = true;
					}
					break;
				case SHA:
					String shaGenerated = EncryptUtil.sha(uid+cfg.getSalt());
					log.debug("apikey={},generate sha={}",key, shaGenerated);
					if (shaGenerated.equals(key)) {
						success = true;
					}
					break;
				default:
					log.debug("nothing to do");
					break;
				}
			}
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				//后置处理 generate apikey
				if(cfg.authUrl !=null &&  request.getURI().getPath().startsWith(cfg.authUrl)){
					log.info("generate apikey :{}",request.getURI().getPath());
					generateApiKey(exchange, chain, cfg);
				}
			}));
			
            
			
        };
	}
	
	public static Mono<Void> setResponseBody(ServerWebExchange exchange, String body) {
		final ServerHttpResponse response = exchange.getResponse();
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return response.writeWith(Flux.just(buffer));
	}
	
	private void generateApiKey(ServerWebExchange exchange, GatewayFilterChain chain, ApiKeyConfig cfg){
		HttpClientResponse clientResponse = exchange.getAttribute(CLIENT_RESPONSE_ATTR);
		ServerHttpResponse response = exchange.getResponse();
		
        String uid4Header = exchange.getResponse().getHeaders().get("_uid_").get(0);
        
    	String keyGenerated = null;
		Algorithm algr =  cfg.getAlg();
		switch (algr) {
		case MD5:
			keyGenerated = EncryptUtil.md5(uid4Header+cfg.getSalt());
			log.debug("generate apikey.md5={}", keyGenerated);
			break;
		case SHA:
			keyGenerated = EncryptUtil.sha(uid4Header+cfg.getSalt());
			log.debug("generate apikey.sha={}", keyGenerated);
			//写入session 或 redis
			break;
		}
		
		if(keyGenerated!=null){
			redisTemplate.opsForValue().set(API_KEY_REDIS_NS+keyGenerated, uid4Header, cfg.getExpires(), TimeUnit.SECONDS);
			exchange.getResponse().getHeaders().add(cfg.getHeader(), keyGenerated);
			exchange.getResponse().getHeaders().remove("_uid_");
		}
	}

	private Mono<Void> writeResponse(ServerWebExchange exchange, ApiKeyConfig cfg, boolean success){
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
	}
	
	@Data
	public static class ApiKeyConfig {
		public static final String HEADER_PARAMETER_NAME = "x-token";
		private Algorithm alg = Algorithm.MD5;
		private String authUrl;
		@NotNull
		private String salt;
		private String header = HEADER_PARAMETER_NAME;
		private long expires = 300; // TimeUnit.SECONDS
		private String failureMessage = "token unauthorized.";
		private List<String> ignoreUrls = Collections.EMPTY_LIST;

		public enum Algorithm {
			MD5, SHA
		}

	}

}
