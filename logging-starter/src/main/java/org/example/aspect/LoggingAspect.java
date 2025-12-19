package org.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.service.LoggerService;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final LoggerService loggerService;

    public LoggingAspect(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @Around("execution(* com.example..*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        loggerService.logInfo("Entering method: " + methodName);

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long ms = (System.nanoTime() - start) / 1_000_000;
            loggerService.logInfo("Method executed successfully: " + methodName + " ms - " + ms);
            return result;
        } catch (Throwable t) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            loggerService.logError("Exception in method: " + methodName, t);
            throw t;
        }
    }
}