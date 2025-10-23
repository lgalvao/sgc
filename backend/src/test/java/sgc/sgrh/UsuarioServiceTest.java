package sgc.sgrh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.dto.UnidadeDto;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

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
            123456789L,
            "Usuário de Teste",
            "teste@email.com",
            "1234",
            unidadeMock,
            Set.of(Perfil.ADMIN, Perfil.CHEFE));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso")
    void autenticar_deveRetornarTrue() {
        boolean resultado = usuarioService.autenticar(123456789L, "senha");
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar lista de perfis e unidades autorizados")
    void autorizar_deveRetornarListaDePerfisUnidades() {
        long tituloEleitoral = 123456789L;

        when(usuarioRepo.findByTituloEleitoral(tituloEleitoral)).thenReturn(Optional.of(usuarioMock));

        List<PerfilUnidade> resultado = usuarioService.autorizar(tituloEleitoral);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(PerfilUnidade::getPerfil).containsExactlyInAnyOrder(Perfil.ADMIN, Perfil.CHEFE);
        assertThat(resultado).extracting(pu -> pu.getUnidade().sigla()).allMatch(sigla -> sigla.equals("SEDOC"));

        verify(usuarioRepo, times(1)).findByTituloEleitoral(tituloEleitoral);
        verifyNoInteractions(sgrhService, unidadeRepo);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado")
    void autorizar_deveLancarExcecao_quandoUsuarioNaoEncontrado() {
        long tituloEleitoral = 999L;

        when(usuarioRepo.findByTituloEleitoral(tituloEleitoral)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autorizar(tituloEleitoral))
            .isInstanceOf(ErroDominioNaoEncontrado.class)
            .hasMessageContaining("Usuário' com id '999' não encontrado");
    }

    @Test
    @DisplayName("Deve simular a entrada com sucesso")
    void entrar_deveExecutarSemErro() {
        long tituloEleitoral = 123456789L;
        UnidadeDto unidadeDtoMock = new UnidadeDto(unidadeMock.getCodigo(), unidadeMock.getNome(), unidadeMock.getSigla(), null, unidadeMock.getTipo().name());
        PerfilUnidade perfilUnidade = new PerfilUnidade(Perfil.ADMIN, unidadeDtoMock);

        // Apenas verifica se o metodo executa sem lançar exceções, já que a implementação atual apenas loga a informação.
        usuarioService.entrar(tituloEleitoral, perfilUnidade);
    }
}