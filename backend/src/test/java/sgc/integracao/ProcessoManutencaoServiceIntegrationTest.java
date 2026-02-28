package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import sgc.processo.dto.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;
import sgc.processo.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes de Integração - ProcessoManutencaoService")
class ProcessoManutencaoServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessoManutencaoService service;

    @Nested
    @DisplayName("Criação de Processo")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar processo do tipo REVISAO com sucesso")
        void deveCriarProcessoRevisaoComSucesso() {
            // Unidade 8 já possui mapa vigente através do Processo 50000 -> Subprocesso 60000 (Mapa 1001) no data.sql
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
            // Unidade 8 já possui mapa vigente através do Processo 50000 -> Subprocesso 60000 (Mapa 1001) no data.sql
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
                    .unidades(List.of(1L))
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
                    .descricao("Processo de Revisão Falho")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    // ADMIN unit usually does not have a map in tests
                    .unidades(List.of(1L))
                    .build();

            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("mapa vigente");
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
                    .unidades(List.of(1L))
                    .build());

            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(criado.getCodigo())
                    .descricao("Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo resultado = service.atualizar(criado.getCodigo(), request);

            assertThat(resultado.getDescricao()).isEqualTo("Atualizada");
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar processo fora da situação CRIADO")
        void deveLancarErroAoAtualizarProcessoEmAndamento() {
            Processo p = processoRepo.findById(50000L).orElseThrow();
            // Processo 50000 is EM_ANDAMENTO in data.sql

            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(p.getCodigo())
                    .descricao("Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(8L))
                    .build();

            assertThatThrownBy(() -> service.atualizar(p.getCodigo(), request))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }

    @Nested
    @DisplayName("Remoção de Processo")
    class RemocaoTests {

        @Test
        @DisplayName("Deve apagar processo na situação CRIADO com sucesso")
        void deveApagarProcessoCriadoComSucesso() {
            Processo criado = service.criar(CriarProcessoRequest.builder()
                    .descricao("A Apagar")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                    .unidades(List.of(1L))
                    .build());

            service.apagar(criado.getCodigo());

            assertThat(processoRepo.findById(criado.getCodigo())).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao apagar processo fora da situação CRIADO")
        void deveLancarErroAoApagarProcessoEmAndamento() {
            Processo p = processoRepo.findById(50000L).orElseThrow();
            // Processo 50000 is EM_ANDAMENTO in data.sql

            assertThatThrownBy(() -> service.apagar(p.getCodigo()))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }
}
