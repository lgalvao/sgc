package sgc.mapa.model;

import org.junit.jupiter.api.*;
import sgc.mapa.dto.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Atividade")
class AtividadeTest {

    @Test
    @DisplayName("deve criar atividade a partir de request")
    void deveCriarAtividadeAPartirDeRequest() {
        CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                .mapaCodigo(10L)
                .descricao("Atividade inicial")
                .build();

        Atividade atividade = Atividade.criarDe(request);

        assertThat(atividade.getDescricao()).isEqualTo("Atividade inicial");
    }

    @Test
    @DisplayName("deve expor codigo do mapa e atualizar descricao")
    void deveExporCodigoDoMapaEAtualizarDescricao() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(99L);

        Atividade atividade = new Atividade();
        atividade.setMapa(mapa);
        atividade.setDescricao("Antes");

        atividade.atualizarDe(AtualizarAtividadeRequest.builder()
                .descricao("Depois")
                .build());

        assertThat(atividade.getMapaCodigo()).isEqualTo(99L);
        assertThat(atividade.getDescricao()).isEqualTo("Depois");
    }
}
