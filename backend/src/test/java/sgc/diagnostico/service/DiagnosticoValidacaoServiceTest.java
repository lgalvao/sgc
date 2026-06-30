package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.diagnostico.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoValidacaoServiceTest {

    @Mock
    AvaliacaoServidorRepo avaliacaoRepo;
    @Mock
    SubprocessoVisualizacaoService subprocessoVisualizacaoService;

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
    @DisplayName("validarConclusaoUnidade deve falhar quando não houver avaliações")
    void validarConclusaoUnidade_deveFalharQuandoSemAvaliacoes() {
        when(avaliacaoRepo.existsByDiagnosticoCodigo(19L)).thenReturn(false);

        assertThatThrownBy(() -> service.validarConclusaoUnidade(19L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_PENDENTE);
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando houver avaliação pendente")
    void validarConclusaoUnidade_deveFalharQuandoAvaliacaoPendente() {
        when(avaliacaoRepo.existsByDiagnosticoCodigo(20L)).thenReturn(true);
        when(avaliacaoRepo.existsAvaliacaoPendentePorDiagnostico(
                20L,
                java.util.EnumSet.of(
                        SituacaoAvaliacaoServidor.CONSENSO_APROVADO,
                        SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                )
        )).thenReturn(true);

        assertThatThrownBy(() -> service.validarConclusaoUnidade(20L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_PENDENTE);
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando houver situação de capacitação pendente para servidor aprovado")
    void validarConclusaoUnidade_deveFalharQuandoOcupacaoPendente() {
        when(avaliacaoRepo.existsByDiagnosticoCodigo(21L)).thenReturn(true);
        when(avaliacaoRepo.existsAvaliacaoPendentePorDiagnostico(
                21L,
                java.util.EnumSet.of(
                        SituacaoAvaliacaoServidor.CONSENSO_APROVADO,
                        SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                )
        )).thenReturn(false);
        when(avaliacaoRepo.existsAvaliacaoAprovadaSemSituacaoCapacitacao(
                21L,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        )).thenReturn(true);

        assertThatThrownBy(() -> service.validarConclusaoUnidade(21L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_PENDENTE);
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve aceitar quando tudo estiver concluído")
    void validarConclusaoUnidade_deveAceitarQuandoTudoConcluido() {
        when(avaliacaoRepo.existsByDiagnosticoCodigo(22L)).thenReturn(true);
        when(avaliacaoRepo.existsAvaliacaoPendentePorDiagnostico(
                22L,
                java.util.EnumSet.of(
                        SituacaoAvaliacaoServidor.CONSENSO_APROVADO,
                        SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                )
        )).thenReturn(false);
        when(avaliacaoRepo.existsAvaliacaoAprovadaSemSituacaoCapacitacao(
                22L,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        )).thenReturn(false);
        when(avaliacaoRepo.existsByDiagnosticoCodigoAndSituacaoServidor(
                22L,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        )).thenReturn(true);

        assertThatCode(() -> service.validarConclusaoUnidade(22L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarConclusaoUnidade deve falhar quando todos os servidores estiverem impossibilitados")
    void validarConclusaoUnidade_deveFalharQuandoTodosImpossibilitados() {
        when(avaliacaoRepo.existsByDiagnosticoCodigo(23L)).thenReturn(true);
        when(avaliacaoRepo.existsAvaliacaoPendentePorDiagnostico(
                23L,
                java.util.EnumSet.of(
                        SituacaoAvaliacaoServidor.CONSENSO_APROVADO,
                        SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                )
        )).thenReturn(false);
        when(avaliacaoRepo.existsAvaliacaoAprovadaSemSituacaoCapacitacao(
                23L,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        )).thenReturn(false);
        when(avaliacaoRepo.existsByDiagnosticoCodigoAndSituacaoServidor(
                23L,
                SituacaoAvaliacaoServidor.CONSENSO_APROVADO
        )).thenReturn(false);

        assertThatThrownBy(() -> service.validarConclusaoUnidade(23L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.DIAGNOSTICO_TODOS_SERVIDORES_IMPOSSIBILITADOS);
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
