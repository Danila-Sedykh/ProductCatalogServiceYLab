package org.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.service.AuditService;
import org.example.annotation.Auditable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(com.example.audit.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        Object result;
        try {
            result = joinPoint.proceed();
            auditService.record(null, auditable.action(),
                    method.getDeclaringClass().getSimpleName() + "." + method.getName());
        } catch (Throwable t) {
            auditService.record(null, auditable.action() + "_FAILED",
                    method.getDeclaringClass().getSimpleName() + "." + method.getName() + " " + t.getMessage());
            throw t;
        }
        return result;
    }
}