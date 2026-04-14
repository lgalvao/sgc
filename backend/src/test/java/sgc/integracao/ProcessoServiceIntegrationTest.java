package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.comum.erros.*;
import sgc.processo.dto.*;
import sgc.integracao.mocks.*;
import sgc.processo.model.*;
import sgc.processo.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Integração - ProcessoService")
class ProcessoServiceIntegrationTest extends BaseIntegrationTest {
    private static final Long CODIGO_UNIDADE_MAPEAMENTO = 8L;
    private static final Long CODIGO_UNIDADE_SEM_MAPA = 15L;

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
}
