package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoNotificacaoService - Cobertura Extra")
class ProcessoNotificacaoServiceCoverageExtraTest {

    @Mock private AlertaFacade servicoAlertas;
    @Mock private EmailService emailService;
    @Mock private EmailModelosService emailModelosService;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private UsuarioFacade usuarioService;
    @Mock private ProcessoRepo processoRepo;
    @Mock private SubprocessoService subprocessoService;

    @InjectMocks
    private ProcessoNotificacaoService notificacaoService;

    @Test
    @DisplayName("emailFinalizacaoProcesso - deve cobrir ramos extras para INTERMEDIARIA, RAIZ, e envio para substituto")
    void emailFinalizacaoProcesso_RamosExtras() {
        Processo proc = new Processo();
        proc.setCodigo(1L);
        proc.setDescricao("Processo 1");


        Unidade u1 = new Unidade();
        u1.setCodigo(10L);
        u1.setSigla("U1");
        u1.setNome("Unidade 1");
        u1.setTipo(TipoUnidade.RAIZ);
        u1.setSituacao(SituacaoUnidade.ATIVA);

        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        u2.setSigla("U2");
        u2.setNome("Unidade 2");
        u2.setTipo(TipoUnidade.INTERMEDIARIA);
        u2.setSituacao(SituacaoUnidade.ATIVA);

        Unidade uSub = new Unidade();
        uSub.setCodigo(30L);
        uSub.setSigla("USUB");
        uSub.setUnidadeSuperior(u2);
        proc.adicionarParticipantes(Set.of(u1, u2));

        when(processoRepo.findByIdComParticipantes(1L)).thenReturn(Optional.of(proc));
        when(organizacaoFacade.unidadesPorCodigos(anyList())).thenReturn(List.of(u1, u2));

        UnidadeResponsavelDto resp1 = new UnidadeResponsavelDto(10L, "titular1", "nome1", "sub1", "nomesub1");
        UnidadeResponsavelDto resp2 = new UnidadeResponsavelDto(20L, "titular2", "nome2", null, null);
        when(organizacaoFacade.buscarResponsaveisUnidades(anyList())).thenReturn(Map.of(10L, resp1, 20L, resp2));

        Usuario userSub = new Usuario();
        userSub.setEmail("sub@test.com");
        when(usuarioService.buscarUsuariosPorTitulos(anyList())).thenReturn(Map.of("sub1", userSub));

        when(emailModelosService.criarEmailProcessoFinalizadoPorUnidade(any(), any())).thenReturn("html");

        notificacaoService.emailFinalizacaoProcesso(1L);

        verify(emailService, atLeastOnce()).enviarEmailHtml(eq("sub@test.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("enviarEmailParaSubstituto - captura exception e faz warning")
    void enviarEmailParaSubstituto_Exception() {
        Usuario userSub = new Usuario();
        userSub.setEmail("sub@test.com");

        doThrow(new RuntimeException("Test Exception")).when(emailService).enviarEmailHtml(anyString(), anyString(), anyString());

        notificacaoService.enviarEmailParaSubstituto("sub1", Map.of("sub1", userSub), "assunto", "html", "Unidade Teste");

        // Verifica se a excecao eh capturada sem explodir
        verify(emailService).enviarEmailHtml(anyString(), anyString(), anyString());
    }
}
