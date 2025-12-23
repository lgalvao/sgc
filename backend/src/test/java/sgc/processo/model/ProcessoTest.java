package sgc.processo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sgc.fixture.UnidadeFixture;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes para Processo")
class ProcessoTest {

    private Processo processo;

    @BeforeEach
    void setUp() {
        processo = new Processo();
    }

    @Nested
    @DisplayName("Construtores")
    class Construtores {

        @Test
        @DisplayName("Deve criar processo com construtor de 5 parâmetros")
        void deveCriarProcessoComConstrutorCincoParametros() {
            // Arrange
            Long codigo = 1L;
            String descricao = "Processo Teste";
            TipoProcesso tipo = TipoProcesso.MAPEAMENTO;
            SituacaoProcesso situacao = SituacaoProcesso.CRIADO;
            LocalDateTime dataCriacao = LocalDateTime.now();

            // Act
            Processo novoProcesso = new Processo(codigo, descricao, tipo, situacao, dataCriacao);

            // Assert
            assertThat(novoProcesso.getCodigo()).isEqualTo(codigo);
            assertThat(novoProcesso.getDescricao()).isEqualTo(descricao);
            assertThat(novoProcesso.getTipo()).isEqualTo(tipo);
            assertThat(novoProcesso.getSituacao()).isEqualTo(situacao);
            assertThat(novoProcesso.getDataCriacao()).isEqualTo(dataCriacao);
        }

        @Test
        @DisplayName("Deve criar processo com construtor de 4 parâmetros")
        void deveCriarProcessoComConstrutorQuatroParametros() {
            // Arrange
            String descricao = "Processo Novo";
            TipoProcesso tipo = TipoProcesso.REVISAO;
            SituacaoProcesso situacao = SituacaoProcesso.CRIADO;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);

            // Act
            Processo novoProcesso = new Processo(descricao, tipo, situacao, dataLimite);

            // Assert
            assertThat(novoProcesso.getDescricao()).isEqualTo(descricao);
            assertThat(novoProcesso.getTipo()).isEqualTo(tipo);
            assertThat(novoProcesso.getSituacao()).isEqualTo(situacao);
            assertThat(novoProcesso.getDataLimite()).isEqualTo(dataLimite);
            assertThat(novoProcesso.getDataCriacao()).isNotNull();
            assertThat(novoProcesso.getCodigo()).isNull();
        }

        @Test
        @DisplayName("Deve criar processo com construtor AllArgs")
        void deveCriarProcessoComConstrutorAllArgs() {
            // Arrange
            LocalDateTime dataCriacao = LocalDateTime.now();
            LocalDateTime dataFinalizacao = LocalDateTime.now().plusDays(10);
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(5);
            String descricao = "Processo Completo";
            SituacaoProcesso situacao = SituacaoProcesso.EM_ANDAMENTO;
            TipoProcesso tipo = TipoProcesso.MAPEAMENTO;
            Set<Unidade> participantes = new HashSet<>();

            // Act
            Processo novoProcesso = new Processo(
                    dataCriacao,
                    dataFinalizacao,
                    dataLimite,
                    descricao,
                    situacao,
                    tipo,
                    participantes
            );

            // Assert
            assertThat(novoProcesso.getDataCriacao()).isEqualTo(dataCriacao);
            assertThat(novoProcesso.getDataFinalizacao()).isEqualTo(dataFinalizacao);
            assertThat(novoProcesso.getDataLimite()).isEqualTo(dataLimite);
            assertThat(novoProcesso.getDescricao()).isEqualTo(descricao);
            assertThat(novoProcesso.getSituacao()).isEqualTo(situacao);
            assertThat(novoProcesso.getTipo()).isEqualTo(tipo);
            assertThat(novoProcesso.getParticipantes()).isEqualTo(participantes);
        }
    }

    @Nested
    @DisplayName("Participantes")
    class Participantes {

        @Test
        @DisplayName("Deve inicializar participantes como conjunto vazio")
        void deveInicializarParticipantesComoConjuntoVazio() {
            // Act
            Set<Unidade> participantes = processo.getParticipantes();

            // Assert
            assertThat(participantes).isNotNull();
            assertThat(participantes).isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar unidades participantes")
        void deveAdicionarUnidadesParticipantes() {
            // Arrange
            Unidade unidade1 = UnidadeFixture.unidadePadrao();
            unidade1.setCodigo(1L);
            unidade1.setNome("Unidade 1");

            Unidade unidade2 = UnidadeFixture.unidadePadrao();
            unidade2.setCodigo(2L);
            unidade2.setNome("Unidade 2");

            // Act
            processo.getParticipantes().add(unidade1);
            processo.getParticipantes().add(unidade2);

            // Assert
            assertThat(processo.getParticipantes()).hasSize(2);
            assertThat(processo.getParticipantes()).contains(unidade1, unidade2);
        }

        @Test
        @DisplayName("Deve substituir conjunto de participantes")
        void deveSubstituirConjuntoDeParticipantes() {
            // Arrange
            Set<Unidade> novosParticipantes = new HashSet<>();
            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setCodigo(1L);
            novosParticipantes.add(unidade);

            // Act
            processo.setParticipantes(novosParticipantes);

            // Assert
            assertThat(processo.getParticipantes()).isEqualTo(novosParticipantes);
            assertThat(processo.getParticipantes()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Datas")
    class Datas {

        @Test
        @DisplayName("Deve configurar data de criação")
        void deveConfigurarDataDeCriacao() {
            // Arrange
            LocalDateTime dataCriacao = LocalDateTime.now();

            // Act
            processo.setDataCriacao(dataCriacao);

            // Assert
            assertThat(processo.getDataCriacao()).isEqualTo(dataCriacao);
        }

        @Test
        @DisplayName("Deve configurar data de finalização")
        void deveConfigurarDataDeFinalizacao() {
            // Arrange
            LocalDateTime dataFinalizacao = LocalDateTime.now().plusDays(30);

            // Act
            processo.setDataFinalizacao(dataFinalizacao);

            // Assert
            assertThat(processo.getDataFinalizacao()).isEqualTo(dataFinalizacao);
        }

        @Test
        @DisplayName("Deve configurar data limite")
        void deveConfigurarDataLimite() {
            // Arrange
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(15);

            // Act
            processo.setDataLimite(dataLimite);

            // Assert
            assertThat(processo.getDataLimite()).isEqualTo(dataLimite);
        }
    }

    @Nested
    @DisplayName("Situação e Tipo")
    class SituacaoETipo {

        @Test
        @DisplayName("Deve configurar situação do processo")
        void deveConfigurarSituacaoDoProcesso() {
            // Act
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            // Assert
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        }

        @Test
        @DisplayName("Deve configurar tipo do processo")
        void deveConfigurarTipoDoProcesso() {
            // Act
            processo.setTipo(TipoProcesso.REVISAO);

            // Assert
            assertThat(processo.getTipo()).isEqualTo(TipoProcesso.REVISAO);
        }

        @Test
        @DisplayName("Deve permitir todas as situações válidas")
        void devePermitirTodasAsSituacoesValidas() {
            // Arrange & Act & Assert
            for (SituacaoProcesso situacao : SituacaoProcesso.values()) {
                processo.setSituacao(situacao);
                assertThat(processo.getSituacao()).isEqualTo(situacao);
            }
        }

        @Test
        @DisplayName("Deve permitir todos os tipos válidos")
        void devePermitirTodosOsTiposValidos() {
            // Arrange & Act & Assert
            for (TipoProcesso tipo : TipoProcesso.values()) {
                processo.setTipo(tipo);
                assertThat(processo.getTipo()).isEqualTo(tipo);
            }
        }
    }

    @Nested
    @DisplayName("Descrição")
    class Descricao {

        @Test
        @DisplayName("Deve configurar descrição do processo")
        void deveConfigurarDescricaoDoProcesso() {
            // Arrange
            String descricao = "Mapeamento de Competências 2024";

            // Act
            processo.setDescricao(descricao);

            // Assert
            assertThat(processo.getDescricao()).isEqualTo(descricao);
        }
    }
}
