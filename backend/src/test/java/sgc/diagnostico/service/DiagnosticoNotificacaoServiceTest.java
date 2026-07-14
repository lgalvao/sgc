package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.alerta.dto.*;
import sgc.comum.config.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoNotificacaoServiceTest {

    @Mock
    DiagnosticoAlertaService alertaService;
    @Mock
    NotificacaoService notificacaoService;
    @Mock
    ResponsavelUnidadeService responsavelService;
    @Mock
    UsuarioService usuarioService;
    @Mock
    UnidadeService unidadeService;
    @Mock
    ConfigAplicacao configAplicacao;
    @Mock
    EmailModelosService emailModelosService;
    @Captor
    ArgumentCaptor<EnfileirarNotificacaoCommand> notificacaoCaptor;
    @InjectMocks
    DiagnosticoNotificacaoService service;

    @Test
    @DisplayName("aceite e devolução repetidos devem usar a movimentação para formar chaves distintas")
    void notificacoesRepetidasDevemUsarCodigoMovimentacaoNaIdempotencia() {
        Unidade unidadeOrigem = unidade(10L, "SESEL");
        Unidade unidadeDestino = unidade(20L, "COSIS");
        Subprocesso subprocesso = subprocesso(unidadeOrigem);
        prepararResponsavel(unidadeOrigem);
        prepararResponsavel(unidadeDestino);

        service.notificarDiagnosticoDevolvido(subprocesso, unidadeDestino, unidadeOrigem, "Ajustar", 101L);
        service.notificarDiagnosticoDevolvido(subprocesso, unidadeDestino, unidadeOrigem, "Ajustar", 102L);
        service.notificarDiagnosticoAceito(subprocesso, unidadeOrigem, unidadeDestino, 103L);
        service.notificarDiagnosticoAceito(subprocesso, unidadeOrigem, unidadeDestino, 104L);

        verify(notificacaoService, times(4)).enfileirar(notificacaoCaptor.capture());
        assertThat(notificacaoCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::chaveIdempotencia)
                .containsExactly(
                        "diagnostico:50:devolvido:destino:10:movimentacao:101",
                        "diagnostico:50:devolvido:destino:10:movimentacao:102",
                        "diagnostico:50:aceito:superior:20:movimentacao:103",
                        "diagnostico:50:aceito:superior:20:movimentacao:104"
                );
    }

    @Test
    @DisplayName("aceite em bloco repetido deve usar as movimentações para formar chaves distintas")
    void aceiteEmBlocoRepetidoDeveUsarCodigosMovimentacaoNaIdempotencia() {
        Unidade unidadeOrigem = unidade(10L, "SESEL");
        Unidade unidadeDestino = unidade(20L, "COSIS");
        Subprocesso subprocesso = subprocesso(unidadeOrigem);
        prepararResponsavel(unidadeDestino);

        service.notificarDiagnosticosAceitosEmBloco(List.of(subprocesso), unidadeOrigem, unidadeDestino, List.of(201L));
        service.notificarDiagnosticosAceitosEmBloco(List.of(subprocesso), unidadeOrigem, unidadeDestino, List.of(202L));

        verify(notificacaoService, times(2)).enfileirar(notificacaoCaptor.capture());
        assertThat(notificacaoCaptor.getAllValues())
                .extracting(EnfileirarNotificacaoCommand::chaveIdempotencia)
                .containsExactly(
                        "diagnostico:70:aceito:bloco:superior:20:unidades:SESEL:movimentacoes:201",
                        "diagnostico:70:aceito:bloco:superior:20:unidades:SESEL:movimentacoes:202"
                );
    }

    private void prepararResponsavel(Unidade unidade) {
        when(responsavelService.buscarResponsavelUnidade(unidade.getCodigo()))
                .thenReturn(new UnidadeResponsavelDto(unidade.getCodigo(), "151515", "Responsável", null, null));
    }

    private Unidade unidade(Long codigo, String sigla) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        return unidade;
    }

    private Subprocesso subprocesso(Unidade unidade) {
        Processo processo = new Processo();
        processo.setCodigo(70L);
        processo.setDescricao("Processo de diagnóstico");
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(50L);
        subprocesso.setUnidade(unidade);
        subprocesso.setProcesso(processo);
        return subprocesso;
    }
}
