package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockChefe;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
@DisplayName("UnidadeHierarquia — integração")
class UnidadeHierarquiaIntegrationTest extends BaseIntegrationTest {
    private static final String API_UNIDADES = "/api/unidades";

    @Test
    @DisplayName("deve listar siglas subordinadas por sigla válida")
    @WithMockChefe
    void deveListarSiglasSubordinadasPorSiglaValida() throws Exception {
        List<String> siglas = executarGetListaTexto(API_UNIDADES + "/sigla/{sigla}/subordinadas", "STIC");

        assertThat(siglas)
                .isNotEmpty()
                .contains("STIC", "COSIS", "SEDESENV", "SESEL");
    }

    @Test
    @DisplayName("deve buscar sigla superior por sigla válida")
    @WithMockChefe
    void deveBuscarSiglaSuperiorPorSiglaValida() throws Exception {
        MvcResult resultado = mockMvc.perform(get(API_UNIDADES + "/sigla/{sigla}/superior", "SEDESENV"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString()).isEqualTo("COSIS");
    }

    @Test
    @DisplayName("deve buscar árvore de unidade por código válido")
    @WithMockChefe
    void deveBuscarArvorePorCodigoValido() throws Exception {
        JsonNode unidade = executarGetJson(API_UNIDADES + "/{codigo}/arvore", 2L);

        assertThat(unidade.get("sigla").stringValue()).isEqualTo("STIC");
        assertThat(localizarUnidadePorSigla(unidade, "SEDESENV")).isNotNull();
        assertThat(localizarUnidadePorSigla(unidade, "SESEL")).isNotNull();
    }

    @Test
    @DisplayName("deve listar todas as unidades")
    @WithMockChefe
    void deveListarTodasAsUnidades() throws Exception {
        JsonNode arvore = executarGetJson(API_UNIDADES);

        assertThat(arvore.isArray()).isTrue();
        assertThat(localizarUnidadePorSigla(arvore, "ADMIN")).isNotNull();
        assertThat(localizarUnidadePorSigla(arvore, "STIC")).isNotNull();
        assertThat(localizarUnidadePorSigla(arvore, "SUB-UNIT")).isNotNull();
    }

    @Test
    @DisplayName("deve buscar unidade por código")
    @WithMockChefe
    void deveBuscarUnidadePorCodigo() throws Exception {
        JsonNode unidade = executarGetJson(API_UNIDADES + "/{codigo}", 8L);

        assertThat(unidade.get("codigo").asLong()).isEqualTo(8L);
        assertThat(unidade.get("sigla").stringValue()).isEqualTo("SEDESENV");
    }

    @Test
    @DisplayName("deve buscar unidade por sigla")
    @WithMockChefe
    void deveBuscarUnidadePorSigla() throws Exception {
        JsonNode unidade = executarGetJson(API_UNIDADES + "/sigla/{sigla}", "SEMARE");

        assertThat(unidade.get("codigo").asLong()).isEqualTo(5L);
        assertThat(unidade.get("sigla").stringValue()).isEqualTo("SEMARE");
    }

    @Test
    @DisplayName("deve listar unidades com mapa vigente")
    @WithMockChefe
    void deveListarUnidadesComMapaVigente() throws Exception {
        List<Long> codigos = executarGetListaLong(API_UNIDADES + "/com-mapa-vigente");

        assertThat(codigos)
                .contains(8L, 9L, 10L, 102L)
                .doesNotContain(5L, 905L);
    }

    @Test
    @DisplayName("deve listar unidades sem mapa vigente")
    @WithMockAdmin
    void deveListarUnidadesSemMapaVigente() throws Exception {
        List<Long> codigos = executarGetListaLong(API_UNIDADES + "/sem-mapa-vigente");

        assertThat(codigos)
                .contains(5L, 11L, 905L)
                .doesNotContain(8L, 9L, 10L, 102L);
    }

    @Test
    @DisplayName("deve retornar árvore com elegibilidade para mapeamento")
    @WithMockChefe
    void deveRetornarArvoreComElegibilidadeParaMapeamento() throws Exception {
        JsonNode arvore = executarGetJson(API_UNIDADES + "/arvore-com-elegibilidade?tipoProcesso=MAPEAMENTO");

        assertThat(contarUnidadesElegiveis(arvore)).isGreaterThan(0);
        assertThat(localizarUnidadePorSigla(arvore, "UNIDADE-SEM-RESP").get("elegivel").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("deve retornar árvore com elegibilidade para revisão")
    @WithMockChefe
    void deveRetornarArvoreComElegibilidadeParaRevisao() throws Exception {
        JsonNode arvore = executarGetJson(API_UNIDADES + "/arvore-com-elegibilidade?tipoProcesso=REVISAO");

        assertThat(contarUnidadesElegiveis(arvore)).isGreaterThan(0);
        assertThat(localizarUnidadePorSigla(arvore, "SEMARE").get("elegivel").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("deve retornar árvore com elegibilidade para diagnóstico")
    @WithMockChefe
    void deveRetornarArvoreComElegibilidadeParaDiagnostico() throws Exception {
        JsonNode arvore = executarGetJson(API_UNIDADES + "/arvore-com-elegibilidade?tipoProcesso=DIAGNOSTICO");

        assertThat(contarUnidadesElegiveis(arvore)).isGreaterThan(0);
        assertThat(localizarUnidadePorSigla(arvore, "SEMARE").get("elegivel").asBoolean()).isFalse();
    }

    private JsonNode executarGetJson(String url, Object... parametros) throws Exception {
        MvcResult resultado = mockMvc.perform(get(url, parametros))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }

    private List<String> executarGetListaTexto(String url, Object... parametros) throws Exception {
        JsonNode json = executarGetJson(url, parametros);
        List<String> valores = new ArrayList<>();
        json.forEach(item -> valores.add(item.stringValue()));
        return valores;
    }

    private List<Long> executarGetListaLong(String url, Object... parametros) throws Exception {
        JsonNode json = executarGetJson(url, parametros);
        List<Long> valores = new ArrayList<>();
        json.forEach(item -> valores.add(item.asLong()));
        return valores;
    }

    private int contarUnidadesElegiveis(JsonNode json) {
        if (json == null || json.isNull()) {
            return 0;
        }
        if (json.isArray()) {
            int total = 0;
            for (JsonNode item : json) {
                total += contarUnidadesElegiveis(item);
            }
            return total;
        }

        int total = json.path("elegivel").asBoolean() ? 1 : 0;
        return total + contarUnidadesElegiveis(json.path("subunidades"));
    }

    private JsonNode localizarUnidadePorSigla(JsonNode json, String sigla) {
        if (json == null || json.isNull()) {
            return null;
        }
        if (json.isArray()) {
            for (JsonNode item : json) {
                JsonNode encontrada = localizarUnidadePorSigla(item, sigla);
                if (encontrada != null) {
                    return encontrada;
                }
            }
            return null;
        }
        if (sigla.equals(json.path("sigla").stringValue())) {
            return json;
        }
        return localizarUnidadePorSigla(json.path("subunidades"), sigla);
    }
}
