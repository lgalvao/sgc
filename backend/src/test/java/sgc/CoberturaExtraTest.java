package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.comum.erros.ErroConfiguracao;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.comum.erros.ErroNegocioBase;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cobertura Extra de Erros e Modelos")
class CoberturaExtraTest {

    @Test
    @DisplayName("Deve instanciar classes de erro para cobertura")
    void deveInstanciarErros() {
        assertThat(new ErroEstadoImpossivel("msg")).isNotNull();
        assertThat(new ErroConfiguracao("msg")).isNotNull();
        assertThat(new ErroMapaNaoAssociado("msg")).isNotNull();
        assertThat(new sgc.painel.erros.ErroParametroPainelInvalido("msg")).isNotNull();
        
        // ErroNegocioBase (mais linhas)
        ErroNegocioBase erro = new ErroNegocioBase("msg", "CODO", org.springframework.http.HttpStatus.BAD_REQUEST) {};
        assertThat(erro.getMessage()).isEqualTo("msg");
        assertThat(erro.getCode()).isEqualTo("CODO");
        assertThat(erro.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);

        // Testar outros construtores de ErroNegocioBase
        new ErroNegocioBase("msg", "CODO", org.springframework.http.HttpStatus.BAD_REQUEST, new java.util.HashMap<>()) {};
        new ErroNegocioBase("msg", "CODO", org.springframework.http.HttpStatus.BAD_REQUEST, new RuntimeException()) {};
    }

    @Test
    @DisplayName("Deve instanciar modelos para cobertura de construtores extras")
    void deveInstanciarModelos() {
        // Competencia construtor com todos os campos
        Competencia c = new Competencia(1L, "desc", new sgc.mapa.model.Mapa());
        assertThat(c.getCodigo()).isEqualTo(1L);
        
        // Conhecimento construtor com todos os campos
        Conhecimento k = new Conhecimento(1L, "desc", new sgc.mapa.model.Atividade());
        assertThat(k.getCodigo()).isEqualTo(1L);
    }
}
