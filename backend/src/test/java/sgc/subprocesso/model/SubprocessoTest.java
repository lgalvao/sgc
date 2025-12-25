package sgc.subprocesso.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sgc.fixture.MapaFixture;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes para Subprocesso")
class SubprocessoTest {

    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(1L);
        
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(10L);
        
        // Mapa será criado individualmente em cada teste quando necessário
    }

    @Nested
    @DisplayName("Construtores")
    class Construtores {

        @Test
        @DisplayName("Deve criar subprocesso com construtor de 6 parâmetros")
        void deveCriarSubprocessoComConstrutorSeisParametros() {
            // Arrange
            Long codigo = 1L;
            SituacaoSubprocesso situacao = SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            Subprocesso subTemp = new Subprocesso();
            Mapa mapa = MapaFixture.mapaPadrao(subTemp);
            mapa.setCodigo(100L);

            // Act
            Subprocesso subprocesso = new Subprocesso(
                    codigo,
                    processo,
                    unidade,
                    mapa,
                    situacao,
                    dataLimite
            );

            // Assert
            assertThat(subprocesso.getCodigo()).isEqualTo(codigo);
            assertThat(subprocesso.getProcesso()).isEqualTo(processo);
            assertThat(subprocesso.getUnidade()).isEqualTo(unidade);
            assertThat(subprocesso.getMapa()).isEqualTo(mapa);
            assertThat(subprocesso.getSituacao()).isEqualTo(situacao);
            assertThat(subprocesso.getDataLimiteEtapa1()).isEqualTo(dataLimite);
        }

        @Test
        @DisplayName("Deve criar subprocesso com construtor de conveniência")
        void deveCriarSubprocessoComConstrutorDeConveniencia() {
            // Arrange
            SituacaoSubprocesso situacao = SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            Subprocesso subTemp = new Subprocesso();
            Mapa mapa = MapaFixture.mapaPadrao(subTemp);

            // Act
            Subprocesso subprocesso = new Subprocesso(
                    processo,
                    unidade,
                    mapa,
                    situacao,
                    dataLimite
            );

            // Assert
            assertThat(subprocesso.getCodigo()).isNull();
            assertThat(subprocesso.getProcesso()).isEqualTo(processo);
            assertThat(subprocesso.getUnidade()).isEqualTo(unidade);
            assertThat(subprocesso.getMapa()).isEqualTo(mapa);
            assertThat(subprocesso.getSituacao()).isEqualTo(situacao);
            assertThat(subprocesso.getDataLimiteEtapa1()).isEqualTo(dataLimite);
        }

        @Test
        @DisplayName("Deve criar subprocesso com builder")
        void deveCriarSubprocessoComBuilder() {
            // Arrange
            LocalDateTime dataLimiteEtapa1 = LocalDateTime.now().plusDays(15);
            LocalDateTime dataLimiteEtapa2 = LocalDateTime.now().plusDays(30);
            
            Subprocesso subTemp = new Subprocesso();
            Mapa mapa = MapaFixture.mapaPadrao(subTemp);

            // Act
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .mapa(mapa)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                    .dataLimiteEtapa1(dataLimiteEtapa1)
                    .dataLimiteEtapa2(dataLimiteEtapa2)
                    .build();

            // Assert
            assertThat(subprocesso.getProcesso()).isEqualTo(processo);
            assertThat(subprocesso.getUnidade()).isEqualTo(unidade);
            assertThat(subprocesso.getMapa()).isEqualTo(mapa);
            assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            assertThat(subprocesso.getDataLimiteEtapa1()).isEqualTo(dataLimiteEtapa1);
            assertThat(subprocesso.getDataLimiteEtapa2()).isEqualTo(dataLimiteEtapa2);
        }
    }

    @Nested
    @DisplayName("Verificação de Estado")
    class VerificacaoDeEstado {

        @ParameterizedTest
        @EnumSource(
                value = SituacaoSubprocesso.class,
                names = {"MAPEAMENTO_CADASTRO_EM_ANDAMENTO", "MAPEAMENTO_MAPA_CRIADO", "REVISAO_CADASTRO_EM_ANDAMENTO"}
        )
        @DisplayName("Deve considerar subprocesso em andamento para situações não finalizadas")
        void deveConsiderarSubprocessoEmAndamentoParaSituacoesNaoFinalizadas(SituacaoSubprocesso situacao) {
            // Arrange
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .situacao(situacao)
                    .build();

            // Act
            boolean emAndamento = subprocesso.isEmAndamento();

            // Assert
            assertThat(emAndamento).isTrue();
        }

        @ParameterizedTest
        @EnumSource(
                value = SituacaoSubprocesso.class,
                names = {"MAPEAMENTO_MAPA_HOMOLOGADO", "REVISAO_MAPA_HOMOLOGADO", "NAO_INICIADO"}
        )
        @DisplayName("Não deve considerar subprocesso em andamento para situações finalizadas")
        void naoDeveConsiderarSubprocessoEmAndamentoParaSituacoesFinalizadas(SituacaoSubprocesso situacao) {
            // Arrange
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .situacao(situacao)
                    .build();

            // Act
            boolean emAndamento = subprocesso.isEmAndamento();

            // Assert
            assertThat(emAndamento).isFalse();
        }
    }

    @Nested
    @DisplayName("Etapa Atual")
    class EtapaAtual {

        @Test
        @DisplayName("Deve retornar etapa 1 quando subprocesso não está finalizado")
        void deveRetornarEtapa1QuandoSubprocessoNaoEstaFinalizado() {
            // Arrange
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                    .build();

            // Act
            Integer etapa = subprocesso.getEtapaAtual();

            // Assert
            assertThat(etapa).isEqualTo(1);
        }

        @ParameterizedTest
        @EnumSource(
                value = SituacaoSubprocesso.class,
                names = {"MAPEAMENTO_MAPA_HOMOLOGADO", "REVISAO_MAPA_HOMOLOGADO"}
        )
        @DisplayName("Deve retornar null quando subprocesso está finalizado")
        void deveRetornarNullQuandoSubprocessoEstaFinalizado(SituacaoSubprocesso situacao) {
            // Arrange
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .situacao(situacao)
                    .build();

            // Act
            Integer etapa = subprocesso.getEtapaAtual();

            // Assert
            assertThat(etapa).isNull();
        }
    }

    @Nested
    @DisplayName("Mapa")
    class MapaTestes {

        @Test
        @DisplayName("Deve retornar mapa quando configurado")
        void deveRetornarMapaQuandoConfigurado() {
            // Arrange
            Subprocesso subTemp = new Subprocesso();
            Mapa mapa = MapaFixture.mapaPadrao(subTemp);
            
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .mapa(mapa)
                    .build();

            // Act
            Mapa mapaRetornado = subprocesso.getMapa();

            // Assert
            assertThat(mapaRetornado).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve retornar null quando mapa não está configurado")
        void deveRetornarNullQuandoMapaNaoEstaConfigurado() {
            // Arrange
            Subprocesso subprocesso = Subprocesso.builder()
                    .processo(processo)
                    .unidade(unidade)
                    .mapa(null)
                    .build();

            // Act
            Mapa mapaRetornado = subprocesso.getMapa();

            // Assert
            assertThat(mapaRetornado).isNull();
        }
    }

    @Nested
    @DisplayName("Datas de Etapa")
    class DatasDeEtapa {

        @Test
        @DisplayName("Deve configurar todas as datas de etapa")
        void deveConfigurarTodasAsDatasDeEtapa() {
            // Arrange
            LocalDateTime dataLimiteEtapa1 = LocalDateTime.now().plusDays(10);
            LocalDateTime dataFimEtapa1 = LocalDateTime.now().plusDays(9);
            LocalDateTime dataLimiteEtapa2 = LocalDateTime.now().plusDays(20);
            LocalDateTime dataFimEtapa2 = LocalDateTime.now().plusDays(19);

            Subprocesso subprocesso = new Subprocesso();

            // Act
            subprocesso.setDataLimiteEtapa1(dataLimiteEtapa1);
            subprocesso.setDataFimEtapa1(dataFimEtapa1);
            subprocesso.setDataLimiteEtapa2(dataLimiteEtapa2);
            subprocesso.setDataFimEtapa2(dataFimEtapa2);

            // Assert
            assertThat(subprocesso.getDataLimiteEtapa1()).isEqualTo(dataLimiteEtapa1);
            assertThat(subprocesso.getDataFimEtapa1()).isEqualTo(dataFimEtapa1);
            assertThat(subprocesso.getDataLimiteEtapa2()).isEqualTo(dataLimiteEtapa2);
            assertThat(subprocesso.getDataFimEtapa2()).isEqualTo(dataFimEtapa2);
        }
    }
}
