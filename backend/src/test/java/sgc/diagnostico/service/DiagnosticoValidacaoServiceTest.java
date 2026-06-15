package sgc.diagnostico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.model.SituacaoCapacitacaoRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.ValorSituacaoCapacitacao;
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
    @Mock SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
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
        AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                .situacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO)
                .build();

        when(avaliacaoRepo.listarPorDiagnostico(20L)).thenReturn(List.of(avaliacao));

        assertThatThrownBy(() -> service.validarConclusaoUnidade(20L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_PENDENTE);
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando houver situação de capacitação sem valor para servidor aprovado")
    void validarConclusaoUnidade_deveFalharQuandoOcupacaoPendente() {
        sgc.organizacao.model.Usuario servidor = sgc.organizacao.model.Usuario.builder().tituloEleitoral("242426").build();
        sgc.mapa.model.Competencia comp = sgc.mapa.model.Competencia.builder().codigo(1L).build();

        AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                .servidor(servidor)
                .competencia(comp)
                .situacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .build();

        SituacaoCapacitacao situacao = SituacaoCapacitacao.builder()
                .servidor(servidor)
                .competencia(comp)
                .situacaoCapacitacao(null)
                .build();

        when(avaliacaoRepo.listarPorDiagnostico(21L)).thenReturn(List.of(avaliacao));
        when(situacaoCapacitacaoRepo.listarPorDiagnostico(21L)).thenReturn(List.of(situacao));

        assertThatThrownBy(() -> service.validarConclusaoUnidade(21L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_PENDENTE);
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve aceitar quando tudo estiver concluído (mesmo com impossibilitados)")
    void validarConclusaoUnidade_deveAceitarQuandoTudoConcluido() {
        sgc.organizacao.model.Usuario s1 = sgc.organizacao.model.Usuario.builder().tituloEleitoral("1").build();
        sgc.organizacao.model.Usuario s2 = sgc.organizacao.model.Usuario.builder().tituloEleitoral("2").build();
        sgc.mapa.model.Competencia c1 = sgc.mapa.model.Competencia.builder().codigo(10L).build();

        AvaliacaoServidor aprovada = AvaliacaoServidor.builder()
                .servidor(s1)
                .competencia(c1)
                .situacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .build();
        
        AvaliacaoServidor impossibilitada = AvaliacaoServidor.builder()
                .servidor(s2)
                .competencia(c1)
                .situacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
                .build();

        SituacaoCapacitacao situacaoS1 = SituacaoCapacitacao.builder()
                .servidor(s1)
                .competencia(c1)
                .situacaoCapacitacao(ValorSituacaoCapacitacao.AC)
                .build();

        when(avaliacaoRepo.listarPorDiagnostico(22L)).thenReturn(List.of(aprovada, impossibilitada));
        when(situacaoCapacitacaoRepo.listarPorDiagnostico(22L)).thenReturn(List.of(situacaoS1));

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
