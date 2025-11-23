package marketplace.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import marketplace.application.AuthService;
import marketplace.application.AuditService;
import marketplace.aspect.AuditAspect;
import marketplace.domain.User;
import marketplace.dto.LoginRequest;
import marketplace.dto.RegisterRequest;
import marketplace.in.servlet.AuthServlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.System.out;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServletTest {

    @Mock
    private AuthService authService;
    @Mock
    private AuditService auditService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private AuthServlet servlet;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new AuthServlet();
        servlet.setupServices(authService, auditService);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    @DisplayName("должен быть успешно выполнен вход в систему")
    void loginSuccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getInputStream()).thenReturn(new MockServletInputStream(
                "{\"username\":\"admin\",\"password\":\"admin\"}".getBytes(StandardCharsets.UTF_8)
        ));
        when(request.getSession()).thenReturn(session);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        when(authService.authenticate("admin", "admin")).thenReturn(Optional.of(new User()));

        servlet.doPost(request, response);
        printWriter.flush();

        verify(authService).authenticate("admin", "admin");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertThat(stringWriter.toString()).contains("success");

    }

    @Test
    @DisplayName("должен возвращать 401 для неверных учетных данных")
    void loginInvalid() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getInputStream()).thenReturn(new MockServletInputStream(
                "{\"username\":\"hacker\",\"password\":\"123\"}".getBytes(StandardCharsets.UTF_8)
        ));
        when(request.getSession()).thenReturn(session);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        when(authService.authenticate("hacker", "123")).thenReturn(Optional.empty());

        servlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(stringWriter.toString()).contains("Invalid credentials");
    }

    @Test
    @DisplayName("должен зарегистрировать нового пользователя")
    void registerSuccess() throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername("newuser");
        regReq.setPassword("123");
        String json = objectMapper.writeValueAsString(regReq);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getInputStream()).thenReturn(new MockServletInputStream(json.getBytes(StandardCharsets.UTF_8)));
        when(request.getSession()).thenReturn(session);

        PrintWriter writer = new PrintWriter(new StringWriter());
        when(response.getWriter()).thenReturn(writer);

        servlet.doPost(request, response);

        verify(authService).register("newuser", "123", "USER");
    }

    private static class MockServletInputStream extends ServletInputStream {

        private final InputStream inputStream;

        public MockServletInputStream(String content) {
            this.inputStream = new ByteArrayInputStream(content.getBytes());
        }

        public MockServletInputStream(byte[] content) {
            this.inputStream = new ByteArrayInputStream(content);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }
    }

}
