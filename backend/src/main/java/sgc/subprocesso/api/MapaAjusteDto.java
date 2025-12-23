package sgc.subprocesso.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import sgc.analise.internal.model.Analise;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.Conhecimento;
import sgc.mapa.internal.model.Competencia;
import sgc.subprocesso.internal.model.Subprocesso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MapaAjusteDto {
    @NotNull
    private final Long codMapa;
    @NotBlank
    private final String unidadeNome;
    @NotNull
    @Valid
    private final List<CompetenciaAjusteDto> competencias;
    private final String justificativaDevolucao;

    public static MapaAjusteDto of(
            Subprocesso sp,
            Analise analise,
            List<Competencia> competencias,
            List<Atividade> atividades,
            List<Conhecimento> conhecimentos) {
        Long codMapa = sp.getMapa().getCodigo();
        String nomeUnidade = sp.getUnidade() != null ? sp.getUnidade().getNome() : "";
        String justificativa = analise != null ? analise.getObservacoes() : null;

        List<CompetenciaAjusteDto> competenciaDtos = new ArrayList<>();

        for (Competencia comp : competencias) {
            List<AtividadeAjusteDto> atividadeDtos = new ArrayList<>();
            for (Atividade ativ : atividades) {
                List<Conhecimento> conhecimentosDaAtividade =
                        conhecimentos.stream()
                                .filter(c -> c.getAtividade().getCodigo().equals(ativ.getCodigo()))
                                .toList();
                boolean isLinked = comp.getAtividades().contains(ativ);
                List<ConhecimentoAjusteDto> conhecimentoDtos =
                        conhecimentosDaAtividade.stream()
                                .map(
                                        con ->
                                                ConhecimentoAjusteDto.builder()
                                                        .conhecimentoCodigo(con.getCodigo())
                                                        .nome(con.getDescricao())
                                                        .incluido(isLinked)
                                                        .build())
                                .collect(Collectors.toList());

                atividadeDtos.add(
                        AtividadeAjusteDto.builder()
                                .codAtividade(ativ.getCodigo())
                                .nome(ativ.getDescricao())
                                .conhecimentos(conhecimentoDtos)
                                .build());
            }

            competenciaDtos.add(
                    CompetenciaAjusteDto.builder()
                            .codCompetencia(comp.getCodigo())
                            .nome(comp.getDescricao())
                            .atividades(atividadeDtos)
                            .build());
        }

        return MapaAjusteDto.builder()
                .codMapa(codMapa)
                .unidadeNome(nomeUnidade)
                .competencias(competenciaDtos)
                .justificativaDevolucao(justificativa)
                .build();
    }
}
