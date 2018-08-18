/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rsoft.gw.config;

import static com.rsoft.gw.support.auth.ApiKeyProperties.PREFIX;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.rsoft.gw.filter.pre.ApiKeyFilter;
import com.rsoft.gw.support.auth.ApiKeyProperties;

/**
 * @author bado
 */
@Configuration
@EnableConfigurationProperties(ApiKeyProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class ApiKeyAutoConfiguration {
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Bean
    public ZuulFilter rateLimiterPreFilter(final ApiKeyProperties properties,
                                           final RouteLocator routeLocator) {
        return new ApiKeyFilter(properties, routeLocator, urlPathHelper);
    }
}
