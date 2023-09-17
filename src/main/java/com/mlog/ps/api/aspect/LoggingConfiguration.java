package com.mlog.ps.api.aspect;

import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class LoggingConfiguration {
//    @Bean
//    @Autowired
//    public LoggingAspect loggingAspect(Tracer tracer) {
//        return new LoggingAspect(tracer);
//    }
}

