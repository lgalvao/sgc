package sgc.alerta;

import org.junit.jupiter.api.*;
import sgc.subprocesso.model.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AssuntosNotificacao")
class AssuntosNotificacaoTest {

    @Test
    @DisplayName("deve retornar assuntos de início de processo")
    void deveRetornarAssuntosDeInicioDeProcesso() {
        assertThat(AssuntosNotificacao.inicioProcesso("MAPEAMENTO", true))
                .isEqualTo("SGC: Início de processo de mapeamento de competências");
        assertThat(AssuntosNotificacao.inicioProcesso("REVISAO", false))
                .isEqualTo("SGC: Início de processo de revisão do mapa de competências em unidades subordinadas");
        assertThat(AssuntosNotificacao.inicioProcesso("DIAGNOSTICO", true))
                .isEqualTo("SGC: Início de processo de diagnóstico");
        assertThat(AssuntosNotificacao.inicioProcesso("OUTRO", true))
                .isEqualTo("SGC: Início de processo de outro");
    }

    @Test
    @DisplayName("deve retornar assuntos simples")
    void deveRetornarAssuntosSimples() {
        assertThat(AssuntosNotificacao.processoFinalizado("Processo X"))
                .isEqualTo("SGC: Finalização do processo Processo X");
        assertThat(AssuntosNotificacao.processoFinalizadoUnidadesSubordinadas("Processo X"))
                .isEqualTo("SGC: Finalização do processo Processo X em unidades subordinadas");
        assertThat(AssuntosNotificacao.lembretePrazo("Processo X"))
                .isEqualTo("SGC: Lembrete de prazo - Processo X");
        assertThat(AssuntosNotificacao.atribuicaoPerfilChefe("SEC"))
                .isEqualTo("SGC: Atribuição de perfil CHEFE na unidade SEC");
        assertThat(AssuntosNotificacao.disponibilizacaoMapaBloco())
                .isEqualTo("SGC: Mapas de competências disponibilizados");
        assertThat(AssuntosNotificacao.aceiteValidacaoBlocoDireto("SEC"))
                .isEqualTo("SGC: Validação do mapa de competências da SEC submetida para análise");
        assertThat(AssuntosNotificacao.aceiteValidacaoBlocoSuperior())
                .isEqualTo("SGC: Validação de mapas de competências submetida para análise");
    }

    @Test
    @DisplayName("deve retornar assuntos de subprocesso")
    void deveRetornarAssuntosDeSubprocesso() {
        assertThat(AssuntosNotificacao.subprocesso(TipoTransicao.CADASTRO_ACEITO, "SEC", false))
                .isEqualTo("SGC: Cadastro de atividades e conhecimentos da SEC submetido para análise");
        assertThat(AssuntosNotificacao.subprocesso(TipoTransicao.CADASTRO_DISPONIBILIZADO, "SEC", false))
                .isEqualTo("SGC: Cadastro de atividades e conhecimentos disponibilizado - SEC");
        assertThat(AssuntosNotificacao.subprocesso(TipoTransicao.REVISAO_CADASTRO_REABERTA, "SEC", true))
                .isEqualTo("SGC: Reabertura de revisão de cadastro - SEC");
    }

    @Test
    @DisplayName("deve retornar assuntos de aceite de cadastro em bloco")
    void deveRetornarAssuntosDeAceiteDeCadastroEmBloco() {
        assertThat(AssuntosNotificacao.cadastroAceitoBloco(true))
                .isEqualTo("SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise");
        assertThat(AssuntosNotificacao.cadastroAceitoBloco(false))
                .isEqualTo("SGC: Cadastros de atividades e conhecimentos submetidos para análise");
    }
}
