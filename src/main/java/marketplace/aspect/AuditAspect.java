package marketplace.aspect;

import jakarta.servlet.http.HttpServletRequest;
import marketplace.application.AuditService;
import marketplace.domain.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AuditAspect {
    private final AuditService auditService;
    private final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    public void setRequest(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public void clearRequest() {
        requestHolder.remove();
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        HttpServletRequest request = requestHolder.get();
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

            throw t;
        }
    }
}
