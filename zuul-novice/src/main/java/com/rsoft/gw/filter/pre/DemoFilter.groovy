package com.rsoft.gw.filter.pre;
 
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
class DemoFilter extends ZuulFilter {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    Object run() {
        try {
        	logger.info("DemoFilter start.");
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            logger.info("send {} request to {}", request.getMethod(), request.getRequestURL().toString());
        } catch (Exception e) {
            logger.error("",e);
        }
 
        return null
    }
    
    @Override
    String filterType() {
        return "pre"
    }
 
    @Override
    int filterOrder() {
        return 50
    }
 
    @Override
    boolean shouldFilter() {
        return true
    }
 
}