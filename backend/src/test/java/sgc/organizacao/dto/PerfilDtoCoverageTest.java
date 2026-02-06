package sgc.organizacao.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Perfil;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PerfilDto Coverage Tests")
class PerfilDtoCoverageTest {

    @Test
    @DisplayName("Deve cobrir o método from")
    void deveCobrirMethodFrom() {
        PerfilDto dto = PerfilDto.from(Perfil.ADMIN);
        assertThat(dto.perfil()).isEqualTo("ADMIN");
        assertThat(dto.descricao()).isEqualTo("ADMIN");
        assertThat(dto.usuarioTitulo()).isNull();
    }

    @Test
    @DisplayName("Deve cobrir o construtor e getters do record")
    void deveCobrirConstrutorEGetters() {
        PerfilDto dto = new PerfilDto("123456789012", 1L, "Unidade", "ADMIN", "Administrador");
        assertThat(dto.usuarioTitulo()).isEqualTo("123456789012");
        assertThat(dto.unidadeCodigo()).isEqualTo(1L);
        assertThat(dto.unidadeNome()).isEqualTo("Unidade");
        assertThat(dto.perfil()).isEqualTo("ADMIN");
        assertThat(dto.descricao()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("Deve cobrir equals, hashCode e toString")
    void deveCobrirMetodosPadrao() {
        PerfilDto dto1 = new PerfilDto("123456789012", 1L, "Unidade", "ADMIN", "Administrador");
        PerfilDto dto2 = new PerfilDto("123456789012", 1L, "Unidade", "ADMIN", "Administrador");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).isNotNull();
        assertThat(dto1).isNotEqualTo(null);
        assertThat(dto1).isNotEqualTo(new Object());
    }
}
