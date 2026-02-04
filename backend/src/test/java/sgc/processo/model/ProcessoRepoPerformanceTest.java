package sgc.processo.model;

import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
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
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade Teste");
        unidade.setSigla("UT");
        unidade.setTituloTitular("Titular");
        unidadeRepo.save(unidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo Finalizado");
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.getParticipantes().add(unidade);
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

        // Verify content for our created process
        Processo returnedProcess = processos.stream()
                .filter(p -> p.getCodigo().equals(processo.getCodigo()))
                .findFirst()
                .orElseThrow();

        assertThat(returnedProcess.getParticipantes()).hasSize(1);
    }
}
