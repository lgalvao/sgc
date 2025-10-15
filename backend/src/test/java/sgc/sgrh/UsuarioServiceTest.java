package sgc.sgrh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para UsuarioService")
class UsuarioServiceTest {

    @Mock
    private SgrhService sgrhService;

    @Mock
    private UnidadeRepo unidadeRepo;

    @InjectMocks
    private UsuarioService usuarioService;

    private Unidade unidadeMock;

    @BeforeEach
    void setUp() {
        unidadeMock = new Unidade("Secretaria de Documentação", "SEDOC");
        unidadeMock.setCodigo(1L);
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
        List<PerfilDto> perfisDto = List.of(
            new PerfilDto(String.valueOf(tituloEleitoral), 1L, "SEDOC", "ADMIN"),
            new PerfilDto(String.valueOf(tituloEleitoral), 1L, "SEDOC", "CHEFE")
        );

        when(sgrhService.buscarPerfisUsuario(anyString())).thenReturn(perfisDto);
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidadeMock));

        List<PerfilUnidade> resultado = usuarioService.autorizar(tituloEleitoral);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(resultado.get(0).getUnidade().getSigla()).isEqualTo("SEDOC");
        assertThat(resultado.get(1).getPerfil()).isEqualTo(Perfil.CHEFE);
        assertThat(resultado.get(1).getUnidade().getSigla()).isEqualTo("SEDOC");

        verify(sgrhService, times(1)).buscarPerfisUsuario(String.valueOf(tituloEleitoral));
        verify(unidadeRepo, times(2)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando unidade não for encontrada")
    void autorizar_deveLancarExcecao_quandoUnidadeNaoEncontrada() {
        long tituloEleitoral = 123456789L;
        List<PerfilDto> perfisDto = List.of(new PerfilDto(String.valueOf(tituloEleitoral), 99L, "INEXISTENTE", "ADMIN"));

        when(sgrhService.buscarPerfisUsuario(anyString())).thenReturn(perfisDto);
        when(unidadeRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.autorizar(tituloEleitoral))
            .isInstanceOf(ErroDominioNaoEncontrado.class)
            .hasMessage("Unidade não encontrada com código: 99");
    }

    @Test
    @DisplayName("Deve simular o login com sucesso")
    void login_deveExecutarSemErro() {
        long tituloEleitoral = 123456789L;
        PerfilUnidade perfilUnidade = new PerfilUnidade(Perfil.ADMIN, unidadeMock);

        // Apenas verifica se o método executa sem lançar exceções,
        // já que a implementação atual apenas loga a informação.
        usuarioService.login(tituloEleitoral, perfilUnidade);
    }
}