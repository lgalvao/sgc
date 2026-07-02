package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.comum.erros.*;
import sgc.integracao.mocks.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Testes de Integração - ProcessoService")
class ProcessoServiceIntegrationTest extends BaseIntegrationTest {
    // Unidade 8 está em processo EM_ANDAMENTO no data.sql — usar apenas onde não se precisar iniciar processo
    private static final Long CODIGO_UNIDADE_MAPEAMENTO = 8L;
    private static final Long CODIGO_UNIDADE_SEM_MAPA = 15L;
    // Unidades livres (sem processo ativo), com mapa vigente para REVISAO/DIAGNOSTICO
    private static final Long CODIGO_UNIDADE_LIVRE_MAPA = 9L;   // SEDIA — mapa vigente 1002
    private static final Long CODIGO_UNIDADE_LIVRE_MAPA2 = 10L; // SESEL — mapa vigente 1003
    @Autowired
    private ProcessoService service;

    @Nested
    @DisplayName("Criação de Processo")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar processo do tipo REVISAO com sucesso")
        void deveCriarProcessoRevisaoComSucesso() {

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Revisão")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(8L))
                    .build();

            Processo resultado = service.criar(request);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getDescricao()).isEqualTo("Processo de Revisão");
            assertThat(resultado.getTipo()).isEqualTo(TipoProcesso.REVISAO);
            assertThat(resultado.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
            assertThat(resultado.getParticipantes()).hasSize(1);
        }

        @Test
        @DisplayName("Deve criar processo do tipo DIAGNOSTICO com sucesso")
        void deveCriarProcessoDiagnosticoComSucesso() {

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Diagnóstico")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(8L))
                    .build();

            Processo resultado = service.criar(request);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTipo()).isEqualTo(TipoProcesso.DIAGNOSTICO);
        }

        @Test
        @DisplayName("Deve criar processo do tipo MAPEAMENTO sem validação de mapa")
        void deveCriarProcessoMapeamentoSemValidacao() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Mapeamento")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build();

            Processo resultado = service.criar(request);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO);
        }

        @Test
        @DisplayName("Deve lançar erro ao criar processo com unidades sem mapa (REVISAO/DIAGNOSTICO)")
        void deveLancarErroAoCriarSemMapa() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Revisão falho")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)

                    .unidades(List.of(CODIGO_UNIDADE_SEM_MAPA))
                    .build();

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Não foi possível concluir a operação.");
        }

        @Test
        @DisplayName("Deve lançar erro ao criar processo com unidade sem responsável efetivo")
        void deveLancarErroAoCriarComUnidadeSemResponsavelEfetivo() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo com unidade inelegível")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(905L))
                    .build();

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Não foi possível concluir a operação.");
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class AtualizacaoTests {

        @Test
        @DisplayName("Deve atualizar processo na situação CRIADO com sucesso")
        void deveAtualizarProcessoCriadoComSucesso() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            Processo criado = service.criar(CriarProcessoRequest.builder()
                    .descricao("Original")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build());

            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(criado.getCodigo())
                    .descricao("Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build();

            Processo resultado = service.atualizar(criado.getCodigo(), request);

            assertThat(resultado.getDescricao()).isEqualTo("Atualizada");
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar processo fora da situação CRIADO")
        void deveLancarErroAoAtualizarProcessoEmAndamento() {
            Processo p = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo em andamento")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build());
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            processoRepo.saveAndFlush(p);

            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(p.getCodigo())
                    .descricao("Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build();

            assertThatThrownBy(() -> service.atualizar(p.getCodigo(), request))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar processo para REVISAO com unidades sem mapa")
        void deveLancarErroAoAtualizarComUnidadeSemMapa() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            Processo criado = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo Original")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build());

            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(criado.getCodigo())
                    .descricao("Atualizada")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(CODIGO_UNIDADE_SEM_MAPA))
                    .build();

            assertThatThrownBy(() -> service.atualizar(criado.getCodigo(), request))
                    .isInstanceOf(ErroValidacao.class);
        }
    }

    @Nested
    @DisplayName("Início de Processo")
    class InicioTests {

        @Test
        @DisplayName("Deve iniciar mapeamento com unidade interoperacional e gerar subprocesso para ela")
        @WithMockAdmin
        void deveIniciarMapeamentoComUnidadeInteroperacional() {
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Mapeamento com STIC")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(2L, 9L))
                    .build());

            service.iniciar(processo.getCodigo(), List.of(2L, 9L));

            Processo processoIniciado = processoRepo.buscarPorCodigoComParticipantes(processo.getCodigo()).orElseThrow();
            assertThat(processoIniciado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            assertThat(processoIniciado.getParticipantes().stream().map(UnidadeProcesso::getUnidadeCodigo).toList())
                    .containsExactlyInAnyOrder(2L, 9L);

            assertThat(subprocessoRepo.listarPorProcessoComUnidade(processo.getCodigo()).stream()
                    .map(subprocesso -> subprocesso.getUnidade().getCodigo())
                    .toList()).containsExactlyInAnyOrder(2L, 9L);
        }
    }

    @Nested
    @DisplayName("Consultas de Processo")
    class ConsultaTests {

        @Test
        @DisplayName("Deve listar processos ativos")
        @WithMockAdmin
        void deveListarAtivos() throws Exception {
            mockMvc.perform(get("/api/processos/ativos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo MAPEAMENTO")
        @WithMockAdmin
        void deveListarUnidadesBloqueadasPorTipoMapeamento() throws Exception {
            mockMvc.perform(get("/api/processos/unidades-bloqueadas")
                            .param("tipo", "MAPEAMENTO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve listar unidades bloqueadas por tipo REVISAO")
        @WithMockAdmin
        void deveListarUnidadesBloqueadasPorTipoRevisao() throws Exception {
            mockMvc.perform(get("/api/processos/unidades-bloqueadas")
                            .param("tipo", "REVISAO"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis como ADMIN")
        @WithMockAdmin
        void deveListarSubprocessosElegiveisComoAdmin() {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo elegíveis")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            List<SubprocessoElegivelDto> resultado = service.listarSubprocessosElegiveis(processo.getCodigo());

            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("Deve listar subprocessos elegíveis como CHEFE")
        @WithMockChefe("333333333333")
        void deveListarSubprocessosElegiveisComoChefe() {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo elegíveis chefe")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            List<SubprocessoElegivelDto> resultado = service.listarSubprocessosElegiveis(processo.getCodigo());

            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("Deve buscar IDs de unidades com processos ativos excluindo processo específico")
        @WithMockAdmin
        void deveBuscarIdsUnidadesComProcessosAtivos() {
            Set<Long> ids = service.buscarIdsUnidadesComProcessosAtivos(-1L);

            assertThat(ids).isNotNull();
        }
    }

    @Nested
    @DisplayName("Início de Processo — tipos distintos")
    class InicioTiposTests {

        @Test
        @DisplayName("Deve iniciar processo REVISAO exercitando branches específicos de REVISAO/DIAGNOSTICO")
        @WithMockAdmin
        void deveIniciarProcessoRevisao() {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo REVISAO inicio")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());

            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            Processo iniciado = processoRepo.findById(processo.getCodigo()).orElseThrow();
            assertThat(iniciado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            assertThat(iniciado.getTipo()).isEqualTo(TipoProcesso.REVISAO);
        }

        @Test
        @DisplayName("Deve iniciar processo DIAGNOSTICO exercitando branches específicos de DIAGNOSTICO")
        @WithMockAdmin
        void deveIniciarProcessoDiagnostico() {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo DIAGNOSTICO inicio")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA2))
                    .build());

            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA2));

            Processo iniciado = processoRepo.findById(processo.getCodigo()).orElseThrow();
            assertThat(iniciado.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            assertThat(iniciado.getTipo()).isEqualTo(TipoProcesso.DIAGNOSTICO);

            List<Subprocesso> subs = subprocessoRepo.listarPorProcessoComUnidade(processo.getCodigo());
            assertThat(subs).isNotEmpty();
            assertThat(subs.getFirst().getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        }

        @Test
        @DisplayName("Deve lançar erro ao tentar iniciar processo já em andamento")
        @WithMockAdmin
        void deveLancarErroAoIniciarProcessoJaEmAndamento() {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo já iniciado")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            assertThatThrownBy(() -> service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA)))
                    .isInstanceOf(ErroValidacao.class);
        }
    }

    @Nested
    @DisplayName("Remoção de Processo")
    class RemocaoTests {

        @Test
        @DisplayName("Deve apagar processo na situação CRIADO com sucesso")
        void deveApagarProcessoCriadoComSucesso() {
            Processo criado = service.criar(CriarProcessoRequest.builder()
                    .descricao("A apagar")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build());

            service.apagar(criado.getCodigo());

            assertThat(processoRepo.findById(criado.getCodigo())).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao apagar processo fora da situação CRIADO")
        void deveLancarErroAoApagarProcessoEmAndamento() {
            Processo p = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo não removível")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_MAPEAMENTO))
                    .build());
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            processoRepo.saveAndFlush(p);

            assertThatThrownBy(() -> service.apagar(p.getCodigo()))
                    .isInstanceOf(ErroValidacao.class);
        }
    }

    @Nested
    @DisplayName("Acesso e Detalhes")
    class AcessoDetalhesTests {

        @Test
        @DisplayName("Deve acessar detalhes do processo como CHEFE da unidade participante (checarAcesso)")
        @WithMockChefe("333333333333")
        void deveAcessarDetalhesComoChefeDaUnidade() throws Exception {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo acesso chefe")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(processo.getCodigo()));
        }

        @Test
        @DisplayName("Deve acessar contexto completo (incluirElegiveis=true) como GESTOR")
        @WithMockGestor("666666666666")
        void deveAcessarContextoCompletoComoGestor() throws Exception {
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo contexto gestor")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA));

            mockMvc.perform(get("/api/processos/{codigo}/contexto-completo", processo.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.codigo").value(processo.getCodigo()));
        }

        @Test
        @DisplayName("Deve negar acesso ao CHEFE sem subprocesso no processo")
        @WithMockChefe("333333333333")
        void deveDenegarAcessoAoChefeForaDoProcesso() throws Exception {
            // Processo com unit 10 — CHEFE de unit 9 não tem acesso
            Processo processo = service.criar(CriarProcessoRequest.builder()
                    .descricao("Processo sem acesso")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(CODIGO_UNIDADE_LIVRE_MAPA2))
                    .build());
            service.iniciar(processo.getCodigo(), List.of(CODIGO_UNIDADE_LIVRE_MAPA2));

            mockMvc.perform(get("/api/processos/{codigo}/detalhes", processo.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }
}
