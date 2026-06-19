package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de Exceções customizadas")
class CustomExceptionsTest {

    @Test
    @DisplayName("Deve instanciar ErroAcessoNegado")
    void testErroDominioAccessoNegado() {
        String message = "Acesso negado";
        ErroAcessoNegado exception = new ErroAcessoNegado(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve instanciar ErroEntidadeNaoEncontrada")
    void testErroDominioNaoEncontrado() {
        String message = "Dominio não encontrado";
        ErroEntidadeNaoEncontrada exception = new ErroEntidadeNaoEncontrada(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve instanciar ErroConfiguracao")
    void testErroConfiguracao() {
        String message = "Erro de configuração";
        ErroConfiguracao exception = new ErroConfiguracao(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve testar construtores de ErroNegocioBase")
    void testErroNegocioBase() {
        ErroNegocioBase erro1 = new ErroNegocioBase("msg", "CODE", HttpStatus.BAD_REQUEST) {
        };
        assertThat(erro1.getMessage()).isEqualTo("msg");
        assertThat(erro1.getCode()).isEqualTo("CODE");
        assertThat(erro1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro1.getDetails()).isEmpty();

        ErroNegocioBase erro2 = new ErroNegocioBase("msg", "CODE", HttpStatus.BAD_REQUEST, new HashMap<>()) {
        };
        assertThat(erro2.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("Deve instanciar ErroAutenticacao como erro de negócio 401")
    void testErroAutenticacao() {
        ErroAutenticacao erro = new ErroAutenticacao("Sessão expirada");

        assertThat(erro.getMessage()).isEqualTo("Sessão expirada");
        assertThat(erro.getCode()).isEqualTo("NAO_AUTORIZADO");
        assertThat(erro.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
