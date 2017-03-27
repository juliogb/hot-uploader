package com.hotmart.hot.uploader.config;

import io.swagger.jaxrs.config.BeanConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("v1")
public class AppConfig extends Application {
    
    public AppConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/hot-uploader/v1");
        beanConfig.setFilterClass("io.swagger.sample.util.ApiAuthorizationFilterImpl");
        beanConfig.setResourcePackage("com.hotmart.hot.uploader.rest");
        beanConfig.setScan(true);
    }
    
    
}
