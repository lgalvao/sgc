package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: ProcessoConsultaService")
class ProcessoConsultaServiceTest {

    @InjectMocks
    private ProcessoConsultaService processoConsultaService;

    @Mock
    private ProcessoRepo processoRepo;

    @Test
    @DisplayName("Deve buscar IDs de unidades em processos ativos")
    void buscarIdsUnidadesEmProcessosAtivos_sucesso() {
        // Arrange
        Long processoIgnorar = 100L;
        List<Long> unidadesMock = List.of(1L, 2L, 3L);

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(unidadesMock);

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).hasSize(3).containsExactlyInAnyOrder(1L, 2L, 3L);

        verify(processoRepo).findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                eq(Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO)),
                eq(processoIgnorar)
        );
    }

    @Test
    @DisplayName("Deve retornar conjunto vazio se não houver processos ativos")
    void buscarIdsUnidadesEmProcessosAtivos_vazio() {
        // Arrange
        Long processoIgnorar = 100L;

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(List.of());

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).isEmpty();
    }
}
