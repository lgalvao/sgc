package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.diagnostico.model.*;
import sgc.integracao.mocks.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@DisplayName("CDU-50: Analisar diagnóstico")
class CDU50IntegrationTest extends DiagnosticoCduIntegrationTestBase {
    private static final String API_DIAGNOSTICO = "/api/subprocessos/{codSubprocesso}/diagnostico";

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @BeforeEach
    void setUp() {
        criarCenarioDiagnosticoBase(9L, "50003", "50004");
        preencherConsenso("50003", 4, 4, 4, 4, 4, 4, 4, 4, SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        preencherAutoavaliacao("50004", 3, 3, 2, 2, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        preencherSituacoesCapacitacao("50003", ValorSituacaoCapacitacao.EC, ValorSituacaoCapacitacao.C);
        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocessoRepo.saveAndFlush(subprocesso);
        movimentacaoRepo.saveAndFlush(sgc.subprocesso.model.Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeRepo.findById(9L).orElseThrow())
                .unidadeDestino(unidadeRepo.findById(6L).orElseThrow())
                .usuario(usuarioRepo.findById("333333333333").orElseThrow())
                .descricao("Conclusão de diagnóstico")
                .build());
        recarregarContexto();
    }

    @Test
    @WithMockGestor("666666666666")
    @DisplayName("GESTOR deve visualizar o diagnóstico da unidade e registrar aceite")
    void gestorDeveVisualizarDiagnosticoERegistrarAceite() throws Exception {
        mockMvc.perform(get(API_DIAGNOSTICO + "/equipe", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servidores.length()").value(2));

        mockMvc.perform(get(API_DIAGNOSTICO + "/unidade", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.unidadeSigla").value("SEDIA"))
                .andExpect(jsonPath("$.servidores.length()").value(2))
                .andExpect(jsonPath("$.movimentacoes.length()").isNotEmpty());

        mockMvc.perform(post(API_DIAGNOSTICO + "/validar", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComumDtos.TextoOpcionalRequest("Pode seguir"))))
                .andExpect(status().isOk());

        recarregarContexto();

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao().name()).isEqualTo("ACEITE_DIAGNOSTICO"));
        assertThat(alertaRepo.findAll()).anySatisfy(alerta ->
                assertThat(alerta.getDescricao()).isEqualTo("Diagnóstico da unidade SEDIA aceito"));
        assertThat(notificacaoEmailRepo.findAll()).anySatisfy(notificacao ->
                assertThat(notificacao.getAssunto()).contains("Diagnóstico da unidade SEDIA aceito"));
    }

    @Test
    @WithMockGestor("666666666666")
    @DisplayName("GESTOR deve devolver para ajustes sem reabrir a autoavaliação da unidade")
    void gestorDeveDevolverParaAjustes() throws Exception {
        buscarAvaliacoes("50003").forEach(avaliacao ->
                avaliacao.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.CONSENSO_CRIADO));
        buscarAvaliacoes("50004").forEach(avaliacao ->
                avaliacao.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA));
        avaliacaoServidorRepo.flush();

        mockMvc.perform(post(API_DIAGNOSTICO + "/devolver", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComumDtos.JustificativaRequest("Ajustar consenso"))))
                .andExpect(status().isOk());

        recarregarContexto();

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        assertThat(buscarAvaliacoes("50003")).allSatisfy(avaliacao ->
                {
                    assertThat(avaliacao.getSituacaoServidor())
                            .isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
                    assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
                });
        assertThat(buscarAvaliacoes("50004")).allSatisfy(avaliacao ->
                {
                    assertThat(avaliacao.getSituacaoServidor())
                            .isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
                    assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
                });
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao().name()).isEqualTo("DEVOLUCAO_DIAGNOSTICO"));
    }

    @Test
    @WithMockAdmin
    @DisplayName("ADMIN deve homologar após o aceite e registrar homologação no histórico")
    void adminDeveHomologarAposAceite() throws Exception {
        analiseRepo.saveAndFlush(Analise.builder()
                .tipo(TipoAnalise.DIAGNOSTICO)
                .subprocesso(subprocesso)
                .acao(TipoAcaoAnalise.ACEITE_DIAGNOSTICO)
                .dataHora(LocalDateTime.now())
                .unidadeCodigo(6L)
                .usuarioTitulo("666666666666")
                .observacoes("Pode seguir")
                .build());
        movimentacaoRepo.saveAndFlush(sgc.subprocesso.model.Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeRepo.findById(6L).orElseThrow())
                .unidadeDestino(unidadeRepo.findById(1L).orElseThrow())
                .usuario(usuarioRepo.findById("666666666666").orElseThrow())
                .descricao("Diagnóstico aceito")
                .build());
        recarregarContexto();

        mockMvc.perform(get(API_DIAGNOSTICO + "/equipe", subprocesso.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servidores.length()").value(2));

        mockMvc.perform(post(API_DIAGNOSTICO + "/homologar", subprocesso.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComumDtos.TextoOpcionalRequest("Homologado"))))
                .andExpect(status().isOk());

        recarregarContexto();

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        assertThat(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo()))
                .anySatisfy(analise -> assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO));
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocesso.getCodigo()))
                .anySatisfy(movimentacao -> assertThat(movimentacao.getDescricao()).isEqualTo("Homologação de diagnóstico"));
    }
}
