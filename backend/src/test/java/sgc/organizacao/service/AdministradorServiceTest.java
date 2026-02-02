package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.model.AdministradorRepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do AdministradorService")
class AdministradorServiceTest {

    @Mock
    private AdministradorRepo administradorRepo;

    @InjectMocks
    private AdministradorService administradorService;

    @Test
    @DisplayName("Deve lançar erro ao tentar remover o único administrador do sistema")
    void deveThrowErroQuandoApenasUmAdministrador() {
        // Setup: usuário existe e é o único admin
        when(administradorRepo.existsById("admin1")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> administradorService.remover("admin1"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("único administrador");

        // Não deve chamar deleteById
        verify(administradorRepo, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Deve remover administrador quando há mais de um no sistema")
    void deveRemoverAdministradorQuandoHaMaisDeUm() {
        // Setup: usuário existe e há mais de um admin
        when(administradorRepo.existsById("admin1")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(2L);

        // Act
        administradorService.remover("admin1");

        // Assert
        verify(administradorRepo).deleteById("admin1");
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar remover usuário que não é administrador")
    void deveThrowErroQuandoUsuarioNaoEhAdministrador() {
        // Setup: usuário não existe
        when(administradorRepo.existsById("naoAdmin")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> administradorService.remover("naoAdmin"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("não é um administrador");

        // Não deve chamar count nem deleteById
        verify(administradorRepo, never()).count();
        verify(administradorRepo, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("Deve adicionar administrador com sucesso")
    void deveAdicionarAdministradorComSucesso() {
        // Setup: usuário não é administrador ainda
        when(administradorRepo.existsById("user1")).thenReturn(false);

        // Act
        administradorService.adicionar("user1");

        // Assert
        verify(administradorRepo).save(argThat(admin -> 
            admin.getUsuarioTitulo().equals("user1")
        ));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar adicionar usuário que já é administrador")
    void deveThrowErroQuandoUsuarioJaEhAdministrador() {
        // Setup: usuário já é administrador
        when(administradorRepo.existsById("admin1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> administradorService.adicionar("admin1"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("já é administrador");

        // Não deve chamar save
        verify(administradorRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar true quando usuário é administrador")
    void deveRetornarTrueQuandoEhAdministrador() {
        // Setup
        when(administradorRepo.existsById("admin1")).thenReturn(true);

        // Act
        boolean resultado = administradorService.isAdministrador("admin1");

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando usuário não é administrador")
    void deveRetornarFalseQuandoNaoEhAdministrador() {
        // Setup
        when(administradorRepo.existsById("user1")).thenReturn(false);

        // Act
        boolean resultado = administradorService.isAdministrador("user1");

        // Assert
        assertThat(resultado).isFalse();
    }
}
