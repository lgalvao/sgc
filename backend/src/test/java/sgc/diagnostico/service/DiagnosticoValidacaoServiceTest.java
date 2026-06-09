package sgc.diagnostico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.OcupacaoCriticaRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoVisualizacaoService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticoValidacaoServiceTest {

    @Mock AvaliacaoServidorRepo avaliacaoRepo;
    @Mock OcupacaoCriticaRepo ocupacaoRepo;
    @Mock SubprocessoVisualizacaoService subprocessoVisualizacaoService;

    @InjectMocks
    DiagnosticoValidacaoService service;

    @Test
    @DisplayName("validarAutoavaliacaoCompleta deve falhar quando houver nota faltante")
    void validarAutoavaliacaoCompleta_deveFalharQuandoIncompleta() {
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        avaliacao.setImportancia(4);
        avaliacao.setDominio(null);

        when(avaliacaoRepo.buscarAvaliacoesDoServidor(10L, "242426")).thenReturn(List.of(avaliacao));

        assertThatThrownBy(() -> service.validarAutoavaliacaoCompleta(10L, "242426"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Preencha importância e domínio para todas as competências.");
    }

    @Test
    @DisplayName("validarAutoavaliacaoCompleta deve aceitar quando todas as notas estiverem preenchidas")
    void validarAutoavaliacaoCompleta_deveAceitarQuandoCompleta() {
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);

        when(avaliacaoRepo.buscarAvaliacoesDoServidor(10L, "242426")).thenReturn(List.of(avaliacao));

        assertThatCode(() -> service.validarAutoavaliacaoCompleta(10L, "242426"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando houver avaliação pendente")
    void validarConclusaoUnidade_deveFalharQuandoAvaliacaoPendente() {
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);

        OcupacaoCritica ocupacao = new OcupacaoCritica();
        ocupacao.setSituacaoCapacitacao(SituacaoCapacitacao.EC);

        when(avaliacaoRepo.listarPorDiagnostico(20L)).thenReturn(List.of(avaliacao));
        when(ocupacaoRepo.listarPorDiagnostico(20L)).thenReturn(List.of(ocupacao));

        assertThatThrownBy(() -> service.validarConclusaoUnidade(20L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Ainda existem avaliações ou ocupações críticas pendentes.");
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando houver ocupação crítica sem situação")
    void validarConclusaoUnidade_deveFalharQuandoOcupacaoPendente() {
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);

        OcupacaoCritica ocupacao = new OcupacaoCritica();
        ocupacao.setSituacaoCapacitacao(null);

        when(avaliacaoRepo.listarPorDiagnostico(21L)).thenReturn(List.of(avaliacao));
        when(ocupacaoRepo.listarPorDiagnostico(21L)).thenReturn(List.of(ocupacao));

        assertThatThrownBy(() -> service.validarConclusaoUnidade(21L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Ainda existem avaliações ou ocupações críticas pendentes.");
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve aceitar quando tudo estiver concluído")
    void validarConclusaoUnidade_deveAceitarQuandoTudoConcluido() {
        AvaliacaoServidor aprovada = new AvaliacaoServidor();
        aprovada.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        AvaliacaoServidor impossibilitada = new AvaliacaoServidor();
        impossibilitada.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);

        OcupacaoCritica ocupacao = new OcupacaoCritica();
        ocupacao.setSituacaoCapacitacao(SituacaoCapacitacao.AC);

        when(avaliacaoRepo.listarPorDiagnostico(22L)).thenReturn(List.of(aprovada, impossibilitada));
        when(ocupacaoRepo.listarPorDiagnostico(22L)).thenReturn(List.of(ocupacao));

        assertThatCode(() -> service.validarConclusaoUnidade(22L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarDiagnosticoHomologavel deve falhar sem aceite prévio")
    void validarDiagnosticoHomologavel_deveFalharSemAceitePrevio() {
        Long codSubprocesso = 99L;
        when(subprocessoVisualizacaoService.possuiAnalise(
                codSubprocesso,
                TipoAnalise.DIAGNOSTICO,
                TipoAcaoAnalise.ACEITE_DIAGNOSTICO
        )).thenReturn(false);

        assertThatThrownBy(() -> service.validarDiagnosticoHomologavel(codSubprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Diagnóstico fora da situação esperada para esta ação.");
    }
}
