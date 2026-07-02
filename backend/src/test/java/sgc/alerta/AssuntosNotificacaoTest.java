package sgc.alerta;

import org.junit.jupiter.api.*;
import sgc.processo.model.*;
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
        assertThat(AssuntosNotificacao.processoFinalizado(TipoProcesso.MAPEAMENTO))
                .isEqualTo("SGC: Finalização de processo de mapeamento");
        assertThat(AssuntosNotificacao.processoFinalizado(TipoProcesso.REVISAO))
                .isEqualTo("SGC: Finalização de processo de revisão");
        assertThat(AssuntosNotificacao.processoFinalizado(TipoProcesso.DIAGNOSTICO))
                .isEqualTo("SGC: Finalização de processo de diagnóstico");
        assertThat(AssuntosNotificacao.processoFinalizadoUnidadesSubordinadas(TipoProcesso.MAPEAMENTO))
                .isEqualTo("SGC: Finalização de processo de mapeamento em unidades subordinadas");
        assertThat(AssuntosNotificacao.lembretePrazo("Processo X"))
                .isEqualTo("SGC: Lembrete de prazo - Processo X");
        assertThat(AssuntosNotificacao.atribuicaoPerfilChefe("SEC"))
                .isEqualTo("SGC: Atribuição de perfil CHEFE na unidade SEC");
        assertThat(AssuntosNotificacao.aceiteValidacaoBlocoDireto("SEC"))
                .isEqualTo("SGC: Validação do mapa de competências da SEC submetida para análise");
        assertThat(AssuntosNotificacao.aceiteValidacaoBlocoSuperior())
                .isEqualTo("SGC: Validação de mapas de competências submetida para análise");
        assertThat(AssuntosNotificacao.diagnosticoAutoavaliacaoConcluida("Maria"))
                .isEqualTo("SGC: Autoavaliação concluída: Maria");
        assertThat(AssuntosNotificacao.diagnosticoConsensoDisponivel())
                .isEqualTo("SGC: Avaliação de consenso criada");
        assertThat(AssuntosNotificacao.diagnosticoConsensoAprovado("Maria"))
                .isEqualTo("SGC: Avaliação de consenso aprovada: Maria");
        assertThat(AssuntosNotificacao.diagnosticoConcluido("SEC"))
                .isEqualTo("SGC: Diagnóstico da unidade SEC submetido para análise");
        assertThat(AssuntosNotificacao.diagnosticoDevolvido("SEC"))
                .isEqualTo("SGC: Diagnóstico da unidade SEC devolvido para ajustes");
        assertThat(AssuntosNotificacao.diagnosticoAceito("SEC"))
                .isEqualTo("SGC: Diagnóstico da unidade SEC aceito");
        assertThat(AssuntosNotificacao.diagnosticosAceitosEmBloco())
                .isEqualTo("SGC: Diagnósticos submetidos para análise");
        assertThat(AssuntosNotificacao.diagnosticoHomologado("SEC"))
                .isEqualTo("SGC: Diagnóstico da unidade SEC homologado");
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
