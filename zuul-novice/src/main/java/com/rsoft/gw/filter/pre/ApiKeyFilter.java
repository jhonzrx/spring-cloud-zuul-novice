package com.rsoft.gw.filter.pre;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.rsoft.gw.support.auth.ApiKeyProperties;
import com.rsoft.gw.support.auth.ApiKeyProperties.ApiKeyConfig;
import com.rsoft.gw.support.auth.ApiKeyProperties.ApiKeyConfig.Algorithm;
import com.rsoft.gw.util.EncryptUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyFilter extends ZuulFilter{
	private final ApiKeyProperties properties;
	private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;
    private final PathMatcher pathMatcher;
    
    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletRequest request = ctx.getRequest();
        final Route route = route();
        
        ApiKeyConfig cfg = this.getKeyConfig(route);
        if(cfg!=null) {
        	List<String> ignoreUrls = cfg.getIgnoreUrls();
        	if(ignoreUrls!=null && ignoreUrls.size()>0){
        		final String routePath = route.getPath();
        		for(String uri : ignoreUrls){
        			if(pathMatcher.match(uri, routePath)){
        				return null;
        			}
        		}
        	}
        	
            log.info(">>> ApiKeyFilter {},{}", request.getMethod(), request.getRequestURL().toString());
            String key = request.getHeader(cfg.getHeader());
            
            if(StringUtils.isBlank(key)){
            	writeResponse(ctx, cfg, false);
            }
            boolean success = false;
            Algorithm algr =  cfg.getAlg();
			switch (algr) {
			case MD5:
				String md5Generated = EncryptUtil.md5(cfg.getSalt());
				log.debug("apikey={},generate md5={}",key, md5Generated);
				if (md5Generated.equals(key)) {
					success = true;
				}
				break;
			case SHA:
				String shaGenerated = EncryptUtil.sha(cfg.getSalt());
				log.debug("apikey={},generate sha={}",key, shaGenerated);
				if (shaGenerated.equals(key)) {
					success = true;
				}
				break;
			default:
				log.debug("nothing to do");
				break;
			}
			
			writeResponse(ctx, cfg, success);
        }

        return null;
    }
    @Override
    public boolean shouldFilter() {
        return properties.isEnabled() && getKeyConfig(route()) != null;
    }
    @Override
    public String filterType() {
        return PRE_TYPE;
    }
    @Override
    public int filterOrder() {
        return FORM_BODY_WRAPPER_FILTER_ORDER;
    }
    private void writeResponse(RequestContext ctx, ApiKeyConfig cfg, boolean success){
    	if (success) {
            ctx.setSendZuulResponse(true); 
            ctx.setResponseStatusCode(200);
        } else {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        	ctx.getResponse().setContentType("text/html;charset=UTF-8");
        	ctx.setResponseBody(cfg.getMessage());
        }
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
