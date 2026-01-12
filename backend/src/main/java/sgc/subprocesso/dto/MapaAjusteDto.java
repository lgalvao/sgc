package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.subprocesso.model.Subprocesso;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class MapaAjusteDto {

        @NotNull(message = "O código do mapa é obrigatório")
        private final Long codMapa;
        @NotBlank(message = "O nome da unidade é obrigatório")
        private final String unidadeNome;
        @NotNull(message = "A lista de competências é obrigatória")
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
                                List<Conhecimento> conhecimentosDaAtividade = conhecimentos.stream()
                                                .filter(c -> c.getAtividade().getCodigo().equals(ativ.getCodigo()))
                                                .toList();
                                boolean isLinked = comp.getAtividades().contains(ativ);
                                List<ConhecimentoAjusteDto> conhecimentoDtos = conhecimentosDaAtividade.stream()
                                                .map(
                                                                con -> ConhecimentoAjusteDto.builder()
                                                                                .conhecimentoCodigo(con.getCodigo())
                                                                                .nome(con.getDescricao())
                                                                                .incluido(isLinked)
                                                                                .build())
                                                .toList();

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
