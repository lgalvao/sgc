package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import sgc.painel.ErroParametroPainelInvalido;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Exceções Customizadas")
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
    @DisplayName("Deve instanciar ErroMapaNaoAssociado")
    void testErroMapaNaoAssociado() {
        String message = "Mapa não associado";
        ErroMapaNaoAssociado exception = new ErroMapaNaoAssociado(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve instanciar ErroParametroPainelInvalido")
    void testErroParametroPainelInvalido() {
        String message = "Parâmetro inválido";
        ErroParametroPainelInvalido exception = new ErroParametroPainelInvalido(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve testar construtores de ErroNegocioBase")
    void testErroNegocioBase() {
        ErroNegocioBase erro1 = new ErroNegocioBase("msg", "CODE", HttpStatus.BAD_REQUEST) {};
        assertThat(erro1.getMessage()).isEqualTo("msg");
        assertThat(erro1.getCode()).isEqualTo("CODE");
        assertThat(erro1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(erro1.getDetails()).isEmpty();

        ErroNegocioBase erro2 = new ErroNegocioBase("msg", "CODE", HttpStatus.BAD_REQUEST, new HashMap<>()) {};
        assertThat(erro2.getDetails()).isEmpty();

        RuntimeException cause = new RuntimeException("causa");
        ErroNegocioBase erro3 = new ErroNegocioBase("msg", "CODE", HttpStatus.BAD_REQUEST, cause) {};
        assertThat(erro3.getCause()).isEqualTo(cause);
        assertThat(erro3.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("ErroNegocio deve retornar empty map para getDetails() por padrão")
    void testErroNegocioGetDetailsDefault() {
        ErroNegocio erroInterface = new ErroNegocio() {
            @Override public String getCode() { return "X"; }
            @Override public HttpStatus getStatus() { return HttpStatus.OK; }
            @Override public String getMessage() { return "M"; }
        };
        assertThat(erroInterface.getDetails()).isEmpty();
    }
}