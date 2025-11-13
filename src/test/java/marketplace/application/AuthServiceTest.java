package marketplace.application;

import marketplace.out.file.FileUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AuthServiceTest {
    private Path tmp;
    private FileUserRepository repo;
    private AuthService auth;

    @BeforeEach
    void init() throws Exception {
        tmp = Files.createTempFile("users",".db");
        repo = new FileUserRepository(tmp);
        auth = new AuthService(repo);
    }

    @AfterEach
    void cleanup() throws Exception { Files.deleteIfExists(tmp); }

    @Test
    void registerAndAuthenticate() {
        boolean r = auth.register("testuser","pass","USER");
        assertThat(r).isTrue();
        assertThat(auth.authenticate("testuser","pass").isPresent()).isTrue();
    }

}
