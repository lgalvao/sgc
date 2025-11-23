package sgc.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("e2e")
@Profile("e2e") // Ensure this test runs only with the e2e profile
public class DirectE2eTestControllerInvocationTest {

    @Autowired
    private E2eTestController e2eTestController;

    @Test
    void testRecarregarDadosTesteDirectly() {
        System.out.println("Attempting to call recarregarDadosTeste() directly...");
        ResponseEntity<Map<String, String>> response = e2eTestController.recarregarDadosTeste();
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody());
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(); // Expecting 2xx success
        assertThat(response.getBody()).containsKey("mensagem");
        System.out.println("Error message from response body: " + response.getBody().get("mensagem"));
    }
}
