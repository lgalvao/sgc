package sgc.mapa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes da Entidade Competencia")
class CompetenciaTest {

    @Test
    @DisplayName("Deve instanciar via builder")
    void deveInstanciarViaBuilder() {
        Mapa mapa = new Mapa();
        Competencia c = Competencia.builder()
                .descricao("desc")
                .mapa(mapa)
                .build();
        
        c.setCodigo(1L);
        assertThat(c.getCodigo()).isEqualTo(1L);
        assertThat(c.getDescricao()).isEqualTo("desc");
        assertThat(c.getMapa()).isEqualTo(mapa);
    }
}
