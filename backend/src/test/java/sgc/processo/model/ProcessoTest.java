package sgc.processo.model;

import org.junit.jupiter.api.*;
import sgc.fixture.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
        @DisplayName("Deve criar processo com Builder (setando código manualmente)")
        void deveCriarProcessoComBuilderCompleto() {

            Long codigo = 1L;
            String descricao = "Processo Teste";
            TipoProcesso tipo = TipoProcesso.MAPEAMENTO;
            SituacaoProcesso situacao = SituacaoProcesso.CRIADO;
            LocalDateTime dataCriacao = LocalDateTime.now();


            Processo novoProcesso = Processo.builder()
                    .descricao(descricao)
                    .tipo(tipo)
                    .situacao(situacao)
                    .dataCriacao(dataCriacao)
                    .build();
            novoProcesso.setCodigo(codigo);


            assertThat(novoProcesso.getCodigo()).isEqualTo(codigo);
            assertThat(novoProcesso.getDescricao()).isEqualTo(descricao);
            assertThat(novoProcesso.getTipo()).isEqualTo(tipo);
            assertThat(novoProcesso.getSituacao()).isEqualTo(situacao);
            assertThat(novoProcesso.getDataCriacao()).isEqualTo(dataCriacao);
        }

        @Test
        @DisplayName("Deve criar processo com Builder (sem código)")
        void deveCriarProcessoComBuilderParcial() {

            String descricao = "Processo Novo";
            TipoProcesso tipo = TipoProcesso.REVISAO;
            SituacaoProcesso situacao = SituacaoProcesso.CRIADO;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);


            Processo novoProcesso = Processo.builder()
                    .descricao(descricao)
                    .tipo(tipo)
                    .situacao(situacao)
                    .dataLimite(dataLimite)
                    .build();


            assertThat(novoProcesso.getDescricao()).isEqualTo(descricao);
            assertThat(novoProcesso.getTipo()).isEqualTo(tipo);
            assertThat(novoProcesso.getSituacao()).isEqualTo(situacao);
            assertThat(novoProcesso.getDataLimite()).isEqualTo(dataLimite);
            assertThat(novoProcesso.getCodigo()).isNull();
        }

        @Test
        @DisplayName("Deve criar processo com Builder AllArgs")
        void deveCriarProcessoComBuilderAllArgs() {

            LocalDateTime dataCriacao = LocalDateTime.now();
            LocalDateTime dataFinalizacao = LocalDateTime.now().plusDays(10);
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(5);
            String descricao = "Processo Completo";
            SituacaoProcesso situacao = SituacaoProcesso.EM_ANDAMENTO;
            TipoProcesso tipo = TipoProcesso.MAPEAMENTO;
            List<UnidadeProcesso> participantes = new ArrayList<>();


            Processo novoProcesso = Processo.builder()
                    .dataCriacao(dataCriacao)
                    .dataFinalizacao(dataFinalizacao)
                    .dataLimite(dataLimite)
                    .descricao(descricao)
                    .situacao(situacao)
                    .tipo(tipo)
                    .participantes(participantes)
                    .build();


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
        @DisplayName("Deve inicializar participantes como lista vazia")
        void deveInicializarParticipantesComoListaVazia() {

            List<UnidadeProcesso> participantes = processo.getParticipantes();


            assertThat(participantes)
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar unidades participantes (criando snapshots)")
        void deveAdicionarUnidadesParticipantes() {

            Unidade unidade1 = UnidadeFixture.unidadePadrao();
            unidade1.setCodigo(1L);
            unidade1.setNome("Unidade 1");

            Unidade unidade2 = UnidadeFixture.unidadePadrao();
            unidade2.setCodigo(2L);
            unidade2.setNome("Unidade 2");


            processo.adicionarParticipantes(Set.of(unidade1));
            processo.adicionarParticipantes(Set.of(unidade2));


            assertThat(processo.getParticipantes()).hasSize(2);
            assertThat(processo.getParticipantes())
                    .extracting(UnidadeProcesso::getUnidadeCodigo)
                    .containsExactlyInAnyOrder(1L, 2L);
            assertThat(processo.getParticipantes())
                    .extracting(UnidadeProcesso::getNome)
                    .containsExactlyInAnyOrder("Unidade 1", "Unidade 2");
        }

        @Test
        @DisplayName("Deve substituir lista de participantes")
        void deveSubstituirListaDeParticipantes() {

            List<UnidadeProcesso> novosParticipantes = new ArrayList<>();
            Unidade unidade = UnidadeFixture.unidadePadrao();
            unidade.setCodigo(1L);

            // Simular criação manual de snapshot já que não temos o ID do processo persistido ainda
            UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(processo, unidade);
            novosParticipantes.add(snapshot);


            processo.setParticipantes(novosParticipantes);


            assertThat(processo.getParticipantes())
                    .isEqualTo(novosParticipantes)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Datas")
    class Datas {
        @Test
        @DisplayName("Deve configurar data de criação")
        void deveConfigurarDataDeCriacao() {

            LocalDateTime dataCriacao = LocalDateTime.now();


            processo.setDataCriacao(dataCriacao);


            assertThat(processo.getDataCriacao())
                    .isNotNull()
                    .isEqualTo(dataCriacao);
        }

        @Test
        @DisplayName("Deve configurar data de finalização")
        void deveConfigurarDataDeFinalizacao() {

            LocalDateTime dataFinalizacao = LocalDateTime.now().plusDays(30);


            processo.setDataFinalizacao(dataFinalizacao);


            assertThat(processo.getDataFinalizacao()).isEqualTo(dataFinalizacao);
        }

        @Test
        @DisplayName("Deve configurar data limite")
        void deveConfigurarDataLimite() {

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(15);


            processo.setDataLimite(dataLimite);


            assertThat(processo.getDataLimite())
                    .isEqualTo(dataLimite);
        }
    }

    @Nested
    @DisplayName("Situação e Tipo")
    class SituacaoETipo {
        @Test
        @DisplayName("Deve configurar situação do processo")
        void deveConfigurarSituacaoDoProcesso() {

            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);


            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        }

        @Test
        @DisplayName("Deve configurar tipo do processo")
        void deveConfigurarTipoDoProcesso() {

            processo.setTipo(TipoProcesso.REVISAO);


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

            String descricao = "Mapeamento de Competências 2024";


            processo.setDescricao(descricao);


            assertThat(processo.getDescricao()).isEqualTo(descricao);
        }
    }
}
