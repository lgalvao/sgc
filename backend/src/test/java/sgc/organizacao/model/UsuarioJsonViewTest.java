package sgc.organizacao.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UsuarioJsonViewTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveSerializarApenasCamposPublicosDoUsuario() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("UNIT");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setNome("Usuario Teste");
        usuario.setEmail("teste@teste.com");
        usuario.setMatricula("88888888");
        usuario.setUnidadeLotacao(unidade);
        usuario.setRamal("1234");

        String json = objectMapper
                .writerWithView(OrganizacaoViews.Publica.class)
                .writeValueAsString(usuario);

        assertThat(json).contains("\"tituloEleitoral\":\"123456789012\"");
        assertThat(json).contains("\"nome\":\"Usuario Teste\"");
        assertThat(json).contains("\"email\":\"teste@teste.com\"");
        assertThat(json).contains("\"matricula\":\"88888888\"");
        assertThat(json).contains("\"unidadeCodigo\":10");

        // Campos sensíveis ou internos não devem aparecer
        assertThat(json).doesNotContain("\"unidadeLotacao\"");
        assertThat(json).doesNotContain("\"ramal\"");
        assertThat(json).doesNotContain("\"authorities\"");
    }
}
