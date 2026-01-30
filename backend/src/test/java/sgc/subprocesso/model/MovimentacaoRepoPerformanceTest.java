package sgc.subprocesso.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.model.ProcessoRepo;

@SpringBootTest
@Transactional
@Tag("integration")
@ActiveProfiles("test")
class MovimentacaoRepoPerformanceTest {

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Test
    @DisplayName("Deve buscar movimentações com join fetch")
    void deveBuscarMovimentacoesComJoinFetch() {
        // Given
        Unidade uOrigem = new Unidade();
        uOrigem.setSigla("UO");
        uOrigem.setNome("Unidade Origem");
        uOrigem.setTipo(TipoUnidade.OPERACIONAL);
        uOrigem = unidadeRepo.save(uOrigem);

        Unidade uDestino = new Unidade();
        uDestino.setSigla("UD");
        uDestino.setNome("Unidade Destino");
        uDestino.setTipo(TipoUnidade.OPERACIONAL);
        uDestino = unidadeRepo.save(uDestino);

        Processo p = new Processo();
        p = processoRepo.save(p);

        Subprocesso sp = Subprocesso.builder()
            .processo(p)
            .unidade(uOrigem)
            .situacao(SituacaoSubprocesso.NAO_INICIADO)
            .build();
        sp = subprocessoRepo.save(sp);

        Movimentacao m1 = Movimentacao.builder()
            .subprocesso(sp)
            .unidadeOrigem(uOrigem)
            .unidadeDestino(uDestino)
            .descricao("Mov 1")
            .dataHora(LocalDateTime.now().minusHours(1))
            .build();
        movimentacaoRepo.save(m1);

        Movimentacao m2 = Movimentacao.builder()
            .subprocesso(sp)
            .unidadeOrigem(uOrigem)
            .unidadeDestino(null)
            .descricao("Mov 2")
            .dataHora(LocalDateTime.now())
            .build();
        movimentacaoRepo.save(m2);

        // When
        List<Movimentacao> result = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescricao()).isEqualTo("Mov 2");
        assertThat(result.get(1).getDescricao()).isEqualTo("Mov 1");

        assertThat(result.get(1).getUnidadeDestino().getSigla()).isEqualTo("UD");
        assertThat(result.get(0).getUnidadeDestino()).isNull();
    }
}
