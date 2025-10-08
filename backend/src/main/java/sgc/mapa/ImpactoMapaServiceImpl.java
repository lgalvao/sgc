package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

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
        private final SubprocessoRepo repositorioSubprocesso;
        private final MapaRepo repositorioMapa;
        private final AtividadeRepo atividadeRepo;
        private final CompetenciaRepo repositorioCompetencia;
        private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

        /**
         * Compara cadastro atual do subprocesso com mapa vigente da unidade.
         * Detecta atividades inseridas, removidas ou alteradas.
         * Identifica competências impactadas.
         */
        @Override
        @Transactional(readOnly = true)
        public ImpactoMapaDto verificarImpactos(Long idSubprocesso) {
                log.info("Verificando impactos no mapa: subprocesso={}", idSubprocesso);

                Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: " + idSubprocesso));

                Optional<Mapa> mapaVigenteOpt = repositorioMapa
                                .findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

                if (mapaVigenteOpt.isEmpty()) {
                        log.info("Unidade sem mapa vigente, não há impactos a analisar");
                        return new ImpactoMapaDto(
                                        false, 0, 0, 0,
                                        List.of(), List.of(), List.of(), List.of());
                }

                Mapa mapaVigente = mapaVigenteOpt.get();

                Mapa mapaSubprocesso = repositorioMapa
                                .findBySubprocessoCodigo(idSubprocesso)
                                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado para subprocesso: " + idSubprocesso));

                List<Atividade> atividadesAtuais = atividadeRepo
                                .findByMapaCodigo(mapaSubprocesso.getCodigo());

                List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);

                List<AtividadeImpactadaDto> inseridas = detectarAtividadesInseridas(atividadesAtuais,
                                atividadesVigentes);
                List<AtividadeImpactadaDto> removidas = detectarAtividadesRemovidas(atividadesAtuais,
                                atividadesVigentes, mapaVigente);
                List<AtividadeImpactadaDto> alteradas = detectarAtividadesAlteradas(atividadesAtuais,
                                atividadesVigentes, mapaVigente);

                List<CompetenciaImpactadaDto> competenciasImpactadas = identificarCompetenciasImpactadas(mapaVigente,
                                removidas, alteradas);

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

        private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
                List<Competencia> competencias = repositorioCompetencia
                                .findByMapaCodigo(mapa.getCodigo());

                Set<Long> idsAtividades = new HashSet<>();
                for (Competencia comp : competencias) {
                        List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade
                                        .findByCompetenciaCodigo(comp.getCodigo());
                        for (CompetenciaAtividade vinculo : vinculos) {
                                idsAtividades.add(vinculo.getId().getAtividadeCodigo());
                        }
                }

                if (idsAtividades.isEmpty()) {
                        return List.of();
                }
                return atividadeRepo.findAllById(idsAtividades);
        }

        private List<AtividadeImpactadaDto> detectarAtividadesInseridas(List<Atividade> atuais,
                        List<Atividade> vigentes) {
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
                                                List.of()
                                ))
                                .toList();
        }

        private List<AtividadeImpactadaDto> detectarAtividadesRemovidas(
                        List<Atividade> atuais,
                        List<Atividade> vigentes,
                        Mapa mapaVigente) {
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

        private List<AtividadeImpactadaDto> detectarAtividadesAlteradas(
                        List<Atividade> atuais,
                        List<Atividade> vigentes,
                        Mapa mapaVigente) {
                Map<Long, String> mapaVigentes = vigentes.stream()
                                .collect(Collectors.toMap(
                                                Atividade::getCodigo,
                                                Atividade::getDescricao));

                return atuais.stream()
                                .filter(a -> mapaVigentes.containsKey(a.getCodigo()))
                                .filter(a -> !mapaVigentes.get(a.getCodigo())
                                                .equalsIgnoreCase(a.getDescricao().trim()))
                                .map(a -> new AtividadeImpactadaDto(
                                                a.getCodigo(),
                                                a.getDescricao(),
                                                "ALTERADA",
                                                mapaVigentes.get(a.getCodigo()),
                                                obterCompetenciasDaAtividade(a.getCodigo(), mapaVigente)))
                                .toList();
        }

        private List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
                        Mapa mapaVigente,
                        List<AtividadeImpactadaDto> removidas,
                        List<AtividadeImpactadaDto> alteradas) {
                Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();

                for (AtividadeImpactadaDto atividade : removidas) {
                        List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade
                                        .findByAtividadeCodigo(atividade.codigo());

                        for (CompetenciaAtividade vinculo : vinculos) {
                                Competencia comp = repositorioCompetencia
                                                .findById(vinculo.getId().getCompetenciaCodigo())
                                                .orElse(null);

                                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                                        CompetenciaImpactoAcumulador acumulador = mapaImpactos
                                                        .computeIfAbsent(comp.getCodigo(),
                                                                        _ -> new CompetenciaImpactoAcumulador(
                                                                                        comp.getCodigo(),
                                                                                        comp.getDescricao()));

                                        acumulador.adicionarImpacto(
                                                        "Atividade removida: " + atividade.descricao());
                                }
                        }
                }

                for (AtividadeImpactadaDto atividade : alteradas) {
                        List<CompetenciaAtividade> vinculos = repositorioCompetenciaAtividade
                                        .findByAtividadeCodigo(atividade.codigo());
                        for (CompetenciaAtividade vinculo : vinculos) {
                                Competencia comp = repositorioCompetencia
                                                .findById(vinculo.getId().getCompetenciaCodigo())
                                                .orElse(null);

                                if (comp != null && comp.getMapa().getCodigo().equals(mapaVigente.getCodigo())) {
                                        CompetenciaImpactoAcumulador acumulador = mapaImpactos
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

                return mapaImpactos.values().stream()
                                .map(acc -> new CompetenciaImpactadaDto(
                                                acc.codigo,
                                                acc.descricao,
                                                new ArrayList<>(acc.atividadesAfetadas),
                                                determinarTipoImpacto(acc.atividadesAfetadas)))
                                .toList();
        }

        private List<String> obterCompetenciasDaAtividade(Long idAtividade, Mapa mapaVigente) {
                return repositorioCompetenciaAtividade
                                .findByAtividadeCodigo(idAtividade)
                                .stream()
                                .map(ca -> {
                                        Competencia comp = repositorioCompetencia
                                                        .findById(ca.getId().getCompetenciaCodigo())
                                                        .orElse(null);
                                        if (comp != null && comp.getMapa().getCodigo()
                                                        .equals(mapaVigente.getCodigo())) {
                                                return comp.getDescricao();
                                        }
                                        return null;
                                })
                                .filter(Objects::nonNull)
                                .toList();
        }

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