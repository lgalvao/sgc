package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoNotificacaoService - Cobertura")
class ProcessoNotificacaoServiceCoverageTest {

    @Mock
    private AlertaFacade alertaService;
    @Mock
    private EmailService emailService;
    @Mock
    private EmailModelosService emailModelosService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private ResponsavelUnidadeService responsavelService;
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private ProcessoNotificacaoService service;

    @Test
    @DisplayName("processarFinalizacaoProcesso - deve ignorar se sem participantes")
    void processarFinalizacaoProcesso_SemParticipantes() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        // p.participantes is initialized as empty list by default

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));

        service.emailFinalizacaoProcesso(codProcesso);

        verifyNoInteractions(responsavelService);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("processarInicioProcesso - sem subprocessos")
    void processarInicioProcesso_SemSubprocessos() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(Collections.emptyList());

        service.emailInicioProcesso(codProcesso);

        verifyNoInteractions(alertaService);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("processarInicioProcesso - com subprocessos e notificacao")
    void processarInicioProcesso_ComSubprocessos() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("P1");

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");
        u.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setProcesso(p);

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Map.of(10L, new UnidadeResponsavelDto(10L, "Titular", "Nome titular", "Substituto", "Nome substituto")));
        when(unidadeService.buscarPorId(10L)).thenReturn(u);
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Map.of("Titular", new Usuario()));

        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("HTML");

        service.emailInicioProcesso(codProcesso);

        verify(emailService, atLeastOnce()).enviarEmailHtml(anyString(), anyString(), anyString());
        verify(alertaService).criarAlertasProcessoIniciado(any(), anyList());
    }

    @Test
    @DisplayName("enviarEmailFinalizacao - deve enviar para unidade INTERMEDIARIA com subordinadas")
    void enviarEmailFinalizacao_Intermediaria() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("P1");

        Unidade inter = new Unidade();
        inter.setCodigo(10L);
        inter.setSigla("INTER");
        inter.setTipo(TipoUnidade.INTERMEDIARIA);
        inter.setSituacao(SituacaoUnidade.ATIVA);

        Unidade sub = new Unidade();
        sub.setCodigo(20L);
        sub.setSigla("SUB");
        sub.setTipo(TipoUnidade.OPERACIONAL);
        sub.setSituacao(SituacaoUnidade.ATIVA);
        sub.setUnidadeSuperior(inter);

        // Setup para processarFinalizacaoProcesso
        p.adicionarParticipantes(Set.of(inter, sub));

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.porCodigos(any())).thenReturn(List.of(inter, sub));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());
        when(usuarioService.buscarUsuariosPorTitulos(any())).thenReturn(Collections.emptyMap());

        when(emailModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(eq("INTER"), eq("P1"), anyList()))
                .thenReturn("HTML");

        service.emailFinalizacaoProcesso(codProcesso);

        verify(emailService).enviarEmailHtml(eq("inter@tre-pe.jus.br"), contains("Finalização"), eq("HTML"));
    }

    @Test
    @DisplayName("enviarEmailFinalizacao - deve ignorar unidade INTERMEDIARIA sem subordinadas participantes")
    void enviarEmailFinalizacao_IntermediariaSemSubordinadas() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);
        p.setDescricao("P1");

        Unidade inter = new Unidade();
        inter.setCodigo(10L);
        inter.setSigla("INTER");
        inter.setTipo(TipoUnidade.INTERMEDIARIA);
        inter.setSituacao(SituacaoUnidade.ATIVA);

        // Setup para processarFinalizacaoProcesso
        p.adicionarParticipantes(Set.of(inter));

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(unidadeService.porCodigos(any())).thenReturn(List.of(inter));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());

        service.emailFinalizacaoProcesso(codProcesso);

        verify(emailService, never()).enviarEmailHtml(eq("inter@tre-pe.jus.br"), anyString(), anyString());
    }

    @Test
    @DisplayName("emailInicioProcesso - deve tratar exceção ao enviar email")
    void emailInicioProcesso_ExcecaoEmail() {
        Long codProcesso = 1L;
        Processo p = new Processo();
        p.setCodigo(codProcesso);

        Unidade u = new Unidade();
        u.setCodigo(10L);
        u.setSigla("U1");
        u.setTipo(TipoUnidade.OPERACIONAL);

        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setProcesso(p);

        when(subprocessoService.listarEntidadesPorProcesso(codProcesso)).thenReturn(List.of(sp));

        when(processoRepo.findByIdComParticipantes(codProcesso)).thenReturn(Optional.of(p));
        when(responsavelService.buscarResponsaveisUnidades(any())).thenReturn(Collections.emptyMap());
        when(unidadeService.buscarPorId(10L)).thenReturn(u);

        when(emailModelosService.criarEmailInicioProcessoConsolidado(any(), any(), any(), anyBoolean(), anyList()))
                .thenReturn("HTML");

        doThrow(new RuntimeException("SMTP Error")).when(emailService).enviarEmailHtml(any(), any(), any());

        // Should not throw exception
        service.emailInicioProcesso(codProcesso);

        verify(alertaService).criarAlertasProcessoIniciado(any(), anyList());
    }
}
