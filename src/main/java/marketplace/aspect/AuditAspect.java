package marketplace.aspect;

import jakarta.servlet.http.HttpServletRequest;
import marketplace.application.AuditService;
import marketplace.domain.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private final AuditService auditService;
    private final HttpServletRequest request;

    public AuditAspect(AuditService auditService, HttpServletRequest request) {
        this.auditService = auditService;
        this.request = request;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        User user = request != null ? (User) request.getSession().getAttribute("user") : null;

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            String details = String.format("Method: %s, Duration: %d ms",
                    joinPoint.getSignature().toShortString(), duration);
            auditService.record(
                    user != null ? user.getId() : null,
                    auditable.action(),
                    details
            );
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - start;

            String details = String.format("Method: %s failed after %d ms, exception: %s",
                    joinPoint.getSignature().toShortString(), duration, t.toString());
            auditService.record(user != null ? user.getId() : null,
                    auditable.action(),
                    details);

            throw t;
        }
    }
}
