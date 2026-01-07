package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("Performance: Processo")
class ProcessoPerformanceTest extends BaseIntegrationTest {

    @Autowired
    private ProcessoService processoService;

    @Test
    @DisplayName("Deve listar processos ativos com participantes")
    void deveListarProcessosAtivos() {
        // Arrange
        Unidade u1 = unidadeRepo.findById(2L).orElseThrow();
        Unidade u2 = unidadeRepo.findById(3L).orElseThrow();

        Processo p1 = new Processo("Proc 1", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(10));
        p1.setParticipantes(Set.of(u1, u2));
        processoRepo.save(p1);

        Processo p2 = new Processo("Proc 2", TipoProcesso.REVISAO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now().plusDays(10));
        p2.setParticipantes(Set.of(u1));
        processoRepo.save(p2);

        processoRepo.flush();

        // Act
        List<ProcessoDto> result = processoService.listarAtivos();

        // Assert
        assertThat(result).extracting(ProcessoDto::getDescricao)
                .contains("Proc 1", "Proc 2");

        assertThat(result).filteredOn(p -> p.getDescricao().equals("Proc 1"))
                .flatExtracting(ProcessoDto::getUnidadesParticipantes)
                .first().asString().contains(u1.getSigla());
    }
}
