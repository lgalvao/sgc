package sgc.mapa.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.TipoImpactoAtividade;
import sgc.mapa.model.TipoImpactoCompetencia;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.*;
import java.util.stream.Collectors;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço responsável por detectar impactos no mapa de competências causados por alterações no
 * cadastro de atividades durante processos de revisão.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImpactoMapaService {
    private static final String MSG_ERRO_CHEFE = """
            O chefe da unidade só pode verificar os impactos com o subprocesso na situação\
             'Revisão do cadastro em andamento'.""";
    private static final String MSG_ERRO_GESTOR = """
            O gestor só pode verificar os impactos com o subprocesso na situação 'Revisão do\
             cadastro disponibilizada'.""";
    private static final String MSG_ERRO_ADMIN = """
            O administrador só pode verificar os impactos com o subprocesso na situação 'Revisão\
             do cadastro disponibilizada', 'Revisão do cadastro homologada' ou 'Mapa\
             Ajustado'.""";

    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;

    /**
     * Realiza a verificação de impactos no mapa de competências, comparando o mapa em revisão de um
     * subprocesso com o mapa vigente da unidade.
     *
     * <p>Este método implementa a lógica do CDU-12. Ele analisa as diferenças entre os dois mapas,
     * identificando atividades inseridas, removidas ou alteradas, e as competências que são
     * afetadas por essas mudanças.
     *
     * <p>O acesso a esta funcionalidade é restrito por perfil e pela situação atual do subprocesso
     * para garantir que a análise de impacto seja feita no momento correto do fluxo de trabalho.
     *
     * @param codSubprocesso O código do subprocesso cujo mapa será analisado.
     * @param usuario        O usuário autenticado que realiza a operação.
     * @return Um {@link ImpactoMapaDto} que encapsula todos os impactos encontrados. Retorna um DTO
     * sem impactos se a unidade não possuir um mapa vigente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou seu mapa não forem encontrados.
     * @throws ErroAccessoNegado         se o usuário não tiver permissão para executar a operação na
     *                                   situação atual do subprocesso.
     */
    @Transactional(readOnly = true)
    public ImpactoMapaDto verificarImpactos(Long codSubprocesso, Usuario usuario) {
        log.debug("Verificando impactos no mapa: subprocesso={}", codSubprocesso);

        Subprocesso subprocesso =
                subprocessoRepo
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        verificarAcesso(usuario, subprocesso);

        Optional<Mapa> mapaVigenteOpt = mapaRepo.findMapaVigenteByUnidade(subprocesso.getUnidade().getCodigo());

        if (mapaVigenteOpt.isEmpty()) {
            log.info("Unidade sem mapa vigente, não há impactos a analisar");
            return ImpactoMapaDto.semImpacto();
        }

        Mapa mapaVigente = mapaVigenteOpt.get();
        Mapa mapaSubprocesso = mapaRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Mapa não encontrado para subprocesso", codSubprocesso)
                );

        List<Atividade> atividadesAtuais = obterAtividadesDoMapa(mapaSubprocesso);
        List<Atividade> atividadesVigentes = obterAtividadesDoMapa(mapaVigente);

        List<AtividadeImpactadaDto> inseridas =
                detectarAtividadesInseridas(atividadesAtuais, atividadesVigentes);

        List<AtividadeImpactadaDto> removidas =
                detectarAtividadesRemovidas(atividadesAtuais, atividadesVigentes, mapaVigente);

        List<AtividadeImpactadaDto> alteradas =
                detectarAtividadesAlteradas(atividadesAtuais, atividadesVigentes, mapaVigente);

        List<CompetenciaImpactadaDto> competenciasImpactadas =
                identificarCompetenciasImpactadas(
                        mapaVigente, removidas, alteradas, atividadesVigentes);

        ImpactoMapaDto impactos =
                ImpactoMapaDto.comImpactos(inseridas, removidas, alteradas, competenciasImpactadas);

        log.info(
                "Análise de impactos concluída: tem={}, inseridas={} removidas={}, alteradas={}",
                impactos.isTemImpactos(),
                impactos.getTotalAtividadesInseridas(),
                impactos.getTotalAtividadesRemovidas(),
                impactos.getTotalAtividadesAlteradas());

        return impactos;
    }

    // ========================================================================================
    // Métodos de verificação de acesso
    // ========================================================================================

    private void verificarAcesso(Usuario usuario, Subprocesso subprocesso) {
        final SituacaoSubprocesso situacao = subprocesso.getSituacao();

        if (hasRole(usuario, "CHEFE")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_EM_ANDAMENTO, NAO_INICIADO), MSG_ERRO_CHEFE);
        } else if (hasRole(usuario, "GESTOR")) {
            validarSituacao(situacao, List.of(REVISAO_CADASTRO_DISPONIBILIZADA), MSG_ERRO_GESTOR);
        } else if (hasRole(usuario, "ADMIN")) {
            validarSituacao(
                    situacao,
                    List.of(
                            REVISAO_CADASTRO_DISPONIBILIZADA,
                            REVISAO_CADASTRO_HOMOLOGADA,
                            REVISAO_MAPA_AJUSTADO),
                    MSG_ERRO_ADMIN);
        }
    }

    private void validarSituacao(
            SituacaoSubprocesso atual, List<SituacaoSubprocesso> esperadas, String mensagemErro) {
        if (!esperadas.contains(atual)) throw new ErroAccessoNegado(mensagemErro);
    }

    private boolean hasRole(Usuario usuario, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal().equals(usuario)) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_%s".formatted(role)));
        }
        return usuario.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_%s".formatted(role)));
    }

    // ========================================================================================
    // Métodos de detecção de impacto em atividades (anteriormente em ImpactoAtividadeService)
    // ========================================================================================

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
    private List<Atividade> obterAtividadesDoMapa(Mapa mapa) {
        log.debug("Buscando atividades e conhecimentos para o mapa {}", mapa.getCodigo());
        return atividadeRepo.findByMapaCodigoWithConhecimentos(mapa.getCodigo());
    }

    private List<AtividadeImpactadaDto> detectarAtividadesInseridas(
            List<Atividade> atuais, List<Atividade> vigentes) {
        Set<String> descricoesVigentes =
                vigentes.stream().map(Atividade::getDescricao).collect(Collectors.toSet());
        List<AtividadeImpactadaDto> inseridas = new ArrayList<>();

        for (Atividade atual : atuais) {
            if (!descricoesVigentes.contains(atual.getDescricao())) {
                inseridas.add(
                        AtividadeImpactadaDto.builder()
                                .codigo(atual.getCodigo())
                                .descricao(atual.getDescricao())
                                .tipoImpacto(TipoImpactoAtividade.INSERIDA)
                                .descricaoAnterior(null)
                                .competenciasVinculadas(List.of())
                                .build());
            }
        }
        return inseridas;
    }

    private List<AtividadeImpactadaDto> detectarAtividadesRemovidas(
            List<Atividade> atuais, List<Atividade> vigentes, Mapa mapaVigente) {

        List<AtividadeImpactadaDto> removidas = new ArrayList<>();
        Map<String, Atividade> atuaisMap = mapAtividadesByDescricao(atuais);

        for (Atividade vigente : vigentes) {
            if (!atuaisMap.containsKey(vigente.getDescricao())) {
                AtividadeImpactadaDto atividadeImpactadaDto =
                        AtividadeImpactadaDto.builder()
                                .codigo(vigente.getCodigo())
                                .descricao(vigente.getDescricao())
                                .tipoImpacto(TipoImpactoAtividade.REMOVIDA)
                                .descricaoAnterior(null)
                                .competenciasVinculadas(
                                        obterCompetenciasDaAtividade(vigente, mapaVigente))
                                .build();

                removidas.add(atividadeImpactadaDto);
            }
        }
        return removidas;
    }

    private List<AtividadeImpactadaDto> detectarAtividadesAlteradas(
            List<Atividade> atuais, List<Atividade> vigentes, Mapa mapaVigente) {
        List<AtividadeImpactadaDto> alteradas = new ArrayList<>();
        Map<String, Atividade> vigentesMap = mapAtividadesByDescricao(vigentes);

        for (Atividade atual : atuais) {
            if (vigentesMap.containsKey(atual.getDescricao())) {
                Atividade vigente = vigentesMap.get(atual.getDescricao());

                // Optimização: Usar listas já carregadas em memória para evitar N+1 queries
                List<Conhecimento> conhecimentosAtuais = atual.getConhecimentos();
                List<Conhecimento> conhecimentosVigentes = vigente.getConhecimentos();

                if (conhecimentosDiferentes(conhecimentosAtuais, conhecimentosVigentes)) {
                    alteradas.add(
                            AtividadeImpactadaDto.builder()
                                    .codigo(atual.getCodigo())
                                    .descricao(atual.getDescricao())
                                    .tipoImpacto(TipoImpactoAtividade.ALTERADA)
                                    .descricaoAnterior(
                                            "Descrição ou conhecimentos associados alterados.")
                                    .competenciasVinculadas(
                                            obterCompetenciasDaAtividade(vigente, mapaVigente))
                                    .build());
                }
            }
        }
        return alteradas;
    }

    private boolean conhecimentosDiferentes(List<Conhecimento> lista1, List<Conhecimento> lista2) {
        if (lista1.size() != lista2.size()) return true;

        Set<String> descricoes1 =
                lista1.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());
        Set<String> descricoes2 =
                lista2.stream().map(Conhecimento::getDescricao).collect(Collectors.toSet());

        return !descricoes1.equals(descricoes2);
    }

    // ========================================================================================
    // Métodos de detecção de impacto em competências (anteriormente em ImpactoCompetenciaService)
    // ========================================================================================

    private List<CompetenciaImpactadaDto> identificarCompetenciasImpactadas(
            Mapa mapaVigente,
            List<AtividadeImpactadaDto> removidas,
            List<AtividadeImpactadaDto> alteradas,
            List<Atividade> atividadesVigentes) {

        Map<Long, CompetenciaImpactoAcumulador> mapaImpactos = new HashMap<>();
        List<Competencia> competenciasDoMapa =
                competenciaRepo.findByMapaCodigo(mapaVigente.getCodigo());

        // Indexar competências por ID da atividade (Invertendo o relacionamento para busca O(1))
        Map<Long, List<Competencia>> atividadeIdToCompetencias = new HashMap<>();
        for (Competencia comp : competenciasDoMapa) {
            for (Atividade ativ : comp.getAtividades()) {
                atividadeIdToCompetencias
                        .computeIfAbsent(ativ.getCodigo(), k -> new ArrayList<>())
                        .add(comp);
            }
        }

        // Indexar IDs das atividades vigentes por descrição para lookup rápido
        Map<String, Long> descricaoToVigenteId =
                atividadesVigentes.stream()
                        .collect(Collectors.toMap(Atividade::getDescricao, Atividade::getCodigo));

        // Processar Atividades Removidas (já possuem ID da atividade vigente)
        for (AtividadeImpactadaDto atividadeDto : removidas) {
            if (atividadeDto.getCodigo() == null) continue;

            List<Competencia> competenciasAfetadas =
                    atividadeIdToCompetencias.getOrDefault(
                            atividadeDto.getCodigo(), Collections.emptyList());

            for (Competencia comp : competenciasAfetadas) {
                CompetenciaImpactoAcumulador acumulador =
                        mapaImpactos.computeIfAbsent(
                                comp.getCodigo(),
                                x ->
                                        CompetenciaImpactoAcumulador.builder()
                                                .codigo(comp.getCodigo())
                                                .descricao(comp.getDescricao())
                                                .build());

                acumulador.adicionarImpacto(
                        "Atividade removida: %s".formatted(atividadeDto.getDescricao()));
            }
        }

        // Processar Atividades Alteradas (possuem ID da atividade atual/nova)
        for (AtividadeImpactadaDto atividadeDto : alteradas) {
            // A atividade alterada tem o ID novo, mas precisamos encontrar as competências vigentes.
            // Como a alteração é detectada por descrição (mesma descrição), usamos a descrição para achar a vigente.
            String descricao = atividadeDto.getDescricao();
            if (descricao == null) continue;

            Long idVigente = descricaoToVigenteId.get(descricao);
            if (idVigente == null) continue;

            List<Competencia> competenciasAfetadas =
                    atividadeIdToCompetencias.getOrDefault(idVigente, Collections.emptyList());

            for (Competencia comp : competenciasAfetadas) {
                CompetenciaImpactoAcumulador acumulador =
                        mapaImpactos.computeIfAbsent(
                                comp.getCodigo(),
                                x ->
                                        CompetenciaImpactoAcumulador.builder()
                                                .codigo(comp.getCodigo())
                                                .descricao(comp.getDescricao())
                                                .build());

                String detalhe =
                        String.format(
                                "Atividade alterada: '%s' → '%s'",
                                atividadeDto.getDescricaoAnterior(),
                                atividadeDto.getDescricao());
                acumulador.adicionarImpacto(detalhe);
            }
        }

        return mapaImpactos.values().stream()
                .map(
                        acc ->
                                new CompetenciaImpactadaDto(
                                        acc.codigo,
                                        acc.descricao,
                                        new ArrayList<>(acc.atividadesAfetadas),
                                        TipoImpactoCompetencia.valueOf(
                                                determinarTipoImpacto(acc.atividadesAfetadas))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> obterCompetenciasDaAtividade(Atividade atividade, Mapa mapaVigente) {
        if (atividade == null) return Collections.emptyList();

        // Otimização: Recebe a entidade já carregada para evitar busca redundante por ID
        return atividade.getCompetencias().stream()
                .filter(c -> c.getMapa().getCodigo().equals(mapaVigente.getCodigo()))
                .map(Competencia::getDescricao)
                .toList();
    }

    private String determinarTipoImpacto(Set<String> atividadesAfetadas) {
        boolean temRemovida =
                atividadesAfetadas.stream().anyMatch(desc -> desc.contains("removida"));

        boolean temAlterada =
                atividadesAfetadas.stream().anyMatch(desc -> desc.contains("alterada"));

        if (temRemovida && temAlterada) {
            return "IMPACTO_GENERICO";
        } else if (temRemovida) {
            return "ATIVIDADE_REMOVIDA";
        } else if (temAlterada) {
            return "ATIVIDADE_ALTERADA";
        }
        return "IMPACTO_GENERICO";
    }

    // ========================================================================================
    // Classes internas
    // ========================================================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CompetenciaImpactoAcumulador {
        private Long codigo;
        private String descricao;
        @Builder.Default
        private Set<String> atividadesAfetadas = new LinkedHashSet<>();

        /* default */ void adicionarImpacto(String descricaoImpacto) {
            atividadesAfetadas.add(descricaoImpacto);
        }
    }
}
