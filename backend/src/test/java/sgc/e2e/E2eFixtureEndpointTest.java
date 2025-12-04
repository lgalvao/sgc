package sgc.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.SituacaoProcesso;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para os endpoints de fixtures do E2eController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
class E2eFixtureEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void devePermitirCriarProcessoMapeamentoViaFixture() {
        // Preparar requisição
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Processo Fixture Teste Mapeamento",
                "ASSESSORIA_11",
                false,
                30
        );

        // Executar
        ResponseEntity<ProcessoDto> response = restTemplate.postForEntity(
                "/e2e/fixtures/processo-mapeamento",
                request,
                ProcessoDto.class
        );

        // Validar
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Processo Fixture Teste Mapeamento", response.getBody().getDescricao());
        assertEquals("MAPEAMENTO", response.getBody().getTipo());
        assertEquals(SituacaoProcesso.CRIADO, response.getBody().getSituacao());
    }

    @Test
    void devePermitirCriarEIniciarProcessoMapeamentoViaFixture() {
        // Preparar requisição
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Processo Fixture Teste Mapeamento Iniciado",
                "ASSESSORIA_12",
                true, // iniciar = true
                30
        );

        // Executar
        ResponseEntity<ProcessoDto> response = restTemplate.postForEntity(
                "/e2e/fixtures/processo-mapeamento",
                request,
                ProcessoDto.class
        );

        // Validar
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Processo Fixture Teste Mapeamento Iniciado", response.getBody().getDescricao());
        assertEquals("MAPEAMENTO", response.getBody().getTipo());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, response.getBody().getSituacao());
    }

    @Test
    void deveRetornarErroQuandoUnidadeNaoExiste() {
        // Preparar requisição com unidade inexistente
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Processo Fixture Teste",
                "UNIDADE_INEXISTENTE",
                false,
                30
        );

        // Executar
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/e2e/fixtures/processo-mapeamento",
                request,
                String.class
        );

        // Validar - deve retornar erro
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    void deveGerarDescricaoAutomaticaQuandoNaoFornecida() {
        // Preparar requisição sem descrição
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                null, // sem descrição
                "ASSESSORIA_21",
                false,
                30
        );

        // Executar
        ResponseEntity<ProcessoDto> response = restTemplate.postForEntity(
                "/e2e/fixtures/processo-mapeamento",
                request,
                ProcessoDto.class
        );

        // Validar
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDescricao());
        assertTrue(response.getBody().getDescricao().contains("Processo Fixture E2E"));
        assertTrue(response.getBody().getDescricao().contains("MAPEAMENTO"));
    }
}
