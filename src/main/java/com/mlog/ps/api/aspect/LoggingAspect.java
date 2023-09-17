package com.mlog.ps.api.aspect;

import brave.Span;
import brave.Tracer;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    private final Tracer tracer;

   @Autowired
    public LoggingAspect(Tracer tracer) {
        this.tracer = tracer;
    }

    @Before("@within(org.springframework.web.bind.annotation.RequestMapping)")
    @NewSpan
    public void beforeLogging(JoinPoint joinPoint) {
        // Check if the method has @RequestBody
        if (hasRequestBody(joinPoint)) {
            Span span = tracer.newTrace().name(joinPoint.getSignature().toShortString()).start();
            // Get the method being invoked
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();

            // Get the method's parameter annotations
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            // Capture and log the @RequestBody parameter
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation.annotationType() == RequestBody.class) {
                        log.info("requestBody {}", args[i].toString());
                        span.tag("requestBody", args[i].toString());
                        break; // Assuming there's only one @RequestBody parameter
                    }
                }
            }

//            tracer.withSpanInScope(span);
        }
    }

    @AfterReturning(value = "@within(org.springframework.web.bind.annotation.RequestMapping)",
            returning = "result")
    @NewSpan
    public void afterLogging(JoinPoint joinPoint, Object result) {
        // Check if the method has @RequestBody
        if (hasRequestBody(joinPoint)) {
            Span span = tracer.currentSpan();
            if (span != null) {
                // Capture and log the response body content
                String responseBody = result != null ? result.toString() : "null";
                log.info("responseBody {}", responseBody);
                span.tag("responseBody", responseBody);
                span.finish();
            }
        }
    }

    // Helper method to check if a method has @RequestBody
    private boolean hasRequestBody(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // Get parameter annotations
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        // Loop through parameter annotations to find @RequestBody
        for (Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == RequestBody.class) {
                    return true;
                }
            }
        }

        return false;

    }

    @Before("@within(com.mlog.ps.api.aspect.EnableLogging) || @annotation(com.mlog.ps.api.aspect.EnableLogging)")
    public void beforeServiceLogging(JoinPoint joinPoint) {
        log.info("Method started: " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(value = "@within(com.mlog.ps.api.aspect.EnableLogging) || @annotation(com.mlog.ps.api.aspect.EnableLogging)",
            returning = "result")
    public void afterServiceLogging(JoinPoint joinPoint, Object result) {
        log.info("Method ended: {} / returned result {}" , joinPoint.getSignature().toShortString(),result);
    }
}
