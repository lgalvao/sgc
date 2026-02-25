package sgc.processo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Modelos - Cobertura de Casos de Borda")
class ModelCoverageTest {

    @Test
    @DisplayName("UnidadeProcesso deve lidar com ID nulo via getters e setters de conveniência")
    void deveLidarComIdNuloEmUnidadeProcesso() throws Exception {
        UnidadeProcesso up = new UnidadeProcesso();
        
        // Simular ID nulo (normalmente é inicializado inline, mas para 100% cobertura das branches defensivas)
        Field idField = UnidadeProcesso.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(up, null);

        // Branch id == null em getUnidadeCodigo
        assertThat(up.getUnidadeCodigo()).isNull();

        // Branch id == null em setUnidadeCodigo (cria novo id)
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

        // Adiciona 1 e 2
        processo.adicionarParticipantes(Set.of(u1, u2));
        assertThat(processo.getParticipantes()).hasSize(2);

        // Sincroniza apenas com o 2 (deve remover o 1 e manter o 2)
        // Isso cobre both true/false do removeIf em Proceso.java:86
        processo.sincronizarParticipantes(new HashSet<>(Collections.singletonList(u2)));
        
        assertThat(processo.getParticipantes()).hasSize(1);
        assertThat(processo.getParticipantes().getFirst().getUnidadeCodigo()).isEqualTo(2L);
    }
}
