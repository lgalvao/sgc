package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoRelatorioServiceTest {

    @Mock
    DiagnosticoRepo diagnosticoRepo;
    @Mock
    AvaliacaoServidorRepo avaliacaoServidorRepo;
    @Mock
    SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    @Mock
    UnidadeService unidadeService;

    @InjectMocks
    DiagnosticoRelatorioService service;

    @Test
    @DisplayName("criarRelatorioGapDiagnostico deve usar o mapa vigente da unidade")
    void criarRelatorioGapDiagnostico_deveUsarMapaVigenteDaUnidade() {
        Subprocesso subprocesso = subprocesso(910L, 12L);
        Diagnostico diagnostico = diagnostico(501L);
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCompetencias(Set.of(competencia(77L, "Competência vigente")));
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        avaliacao.setCompetencia(competencia(77L, "Competência vigente"));
        avaliacao.setGap(2);

        when(diagnosticoRepo.findBySubprocessoCodigo(910L)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoServidorRepo.listarPorDiagnostico(501L)).thenReturn(List.of(avaliacao));
        when(unidadeService.buscarMapaVigente(12L)).thenReturn(Optional.of(mapaVigente));

        var dto = service.criarRelatorioGapDiagnostico(subprocesso);

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.competenciaCodigo()).isEqualTo(77L);
            assertThat(item.competenciaDescricao()).isEqualTo("Competência vigente");
        });
    }

    @Test
    @DisplayName("criarRelatorioGapDiagnostico deve falhar sem mapa vigente da unidade")
    void criarRelatorioGapDiagnostico_deveFalharSemMapaVigente() {
        Subprocesso subprocesso = subprocesso(910L, 12L);
        Diagnostico diagnostico = diagnostico(501L);

        when(diagnosticoRepo.findBySubprocessoCodigo(910L)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoServidorRepo.listarPorDiagnostico(501L)).thenReturn(List.of());
        when(unidadeService.buscarMapaVigente(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarRelatorioGapDiagnostico(subprocesso))
                .hasMessageContaining("Processo de diagnóstico sem mapa vigente");
    }

    private Diagnostico diagnostico(Long codigo) {
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigo(codigo);
        return diagnostico;
    }

    private Subprocesso subprocesso(Long codigo, Long unidadeCodigo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(unidadeCodigo);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(codigo);
        subprocesso.setUnidade(unidade);
        return subprocesso;
    }

    private Competencia competencia(Long codigo, String descricao) {
        Competencia competencia = new Competencia();
        competencia.setCodigo(codigo);
        competencia.setDescricao(descricao);
        return competencia;
    }
}
