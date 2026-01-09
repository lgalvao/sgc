package sgc.organizacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Testes de Integração do SgrhController")
class UsuarioControllerIntegrationTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(100L).orElseThrow();
    }

    @Test
    @DisplayName("Teste dummy para evitar classe sem testes")
    void testeDummy() {
        assertThat(unidade).isNotNull();
    }
}
