package sgc.diagnostico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.diagnostico.model.SituacaoCapacitacaoRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.UnidadeService;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticoRelatorioServiceTest {

    @Mock DiagnosticoRepo diagnosticoRepo;
    @Mock AvaliacaoServidorRepo avaliacaoServidorRepo;
    @Mock SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    @Mock UnidadeService unidadeService;

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
