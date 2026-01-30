package sgc.subprocesso.service;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import java.util.Collections;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.SubprocessoRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacadeCoverageTest")
class SubprocessoFacadeCoverageTest {
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private SubprocessoAjusteMapaService ajusteMapaService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private SubprocessoContextoService contextoService;
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock
    private UsuarioFacade usuarioService;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    @DisplayName("importarAtividades - Delegação para AtividadeService")
    void importarAtividades_Delegation() {
        Long codDestino = 1L;
        Long codOrigem = 2L;

        facade.importarAtividades(codDestino, codOrigem);

        verify(atividadeService).importarAtividades(codDestino, codOrigem);
    }

    @Test
    @DisplayName("listarEntidadesPorProcesso - Delegation")
    void listarEntidadesPorProcesso_Delegation() {
        facade.listarEntidadesPorProcesso(1L);
        verify(crudService).listarEntidadesPorProcesso(1L);
    }

    @Test
    @DisplayName("listarPorProcessoESituacao - Delegation")
    void listarPorProcessoESituacao_Delegation() {
        facade.listarPorProcessoESituacao(1L, SituacaoSubprocesso.NAO_INICIADO);
        verify(crudService).listarPorProcessoESituacao(1L, SituacaoSubprocesso.NAO_INICIADO);
    }

    @Test
    @DisplayName("listarPorProcessoUnidadeESituacoes - Delegation")
    void listarPorProcessoUnidadeESituacoes_Delegation() {
        facade.listarPorProcessoUnidadeESituacoes(1L, 2L, null);
        verify(crudService).listarPorProcessoUnidadeESituacoes(1L, 2L, null);
    }

    @Test
    @DisplayName("calcularPermissoes - Delegação para PermissaoCalculator")
    void calcularPermissoes_Delegation() {
        Subprocesso sp = new Subprocesso();
        Usuario usuario = new Usuario();

        facade.calcularPermissoes(sp, usuario);

        verify(permissaoCalculator).calcularPermissoes(sp, usuario);
    }

    @Test
    @DisplayName("salvarAjustesMapa - Delegação para AjusteMapaService")
    void salvarAjustesMapa_Delegation() {
        Long codSubprocesso = 1L;
        List<CompetenciaAjusteDto> ajustes = Collections.emptyList();

        facade.salvarAjustesMapa(codSubprocesso, ajustes);

        verify(ajusteMapaService).salvarAjustesMapa(codSubprocesso, ajustes);
    }

    @Test
    @DisplayName("obterPermissoes - Delegação para PermissaoCalculator")
    void obterPermissoes_Delegation() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        facade.obterPermissoes(codigo);

        verify(permissaoCalculator).obterPermissoes(codigo, usuario);
    }

    @Test
    @DisplayName("obterMapaParaAjuste - Delegação para AjusteMapaService")
    void obterMapaParaAjuste_Delegation() {
        Long codigo = 1L;

        facade.obterMapaParaAjuste(codigo);

        verify(ajusteMapaService).obterMapaParaAjuste(codigo);
    }

    @Test
    @DisplayName("listarAtividadesSubprocesso - Delegação para AtividadeService")
    void listarAtividadesSubprocesso_Delegation() {
        Long codigo = 1L;

        facade.listarAtividadesSubprocesso(codigo);

        verify(atividadeService).listarAtividadesSubprocesso(codigo);
    }

    @Test
    @DisplayName("obterDetalhes - Delegação para ContextoService")
    void obterDetalhes_Delegation() {
        Long codigo = 1L;
        Usuario usuario = new Usuario();

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        facade.obterDetalhes(codigo, Perfil.ADMIN);

        verify(contextoService).obterDetalhes(codigo, usuario);
    }

    @Test
    @DisplayName("obterContextoEdicao - Delegação para ContextoService")
    void obterContextoEdicao_Delegation() {
        Long codigo = 1L;

        facade.obterContextoEdicao(codigo, Perfil.ADMIN);

        verify(contextoService).obterContextoEdicao(codigo);
    }

    @Test
    @DisplayName("obterSugestoes - Delegação para ContextoService")
    void obterSugestoes_Delegation() {
        Long codigo = 1L;

        facade.obterSugestoes(codigo);

        verify(contextoService).obterSugestoes(codigo);
    }

    @Test
    @DisplayName("obterCadastro - Delegação para ContextoService")
    void obterCadastro_Delegation() {
        Long codigo = 1L;

        facade.obterCadastro(codigo);

        verify(contextoService).obterCadastro(codigo);
    }


}