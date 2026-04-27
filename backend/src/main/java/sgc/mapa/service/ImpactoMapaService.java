package sgc.mapa.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImpactoMapaService {
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final MapaManutencaoService mapaManutencaoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final UsuarioFacade usuarioFacade;

    @Transactional(readOnly = true)
    public ImpactoMapaResponse verificarImpactos(Subprocesso subprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        validarPermissaoVisualizacaoImpacto(subprocesso, usuario);

        return calcularImpactos(subprocesso);
    }

    @Transactional(readOnly = true)
    public ImpactoMapaResponse calcularImpactos(Subprocesso subprocesso) {
        if (subprocesso.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            log.debug("Processo de mapeamento não gera impactos de revisão");
            return ImpactoMapaResponse.semImpacto();
        }

        Optional<Mapa> mapaVigenteOpt = mapaRepo.buscarMapaVigentePorUnidade(subprocesso.getUnidade().getCodigo());
        if (mapaVigenteOpt.isEmpty()) {
            log.warn("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaResponse.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Long codSubprocesso = subprocesso.getCodigo();
        Mapa mapaSubprocesso = mapaRepo.buscarPorSubprocesso(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa (por subprocesso)", codSubprocesso));
        List<Atividade> atividadesAtuais = obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);
        List<Competencia> competenciasVigentes = competenciaRepo.findByMapa_Codigo(mapaVigente.getCodigo());
        Map<Long, List<Competencia>> codAtividadeParaCompetenciasVigentes = construirMapaAtividadeCompetencias(competenciasVigentes);
        Map<String, Atividade> mapaVigentes = atividadesPorDescricao(atividadesVigentes);
        Map<String, Atividade> mapaAtuais = atividadesPorDescricao(atividadesAtuais);

        List<AtividadeImpactadaDto> alteradas = detectarAlteradas(atividadesAtuais, mapaVigentes, codAtividadeParaCompetenciasVigentes);
        List<AtividadeImpactadaDto> inseridas = detectarInseridas(atividadesAtuais, mapaVigentes.keySet(), alteradas);
        List<AtividadeImpactadaDto> removidas = detectarRemovidas(mapaAtuais, atividadesVigentes, codAtividadeParaCompetenciasVigentes, alteradas);

        List<CompetenciaImpactadaDto> competenciasImpactadas = calcularImpactosCompetencias(
                competenciasVigentes, removidas, alteradas, atividadesVigentes);

        return ImpactoMapaResponse.builder()
                .temImpactos(!inseridas.isEmpty() || !removidas.isEmpty() || !alteradas.isEmpty())
                .inseridas(inseridas)
                .removidas(removidas)
                .alteradas(alteradas)
                .competenciasImpactadas(competenciasImpactadas)
                .totalInseridas(inseridas.size())
                .totalRemovidas(removidas.size())
                .totalAlteradas(alteradas.size())
                .totalCompetenciasImpactadas(competenciasImpactadas.size())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean podeVisualizarImpactos(Subprocesso subprocesso) {
        if (subprocesso.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            return false;
        }
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        return permissionEvaluator.verificarPermissao(usuario, subprocesso, VERIFICAR_IMPACTOS)
                && situacaoPermiteVisualizarImpactos(usuario.getPerfilAtivo(), subprocesso.getSituacao());
    }

    private void validarPermissaoVisualizacaoImpacto(Subprocesso subprocesso, Usuario usuario) {
        if (subprocesso.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            throw new ErroValidacao("Visualização de impactos disponível apenas para processos de revisão.");
        }

        if (!permissionEvaluator.verificarPermissao(usuario, subprocesso, VERIFICAR_IMPACTOS)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_VERIFICAR_IMPACTOS);
        }

        if (!situacaoPermiteVisualizarImpactos(usuario.getPerfilAtivo(), subprocesso.getSituacao())) {
            throw new ErroValidacao(
                    Mensagens.SITUACAO_IMPEDE_IMPACTO
                            .formatted(subprocesso.getSituacao(), usuario.getPerfilAtivo()));
        }
    }

    private boolean situacaoPermiteVisualizarImpactos(Perfil perfil, SituacaoSubprocesso situacao) {
        return switch (perfil) {
            case Perfil.CHEFE -> situacao == NAO_INICIADO || situacao == REVISAO_CADASTRO_EM_ANDAMENTO;
            case Perfil.GESTOR -> situacao == REVISAO_CADASTRO_DISPONIBILIZADA;
            case Perfil.ADMIN -> Set.of(
                    NAO_INICIADO,
                    REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_DISPONIBILIZADA,
                    REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO
            ).contains(situacao);
            default -> false;
        };
    }

    private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        return mapaManutencaoService.atividadesMapaCodigoComConhecimentos(mapa.getCodigo());
    }

    private List<AtividadeImpactadaDto> detectarInseridas(List<Atividade> atuais, Set<String> descVigentes, List<AtividadeImpactadaDto> alteradas) {
        Set<String> descAtuaisAlteradas = alteradas.stream()
                .map(AtividadeImpactadaDto::descricao)
                .collect(Collectors.toSet());

        List<AtividadeImpactadaDto> inseridas = new ArrayList<>();
        for (Atividade atual : atuais) {
            if (!descVigentes.contains(atual.getDescricao()) && !descAtuaisAlteradas.contains(atual.getDescricao())) {
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(atual.getCodigo())
                        .descricao(atual.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.INSERIDA)
                        .descricaoAnterior(atual.getDescricao())
                        .conhecimentos(atual.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                        .conhecimentosAdicionados(List.of())
                        .conhecimentosRemovidos(List.of())
                        .competenciasVinculadas(List.of())
                        .build();

                inseridas.add(dto);
            }
        }
        return inseridas;
    }

    private List<AtividadeImpactadaDto> detectarRemovidas(
            Map<String, Atividade> atuaisMap,
            List<Atividade> vigentes,
            Map<Long, List<Competencia>> competenciasVinculadas,
            List<AtividadeImpactadaDto> alteradas) {

        Set<String> descAnterioresAlteradas = alteradas.stream()
                .map(AtividadeImpactadaDto::descricaoAnterior)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<AtividadeImpactadaDto> removidas = new ArrayList<>();

        for (Atividade vigente : vigentes) {
            if (!atuaisMap.containsKey(vigente.getDescricao()) && !descAnterioresAlteradas.contains(vigente.getDescricao())) {
                Long vigenteCodigo = vigente.getCodigo();
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(vigenteCodigo)
                        .descricao(vigente.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.REMOVIDA)
                        .descricaoAnterior(vigente.getDescricao())
                        .conhecimentos(vigente.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                        .conhecimentosAdicionados(List.of())
                        .conhecimentosRemovidos(List.of())
                        .competenciasVinculadas(obterNomesCompetencias(vigenteCodigo, competenciasVinculadas))
                        .build();

                removidas.add(dto);
            }
        }
        return removidas;
    }
    private List<AtividadeImpactadaDto> detectarAlteradas(
            List<Atividade> atuais,
            Map<String, Atividade> vigentesMap,
            Map<Long, List<Competencia>> codAtividadeParaCompetenciasVigentes) {

        List<AtividadeImpactadaDto> alteradas = new ArrayList<>();
        Set<String> descAtuaisEncontradasNoVigente = new HashSet<>();
        Set<String> descVigentesEncontradasNoAtual = new HashSet<>();

        // 1. Alterações simples (mesma descrição, conhecimentos mudaram)
        for (Atividade atual : atuais) {
            if (vigentesMap.containsKey(atual.getDescricao())) {
                Atividade vigente = vigentesMap.get(atual.getDescricao());
                descAtuaisEncontradasNoVigente.add(atual.getDescricao());
                descVigentesEncontradasNoAtual.add(vigente.getDescricao());

                Collection<Conhecimento> conhecimentosAtuais = atual.getConhecimentos();
                Collection<Conhecimento> conhecimentosVigentes = vigente.getConhecimentos();

                if (conhecimentosDiferentes(conhecimentosAtuais, conhecimentosVigentes)) {
                    Set<String> descAtuais = conhecimentosAtuais.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
                    Set<String> descVigentes = conhecimentosVigentes.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());

                    List<String> adicionados = descAtuais.stream().filter(d -> !descVigentes.contains(d)).toList();
                    List<String> removidos = descVigentes.stream().filter(d -> !descAtuais.contains(d)).toList();

                    alteradas.add(AtividadeImpactadaDto.builder()
                                    .codigo(atual.getCodigo())
                                    .descricao(atual.getDescricao())
                                    .tipoImpacto(TipoImpactoAtividade.ALTERADA)
                                    .descricaoAnterior(vigente.getDescricao())
                                    .conhecimentos(vigente.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                                    .conhecimentosAdicionados(adicionados)
                                    .conhecimentosRemovidos(removidos)
                                    .competenciasVinculadas(obterNomesCompetencias(vigente.getCodigo(), codAtividadeParaCompetenciasVigentes))
                                    .build());
                }
            }
        }

        // 2. Detecção de mudança de descrição (Heurística baseada em competências vinculadas)
        List<Atividade> potenciaisInseridas = atuais.stream()
                .filter(a -> !descAtuaisEncontradasNoVigente.contains(a.getDescricao()))
                .toList();

        List<Atividade> potenciaisRemovidas = vigentesMap.values().stream()
                .filter(a -> !descVigentesEncontradasNoAtual.contains(a.getDescricao()))
                .toList();

        for (Atividade atual : potenciaisInseridas) {
            Set<String> descCompsAtuais = atual.getCompetencias().stream()
                    .map(Competencia::getDescricao)
                    .collect(Collectors.toSet());

            if (descCompsAtuais.isEmpty()) continue;

            for (Atividade vigente : potenciaisRemovidas) {
                List<Competencia> compsVigentes = codAtividadeParaCompetenciasVigentes.getOrDefault(vigente.getCodigo(), List.of());
                Set<String> descCompsVigentes = compsVigentes.stream()
                        .map(Competencia::getDescricao)
                        .collect(Collectors.toSet());

                // Se as competências batem perfeitamente, assumimos que é a mesma atividade com nome novo
                if (!descCompsVigentes.isEmpty() && descCompsVigentes.equals(descCompsAtuais)) {
                    Collection<Conhecimento> conhecimentosAtuais = atual.getConhecimentos();
                    Collection<Conhecimento> conhecimentosVigentes = vigente.getConhecimentos();
                    Set<String> dAtuais = conhecimentosAtuais.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
                    Set<String> dVigentes = conhecimentosVigentes.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
                    List<String> adicionados = dAtuais.stream().filter(d -> !dVigentes.contains(d)).toList();
                    List<String> removidos = dVigentes.stream().filter(d -> !dAtuais.contains(d)).toList();

                    alteradas.add(AtividadeImpactadaDto.builder()
                            .codigo(atual.getCodigo())
                            .descricao(atual.getDescricao())
                            .tipoImpacto(TipoImpactoAtividade.ALTERADA)
                            .descricaoAnterior(vigente.getDescricao())
                            .conhecimentos(vigente.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                            .conhecimentosAdicionados(adicionados)
                            .conhecimentosRemovidos(removidos)
                            .competenciasVinculadas(obterNomesCompetencias(vigente.getCodigo(), codAtividadeParaCompetenciasVigentes))
                            .build());
                    
                    descAtuaisEncontradasNoVigente.add(atual.getDescricao());
                    descVigentesEncontradasNoAtual.add(vigente.getDescricao());
                    break;
                }
            }
        }

        return alteradas;
    }

    private Map<String, Atividade> atividadesPorDescricao(List<Atividade> atividades) {
        return atividades.stream().collect(Collectors.toMap(
                Atividade::getDescricao,
                atividade -> atividade,
                (existente, substituto) -> existente
        ));
    }

    private boolean conhecimentosDiferentes(Collection<Conhecimento> lista1, Collection<Conhecimento> lista2) {
        if (lista1.size() != lista2.size()) return true;
        if (lista1.isEmpty()) return false;

        Set<String> descricoes1 = HashSet.newHashSet(lista1.size());
        for (Conhecimento c : lista1) {
            descricoes1.add(c.getDescricao());
        }

        Set<String> descricoes2 = HashSet.newHashSet(lista2.size());
        for (Conhecimento c : lista2) {
            descricoes2.add(c.getDescricao());
        }

        return !descricoes1.equals(descricoes2);
    }

    private List<String> obterNomesCompetencias(Long codigoAtividade,
                                                Map<Long, List<Competencia>> codAtividadeParaCompetencias) {
        return codAtividadeParaCompetencias.getOrDefault(codigoAtividade, List.of())
                .stream()
                .map(Competencia::getDescricao)
                .toList();
    }

    private void processarRemovidas(
            List<AtividadeImpactadaDto> removidas,
            Map<Long, List<Competencia>> codAtividadeParaCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        removidas
                .forEach(dto -> {
                    List<Competencia> competenciasAfetadas = codAtividadeParaCompetencias.getOrDefault(
                            dto.codigo(), List.of());
                    competenciasAfetadas.forEach(comp -> adicionarImpacto(mapaImpactos, comp,
                            "Atividade removida: %s".formatted(dto.descricao()),
                            TipoImpactoCompetencia.ATIVIDADE_REMOVIDA));
                });
    }

    private void processarAlteradas(
            List<AtividadeImpactadaDto> alteradas,
            Map<String, Long> descricaoParaCodVigente,
            Map<Long, List<Competencia>> codAtividadeParaCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        alteradas.stream()
                .filter(dto -> descricaoParaCodVigente.containsKey(dto.descricaoAnterior()))
                .forEach(dto -> {
                    Long codVigente = descricaoParaCodVigente.get(dto.descricaoAnterior());
                    List<Competencia> competenciasAfetadas = codAtividadeParaCompetencias.getOrDefault(codVigente,
                            List.of());

                    competenciasAfetadas.forEach(comp -> {
                        String detalheBase = "Atividade alterada: %s".formatted(dto.descricaoAnterior());
                        adicionarImpacto(mapaImpactos, comp, detalheBase, TipoImpactoCompetencia.ATIVIDADE_ALTERADA);

                        if (!dto.descricaoAnterior().equals(dto.descricao())) {
                            adicionarImpacto(mapaImpactos, comp, "Descrição alterada para %s".formatted(dto.descricao()), TipoImpactoCompetencia.ATIVIDADE_ALTERADA);
                        }

                        dto.conhecimentosAdicionados().forEach(c -> 
                            adicionarImpacto(mapaImpactos, comp, "Conhecimento %s adicionado".formatted(c), TipoImpactoCompetencia.ATIVIDADE_ALTERADA));
                        
                        dto.conhecimentosRemovidos().forEach(c -> 
                            adicionarImpacto(mapaImpactos, comp, "Conhecimento %s removido".formatted(c), TipoImpactoCompetencia.ATIVIDADE_ALTERADA));
                    });
                });
    }

    private List<CompetenciaImpactadaDto> calcularImpactosCompetencias(
            List<Competencia> vigentes,
            List<AtividadeImpactadaDto> atividadesRemovidas,
            List<AtividadeImpactadaDto> atividadesAlteradas,
            List<Atividade> atividadesVigentesReferencia) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();

        // Impactos por Atividades (Conforme CDU-12, impacto vem de remoção ou alteração de atividades vigentes)
        Map<Long, List<Competencia>> codAtividadeParaCompetenciasVigentes = construirMapaAtividadeCompetencias(vigentes);
        Map<String, Long> descricaoParaCodVigente = atividadesVigentesReferencia.stream()
                .collect(Collectors.toMap(
                        Atividade::getDescricao,
                        Atividade::getCodigo,
                        (existing, replacement) -> existing));

        processarRemovidas(atividadesRemovidas, codAtividadeParaCompetenciasVigentes, mapaImpactos);
        processarAlteradas(atividadesAlteradas, descricaoParaCodVigente, codAtividadeParaCompetenciasVigentes, mapaImpactos);

        return mapaImpactos.values().stream()
                .map(this::converterParaDto)
                .sorted(Comparator.comparing(CompetenciaImpactadaDto::descricao))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void adicionarImpacto(
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos,
            Competencia comp,
            String detalhe,
            TipoImpactoCompetencia tipoImpacto) {

        CompetenciaImpactoAcumulador acumulador = mapaImpactos.computeIfAbsent(
                comp.getCodigo(),
                x -> new CompetenciaImpactoAcumulador(comp.getCodigo(), comp.getDescricao()));

        acumulador.adicionarImpacto(detalhe, tipoImpacto);
    }

    private CompetenciaImpactadaDto converterParaDto(CompetenciaImpactoAcumulador acc) {
        return new CompetenciaImpactadaDto(
                acc.codigo,
                acc.descricao,
                new ArrayList<>(acc.atividadesAfetadas),
                acc.obterTiposImpacto());
    }

    private Map<Long, List<Competencia>> construirMapaAtividadeCompetencias(List<Competencia> competencias) {
        Map<Long, List<Competencia>> mapa = new HashMap<>();
        for (Competencia comp : competencias) {
            Set<Atividade> atividades = comp.getAtividades();
            if (atividades.isEmpty()) {
                continue;
            }
            for (Atividade ativ : atividades) {
                mapa.computeIfAbsent(ativ.getCodigo(), k -> new ArrayList<>()).add(comp);
            }
        }
        return mapa;
    }

    private static class CompetenciaImpactoAcumulador {
        private final Long codigo;
        private final String descricao;
        private final Set<String> atividadesAfetadas = new LinkedHashSet<>();
        private final Set<TipoImpactoCompetencia> tiposImpacto = new LinkedHashSet<>();

        public CompetenciaImpactoAcumulador(Long codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        void adicionarImpacto(String descricaoImpacto, TipoImpactoCompetencia tipoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
            tiposImpacto.add(tipoImpacto);
        }

        List<TipoImpactoCompetencia> obterTiposImpacto() {
            return new ArrayList<>(tiposImpacto);
        }
    }
}
