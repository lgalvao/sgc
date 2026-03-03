package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacade - Erros AcaoEmBloco")
class ProcessoFacadeAcaoEmBlocoErroTest {

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    void executarAcaoEmBloco_erroAcesso_disponibilizar() {
        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(10L, List.of(20L))).thenReturn(List.of(sp));
        when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("DISPONIBILIZAR_MAPA"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.DISPONIBILIZAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }

    @Test
    void executarAcaoEmBloco_erroAcesso_aceitarCadastro() {
        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(10L, List.of(20L))).thenReturn(List.of(sp));
        when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_CADASTRO"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.ACEITAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }

    @Test
    void executarAcaoEmBloco_erroAcesso_aceitarMapa() {
        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(10L, List.of(20L))).thenReturn(List.of(sp));
        when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_MAPA"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.ACEITAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }

    @Test
    void executarAcaoEmBloco_erroAcesso_homologarCadastro() {
        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(10L, List.of(20L))).thenReturn(List.of(sp));
        when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_CADASTRO"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.HOMOLOGAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }

    @Test
    void executarAcaoEmBloco_erroAcesso_homologarMapa() {
        Usuario usuario = new Usuario();
        when(usuarioService.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(10L, List.of(20L))).thenReturn(List.of(sp));
        when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_MAPA"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.HOMOLOGAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }
}
