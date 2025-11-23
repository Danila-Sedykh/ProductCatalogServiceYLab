package marketplace.in.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import marketplace.application.AuditService;
import marketplace.application.AuthService;
import marketplace.aspect.AuditAspect;
import marketplace.config.*;
import marketplace.db.DataSourceFactory;
import marketplace.dto.AuthResponse;
import marketplace.dto.LoginRequest;
import marketplace.dto.RegisterRequest;
import marketplace.out.repository.AuditRepositoryJdbc;
import marketplace.out.repository.UserRepositoryJdbc;
import org.hibernate.validator.HibernateValidator;

import java.io.IOException;
import java.util.Set;

@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/register"})
public class AuthServlet extends HttpServlet {

    private AuthService authService;
    private AuditService auditService;
    private AuditAspect auditAspect;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Validator validator;

    @Override
    public void init() {
        var ds = DataSourceFactory.create(ConfigLoader.get("db.url"),
                ConfigLoader.get("db.username"),
                ConfigLoader.get("db.password"),
                Integer.parseInt(ConfigLoader.get("db.maximumPoolSize")));
        var userRepo = new UserRepositoryJdbc(ds, ConfigLoader.get("db.appSchema"));
        var auditRepo = new AuditRepositoryJdbc(ds, ConfigLoader.get("db.appSchema"));
        this.authService = new AuthService(userRepo);
        this.auditService = new AuditService(auditRepo);
        this.auditAspect = new AuditAspect(auditService);

        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure().buildValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        auditAspect.setRequest(req);
        try {
            if (req.getRequestURI().endsWith("/login")) {
                handleLogin(req, resp);
            } else if (req.getRequestURI().endsWith("/register")) {
                handleRegister(req, resp);
            }
        } finally {
            auditAspect.clearRequest();
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest loginReq = objectMapper.readValue(req.getInputStream(), LoginRequest.class);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(loginReq);
        if (!violations.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(violations));
            return;
        }

        var userOpt = authService.authenticate(loginReq.getUsername(), loginReq.getPassword());
        if (userOpt.isPresent()) {
            req.getSession().setAttribute("user", userOpt.get());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(objectMapper.writeValueAsString(
                    new AuthResponse("success", userOpt.get().getRole())));
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Invalid credentials\"}");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterRequest regReq = objectMapper.readValue(req.getInputStream(), RegisterRequest.class);

        boolean ok = authService.register(regReq.getUsername(), regReq.getPassword(), regReq.getRole());
        if (ok) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"status\":\"registered\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"error\":\"User already exists\"}");
        }
    }

    public void setupServices(AuthService authService, AuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
        this.auditAspect = new AuditAspect(auditService);
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}