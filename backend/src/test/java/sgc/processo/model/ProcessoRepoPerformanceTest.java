package sgc.processo.model;

import jakarta.persistence.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.testutils.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
class ProcessoRepoPerformanceTest {
    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void listarPorSituacaoComParticipantes_deveCarregarParticipantesComFetch() {
        // Arrange
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comNome("Unidade Teste")
                .comSigla("UT")
                .comTituloTitular("Titular")
                .build();
        unidadeRepo.save(unidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo Finalizado");
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Processo> processos = processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);

        // Assert
        assertThat(processos).isNotEmpty();

        for (Processo p : processos) {
            assertThat(Hibernate.isInitialized(p.getParticipantes()))
                    .as("Participantes devem ser inicializados pelo JOIN FETCH. Processo: " + p.getCodigo())
                    .isTrue();
        }

        Processo returnedProcess = processos.stream()
                .filter(p -> p.getCodigo().equals(processo.getCodigo()))
                .findFirst()
                .orElseThrow();

        assertThat(returnedProcess.getParticipantes()).hasSize(1);
    }
}
