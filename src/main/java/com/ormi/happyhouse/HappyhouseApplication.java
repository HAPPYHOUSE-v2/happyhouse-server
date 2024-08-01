package com.ormi.happyhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
public class HappyhouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HappyhouseApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> registrationBean = new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
