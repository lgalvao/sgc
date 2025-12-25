package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder().toFactory();

    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaCompletoMapper mapaCompletoMapper;

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorCodigo(Long codigo) {
        return mapaRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public Mapa criar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    public Mapa atualizar(Long codigo, Mapa mapa) {
        return mapaRepo.findById(codigo)
                .map(
                        existente -> {
                            existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                            existente.setObservacoesDisponibilizacao(
                                    mapa.getObservacoesDisponibilizacao());
                            existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
                            return mapaRepo.save(existente);
                        })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", codigo));
    }

    public void excluir(Long codigo) {
        if (!mapaRepo.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Mapa", codigo);
        }
        mapaRepo.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long codMapa, Long codSubprocesso) {
        log.debug("Obtendo mapa completo: codigo={}, codSubprocesso={}", codMapa, codSubprocesso);

        Mapa mapa =
                mapaRepo.findById(codMapa)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado: %d".formatted(codMapa)));

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        return mapaCompletoMapper.toDto(mapa, codSubprocesso, competencias);
    }

    public MapaCompletoDto salvarMapaCompleto(
            Long codMapa, SalvarMapaRequest request, String usuarioTituloEleitoral) {
        log.info("Salvando mapa completo: codigo={}, usuario={}", codMapa, usuarioTituloEleitoral);

        Mapa mapa =
                mapaRepo.findById(codMapa)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Mapa não encontrado: %d".formatted(codMapa)));

        var sanitizedObservacoes = HTML_SANITIZER_POLICY.sanitize(request.getObservacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapa = mapaRepo.save(mapa);

        List<Competencia> competenciasAtuais = competenciaRepo.findByMapaCodigo(codMapa);
        Set<Long> idsAtuais =
                competenciasAtuais.stream().map(Competencia::getCodigo).collect(Collectors.toSet());

        Set<Long> idsNovos =
                request.getCompetencias().stream()
                        .map(CompetenciaMapaDto::getCodigo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        Set<Long> codsParaRemover = new HashSet<>(idsAtuais);
        codsParaRemover.removeAll(idsNovos);

        for (Long codParaRemover : codsParaRemover) {
            competenciaRepo.deleteById(codParaRemover);
            log.debug("Competência {} removida do mapa {}", codParaRemover, codMapa);
        }

        for (CompetenciaMapaDto compDto : request.getCompetencias()) {
            Competencia competencia;
            if (compDto.getCodigo() == null) {
                competencia = new Competencia();
                competencia.setMapa(mapa);
                competencia.setDescricao(compDto.getDescricao());
                competencia = competenciaRepo.save(competencia);
                log.debug("Nova competência criada: {}", competencia.getCodigo());
            } else {
                competencia =
                        competenciaRepo
                                .findById(compDto.getCodigo())
                                .orElseThrow(
                                        () ->
                                                new ErroEntidadeNaoEncontrada(
                                                        "Competência não encontrada: %d"
                                                                .formatted(compDto.getCodigo())));

                competencia.setDescricao(compDto.getDescricao());
                competencia = competenciaRepo.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            atualizarVinculosAtividades(competencia.getCodigo(), compDto.getAtividadesCodigos());
        }

        validarIntegridadeMapa(codMapa);

        List<Competencia> competenciasFinais = competenciaRepo.findByMapaCodigo(codMapa);

        return mapaCompletoMapper.toDto(mapa, null, competenciasFinais);
    }

    // ========================================================================================
    // Métodos auxiliares (anteriormente em MapaVinculoService e MapaIntegridadeService)
    // ========================================================================================

    /**
     * Sincroniza os vínculos entre uma competência e uma lista de atividades.
     *
     * <p>O método compara a lista de IDs de atividades fornecida com os vínculos existentes para a
     * competência e realiza as seguintes operações:
     *
     * <ul>
     *   <li>Remove vínculos com atividades que não estão na nova lista.
     *   <li>Cria novos vínculos para atividades que estão na nova lista mas não nos vínculos
     *       atuais.
     * </ul>
     *
     * @param codCompetencia      O código da competência a ser atualizada.
     * @param novosCodsAtividades A lista completa de IDs de atividades que devem estar vinculadas à
     *                            competência.
     */
    private void atualizarVinculosAtividades(Long codCompetencia, List<Long> novosCodsAtividades) {
        Competencia competencia = competenciaRepo.findById(codCompetencia).orElseThrow();

        Set<Atividade> novasAtividades =
                new HashSet<>(atividadeRepo.findAllById(novosCodsAtividades));

        competencia.setAtividades(novasAtividades);
        competenciaRepo.save(competencia);

        log.debug(
                "Atualizados {} vínculos para competência {}",
                novosCodsAtividades.size(),
                codCompetencia);
    }

    /**
     * Valida a integridade de um mapa, verificando se existem atividades ou competências órfãs.
     *
     * <p>Este método loga avisos (warnings) para:
     *
     * <ul>
     *   <li>Atividades que não estão vinculadas a nenhuma competência.
     *   <li>Competências que não estão vinculadas a nenhuma atividade.
     * </ul>
     *
     * <p>Nota: Esta é uma validação defensiva. Em operação normal, não deve haver atividades ou
     * competências órfãs se as camadas de negócio estiverem corretamente configuradas. Serve como
     * proteção contra dados inconsistentes e para diagnosticar problemas.
     *
     * @param codMapa O código do mapa a ser validado.
     */
    private void validarIntegridadeMapa(Long codMapa) {
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);

        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                log.warn(
                        "Atividade {} não vinculada a nenhuma competência no mapa {}",
                        atividade.getCodigo(),
                        codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                log.warn(
                        "Competência {} sem atividades vinculadas no mapa {}",
                        competencia.getCodigo(),
                        codMapa);
            }
        }
    }
}
