package sgc.integracao;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.login.GerenciadorJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("secure-test")
public abstract class BaseRestAssuredTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected GerenciadorJwt gerenciadorJwt;

    @BeforeEach
    public void setup() {
        System.out.println("Configuring RestAssured with port: " + port);
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    protected String gerarToken(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
        return gerenciadorJwt.gerarToken(tituloEleitoral, perfil, unidadeCodigo);
    }
}
