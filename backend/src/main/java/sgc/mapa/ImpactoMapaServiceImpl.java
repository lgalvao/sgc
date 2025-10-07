package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.competencia.Competencia;
import sgc.competencia.CompetenciaAtividade;
import sgc.competencia.CompetenciaAtividadeRepository;
import sgc.competencia.CompetenciaRepository;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de verificação de impactos no mapa de competências.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImpactoMapaServiceImpl implements ImpactoMapaService {
        private final SubprocessoRepository subprocessoRepository;
        private final MapaRepository mapaRepository;
        private final AtividadeRepository atividadeRepository;
        private final CompetenciaRepository competenciaRepository;
        private final CompetenciaAtividadeRepository competenciaAtividadeRepository;

        /**
         * Compara cadastro atual do subprocesso com mapa vigente da unidade.
         * Detecta atividades inseridas, removidas ou alteradas.
         * Identifica competências impactadas.
         */
        @Override
        @Transactional(readOnly = true)
        public ImpactoMapaDto verificarImpactos(Long subprocessoId) {
                log.info("Verificando impactos no mapa: subprocesso={}", subprocessoId);

                // 1. Buscar subprocesso
                Subprocesso subprocesso = subprocessoRepository.findById(subprocessoId)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: " + subprocessoId));

                // 2. Buscar mapa vigente da unidade
                Optional<Mapa> mapaVigenteOpt = mapaRepository
                                .findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

                if (mapaVigenteOpt.isEmpty()) {
                        // Sem mapa vigente = não há impactos (é mapeamento inicial)
                        log.info("Unidade sem mapa vigente, não há impactos a analisar");
                        return new ImpactoMapaDto(
                                        false, 0, 0, 0,
                                        List.of(), List.of(), List.of(), List.of());
                }

                Mapa mapaVigente = mapaVigenteOpt.get();

                // 3. Buscar mapa do subprocesso
                Mapa mapaSubprocesso = mapaRepository
                                .findBySubprocessoCodigo(subprocessoId)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado para subprocesso: " + subprocessoId));

                // 4. Buscar atividades do cadastro atual (mapa do subprocesso)
                List<Atividade> atividadesAtuais = atividadeRepository
                                .findByMapaCodigo(mapaSubprocesso.getCodigo());

                // 5. Buscar atividades do mapa vigente
                List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);

                // 6. Comparar e detectar mudanças
                List<AtividadeImpactadaDto> inseridas = detectarAtividadesInseridas(atividadesAtuais,
                                atividadesVigentes);
                List<AtividadeImpactadaDto> removidas = detectarAtividadesRemovidas(atividadesAtuais,
                                atividadesVigentes, mapaVigente);
                List<AtividadeImpactadaDto> alteradas = detectarAtividadesAlteradas(atividadesAtuais,
                                atividadesVigentes, mapaVigente);

                // 7. Identificar competências impactadas
                List<CompetenciaImpactadaDto> competenciasImpactadas = identificarCompetenciasImpactadas(mapaVigente,
                                removidas, alteradas);

                // 8. Montar resultado
                boolean temImpactos = !inseridas.isEmpty() || !removidas.isEmpty() || !alteradas.isEmpty();

                log.info("Análise de impactos concluída: tem={}, inseridas={}, removidas={}, alteradas={}",
                                temImpactos, inseridas.size(), removidas.size(), alteradas.size());

                return new ImpactoMapaDto(
                                temImpactos,
                                inseridas.size(),
                                removidas.size(),
                                alteradas.size(),
                                inseridas,
                                removidas,
                                alteradas,
                                competenciasImpactadas);
        }

        /**
         * Obtém todas atividades vinculadas a um mapa através das competências.
         */
        private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
                // Buscar competências do mapa
                List<Competencia> competencias = competenciaRepository
                                .findByMapaCodigo(mapa.getCodigo());

                // Coletar IDs de todas atividades vinculadas
                Set<Long> atividadesIds = new HashSet<>();
                for (Competencia comp : competencias) {
                        List<CompetenciaAtividade> vinculos = competenciaAtividadeRepository
                                        .findByCompetenciaCodigo(comp.getCodigo());
                        for (CompetenciaAtividade vinculo : vinculos) {
                                atividadesIds.add(vinculo.getId().getAtividadeCodigo());
                        }
                }

                // Buscar atividades
                if (atividadesIds.isEmpty()) {
                        return List.of();
                }
                return atividadeRepository.findAllById(atividadesIds);
        }

        /**
         * Detecta atividades que foram inseridas (estão no atual, não estavam no
         * vigente).
         * Critério: comparação por descrição (case-insensitive).
         */
        private List<AtividadeImpactadaDto> detectarAtividadesInseridas(List<Atividade> atuais,
                        List<Atividade> vigentes) {
                // Criar set de descrições vigentes (case-insensitive)
                Set<String> descricoesVigentes = vigentes.stream()
                                .map(a -> a.getDescricao().toLowerCase().trim())
                                .collect(Collectors.toSet());

                return atuais.stream()
                                .filter(a -> !descricoesVigentes.contains(
                                                a.getDescricao().toLowerCase().trim()))
                                .map(a -> new AtividadeImpactadaDto(
                                                a.getCodigo(),
                                                a.getDescricao(),
                                                "INSERIDA",
                                                null,
                                                List.of() // Atividades novas não têm competências no mapa vigente
                                ))
                                .toList();
        }

        /**
         * Detecta atividades que foram removidas (estavam no vigente, não estão no
         * atual).
         * Critério: comparação por descrição (case-insensitive).
         */
        private List<AtividadeImpactadaDto> detectarAtividadesRemovidas(
                        List<Atividade> atuais,
                        List<Atividade> vigentes,
                        Mapa mapaVigente) {
                // Criar set de descrições atuais (case-insensitive)
                Set<String> descricoesAtuais = atuais.stream()
                                .map(a -> a.getDescricao().toLowerCase().trim())
                                .collect(Collectors.toSet());

                return vigentes.stream()
                                .filter(a -> !descricoesAtuais.contains(
                                                a.getDescricao().toLowerCase().trim()))
                                .map(a -> new AtividadeImpactadaDto(
                                                a.getCodigo(),
                                                a.getDescricao(),
                                                "REMOVIDA",
                                                null,
                                                obterCompetenciasDaAtividade(a.getCodigo(), mapaVigente)))
                                .toList();
        }

        /**
         * Detecta atividades com descrição alterada.
         * Critério: mesmo código, descrição diferente.
         */
        private List<AtividadeImpactadaDto> detectarAtividadesAlteradas(
                        List<Atividade> atuais,
                        List<Atividade> vigentes,
                        Mapa mapaVigente) {
                // Criar map de vigentes por código
                Map<Long, String> vigentesMap = vigentes.stream()
                                .collect(Collectors.toMap(
                                                Atividade::getCodigo,
                                                Atividade::getDescricao));

                return atuais.stream()
                                .filter(a -> vigentesMap.containsKey(a.getCodigo()))
                                .filter(a -> !vigentesMap.get(a.getCodigo())
                                                .equalsIgnoreCase(a.getDescricao().trim()))
                                .map(a -> new AtividadeImpactadaDto(
                                                a.getCodigo(),
                                                a.getDescricao(),
                                                "ALTERADA",
                                                vigentesMap.get(a.getCodigo()), // Descrição anterior
                                                obterCompetenciasDaAtividade(a.getCodigo(), mapaVigente)))
                                .toList();
        }

        /**
         * Identifica quais competências foram impactadas pelas mudanças.
         * Processa apenas atividades removidas e alteradas.
         */
        private List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
                        Mapa mapaVigente,
                        List<AtividadeImpactadaDto> removidas,
                        List<AtividadeImpactadaDto> alteradas) {
                // Map para acumular impactos por competência
                Map<Long, CompetenciaImpactoAcumulador> impactosMap = new HashMap<>();

                // Processar atividades removidas
                for (AtividadeImpactadaDto atividade : removidas) {
                        List<CompetenciaAtividade> vinculos = competenciaAtividadeRepository
                                        .findByAtividadeCodigo(atividade.codigo());

                        for (CompetenciaAtividade vinculo : vinculos) {
                                Competencia comp = competenciaRepository
                                                .findById(vinculo.getId().getCompetenciaCodigo())
                                                .orElse(null);

                                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                                        CompetenciaImpactoAcumulador acumulador = impactosMap
                                                        .computeIfAbsent(comp.getCodigo(),
                                                                        _ -> new CompetenciaImpactoAcumulador(
                                                                                        comp.getCodigo(),
                                                                                        comp.getDescricao()));

                                        acumulador.adicionarImpacto(
                                                        "Atividade removida: " + atividade.descricao());
                                }
                        }
                }

                // Processar atividades alteradas
                for (AtividadeImpactadaDto atividade : alteradas) {
                        List<CompetenciaAtividade> vinculos = competenciaAtividadeRepository
                                        .findByAtividadeCodigo(atividade.codigo());
                        for (CompetenciaAtividade vinculo : vinculos) {
                                Competencia comp = competenciaRepository
                                                .findById(vinculo.getId().getCompetenciaCodigo())
                                                .orElse(null);

                                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                                        CompetenciaImpactoAcumulador acumulador = impactosMap
                                                        .computeIfAbsent(comp.getCodigo(),
                                                                        _ -> new CompetenciaImpactoAcumulador(
                                                                                        comp.getCodigo(),
                                                                                        comp.getDescricao()));

                                        String detalhe = String.format(
                                                        "Atividade alterada: '%s' → '%s'",
                                                        atividade.descricaoAnterior(),
                                                        atividade.descricao());
                                        acumulador.adicionarImpacto(detalhe);
                                }
                        }
                }

                // Converter acumuladores para DTOs
                return impactosMap.values().stream()
                                .map(acc -> new CompetenciaImpactadaDto(
                                                acc.codigo,
                                                acc.descricao,
                                                new ArrayList<>(acc.atividadesAfetadas),
                                                determinarTipoImpacto(acc.atividadesAfetadas)))
                                .toList();
        }

        /**
         * Obtém nomes das competências vinculadas a uma atividade no mapa vigente.
         */
        private List<String> obterCompetenciasDaAtividade(Long atividadeId, Mapa mapaVigente) {
                return competenciaAtividadeRepository
                                .findByAtividadeCodigo(atividadeId)
                                .stream()
                                .map(ca -> {
                                        Competencia comp = competenciaRepository
                                                        .findById(ca.getId().getCompetenciaCodigo())
                                                        .orElse(null);
                                        // Filtrar apenas competências do mapa vigente
                                        if (comp != null && comp.getMapa().getCodigo()
                                                        .equals(mapaVigente.getCodigo())) {
                                                return comp.getDescricao();
                                        }
                                        return null;
                                })
                                .filter(Objects::nonNull)
                                .toList();
        }

        /**
         * Determina o tipo predominante de impacto baseado nas descrições.
         */
        private String determinarTipoImpacto(Set<String> atividadesAfetadas) {
                boolean temRemovida = atividadesAfetadas.stream()
                                .anyMatch(desc -> desc.contains("removida"));
                boolean temAlterada = atividadesAfetadas.stream()
                                .anyMatch(desc -> desc.contains("alterada"));

                if (temRemovida && temAlterada) {
                        return "ATIVIDADE_REMOVIDA_E_ALTERADA";
                } else if (temRemovida) {
                        return "ATIVIDADE_REMOVIDA";
                } else if (temAlterada) {
                        return "ATIVIDADE_ALTERADA";
                }
                return "IMPACTO_GENERICO";
        }

        /**
         * Classe auxiliar para acumular impactos em uma competência.
         */
        private static class CompetenciaImpactoAcumulador {
                final Long codigo;
                final String descricao;
                final Set<String> atividadesAfetadas = new LinkedHashSet<>();

                CompetenciaImpactoAcumulador(Long codigo, String descricao) {
                        this.codigo = codigo;
                        this.descricao = descricao;
                }

                void adicionarImpacto(String descricaoImpacto) {
                        atividadesAfetadas.add(descricaoImpacto);
                }
        }
}