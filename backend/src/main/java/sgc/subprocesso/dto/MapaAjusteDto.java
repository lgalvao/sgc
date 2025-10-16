package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import sgc.analise.modelo.Analise;
import sgc.atividade.modelo.Atividade;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.subprocesso.modelo.Subprocesso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para mapa de competências no contexto de ajustes.
 * CDU-16 item 4
 */
@Getter
@Builder
public class MapaAjusteDto {
    @NotNull private final Long mapaId;
    @NotBlank private final String unidadeNome;
    @NotNull @Valid private final List<CompetenciaAjusteDto> competencias;
    private final String justificativaDevolucao;

    public static MapaAjusteDto of(Subprocesso sp, Analise analise, List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, List<CompetenciaAtividade> competenciaAtividades) {
        Long idMapa = sp.getMapa().getCodigo();
        String nomeUnidade = sp.getUnidade() != null ? sp.getUnidade().getNome() : "";

        String justificativa = analise != null ? analise.getObservacoes() : null;

        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            for (Atividade ativ : atividades) {
                List<Conhecimento> conhecimentosDaAtividade = conhecimentos.stream().filter(c -> c.getAtividade().getCodigo().equals(ativ.getCodigo())).toList();
                boolean isLinked = competenciaAtividades.stream().anyMatch(ca -> ca.getId().getCompetenciaCodigo().equals(comp.getCodigo()) && ca.getId().getAtividadeCodigo().equals(ativ.getCodigo()));
                List<ConhecimentoAjusteDto> conhecimentoDtos = conhecimentosDaAtividade.stream()
                        .map(con -> ConhecimentoAjusteDto.builder()
                            .conhecimentoId(con.getCodigo())
                            .nome(con.getDescricao())
                            .incluido(isLinked)
                            .build())
                        .collect(Collectors.toList());
                atividadeDtos.add(AtividadeAjusteDto.builder()
                    .atividadeId(ativ.getCodigo())
                    .nome(ativ.getDescricao())
                    .conhecimentos(conhecimentoDtos)
                    .build());
            }
            competenciaDtos.add(CompetenciaAjusteDto.builder()
                .competenciaId(comp.getCodigo())
                .nome(comp.getDescricao())
                .atividades(atividadeDtos)
                .build());
        }

        return MapaAjusteDto.builder()
            .mapaId(idMapa)
            .unidadeNome(nomeUnidade)
            .competencias(competenciaDtos)
            .justificativaDevolucao(justificativa)
            .build();
    }
}