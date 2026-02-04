package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.AdministradorService;
import sgc.organizacao.service.UnidadeResponsavelService;
import sgc.organizacao.service.UsuarioConsultaService;
import sgc.organizacao.service.UsuarioPerfilService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioFacade - Testes de Cobertura")
class UsuarioFacadeCoverageTest {

    @Mock
    private UsuarioConsultaService usuarioConsultaService;

    @Mock
    private UsuarioPerfilService usuarioPerfilService;

    @Mock
    private AdministradorService administradorService;

    @Mock
    private UnidadeResponsavelService unidadeResponsavelService;

    @InjectMocks
    private UsuarioFacade facade;

    @Test
    @DisplayName("buscarPerfisUsuario: deve filtrar unidades inativas")
    void buscarPerfisUsuario_DeveFiltrarUnidadesInativas() {
        // Arrange
        String titulo = "123456";
        Usuario usuario = criarUsuario(titulo);
        Unidade unidadeInativa = criarUnidade(1L, "UNID1");
        unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);

        UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidadeInativa, Perfil.CHEFE);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                .thenReturn(Optional.of(usuario));
        when(usuarioPerfilService.buscarPorUsuario(titulo))
                .thenReturn(List.of(atribuicao));

        // Act
        List<PerfilDto> resultado = facade.buscarPerfisUsuario(titulo);

        // Assert
        assertThat(resultado).isEmpty();
    }

    // Helpers
    private Usuario criarUsuario(String titulo) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario@test.com");
        usuario.setMatricula("12345");
        usuario.setUnidadeLotacao(criarUnidade(1L, "UNID1"));
        return usuario;
    }

    private Unidade criarUnidade(Long codigo, String sigla) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome("Unidade Teste");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }

    private UsuarioPerfil criarAtribuicao(Usuario usuario, Unidade unidade, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setUnidade(unidade);
        atribuicao.setUnidadeCodigo(unidade.getCodigo());
        atribuicao.setPerfil(perfil);
        return atribuicao;
    }
}
