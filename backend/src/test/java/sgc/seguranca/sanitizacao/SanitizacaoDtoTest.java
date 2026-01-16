package sgc.seguranca.sanitizacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.api.Test;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.organizacao.dto.CriarAtribuicaoTemporariaRequest;
import sgc.subprocesso.dto.DevolverCadastroRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanitizacaoDtoTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveSanitizarCriarAnaliseRequest() throws Exception {
        String json = """
            {
                "observacoes": "<script>alert('xss')</script>Observação",
                "motivo": "<b>Motivo</b>",
                "siglaUnidade": "SIGLA",
                "tituloUsuario": "123456789012"
            }
            """;

        CriarAnaliseRequest request = mapper.readValue(json, CriarAnaliseRequest.class);

        assertEquals("Observação", request.observacoes());
        assertEquals("Motivo", request.motivo());
    }

    @Test
    void deveSanitizarCriarAtribuicaoTemporariaRequest() throws Exception {
        String json = """
            {
                "tituloEleitoralUsuario": "123",
                "dataInicio": "2024-01-01",
                "dataTermino": "2024-12-31",
                "justificativa": "<img src=x onerror=alert(1)>Justificativa"
            }
            """;

        CriarAtribuicaoTemporariaRequest request = mapper.readValue(json, CriarAtribuicaoTemporariaRequest.class);

        assertEquals("Justificativa", request.justificativa());
    }

    static class DummyDto {
        @SanitizarHtml
        public String value;

        @JsonDeserialize(using = DeserializadorHtmlSanitizado.class)
        public String direct;
    }

    @Test
    void deveSanitizarDummyDto() throws Exception {
        String json = """
            {
                "value": "<script>alert(1)</script>Value",
                "direct": "<script>alert(1)</script>Direct"
            }
            """;
        DummyDto dto = mapper.readValue(json, DummyDto.class);
        assertEquals("Value", dto.value);
        assertEquals("Direct", dto.direct);
    }

    @Test
    void testUtilSanitizacao() {
        String input = "<script>alert(1)</script>Value";
        String output = UtilSanitizacao.sanitizar(input);
        assertEquals("Value", output);
    }
}
