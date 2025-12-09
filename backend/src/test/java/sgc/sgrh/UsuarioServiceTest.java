package sgc.sgrh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.service.UsuarioService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para UsuarioService")
class UsuarioServiceTest {

    @Mock private UsuarioRepo usuarioRepo;

    @InjectMocks private UsuarioService usuarioService;

    private Unidade unidadeMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        unidadeMock = new Unidade("Secretaria de Documentação", "SEDOC");
        unidadeMock.setCodigo(1L);
        unidadeMock.setTipo(TipoUnidade.INTEROPERACIONAL);
        usuarioMock =
                new Usuario(
                        "123456789", "Usuário de Teste", "teste@email.com", "1234", unidadeMock);

        usuarioMock
                .getAtribuicoes()
                .add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(usuarioMock)
                                .unidade(unidadeMock)
                                .perfil(Perfil.ADMIN)
                                .build());
        usuarioMock
                .getAtribuicoes()
                .add(
                        sgc.sgrh.model.UsuarioPerfil.builder()
                                .usuario(usuarioMock)
                                .unidade(unidadeMock)
                                .perfil(Perfil.CHEFE)
                                .build());
    }

    @Test
    @DisplayName("Deve autenticar")
    void autenticar_deveRetornarTrue() {
        boolean resultado = usuarioService.autenticar("123456789", "senha");
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar lista de perfis e unidades autorizados")
    void autorizar_deveRetornarListaDePerfisUnidades() {
        String tituloEleitoral = "123456789";

        when(usuarioRepo.findById(tituloEleitoral)).thenReturn(Optional.of(usuarioMock));

        List<PerfilUnidade> resultado = usuarioService.autorizar(tituloEleitoral);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(PerfilUnidade::getPerfil)
                .containsExactlyInAnyOrder(Perfil.ADMIN, Perfil.CHEFE);
        assertThat(resultado)
                .extracting(pu -> pu.getUnidade().getSigla())
                .allMatch(sigla -> sigla.equals("SEDOC"));

        verify(usuarioRepo, times(1)).findById(tituloEleitoral);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void autorizar_deveLancarExcecao_quandoUsuarioNaoEncontrado() {
        String tituloEleitoral = "999";

        when(usuarioRepo.findById(tituloEleitoral)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autorizar(tituloEleitoral))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Usuário' com codigo '999' não encontrado");
    }

    @Test
    @DisplayName("Deve simular a entrada")
    void entrar_deveExecutarSemErro() {
        String tituloEleitoral = "123456789";
        UnidadeDto unidadeDtoMock =
                new UnidadeDto(
                        unidadeMock.getCodigo(),
                        unidadeMock.getNome(),
                        unidadeMock.getSigla(),
                        null,
                        unidadeMock.getTipo().name(),
                        false);
        PerfilUnidade perfilUnidade = new PerfilUnidade(Perfil.ADMIN, unidadeDtoMock);

        assertThatCode(() -> usuarioService.entrar(tituloEleitoral, perfilUnidade))
                .doesNotThrowAnyException();
    }
}
