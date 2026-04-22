package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoNotificacaoService - Cobertura de Testes")
class SubprocessoNotificacaoServiceCoverageTest {

    @InjectMocks
    private SubprocessoNotificacaoService target;

    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    @Test
    @DisplayName("criarNotificacaoSuperior - deve enviar apenas ao superior imediato")
    void criarNotificacaoSuperior_ApenasSuperiorImediato() {
        Processo p = new Processo(); p.setDescricao("P"); p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso(); sp.setProcesso(p);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setSigla("U");
        sp.setUnidade(u);
        Unidade destino = new Unidade(); destino.setCodigo(99L);
        
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(sp)
                .unidadeOrigem(u)
                .unidadeDestino(destino)
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .build();
        
        when(unidadeHierarquiaService.buscarCodigoPai(10L)).thenReturn(20L);
        
        UnidadeResumoLeitura rl1 = mock(UnidadeResumoLeitura.class);
        when(rl1.sigla()).thenReturn("S1");

        when(unidadeService.buscarResumosPorCodigos(List.of(20L))).thenReturn(List.of(rl1));
        when(templateEngine.process(anyString(), any())).thenReturn("corpo");
        
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "criarNotificacaoSuperior", cmd, new HashMap<>());
        
        verify(notificacaoService).enfileirar(argThat(cmdEmail ->
                "s1@tre-pe.jus.br".equals(cmdEmail.destinatario())
        ));
    }
}
