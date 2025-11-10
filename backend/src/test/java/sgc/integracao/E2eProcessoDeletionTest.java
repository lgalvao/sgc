package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.util.TestUtil;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("E2E: Teste de Deleção de Processo via Repositórios")
public class E2eProcessoDeletionTest extends BaseIntegrationTest {

    private static final String API_PROCESSOS = "/api/processos";

    @Autowired
    private TestUtil testUtil;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    private void apagarProcessoPorCodigo(Long codigo) {
        List<Long> codigosProcesso = List.of(codigo);
        
        // 1. Buscar e deletar alertas
        var alertaIds = alertaRepo.findIdsByProcessoCodigoIn(codigosProcesso);
        if (!alertaIds.isEmpty()) {
            alertaUsuarioRepo.deleteByIdAlertaCodigoIn(alertaIds);
        }
        alertaRepo.deleteByProcessoCodigoIn(codigosProcesso);
        
        // 2. Remover movimentações e subprocessos antes do processo (FKs)
        codigosProcesso.forEach(codProcesso -> {
            subprocessoRepo.findByProcessoCodigo(codProcesso).forEach(sp -> {
                movimentacaoRepo.findBySubprocessoCodigo(sp.getCodigo()).forEach(mv -> movimentacaoRepo.deleteById(mv.getCodigo()));
                subprocessoRepo.deleteById(sp.getCodigo());
            });
            processoRepo.deleteById(codProcesso);
        });
    }

    @Test
    @DisplayName("Deve criar um processo novo e deletá-lo via repositórios sem erros de integridade")
    void testCriarEDeletarProcessoViaRepositorios() throws Exception {
        // Arrange: Criar um novo processo
        CriarProcessoReq criarReq = new CriarProcessoReq(
                "Processo de Teste para E2E Deletion",
                sgc.processo.model.TipoProcesso.MAPEAMENTO,
                null,
                List.of(1L) // ADMIN-UNIT
        );

        // Act: Criar o processo
        var resultadoCriacao = mockMvc.perform(post(API_PROCESSOS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtil.toJson(criarReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.descricao", equalTo("Processo de Teste para E2E Deletion")))
                .andExpect(jsonPath("$.situacao", equalTo("CRIADO")))
                .andReturn();

        // Extrair o código do processo criado
        String responseBody = resultadoCriacao.getResponse().getContentAsString();
        Long codigoProcesso = testUtil.extrairCodigoDaResposta(responseBody);

        // Verificar que o processo foi criado no banco
        assertTrue(processoRepo.existsById(codigoProcesso),
                "Processo deveria existir no banco após criação");

        // Act: Deletar o processo via repositórios (simula endpoint e2e)
        apagarProcessoPorCodigo(codigoProcesso);

        // Assert: Verificar que o processo foi deletado
        assertFalse(processoRepo.existsById(codigoProcesso),
                "Processo deveria ser deletado após chamada ao método de deleção");

        // Assert: Verificar que não há subprocessos órfãos
        assertTrue(subprocessoRepo.findByProcessoCodigo(codigoProcesso).isEmpty(),
                "Não deveria haver subprocessos órfãos após deleção do processo");
    }

    @Test
    @DisplayName("Deve deletar sem erro um processo inexistente")
    void testDeletarProcessoInexistente() throws Exception {
        // Arrange
        Long codigoProcessoInexistente = 999999L;
        assertFalse(processoRepo.existsById(codigoProcessoInexistente), "Pré-requisito: processo deve não existir");

        // Act: Deletar não deve lançar exceção
        apagarProcessoPorCodigo(codigoProcessoInexistente);

        // Assert: confirmar que realmente não existe
        assertFalse(processoRepo.existsById(codigoProcessoInexistente));
    }

    @Test
    @DisplayName("Deve deletar múltiplos processos sequencialmente sem erros de integridade")
    void testDeletarMultiplosProcessosSequencialmente() throws Exception {
        // Arrange: Criar múltiplos processos
        var codigosProcessos = new java.util.ArrayList<Long>();

        for (int i = 0; i < 3; i++) {
            CriarProcessoReq criarReq = new CriarProcessoReq(
                    "Processo Teste " + i,
                    sgc.processo.model.TipoProcesso.MAPEAMENTO,
                    null,
                    List.of(1L)
            );

            var resultadoCriacao = mockMvc.perform(post(API_PROCESSOS)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(testUtil.toJson(criarReq)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String responseBody = resultadoCriacao.getResponse().getContentAsString();
            Long codigoProcesso = testUtil.extrairCodigoDaResposta(responseBody);
            codigosProcessos.add(codigoProcesso);
        }

        // Verificar que todos foram criados
        assertEquals(3, codigosProcessos.size(), "Todos os 3 processos deveriam ter sido criados");
        for (Long codigo : codigosProcessos) {
            assertTrue(processoRepo.existsById(codigo), "Processo " + codigo + " deveria existir");
        }

        // Act: Deletar todos via método direto
        for (Long codigo : codigosProcessos) {
            apagarProcessoPorCodigo(codigo);
        }

        // Assert: Verificar que todos foram deletados
        for (Long codigo : codigosProcessos) {
            assertFalse(processoRepo.existsById(codigo),
                    "Processo " + codigo + " deveria estar deletado");
        }
    }
}

