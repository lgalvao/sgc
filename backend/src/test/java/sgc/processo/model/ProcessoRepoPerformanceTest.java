package sgc.processo.model;

import jakarta.persistence.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
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

        Unidade unidade = new Unidade();
        unidade.setNome("Unidade teste");
        unidade.setSigla("UT");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidadeRepo.save(unidade);

        Processo processo = new Processo();
        processo.setDescricao("Processo finalizado");
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataLimite(java.time.LocalDateTime.now().plusDays(30));
        processo.adicionarParticipantes(Set.of(unidade));
        processoRepo.save(processo);

        entityManager.flush();
        entityManager.clear();

        List<Processo> processos = processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);

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
