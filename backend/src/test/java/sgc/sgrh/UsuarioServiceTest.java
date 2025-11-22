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
import sgc.sgrh.service.SgrhService;
import sgc.sgrh.service.UsuarioService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para UsuarioService")
class UsuarioServiceTest {

    @Mock
    private SgrhService sgrhService;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UsuarioRepo usuarioRepo;

    @InjectMocks
    private UsuarioService usuarioService;

    private Unidade unidadeMock;
    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        unidadeMock = new Unidade("Secretaria de Documentação", "SEDOC");
        unidadeMock.setCodigo(1L);
        unidadeMock.setTipo(TipoUnidade.INTERMEDIARIA);
        usuarioMock = new Usuario(
            "123456789",
            "Usuário de Teste",
            "teste@email.com",
            "1234",
            unidadeMock,
            Set.of(Perfil.ADMIN, Perfil.CHEFE));
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
        assertThat(resultado).extracting(PerfilUnidade::getPerfil).containsExactlyInAnyOrder(Perfil.ADMIN, Perfil.CHEFE);
        assertThat(resultado).extracting(pu -> pu.getUnidade().getSigla()).allMatch(sigla -> sigla.equals("SEDOC"));

        verify(usuarioRepo, times(1)).findById(tituloEleitoral);
        verifyNoInteractions(sgrhService, unidadeRepo);
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
        UnidadeDto unidadeDtoMock = new UnidadeDto(unidadeMock.getCodigo(), unidadeMock.getNome(), unidadeMock.getSigla(), null, unidadeMock.getTipo().name(), false);
        PerfilUnidade perfilUnidade = new PerfilUnidade(Perfil.ADMIN, unidadeDtoMock);

        // Apenas verifica se o metodo executa sem lançar exceções, já que a implementação atual apenas loga a informação.
        usuarioService.entrar(tituloEleitoral, perfilUnidade);
    }
}