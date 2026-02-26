package sgc.mapa.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço responsável por detectar impactos no mapa de competências causados
 * por alterações no cadastro de atividades durante processos de revisão.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImpactoMapaService {
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final MapaManutencaoService mapaManutencaoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final ComumRepo repo;

    /**
     * Realiza a verificação de impactos no mapa de competências, comparando o mapa
     * em revisão de um subprocesso com o mapa vigente da unidade.
     * <p>
     * Analisa as diferenças entre os dois mapas, identificando atividades
     * inseridas, removidas ou alteradas, e as competências que são afetadas por essas
     * mudanças.
     * <p>
     * O acesso a esta funcionalidade é restrito por perfil e pela situação atual do
     * subprocesso para garantir que a análise de impacto seja feita no momento correto do fluxo
     * de trabalho.
     */
    @Transactional(readOnly = true)
    public ImpactoMapaResponse verificarImpactos(Subprocesso subprocesso, Usuario usuario) {
        if (!permissionEvaluator.checkPermission(usuario, subprocesso, "VERIFICAR_IMPACTOS")) {
            throw new ErroAcessoNegado("Usuário não tem permissão para verificar impactos.");
        }

        checkSituacao(usuario, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo.findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());
        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaResponse.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = repo.buscar(Mapa.class, "subprocesso.codigo", subprocesso.getCodigo());
        List<Atividade> atividadesAtuais = obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);
        List<Competencia> competenciasMapa = competenciaRepo.findByMapa_Codigo(mapaVigente.getCodigo());

        Map<Long, List<Competencia>> atividadeIdToCompetencias = construirMapaAtividadeCompetencias(competenciasMapa);
        Map<String, Atividade> mapaVigentes = atividadesPorDescricao(atividadesVigentes);
        Map<String, Atividade> mapaAtuais = atividadesPorDescricao(atividadesAtuais);

        List<AtividadeImpactadaDto> inseridas = detectarInseridas(atividadesAtuais, mapaVigentes.keySet());
        List<AtividadeImpactadaDto> removidas = detectarRemovidas(mapaAtuais, atividadesVigentes, atividadeIdToCompetencias);
        List<AtividadeImpactadaDto> alteradas = detectarAlteradas(atividadesAtuais, mapaVigentes, atividadeIdToCompetencias);

        List<CompetenciaImpactadaDto> competenciasImpactadas = competenciasImpactadas(
                competenciasMapa, removidas, alteradas, atividadesVigentes);

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

    private void checkSituacao(Usuario usuario, Subprocesso sp) {
        SituacaoSubprocesso situacao = sp.getSituacao();
        Perfil perfil = usuario.getPerfilAtivo();

        boolean situacaoValida;

        // Regras de negócio migradas do SgcPermissionEvaluator (CDU-12)
        if (perfil == Perfil.CHEFE) {
            // CHEFE - Revisão do cadastro em andamento na sua unidade (ou NAO_INICIADO se aplicável)
            situacaoValida = (situacao == NAO_INICIADO || situacao == REVISAO_CADASTRO_EM_ANDAMENTO);
        } else if (perfil == Perfil.GESTOR) {
            // GESTOR - Revisão do cadastro disponibilizada
            // Nota: O checkHierarquia já garantiu que é da unidade/subordinada,
            // mas aqui validamos se o momento do fluxo é adequado para o Gestor ver impactos.
            // Para visualização (VERIFICAR_IMPACTOS), GESTOR também pode ver em outras fases se desejar?
            // A regra original era estrita:
            // "if (situacao == REVISAO_CADASTRO_DISPONIBILIZADA) { if (perfil == GESTOR ... "
            // Isso implicava que GESTOR só via impactos SE estivesse DISPONIBILIZADA.
            // Vamos manter a regra original para fidelidade.
            situacaoValida = (situacao == REVISAO_CADASTRO_DISPONIBILIZADA);
        } else if (perfil == Perfil.ADMIN) {
            // ADMIN - Várias fases
            situacaoValida = Set.of(NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO).contains(situacao);
        } else {
            situacaoValida = false;
        }

        if (!situacaoValida) {
            throw new ErroValidacao(
                    "Situação do subprocesso (%s) não permite verificação de impactos para o perfil %s."
                            .formatted(situacao, perfil));
        }
    }

    /**
     * Obtém todas as atividades associadas a um mapa, com seus conhecimentos.
     */
    private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        return mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
    }

    /**
     * Detecta atividades que foram inseridas no mapa atual em comparação com o
     * vigente.
     */
    private List<AtividadeImpactadaDto> detectarInseridas(List<Atividade> atuais, Set<String> descVigentes) {
        List<AtividadeImpactadaDto> inseridas = new ArrayList<>();
        for (Atividade atual : atuais) {
            if (!descVigentes.contains(atual.getDescricao())) {
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(atual.getCodigo())
                        .descricao(atual.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.INSERIDA)
                        .descricaoAnterior(atual.getDescricao())
                        .conhecimentos(atual.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
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
    private List<AtividadeImpactadaDto> detectarRemovidas(
            Map<String, Atividade> atuaisMap,
            List<Atividade> vigentes,
            Map<Long, List<Competencia>> competenciasVinculadas) {

        List<AtividadeImpactadaDto> removidas = new ArrayList<>();

        for (Atividade vigente : vigentes) {
            if (!atuaisMap.containsKey(vigente.getDescricao())) {
                Long vigenteCodigo = vigente.getCodigo();
                AtividadeImpactadaDto dto = AtividadeImpactadaDto.builder()
                        .codigo(vigenteCodigo)
                        .descricao(vigente.getDescricao())
                        .tipoImpacto(TipoImpactoAtividade.REMOVIDA)
                        .descricaoAnterior(vigente.getDescricao())
                        .conhecimentos(vigente.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                        .competenciasVinculadas(obterNomesCompetencias(vigenteCodigo, competenciasVinculadas))
                        .build();

                removidas.add(dto);
            }
        }
        return removidas;
    }

    /**
     * Detecta atividades que foram alteradas (em seus conhecimentos) no mapa atual
     * em comparação com o vigente.
     */
    private List<AtividadeImpactadaDto> detectarAlteradas(
            List<Atividade> atuais,
            Map<String, Atividade> vigentesMap,
            Map<Long, List<Competencia>> atividadeIdToCompetencias) {

        List<AtividadeImpactadaDto> alteradas = new ArrayList<>();

        for (Atividade atual : atuais) {
            if (vigentesMap.containsKey(atual.getDescricao())) {
                Atividade vigente = vigentesMap.get(atual.getDescricao());

                Collection<Conhecimento> conhecimentosAtuais = atual.getConhecimentos();
                Collection<Conhecimento> conhecimentosVigentes = vigente.getConhecimentos();

                if (conhecimentosDiferentes(conhecimentosAtuais, conhecimentosVigentes)) {
                    alteradas.add(
                            AtividadeImpactadaDto.builder()
                                    .codigo(atual.getCodigo())
                                    .descricao(atual.getDescricao())
                                    .tipoImpacto(TipoImpactoAtividade.ALTERADA)
                                    .descricaoAnterior(vigente.getDescricao())
                                    .conhecimentos(vigente.getConhecimentos().stream().map(Conhecimento::getDescricao).toList())
                                    .competenciasVinculadas(
                                            obterNomesCompetencias(vigente.getCodigo(), atividadeIdToCompetencias))
                                    .build());
                }
            }
        }
        return alteradas;
    }

    /**
     * Constrói um mapa de Atividades indexado pela descrição.
     */
    private Map<String, Atividade> atividadesPorDescricao(List<Atividade> atividades) {
        return atividades.stream().collect(Collectors.toMap(
                Atividade::getDescricao,
                atividade -> atividade,
                (existente, substituto) -> existente // Mantém a primeira ocorrência em caso de duplicata
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
                                                Map<Long, List<Competencia>> atividadeIdToCompetencias) {
        return atividadeIdToCompetencias.getOrDefault(codigoAtividade, List.of())
                .stream()
                .map(Competencia::getDescricao)
                .toList();
    }

    /**
     * Identifica quais competências foram impactadas pelas atividades removidas ou alteradas.
     */
    private List<CompetenciaImpactadaDto> competenciasImpactadas(
            List<Competencia> competenciasDoMapa,
            List<AtividadeImpactadaDto> removidas,
            List<AtividadeImpactadaDto> alteradas,
            List<Atividade> atividadesVigentes) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();

        Map<Long, List<Competencia>> atividadeIdToCompetencias = construirMapaAtividadeCompetencias(competenciasDoMapa);
        Map<String, Long> descricaoToVigenteId = atividadesVigentes.stream()
                .collect(Collectors.toMap(
                        Atividade::getDescricao,
                        Atividade::getCodigo,
                        (existing, replacement) -> existing));

        processarRemovidas(removidas, atividadeIdToCompetencias, mapaImpactos);
        processarAlteradas(alteradas, descricaoToVigenteId, atividadeIdToCompetencias, mapaImpactos);

        return mapaImpactos.values().stream()
                .map(this::converterParaDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void processarRemovidas(
            List<AtividadeImpactadaDto> removidas,
            Map<Long, List<Competencia>> atividadeIdToCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        removidas
                .forEach(dto -> {
                    List<Competencia> competenciasAfetadas = atividadeIdToCompetencias.getOrDefault(
                            dto.codigo(), List.of());
                    competenciasAfetadas.forEach(comp -> adicionarImpacto(mapaImpactos, comp,
                            "Atividade removida: %s".formatted(dto.descricao()),
                            TipoImpactoCompetencia.ATIVIDADE_REMOVIDA));
                });
    }

    private void processarAlteradas(
            List<AtividadeImpactadaDto> alteradas,
            Map<String, Long> descricaoToVigenteId,
            Map<Long, List<Competencia>> atividadeIdToCompetencias,
            Map<Long, CompetenciaImpactoAcumulador> mapaImpactos) {

        alteradas.stream()
                .filter(dto -> descricaoToVigenteId.containsKey(dto.descricao()))
                .forEach(dto -> {
                    Long idVigente = descricaoToVigenteId.get(dto.descricao());
                    List<Competencia> competenciasAfetadas = atividadeIdToCompetencias.getOrDefault(idVigente,
                            List.of());

                    competenciasAfetadas.forEach(comp -> {
                        String detalhe = "Atividade alterada: '%s' → '%s'".formatted(
                                dto.descricaoAnterior(), dto.descricao());
                        adicionarImpacto(mapaImpactos, comp, detalhe, TipoImpactoCompetencia.ATIVIDADE_ALTERADA);
                    });
                });
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

    /**
     * Constrói um mapa invertido de Atividade ID → Lista de Competências.
     */
    private Map<Long, List<Competencia>> construirMapaAtividadeCompetencias(List<Competencia> competencias) {
        Map<Long, List<Competencia>> mapa = new HashMap<>();
        for (Competencia comp : competencias) {
            for (Atividade ativ : comp.getAtividades()) {
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