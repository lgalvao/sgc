package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.TipoImpactoAtividade;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ImpactoAtividadeService {

    private static final Logger log = LoggerFactory.getLogger(ImpactoAtividadeService.class);

    private final ImpactoCompetenciaService impactoCompetenciaService;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;

    private Map<String, Atividade> mapAtividadesByDescricao(List<Atividade> atividades) {
        return atividades.stream()
                .collect(Collectors.toMap(Atividade::getDescricao, atividade -> atividade));
    }

    /**
     * Obtém todas as atividades associadas a um mapa, com seus conhecimentos.
     *
     * @param mapa O mapa do qual as atividades serão extraídas.
     * @return Uma {@link List} de {@link Atividade}s.
     */
    public List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        log.info("Buscando atividades e conhecimentos para o mapa {}", mapa.getCodigo());
        return atividadeRepo.findByMapaCodigoWithConhecimentos(mapa.getCodigo());
    }

    public List<AtividadeImpactadaDto> detectarAtividadesInseridas(List<Atividade> atuais, List<Atividade> vigentes) {
        Set<String> descricoesVigentes = vigentes.stream().map(Atividade::getDescricao).collect(Collectors.toSet());
        List<AtividadeImpactadaDto> inseridas = new ArrayList<>();

        for (Atividade atual : atuais) {
            if (!descricoesVigentes.contains(atual.getDescricao())) {
                inseridas.add(new AtividadeImpactadaDto(
                        atual.getCodigo(),
                        atual.getDescricao(),
                        TipoImpactoAtividade.INSERIDA,
                        null,
                        List.of()
                ));
            }
        }
        return inseridas;
    }

    public List<AtividadeImpactadaDto> detectarAtividadesRemovidas(
            List<Atividade> atuais,
            List<Atividade> vigentes,
            Mapa mapaVigente) {
        log.info("detectarAtividadesRemovidas - Atividades Atuais: {}", atuais.stream().map(a -> a.getCodigo() + ":" + a.getDescricao()).collect(Collectors.joining(", ")));
        log.info("detectarAtividadesRemovidas - Atividades Vigentes: {}", vigentes.stream().map(a -> a.getCodigo() + ":" + a.getDescricao()).collect(Collectors.joining(", ")));

        List<AtividadeImpactadaDto> removidas = new ArrayList<>();
        Map<String, Atividade> atuaisMap = mapAtividadesByDescricao(atuais);

        for (Atividade vigente : vigentes) {
            if (!atuaisMap.containsKey(vigente.getDescricao())) {
                removidas.add(new AtividadeImpactadaDto(
                        vigente.getCodigo(),
                        vigente.getDescricao(),
                        TipoImpactoAtividade.REMOVIDA,
                        null,
                        impactoCompetenciaService.obterCompetenciasDaAtividade(vigente.getCodigo(), mapaVigente)
                ));
            }
        }
        log.info("detectarAtividadesRemovidas - {} atividades removidas.", removidas.size());
        return removidas;
    }

    public List<AtividadeImpactadaDto> detectarAtividadesAlteradas(List<Atividade> atuais, List<Atividade> vigentes, Mapa mapaVigente) {
        log.info("detectarAtividadesAlteradas - Atividades Atuais: {}", atuais.stream().map(a -> a.getCodigo() + ":" + a.getDescricao()).collect(Collectors.joining(", ")));
        log.info("detectarAtividadesAlteradas - Atividades Vigentes: {}", vigentes.stream().map(a -> a.getCodigo() + ":" + a.getDescricao()).collect(Collectors.joining(", ")));

        List<AtividadeImpactadaDto> alteradas = new ArrayList<>();
        Map<String, Atividade> vigentesMap = mapAtividadesByDescricao(vigentes);

        for (Atividade atual : atuais) {
            if (vigentesMap.containsKey(atual.getDescricao())) {
                Atividade vigente = vigentesMap.get(atual.getDescricao());
                // Compare conhecimentos associated with the activities
                List<Conhecimento> conhecimentosAtuais = conhecimentoRepo.findByAtividadeCodigo(atual.getCodigo());
                List<Conhecimento> conhecimentosVigentes = conhecimentoRepo.findByAtividadeCodigo(vigente.getCodigo());

                if (conhecimentosDiferentes(conhecimentosAtuais, conhecimentosVigentes)) {
                    alteradas.add(new AtividadeImpactadaDto(
                            atual.getCodigo(),
                            atual.getDescricao(),
                            TipoImpactoAtividade.ALTERADA,
                            "Descrição ou conhecimentos associados alterados.", // More specific message can be added if needed
                            impactoCompetenciaService.obterCompetenciasDaAtividade(atual.getCodigo(), mapaVigente)
                    ));
                }
            }
        }
        log.info("detectarAtividadesAlteradas - {} atividades alteradas.", alteradas.size());
        return alteradas;
    }

    private boolean conhecimentosDiferentes(List<Conhecimento> lista1, List<Conhecimento> lista2) {
        if (lista1.size() != lista2.size()) {
            return true;
        }
        Set<String> descricoes1 = lista1.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
        Set<String> descricoes2 = lista2.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
        return !descricoes1.equals(descricoes2);
    }
}