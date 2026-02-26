package sgc.subprocesso.dto;

import lombok.*;
import sgc.mapa.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

/**
 * DTO de resposta para visualização do mapa na tela de ajustes.
 *
 * <p>Usado exclusivamente como resposta de API pelo endpoint
 * {@code GET /subprocessos/{codigo}/mapa-ajuste}.
 *
 * <p>Para enviar ajustes, use {@link SalvarAjustesRequest}.
 */
@Getter
@Builder
public class MapaAjusteDto {

    private final Long codMapa;
    private final String unidadeNome;
    private final List<CompetenciaAjusteDto> competencias;
    private final String justificativaDevolucao;

    public static MapaAjusteDto of(
            Subprocesso sp,
            Analise analise,
            List<Competencia> competencias,
            List<Atividade> atividades,
            List<Conhecimento> conhecimentos) {
        Long codMapa = sp.getMapa().getCodigo();
        String nomeUnidade = sp.getUnidade().getNome();
        String justificativa = analise.getObservacoes();

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
