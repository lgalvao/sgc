package sgc.mapa.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes da Entidade Conhecimento")
class ConhecimentoTest {

    @Test
    @DisplayName("Deve instanciar via builder")
    void deveInstanciarViaBuilder() {
        Atividade atividade = new Atividade();
        Conhecimento k = Conhecimento.builder()
                .descricao("desc")
                .atividade(atividade)
                .build();

        k.setCodigo(1L);
        assertThat(k.getCodigo()).isEqualTo(1L);
        assertThat(k.getDescricao()).isEqualTo("desc");
        assertThat(k.getAtividade()).isEqualTo(atividade);
    }

    @Test
    @DisplayName("Deve retornar código da atividade")
    void deveRetornarCodigoAtividade() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        Conhecimento k = Conhecimento.builder()
                .atividade(atividade)
                .build();

        assertThat(k.getCodigoAtividade()).isEqualTo(100L);
    }
}
