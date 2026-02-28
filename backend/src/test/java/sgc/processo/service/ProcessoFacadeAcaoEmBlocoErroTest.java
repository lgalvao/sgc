package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.processo.ProcessoFacade;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.model.AcaoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_CADASTRO"))).thenReturn(false);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_MAPA"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_MAPA"))).thenReturn(true);

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
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_MAPA"))).thenReturn(false);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_MAPA"))).thenReturn(true);

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
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_MAPA"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_CADASTRO"))).thenReturn(false);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_MAPA"))).thenReturn(true);

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
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("ACEITAR_MAPA"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_CADASTRO"))).thenReturn(true);
        lenient().when(permissionEvaluator.checkPermission(eq(usuario), any(List.class), eq("HOMOLOGAR_MAPA"))).thenReturn(false);

        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(20L), AcaoProcesso.HOMOLOGAR, null);
        assertThrows(ErroAcessoNegado.class, () -> processoFacade.executarAcaoEmBloco(10L, req));
    }
}
