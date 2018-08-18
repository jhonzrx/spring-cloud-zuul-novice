package com.rsoft.gw.support.auth;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.Maps;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(ApiKeyProperties.PREFIX)
public class ApiKeyProperties {
	public static final String PREFIX = "zuul.apikey";
    private boolean enabled;
    private long expires; //TimeUnit.MINUTES
    @NotNull
    private Map<String, ApiKeyConfig> routes = Maps.newHashMap();
    
    @Data
    @NoArgsConstructor
    public static class ApiKeyConfig {
    	public static final String HEADER_PARAMETER_NAME = "x-token";
        private Algorithm alg = Algorithm.MD5;
        private String authUrl;
        @NotNull
        private String salt;
        private String header = HEADER_PARAMETER_NAME;
        private long expires=300; //TimeUnit.SECONDS
        private String message="token unauthorized.";
        private List<String> ignoreUrls;
        
        public enum Algorithm {
            MD5,
            SHA
        }
        
    }
    
	
}
