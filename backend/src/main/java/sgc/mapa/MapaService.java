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
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import org.owasp.html.PolicyFactory;
import org.owasp.html.HtmlPolicyBuilder;
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
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interface do serviço de negócio para gerenciar Mapas de Competências.
 * <p>
 * Provê operações de alto nível para manipular mapas completos,
 * incluindo competências e vínculos com atividades de forma agregada.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final MapaRepo repositorioMapa;
    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;
    private final AtividadeRepo atividadeRepo;
    private final SubprocessoRepo repositorioSubprocesso;
    private final MapaVisualizacaoService mapaVisualizacaoService;

    /**
     * Obtém um mapa completo com todas as competências e atividades vinculadas.
     *
     * @param idMapa Código do mapa
     * @throws ErroDominioNaoEncontrado se o mapa não existir
     */
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long idMapa) {
        log.debug("Obtendo mapa completo: id={}", idMapa);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(idMapa)));

        Long idSubprocesso = buscarSubprocessoDoMapa(idMapa);
        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(idMapa);

        List<CompetenciaMapaDto> competenciasDto = competencias.stream()
                .map(c -> {
                    List<Long> idsAtividades = repositorioCompetenciaAtividade
                            .findByCompetenciaCodigo(c.getCodigo())
                            .stream()
                            .map(ca -> ca.getId().getAtividadeCodigo())
                            .toList();

                    return new CompetenciaMapaDto(
                            c.getCodigo(),
                            c.getDescricao(),
                            idsAtividades
                    );
                })
                .toList();

        return new MapaCompletoDto(
                mapa.getCodigo(),
                idSubprocesso,
                mapa.getObservacoesDisponibilizacao(),
                competenciasDto
        );
    }

    /**
     * Salva um mapa completo de forma atômica.
     * <p>
     * Operação transacional que:
     * - Atualiza observações do mapa
     * - Remove competências excluídas
     * - Cria novas competências
     * - Atualiza competências existentes
     * - Atualiza todos os vínculos com atividades
     *
     * @param idMapa                 Código do mapa a ser atualizado
     * @param request                Request com dados do mapa completo
     * @param usuarioTituloEleitoral Título do usuário que está salvando (para auditoria)
     * @return DTO com o mapa completo atualizado
     * @throws ErroDominioNaoEncontrado se o mapa não existir
     */
    public MapaCompletoDto salvarMapaCompleto(Long idMapa, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: id={}, usuario={}", idMapa, usuarioTituloEleitoral);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa não encontrado: %d".formatted(idMapa)));

        // Sanitize the observations before saving
        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.observacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = repositorioMapa.save(mapa);

        List<Competencia> competenciasAtuais = repositorioCompetencia.findByMapaCodigo(idMapa);
        Set<Long> idsAtuais = competenciasAtuais.stream()
                .map(Competencia::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> idsNovos = request.competencias().stream()
                .map(CompetenciaMapaDto::codigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> idsParaRemover = new HashSet<>(idsAtuais);
        idsParaRemover.removeAll(idsNovos);

        for (Long idParaRemover : idsParaRemover) {
            repositorioCompetenciaAtividade.deleteByCompetenciaCodigo(idParaRemover);
            repositorioCompetencia.deleteById(idParaRemover);
            log.debug("Competência {} removida do mapa {}", idParaRemover, idMapa);
        }

        for (CompetenciaMapaDto compDto : request.competencias()) {
            Competencia competencia;
            if (compDto.codigo() == null) {
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.descricao());
                competencia = repositorioCompetencia.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                competencia = repositorioCompetencia.findById(compDto.codigo())
                        .orElseThrow(() -> new ErroDominioNaoEncontrado("Competência não encontrada: %d".formatted(compDto.codigo())));
                competencia.setDescricao(compDto.descricao());
                competencia = repositorioCompetencia.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }

        validarIntegridadeMapa(idMapa);
        return obterMapaCompleto(idMapa);
    }

    /**
     * Obtém o mapa de um subprocesso.
     *
     * @param idSubprocesso Código do subprocesso
     * @return DTO com o mapa completo do subprocesso
     * @throws ErroDominioNaoEncontrado se o subprocesso ou mapa não existir
     */
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaSubprocesso(Long idSubprocesso) {
        log.debug("Obtendo mapa do subprocesso: id={}", idSubprocesso);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }

        return obterMapaCompleto(subprocesso.getMapa().getCodigo());
    }

    /**
     * Salva o mapa de um subprocesso.
     * <p>
     * Se for a primeira vez que competências são criadas,
     * atualiza a situação do subprocesso para MAPA_CRIADO.
     *
     * @param idSubprocesso          Código do subprocesso
     * @param request                Request com dados do mapa completo
     * @param usuarioTituloEleitoral Título do usuário que está salvando (para auditoria)
     * @return DTO com o mapa completo atualizado
     * @throws ErroDominioNaoEncontrado se o subprocesso ou mapa não existir
     * @throws IllegalStateException     se a situação do subprocesso não permitir a operação
     */
    public MapaCompletoDto salvarMapaSubprocesso(Long idSubprocesso, SalvarMapaRequest request, Long usuarioTituloEleitoral) {
        log.info("Salvando mapa do subprocesso: idSubprocesso={}, usuario={}", idSubprocesso, usuarioTituloEleitoral);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.CADASTRO_HOMOLOGADO && situacao != SituacaoSubprocesso.MAPA_CRIADO) {
            throw new IllegalStateException("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }

        Long idMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(idMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = salvarMapaCompleto(idMapa, request, usuarioTituloEleitoral);

        if (eraVazio && temNovasCompetencias && situacao == SituacaoSubprocesso.CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", idSubprocesso);
        }

        return mapaDto;
    }

    /**
     * Valida se o mapa está completo e pronto para ser disponibilizado.
     * <p>
     * Verifica:
     * - Todas competências têm pelo menos uma atividade vinculada
     * - Todas atividades estão vinculadas a pelo menos uma competência
     *
     * @param idMapa Código do mapa
     * @throws IllegalStateException se o mapa não estiver válido
     */
    @Transactional(readOnly = true)
    public void validarMapaCompleto(Long idMapa) {
        log.debug("Validando integridade do mapa: idMapa={}", idMapa);

        MapaCompletoDto mapa = obterMapaCompleto(idMapa);

        for (CompetenciaMapaDto comp : mapa.competencias()) {
            if (comp.atividadesCodigos().isEmpty()) {
                throw new IllegalStateException("A competência '%s' não possui atividades vinculadas".formatted(comp.descricao()));
            }
        }

        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);

        for (Atividade atividade : atividades) {
            boolean temVinculo = repositorioCompetenciaAtividade.existsByAtividadeCodigo(atividade.getCodigo());
            if (!temVinculo) {
                throw new IllegalStateException("A atividade '%s' não está vinculada a nenhuma competência".formatted(atividade.getDescricao()));
            }
        }
        log.debug("Mapa {} validado com sucesso", idMapa);
    }

    private void atualizarVinculosAtividades(Long idCompetencia, List<Long> novosIdsAtividades) {
        List<CompetenciaAtividade> vinculosAtuais = repositorioCompetenciaAtividade.findByCompetenciaCodigo(idCompetencia);
        Set<Long> idsAtuais = vinculosAtuais.stream()
                .map(v -> v.getId().getAtividadeCodigo())
                .collect(Collectors.toSet());

        Set<Long> novosIds = new HashSet<>(novosIdsAtividades);

        // Remover os que não estão na nova lista
        vinculosAtuais.stream()
                .filter(v -> !novosIds.contains(v.getId().getAtividadeCodigo()))
                .forEach(repositorioCompetenciaAtividade::delete);

        // Adicionar os que não estão na lista atual
        Competencia competencia = repositorioCompetencia.findById(idCompetencia).orElseThrow();
        novosIds.stream()
                .filter(id -> !idsAtuais.contains(id))
                .forEach(idAtividade -> atividadeRepo.findById(idAtividade).ifPresent(atividade -> {
                    CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(idAtividade, idCompetencia);
                    CompetenciaAtividade vinculo = new CompetenciaAtividade(id, competencia, atividade);
                    repositorioCompetenciaAtividade.save(vinculo);
                }));

        log.debug("Atualizados {} vínculos para competência {}", novosIdsAtividades.size(), idCompetencia);
    }

    private void validarIntegridadeMapa(Long idMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);
        List<Competencia> competencias = repositorioCompetencia.findByMapaCodigo(idMapa);

        for (Atividade atividade : atividades) {
            if (!repositorioCompetenciaAtividade.existsByAtividadeCodigo(atividade.getCodigo())) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", atividade.getCodigo(), idMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (repositorioCompetenciaAtividade.findByCompetenciaCodigo(competencia.getCodigo()).isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", competencia.getCodigo(), idMapa);
            }
        }
    }

    private Long buscarSubprocessoDoMapa(Long idMapa) {
        return repositorioSubprocesso.findByMapaCodigo(idMapa)
                .map(Subprocesso::getCodigo)
                .orElse(null);
    }


}