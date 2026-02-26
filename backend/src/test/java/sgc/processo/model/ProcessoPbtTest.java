package sgc.processo.model;

import net.jqwik.api.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("PBT")
class ProcessoPbtTest {

    @Property
    void sincronizarParticipantes_mantemInvariante(@ForAll("conjuntoDeUnidades") Set<Unidade> unidadesIniciais,
                                                   @ForAll("conjuntoDeUnidades") Set<Unidade> novasUnidades) {
        // Arrange
        Processo processo = new Processo();
        processo.setParticipantes(new ArrayList<>()); // Inicializa lista vazia
        processo.adicionarParticipantes(unidadesIniciais);

        // Act
        processo.sincronizarParticipantes(novasUnidades);

        // Assert
        List<Long> codigosParticipantes = processo.getCodigosParticipantes();
        List<Long> codigosNovasUnidades = novasUnidades.stream()
                .map(Unidade::getCodigo)
                .toList();

        assertThat(codigosParticipantes)
                .containsExactlyInAnyOrderElementsOf(codigosNovasUnidades);
    }

    @Provide
    Arbitrary<Set<Unidade>> conjuntoDeUnidades() {
        return Arbitraries.longs().between(1, 100).map(id -> {
            Unidade u = new Unidade();
            u.setCodigo(id);
            u.setSigla("U" + id);
            u.setNome("Unidade " + id);
            u.setSituacao(SituacaoUnidade.ATIVA);
            u.setTipo(TipoUnidade.OPERACIONAL);
            return u;
        }).set().ofMinSize(0).ofMaxSize(10);
    }
}
