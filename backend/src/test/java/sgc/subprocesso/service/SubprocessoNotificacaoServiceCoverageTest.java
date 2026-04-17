package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.EmailService;
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
    private EmailService emailService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private org.thymeleaf.spring6.SpringTemplateEngine templateEngine;

    @Test
    @DisplayName("enviarNotificacaoSuperior - deve cobrir merge function com duplicatas")
    void enviarNotificacaoSuperior_Duplicatas() {
        Processo p = new Processo(); p.setDescricao("P"); p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso(); sp.setProcesso(p);
        Unidade u = new Unidade(); u.setCodigo(10L); u.setSigla("U");
        sp.setUnidade(u);
        
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(sp)
                .unidadeOrigem(u)
                .unidadeDestino(new Unidade())
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .build();
        
        when(unidadeHierarquiaService.buscarCodigosSuperiores(10L)).thenReturn(List.of(20L));
        
        UnidadeResumoLeitura rl1 = mock(UnidadeResumoLeitura.class);
        when(rl1.codigo()).thenReturn(20L);
        when(rl1.sigla()).thenReturn("S1");
        
        UnidadeResumoLeitura rl2 = mock(UnidadeResumoLeitura.class);
        when(rl2.codigo()).thenReturn(20L);
        
        when(unidadeService.buscarResumosPorCodigos(anyList())).thenReturn(List.of(rl1, rl2));
        when(templateEngine.process(anyString(), any())).thenReturn("corpo");
        
        // Chamar o método privado via reflection para atingir linha 92
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(target, "enviarNotificacaoSuperior", cmd, new HashMap<>());
        
        verify(emailService).enviarEmailHtml(eq("s1@tre-pe.jus.br"), anyString(), anyString());
    }
}
