package sgc.seguranca.dto;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UsuarioAcessoAdTest")
class UsuarioAcessoAdTest {

    @Test
    @DisplayName("Deve cobrir getters e setters do DTO e da lotação")
    void deveCobrirGettersESettersDtoELotacao() {
        UsuarioAcessoAd.LotacaoAd lotacao = new UsuarioAcessoAd.LotacaoAd();
        lotacao.setCodigo(123);
        lotacao.setSigla("DIREX");
        lotacao.setNome("Diretoria Executiva");

        UsuarioAcessoAd usuario = new UsuarioAcessoAd();
        usuario.setLogin("joao.silva");
        usuario.setNome("João da Silva");
        usuario.setEmail("joao.silva@sgc.gov.br");
        usuario.setNivel(2);
        usuario.setNomeNivel("Tático");
        usuario.setTipo("SERVIDOR");
        usuario.setLotacao(lotacao);

        assertThat(usuario.getLogin()).isEqualTo("joao.silva");
        assertThat(usuario.getNome()).isEqualTo("João da Silva");
        assertThat(usuario.getEmail()).isEqualTo("joao.silva@sgc.gov.br");
        assertThat(usuario.getNivel()).isEqualTo(2);
        assertThat(usuario.getNomeNivel()).isEqualTo("Tático");
        assertThat(usuario.getTipo()).isEqualTo("SERVIDOR");

        UsuarioAcessoAd.LotacaoAd lotacao1 = usuario.getLotacao();
        assertThat(lotacao1).isSameAs(lotacao);
        assertThat(lotacao1.getCodigo()).isEqualTo(123);
        assertThat(lotacao1.getSigla()).isEqualTo("DIREX");
        assertThat(lotacao1.getNome()).isEqualTo("Diretoria Executiva");
    }
}
