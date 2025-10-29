package com.stock.stock_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setOneIndexedParameters(false); // page=0 default
            resolver.setMaxPageSize(200);
            resolver.setPageParameterName("page");
            resolver.setSizeParameterName("size");
        };
    }

    @Bean
    SortHandlerMethodArgumentResolverCustomizer sortCustomizer() {
        return r -> r.setSortParameter("sort");
    }
}
