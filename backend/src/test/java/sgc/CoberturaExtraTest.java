package sgc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import sgc.comum.erros.ErroConfiguracao;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.comum.erros.ErroNegocio;
import sgc.comum.erros.ErroNegocioBase;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;

@DisplayName("Cobertura Extra de Erros e Modelos")
@Tag("unit")
class CoberturaExtraTest {
    @Test
    @DisplayName("Deve instanciar classes de erro para cobertura")
    void deveInstanciarErros() {
        assertThat(new ErroEstadoImpossivel("msg")).isNotNull();
        assertThat(new ErroConfiguracao("msg")).isNotNull();
        assertThat(new ErroMapaNaoAssociado("msg")).isNotNull();
        assertThat(new ErroParametroPainelInvalido("msg")).isNotNull();

        ErroNegocioBase erro = new ErroNegocioBase("msg", "CODO", HttpStatus.BAD_REQUEST) {
        };
        assertThat(erro.getMessage()).isEqualTo("msg");
        assertThat(erro.getCode()).isEqualTo("CODO");
        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Cobertura para o método default da interface ErroNegocio
        ErroNegocio erroInterface = new ErroNegocio() {
            @Override
            public String getCode() {
                return "X";
            }

            @Override
            public HttpStatus getStatus() {
                return HttpStatus.OK;
            }

            @Override
            public String getMessage() {
                return "M";
            }
        };
        assertThat(erroInterface.getDetails()).isNull();

        assertThat(new ErroNegocioBase("msg", "CODO", HttpStatus.BAD_REQUEST, new HashMap<>()) {
        }).isNotNull();
        assertThat(new ErroNegocioBase("msg", "CODO", HttpStatus.BAD_REQUEST, new RuntimeException()) {
        }).isNotNull();
    }

    @Test
    @DisplayName("Deve instanciar modelos para cobertura de construtores extras")
    void deveInstanciarModelos() {
        Competencia c = Competencia.builder().descricao("desc").mapa(new Mapa()).build();
        c.setCodigo(1L);
        assertThat(c.getCodigo()).isEqualTo(1L);

        Conhecimento k = Conhecimento.builder().descricao("desc").atividade(new Atividade()).build();
        k.setCodigo(1L);
        assertThat(k.getCodigo()).isEqualTo(1L);

        Unidade u = Unidade.builder().nome("Nome").sigla("SIGLA").build();
        assertThat(u.getSigla()).isEqualTo("SIGLA");
    }
}
