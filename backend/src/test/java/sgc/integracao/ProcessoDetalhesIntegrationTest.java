package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração focados no endpoint /api/processos/{id}. Este endpoint é
 * chamado pelo
 * ProcessoView para mostrar a lista de unidades participantes.
 */
@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Endpoint: /api/processos/{id}")
@Import(TestSecurityConfig.class)
class ProcessoDetalhesIntegrationTest extends BaseIntegrationTest {
    private static final String API_PROCESSO_DETALHES = "/api/processos/{codProcesso}";

    private Processo processoEmAndamento;
    private Processo processoFinalizado;

    @BeforeEach
    void setUp() {
        Unidade unidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC

        // Processo em andamento
        processoEmAndamento = Processo.builder()
                .descricao("Processo em Andamento")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoEmAndamento.setParticipantes(Set.of(unidade));
        processoEmAndamento = processoRepo.saveAndFlush(processoEmAndamento);

        var mapaEmAndamento = mapaRepo.save(new Mapa());
        var subprocessoEmAndamento = Subprocesso.builder()
                .processo(processoEmAndamento)
                .unidade(unidade)
                .mapa(mapaEmAndamento)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .dataLimiteEtapa1(processoEmAndamento.getDataLimite())
                .build();
        subprocessoRepo.save(subprocessoEmAndamento);

        // Processo finalizado
        processoFinalizado = Processo.builder()
                .descricao("Processo Finalizado")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.FINALIZADO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoFinalizado.setParticipantes(Set.of(unidade));
        processoFinalizado.setDataFinalizacao(LocalDateTime.now());
        processoFinalizado = processoRepo.saveAndFlush(processoFinalizado);

        var mapaFinalizado = mapaRepo.save(new Mapa());
        var subprocessoFinalizado = Subprocesso.builder()
                .processo(processoFinalizado)
                .unidade(unidade)
                .mapa(mapaFinalizado)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processoFinalizado.getDataLimite())
                .build();
        subprocessoRepo.save(subprocessoFinalizado);
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar detalhes com unidades para processo em andamento")
    void deveRetornarDetalhesComUnidades_QuandoProcessoEmAndamento() throws Exception {
        mockMvc.perform(get(API_PROCESSO_DETALHES, processoEmAndamento.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(processoEmAndamento.getCodigo().intValue())))
                .andExpect(jsonPath("$.situacao", is("EM_ANDAMENTO")));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar detalhes com unidades para processo FINALIZADO")
    void deveRetornarDetalhesComUnidades_QuandoProcessoFinalizado() throws Exception {
        mockMvc.perform(get(API_PROCESSO_DETALHES, processoFinalizado.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(processoFinalizado.getCodigo().intValue())))
                .andExpect(jsonPath("$.situacao", is("FINALIZADO")));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 quando processo não existe")
    void deveRetornar404_QuandoProcessoNaoExiste() throws Exception {
        mockMvc.perform(get(API_PROCESSO_DETALHES, 999999L)).andExpect(status().isNotFound());
    }
}
