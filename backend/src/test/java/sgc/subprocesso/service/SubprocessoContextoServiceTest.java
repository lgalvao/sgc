package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.AccessControlService;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.security.SubprocessoSecurity;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoContextoService")
class SubprocessoContextoServiceTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private SubprocessoSecurity subprocessoSecurity;

    @InjectMocks
    private SubprocessoContextoService service;

    @Test
    @DisplayName("obterDetalhes por ID - Sucesso")
    void obterDetalhesPorId() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setTituloTitular("T1");
        Subprocesso sp = Subprocesso.builder().codigo(id).unidade(u).build();

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        when(usuarioService.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioService.buscarPorLogin("T1")).thenReturn(new Usuario());
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());

        SubprocessoDetalheResponse result = service.obterDetalhes(id, user);

        assertThat(result).isNotNull();
        verify(accessControlService).verificarPermissao(eq(user), any(), eq(sp));
    }

    @Test
    @DisplayName("obterDetalhes - Tratar erro ao buscar titular")
    void obterDetalhes_ErroTitular() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setTituloTitular("T1");
        Subprocesso sp = Subprocesso.builder().codigo(id).unidade(u).build();

        when(usuarioService.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioService.buscarPorLogin("T1")).thenThrow(new RuntimeException("Erro"));
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());

        SubprocessoDetalheResponse result = service.obterDetalhes(sp, user);

        assertThat(result).isNotNull();
        assertThat(result.titular()).isNull();
    }

    @Test
    @DisplayName("obterContextoEdicao - Sucesso")
    void obterContextoEdicao() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = new Unidade();
        u.setSigla("U1");
        u.setTituloTitular("T1");
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        Subprocesso sp = Subprocesso.builder().codigo(id).unidade(u).mapa(mapa).build();

        when(usuarioService.obterUsuarioAutenticado()).thenReturn(user);
        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        when(usuarioService.buscarResponsavelAtual("U1")).thenReturn(new Usuario());
        when(usuarioService.buscarPorLogin("T1")).thenReturn(new Usuario());
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(id)).thenReturn(List.of());
        when(atividadeService.listarAtividadesSubprocesso(id)).thenReturn(List.of());
        when(mapaFacade.obterPorCodigo(10L)).thenReturn(new Mapa());

        ContextoEdicaoResponse result = service.obterContextoEdicao(id);

        assertThat(result).isNotNull();
        assertThat(result.unidade()).isSameAs(u);
    }
}
