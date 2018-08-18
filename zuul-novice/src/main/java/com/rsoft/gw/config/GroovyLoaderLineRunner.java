package com.rsoft.gw.config;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import com.netflix.zuul.monitoring.MonitoringHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j//@Order(value=0)
@Component
public class GroovyLoaderLineRunner implements CommandLineRunner {
    @Value("${zuul.groovy.filter}")
    private String groovyfilter;
    @Value("${zuul.groovy.polling-interval-seconds:5}")
    private Integer pollingIntervalSeconds; //TimeUnit.SECONDS
 
    @Override
    public void run(String... args) throws Exception {
        MonitoringHelper.initMocks();
        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        try {
        	URL url = this.getClass().getResource("/");
        	String[] filterPath = groovyfilter.split(",");
        	List<String> directories = new ArrayList();
        	for(String dir : filterPath){
        		directories.add(url.getPath().toString()+dir);
        	}
        	
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            log.info("load groovy filter: "+groovyfilter);
            
            FilterFileManager.init(pollingIntervalSeconds, directories.toArray(new String[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}