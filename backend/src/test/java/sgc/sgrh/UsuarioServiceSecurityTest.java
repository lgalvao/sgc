package sgc.sgrh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.sgrh.dto.EntrarReq;
import sgc.sgrh.model.*;
import sgc.sgrh.service.UsuarioService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceSecurityTest {

    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;

    @InjectMocks private UsuarioService usuarioService;

    @Test
    @DisplayName("SEGURANÇA: Deve NEGAR entrar com perfil que não possui")
    void deveNegarEntrarComPerfilQueNaoPossui() {
        // Setup
        String titulo = "123";
        Long unidadeId = 1L;
        String perfilSolicitado = "ADMIN";

        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral(titulo)
                .unidadeCodigo(unidadeId)
                .perfil(perfilSolicitado)
                .build();

        // Mocks
        when(unidadeRepo.existsById(unidadeId)).thenReturn(true);

        // Mock autorizacao (Usuário existe, mas não tem perfis)
        Usuario usuario = new Usuario(titulo, "Nome", "email", "ramal", null);
        when(usuarioRepo.findById(titulo)).thenReturn(Optional.of(usuario));
        when(usuarioPerfilRepo.findByUsuarioTitulo(titulo)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.entrar(req))
                .isInstanceOf(ErroAccessoNegado.class)
                .hasMessageContaining("Usuário não possui permissão");
    }

    @Test
    @DisplayName("SEGURANÇA: Deve PERMITIR entrar com perfil que possui")
    void devePermitirEntrarComPerfilQuePossui() {
        // Setup
        String titulo = "123";
        Long unidadeId = 1L;
        String perfilSolicitado = "ADMIN";

        EntrarReq req = EntrarReq.builder()
                .tituloEleitoral(titulo)
                .unidadeCodigo(unidadeId)
                .perfil(perfilSolicitado)
                .build();

        // Mocks
        when(unidadeRepo.existsById(unidadeId)).thenReturn(true);

        Unidade unidade = new Unidade("Unidade", "SIGLA");
        unidade.setCodigo(unidadeId);
        unidade.setTipo(TipoUnidade.INTEROPERACIONAL);

        Usuario usuario = new Usuario(titulo, "Nome", "email", "ramal", unidade);
        UsuarioPerfil perfil = UsuarioPerfil.builder()
                .usuario(usuario)
                .unidade(unidade)
                .perfil(Perfil.ADMIN)
                .build();
        usuario.setAtribuicoes(Set.of(perfil));

        when(usuarioRepo.findById(titulo)).thenReturn(Optional.of(usuario));
        when(usuarioPerfilRepo.findByUsuarioTitulo(titulo)).thenReturn(List.of(perfil));

        // Act & Assert
        assertThatCode(() -> usuarioService.entrar(req))
                .doesNotThrowAnyException();
    }
}
