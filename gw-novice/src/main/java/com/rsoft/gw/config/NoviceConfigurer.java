package com.rsoft.gw.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rsoft.gw.filter.factory.ElapsedFilter;
import com.rsoft.gw.filter.global.TraceFilter;

@Configuration
public class NoviceConfigurer {
	@Bean
	public RouteLocator customerRouteLocator(RouteLocatorBuilder builder) {
	    // @formatter:off
	    return builder.routes()
            .route(r -> r.path("/demo/**")
                         .filters(f -> f.stripPrefix(1)
                                        .filter(new ElapsedFilter())
                                        .addResponseHeader("X-Response-Default-Foo", "Default-Bar"))
                         .uri("lb://testService")
                         .order(0)
                         .id("demo_service")
            )
            .build();
	    // @formatter:on
	}
	
	@Bean
	public TraceFilter traceFilter(){
		return new TraceFilter();
	}
}
