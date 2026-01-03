package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoPermissoesService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoDetalheService")
class SubprocessoDetalheServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoPermissoesService subprocessoPermissoesService;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;

    @InjectMocks
    private SubprocessoDetalheService service;

    @Test
    @DisplayName("Deve listar atividades")
    void deveListarAtividades() {
        Subprocesso sp = new Subprocesso();
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        Atividade ativ = new Atividade();
        ativ.setDescricao("Test");
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativ));

        List<AtividadeVisualizacaoDto> result = service.listarAtividadesSubprocesso(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("obterDetalhes sucesso ADMIN")
    void obterDetalhesSucessoAdmin() {
        Usuario admin = new Usuario();
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        admin.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setSigla("U1");

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.ADMIN, null, admin);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha acesso negado")
    void obterDetalhesFalhaAcesso() {
        Usuario user = new Usuario();
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.CHEFE);
        up.setUnidade(u1);
        user.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade u2 = new Unidade();
        u2.setCodigo(20L);
        sp.setUnidade(u2);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.CHEFE, 10L, user))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso GESTOR unidade subordinada")
    void obterDetalhesSucessoGestorSubordinada() {
        Usuario gestor = new Usuario();
        Unidade uSuperior = new Unidade();
        uSuperior.setCodigo(100L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(uSuperior);
        gestor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade uSubordinada = new Unidade();
        uSubordinada.setCodigo(101L);
        uSubordinada.setUnidadeSuperior(uSuperior);
        sp.setUnidade(uSubordinada);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.GESTOR, null, gestor);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDetalhes falha GESTOR unidade nao subordinada")
    void obterDetalhesFalhaGestorNaoSubordinada() {
        Usuario gestor = new Usuario();
        Unidade uGestor = new Unidade();
        uGestor.setCodigo(100L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(uGestor);
        gestor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade uOutra = new Unidade();
        uOutra.setCodigo(200L);
        sp.setUnidade(uOutra);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.obterDetalhes(1L, Perfil.GESTOR, null, gestor))
                .isInstanceOf(ErroAccessoNegado.class);
    }

    @Test
    @DisplayName("obterDetalhes sucesso SERVIDOR mesma unidade")
    void obterDetalhesSucessoServidorMesmaUnidade() {
        Usuario servidor = new Usuario();
        Unidade u1 = new Unidade();
        u1.setCodigo(10L);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.SERVIDOR);
        up.setUnidade(u1);
        servidor.setAtribuicoes(Set.of(up));

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setUnidade(u1);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoDetalheMapper.toDto(any(), any(), any(), any(), any()))
                .thenReturn(SubprocessoDetalheDto.builder().build());

        SubprocessoDetalheDto result = service.obterDetalhes(1L, Perfil.SERVIDOR, null, servidor);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterPermissoes")
    void obterPermissoes() {
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(subprocessoPermissoesService.calcularPermissoes(sp, user))
                .thenReturn(SubprocessoPermissoesDto.builder().build());

        SubprocessoPermissoesDto result = service.obterPermissoes(1L, user);
        assertThat(result).isNotNull();
    }
}
