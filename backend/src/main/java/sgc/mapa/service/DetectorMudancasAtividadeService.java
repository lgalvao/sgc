package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.TipoImpactoAtividade;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado na detecção de alterações em atividades entre duas versões de mapa.
 * Responsável por identificar inserções, remoções e modificações em atividades e seus conhecimentos.
 *
 * <p>Utiliza mapas em memória para otimizar a comparação e evitar excesso de consultas ao banco de dados.
 *
 * @see DetectorImpactoCompetenciaService para análise de impactos em competências
 */
@Service
@RequiredArgsConstructor
public class DetectorMudancasAtividadeService {
    /**
     * Detecta atividades que foram inseridas no mapa atual em comparação com o vigente.
     */
    public List<AtividadeImpactadaDto> detectarInseridas(List<Atividade> atuais, List<Atividade> vigentes) {
        Set<String> descVigentes = vigentes.stream().map(Atividade::getDescricao).collect(Collectors.toSet());

        List<AtividadeImpactadaDto> inseridas = new ArrayList<>();
        for (Atividade atual : atuais) {
            if (!descVigentes.contains(atual.getDescricao())) {
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(atual.getCodigo())
                        .descricao(atual.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.INSERIDA)
                        .descricaoAnterior(null)
                        .competenciasVinculadas(List.of())
                        .build();

                inseridas.add(dto);
            }
        }
        return inseridas;
    }

    /**
     * Detecta atividades que foram removidas do mapa atual em comparação com o vigente.
     */
    public List<AtividadeImpactadaDto> detectarRemovidas(
            List<Atividade> atuais,
            List<Atividade> vigentes,
            Map<Long, List<Competencia>> competenciasVinculadas) {

        List<AtividadeImpactadaDto> removidas = new ArrayList<>();
        Map<String, Atividade> atuaisMap = atividadesPorDescricao(atuais);

        for (Atividade vigente : vigentes) {
            if (!atuaisMap.containsKey(vigente.getDescricao())) {
                Long vigenteCodigo = vigente.getCodigo();
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(vigenteCodigo)
                        .descricao(vigente.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.REMOVIDA)
                        .descricaoAnterior(null)
                        .competenciasVinculadas(obterNomesCompetencias(vigenteCodigo, competenciasVinculadas))
                        .build();

                removidas.add(dto);
            }
        }
        return removidas;
    }

    /**
     * Detecta atividades que foram alteradas (em seus conhecimentos) no mapa atual em comparação com o vigente.
     */
    public List<AtividadeImpactadaDto> detectarAlteradas(
            List<Atividade> atuais,
            List<Atividade> vigentes,
            Map<Long, List<Competencia>> atividadeIdToCompetencias) {

        List<AtividadeImpactadaDto> alteradas = new ArrayList<>();
        Map<String, Atividade> vigentesMap = atividadesPorDescricao(vigentes);

        for (Atividade atual : atuais) {
            if (vigentesMap.containsKey(atual.getDescricao())) {
                Atividade vigente = vigentesMap.get(atual.getDescricao());

                List<Conhecimento> conhecimentosAtuais = atual.getConhecimentos();
                List<Conhecimento> conhecimentosVigentes = vigente.getConhecimentos();

                if (conhecimentosDiferentes(conhecimentosAtuais, conhecimentosVigentes)) {
                    alteradas.add(
                            AtividadeImpactadaDto.builder()
                                    .codigo(atual.getCodigo())
                                    .descricao(atual.getDescricao())
                                    .tipoImpacto(TipoImpactoAtividade.ALTERADA)
                                    .descricaoAnterior("Descrição ou conhecimentos associados alterados.")
                                    .competenciasVinculadas(
                                            obterNomesCompetencias(vigente.getCodigo(), atividadeIdToCompetencias))
                                    .build());
                }
            }
        }
        return alteradas;
    }

    private Map<String, Atividade> atividadesPorDescricao(List<Atividade> atividades) {
        return atividades.stream().collect(Collectors.toMap(Atividade::getDescricao, atividade -> atividade));
    }

    private boolean conhecimentosDiferentes(List<Conhecimento> lista1, List<Conhecimento> lista2) {
        if (lista1.size() != lista2.size()) return true;

        Set<String> descricoes1 = lista1.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
        Set<String> descricoes2 = lista2.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());

        return !descricoes1.equals(descricoes2);
    }

    private List<String> obterNomesCompetencias(Long codigoAtividade, Map<Long, List<Competencia>> atividadeIdToCompetencias) {
        return atividadeIdToCompetencias.getOrDefault(codigoAtividade, List.of())
                .stream()
                .map(Competencia::getDescricao)
                .toList();
    }
}
