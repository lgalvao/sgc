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
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de negócio para Mapas de Competências.
 * <p>
 * Gerencia operações agregadas sobre mapas, competências e vínculos com atividades.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaServiceImpl implements MapaService {
    private final MapaRepo repositorioMapa;
    private final CompetenciaRepo repositorioCompetencia;
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;
    private final AtividadeRepo atividadeRepo;
    private final SubprocessoRepo repositorioSubprocesso;

    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaCompleto(Long idMapa) {
        log.debug("Obtendo mapa completo: id={}", idMapa);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(idMapa)));

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

    @Override
    public MapaCompletoDto salvarMapaCompleto(Long idMapa, SalvarMapaRequest request, String usuarioTitulo) {
        log.info("Salvando mapa completo: id={}, usuario={}", idMapa, usuarioTitulo);

        Mapa mapa = repositorioMapa.findById(idMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(idMapa)));

        mapa.setObservacoesDisponibilizacao(request.observacoes());
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
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada: %d".formatted(compDto.codigo())));
                competencia.setDescricao(compDto.descricao());
                competencia = repositorioCompetencia.save(competencia);
                log.debug("Competência atualizada: {}", competencia.getCodigo());
            }
            atualizarVinculosAtividades(competencia.getCodigo(), compDto.atividadesCodigos());
        }

        validarIntegridadeMapa(idMapa);
        return obterMapaCompleto(idMapa);
    }

    @Override
    @Transactional(readOnly = true)
    public MapaCompletoDto obterMapaSubprocesso(Long idSubprocesso) {
        log.debug("Obtendo mapa do subprocesso: id={}", idSubprocesso);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }

        return obterMapaCompleto(subprocesso.getMapa().getCodigo());
    }

    @Override
    public MapaCompletoDto salvarMapaSubprocesso(Long idSubprocesso, SalvarMapaRequest request, String usuarioTitulo) {
        log.info("Salvando mapa do subprocesso: idSubprocesso={}, usuario={}", idSubprocesso, usuarioTitulo);

        Subprocesso subprocesso = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        String situacao = subprocesso.getSituacaoId();
        if (!"CADASTRO_HOMOLOGADO".equals(situacao) && !"MAPA_CRIADO".equals(situacao)) {
            throw new IllegalStateException("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }

        Long idMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = repositorioCompetencia.findByMapaCodigo(idMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = salvarMapaCompleto(idMapa, request, usuarioTitulo);

        if (eraVazio && temNovasCompetencias && "CADASTRO_HOMOLOGADO".equals(situacao)) {
            subprocesso.setSituacaoId("MAPA_CRIADO");
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para MAPA_CRIADO", idSubprocesso);
        }

        return mapaDto;
    }

    @Override
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

    private void atualizarVinculosAtividades(Long idCompetencia, List<Long> idsAtividades) {
        repositorioCompetenciaAtividade.deleteByCompetenciaCodigo(idCompetencia);

        for (Long idAtividade : idsAtividades) {
            if (!atividadeRepo.existsById(idAtividade)) {
                log.warn("Tentativa de vincular atividade inexistente: {}", idAtividade);
                continue;
            }
            CompetenciaAtividade vinculo = new CompetenciaAtividade();
            CompetenciaAtividade.Id id = new CompetenciaAtividade.Id(idAtividade, idCompetencia);
            vinculo.setId(id);
            repositorioCompetenciaAtividade.save(vinculo);
        }
        log.debug("Atualizados {} vínculos para competência {}", idsAtividades.size(), idCompetencia);
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
        return repositorioSubprocesso.findAll().stream()
                .filter(s -> s.getMapa() != null && s.getMapa().getCodigo().equals(idMapa))
                .map(Subprocesso::getCodigo)
                .findFirst()
                .orElse(null);
    }
}