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

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Endpoint: /api/subprocessos/buscar")
@Import(TestSecurityConfig.class)
class SubprocessoBuscarIntegrationTest extends BaseIntegrationTest {
    private static final String API_SUBPROCESSOS_BUSCAR = "/api/subprocessos/buscar";

    private Unidade unidade;
    private Processo processoEmAndamento;
    private Processo processoFinalizado;
    private Subprocesso subprocessoEmAndamento;
    private Subprocesso subprocessoFinalizado;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(11L).orElseThrow(); // SENIC

        // Processo em andamento
        processoEmAndamento = Processo.builder()
                .descricao("Processo em Andamento")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoEmAndamento.setParticipantes(Set.of(unidade));
        processoRepo.save(processoEmAndamento);

        var mapaEmAndamento = mapaRepo.save(new Mapa());
        subprocessoEmAndamento = Subprocesso.builder()
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
        processoRepo.save(processoFinalizado);

        var mapaFinalizado = mapaRepo.save(new Mapa());
        subprocessoFinalizado = Subprocesso.builder().processo(processoFinalizado).unidade(unidade).mapa(mapaFinalizado)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(processoFinalizado.getDataLimite()).build();
        subprocessoRepo.save(subprocessoFinalizado);
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar subprocesso para processo em andamento")
    void deveRetornarSubprocesso_QuandoProcessoEmAndamento() throws Exception {
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", processoEmAndamento.getCodigo().toString())
                        .param("siglaUnidade", unidade.getSigla()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocessoEmAndamento.getCodigo().intValue())));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar subprocesso para processo FINALIZADO")
    void deveRetornarSubprocesso_QuandoProcessoFinalizado() throws Exception {
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", processoFinalizado.getCodigo().toString())
                        .param("siglaUnidade", unidade.getSigla()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is(subprocessoFinalizado.getCodigo().intValue())));
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 quando sigla de unidade não existe")
    void deveRetornar404_QuandoSiglaUnidadeNaoExiste() throws Exception {
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", processoEmAndamento.getCodigo().toString())
                        .param("siglaUnidade", "UNIDADE_INEXISTENTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 quando processo não existe")
    void deveRetornar404_QuandoProcessoNaoExiste() throws Exception {
        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", "999999")
                        .param("siglaUnidade", unidade.getSigla()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockAdmin
    @DisplayName("Deve retornar 404 quando unidade existe mas não participa do processo")
    void deveRetornar404_QuandoUnidadeNaoParticipaDoProcesso() throws Exception {
        // Busca uma unidade que existe mas não participa do processo
        Unidade outraUnidade = unidadeRepo.findById(1L).orElseThrow(); // SEDOC

        mockMvc.perform(get(API_SUBPROCESSOS_BUSCAR)
                        .param("codProcesso", processoEmAndamento.getCodigo().toString())
                        .param("siglaUnidade", outraUnidade.getSigla()))
                .andExpect(status().isNotFound());
    }
}
