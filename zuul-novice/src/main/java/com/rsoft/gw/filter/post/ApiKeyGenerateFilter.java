package com.rsoft.gw.filter.post;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.PathMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.rsoft.gw.filter.pre.ApiKeyFilter;
import com.rsoft.gw.support.auth.ApiKeyProperties;
import com.rsoft.gw.support.auth.ApiKeyProperties.ApiKeyConfig;
import com.rsoft.gw.support.auth.ApiKeyProperties.ApiKeyConfig.Algorithm;
import com.rsoft.gw.support.auth.AuthService;
import com.rsoft.gw.util.EncryptUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyGenerateFilter extends ZuulFilter {
	private final ApiKeyProperties properties;
	private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final PathMatcher pathMatcher;
	@Autowired
	private AuthService authService;
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Override
	public boolean shouldFilter() {
		final RequestContext ctx = RequestContext.getCurrentContext();
		final ApiKeyConfig cfg = getKeyConfig(route());
		final Route route = route();
		return properties.isEnabled() && 
				cfg != null && 
				!cfg.getAuthUrl().isEmpty() && 
				pathMatcher.match(route.getPath(), cfg.getAuthUrl()
		);
	}

	@Override
	public Object run() {
		try {
			final RequestContext ctx = RequestContext.getCurrentContext();
	        final HttpServletRequest request = ctx.getRequest();
	        final Route route = route();
	        InputStream stream = ctx.getResponseDataStream();
	        String body = StreamUtils.copyToString(stream, Charset.forName("UTF-8"));
	        
			ApiKeyConfig cfg = this.getKeyConfig(route);
	        if(cfg!=null && StringUtils.isNotBlank(body)) {
	        	String keyGenerated = null;
				Algorithm algr =  cfg.getAlg();
				switch (algr) {
				case MD5:
					keyGenerated = EncryptUtil.md5(body+cfg.getSalt());
					log.debug("generate apikey.md5={}", keyGenerated);
					break;
				case SHA:
					keyGenerated = EncryptUtil.sha(body+cfg.getSalt());
					log.debug("generate apikey.sha={}", keyGenerated);
					//写入session 或 redis
					break;
				}
				if(keyGenerated!=null){
					redisTemplate.opsForValue().set(ApiKeyFilter.API_KEY_REDIS_NS+keyGenerated, body, cfg.getExpires(), TimeUnit.SECONDS);
					ctx.setResponseBody(keyGenerated);
				}
	        }
		} catch (IOException e) {
			throw new ZuulRuntimeException(e);
		}
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.POST_TYPE;
	}

	@Override
	public int filterOrder() {
		return FilterConstants.SEND_RESPONSE_FILTER_ORDER - 2;
	}
	
	 Route route() {
	        String requestURI = urlPathHelper.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
	        return routeLocator.getMatchingRoute(requestURI);
	    }
	    protected ApiKeyConfig getKeyConfig(final Route route) {
	        if (route != null) {
	            return (ApiKeyConfig)properties.getRoutes().get(route.getId());
	        }
	        return null;
	    }
}
