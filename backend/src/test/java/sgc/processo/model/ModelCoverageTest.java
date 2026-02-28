package sgc.processo.model;

import org.junit.jupiter.api.*;
import sgc.organizacao.model.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Modelos - Cobertura de Casos de Borda")
class ModelCoverageTest {

    @Test
    @DisplayName("UnidadeProcesso deve lidar com ID nulo via getters e setters de conveniência")
    void deveLidarComIdNuloEmUnidadeProcesso() throws Exception {
        UnidadeProcesso up = new UnidadeProcesso();
        Field idField = UnidadeProcesso.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(up, null);
        assertThat(up.getUnidadeCodigo()).isNull();

        up.setUnidadeCodigo(999L);
        assertThat(up.getUnidadeCodigo()).isEqualTo(999L);
        assertThat(idField.get(up)).isNotNull();
    }

    @Test
    @DisplayName("Processo deve remover participantes que não estão mais na lista ao sincronizar")
    void deveRemoverParticipantesAoSincronizar() {
        Processo processo = new Processo();

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSigla("U1");
        u1.setSituacao(SituacaoUnidade.ATIVA);
        u1.setMatriculaTitular("1234");
        u1.setTituloTitular("123456");

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setSigla("U2");
        u2.setSituacao(SituacaoUnidade.ATIVA);
        u2.setMatriculaTitular("5678");
        u2.setTituloTitular("789012");


        processo.adicionarParticipantes(Set.of(u1, u2));
        assertThat(processo.getParticipantes()).hasSize(2);


        // Isso cobre both true/false do removeIf em Proceso.java:86
        processo.sincronizarParticipantes(new HashSet<>(Collections.singletonList(u2)));

        assertThat(processo.getParticipantes()).hasSize(1);
        assertThat(processo.getParticipantes().getFirst().getUnidadeCodigo()).isEqualTo(2L);
    }
}
