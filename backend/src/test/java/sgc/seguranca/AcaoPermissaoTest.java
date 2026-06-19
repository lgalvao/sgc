package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Perfil;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AcaoPermissao")
class AcaoPermissaoTest {

    @Test
    @DisplayName("deve indicar dependencia de localizacao para acoes de escrita")
    void deveIndicarDependenciaDeLocalizacaoParaAcoesDeEscrita() {
        assertThat(AcaoPermissao.EDITAR_CADASTRO.dependeLocalizacao()).isTrue();
        assertThat(AcaoPermissao.VISUALIZAR_PROCESSO.dependeLocalizacao()).isFalse();
    }

    @Test
    @DisplayName("deve respeitar perfis permitidos")
    void deveRespeitarPerfisPermitidos() {
        assertThat(AcaoPermissao.EDITAR_CADASTRO.permitePerfil(Perfil.CHEFE)).isTrue();
        assertThat(AcaoPermissao.EDITAR_CADASTRO.permitePerfil(Perfil.GESTOR)).isFalse();
        assertThat(AcaoPermissao.VISUALIZAR_PROCESSO.permitePerfil(Perfil.ADMIN)).isTrue();
        assertThat(AcaoPermissao.VISUALIZAR_PROCESSO.permitePerfil(Perfil.SERVIDOR)).isTrue();
    }
}
