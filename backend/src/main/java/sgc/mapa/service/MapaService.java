package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.seguranca.SanitizacaoUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaService {

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

    public Mapa salvar(Mapa mapa) {
        return mapaRepo.save(mapa);
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

    public java.util.Optional<Mapa> buscarMapaVigentePorUnidade(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade);
    }

    public java.util.Optional<Mapa> buscarPorSubprocessoCodigo(Long codSubprocesso) {
        return mapaRepo.findBySubprocessoCodigo(codSubprocesso);
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

        var sanitizedObservacoes = SanitizacaoUtil.sanitizar(request.getObservacoes());
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapaRepo.save(mapa);

        // 1. Fetch current state
        List<Competencia> competenciasAtuais = competenciaRepo.findByMapaCodigo(codMapa);
        List<Atividade> atividadesAtuais = atividadeRepo.findByMapaCodigo(codMapa);
        Set<Long> atividadesDoMapaIds = atividadesAtuais.stream()
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        // 2. Identify and Process Deletions
        Set<Long> idsNovos = request.getCompetencias().stream()
                .map(CompetenciaMapaDto::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Competencia> paraRemover = competenciasAtuais.stream()
                .filter(c -> !idsNovos.contains(c.getCodigo()))
                .toList();

        // Remove relationships from the owning side (Atividade)
        if (!paraRemover.isEmpty()) {
            for (Atividade atividade : atividadesAtuais) {
                atividade.getCompetencias().removeAll(paraRemover);
            }
            competenciaRepo.deleteAll(paraRemover);
            competenciasAtuais.removeAll(paraRemover);
            log.debug("Removidas {} competências.", paraRemover.size());
        }

        // 3. Process Upserts (Insert/Update Competencies)
        Map<Long, Competencia> mapaCompetenciasExistentes = competenciasAtuais.stream()
                .collect(Collectors.toMap(Competencia::getCodigo, c -> c));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();

        for (CompetenciaMapaDto compDto : request.getCompetencias()) {
            Competencia competencia;
            if (compDto.getCodigo() == null) {
                competencia = new Competencia(compDto.getDescricao(), mapa);
                log.debug("Criando nova competência: {}", compDto.getDescricao());
            } else {
                competencia = mapaCompetenciasExistentes.get(compDto.getCodigo());
                if (competencia == null) {
                    throw new ErroEntidadeNaoEncontrada("Competência não encontrada: " + compDto.getCodigo());
                }
                competencia.setDescricao(compDto.getDescricao());
            }
            competenciasParaSalvar.add(competencia);
        }

        List<Competencia> competenciasSalvas = competenciaRepo.saveAll(competenciasParaSalvar);

        // 4. Update Relationships (Atividade -> Competencia)
        Map<Long, Set<Competencia>> mapAtividadeCompetencias = new HashMap<>();

        // Initialize with empty sets for all activities to handle clearing
        for (Long ativId : atividadesDoMapaIds) {
            mapAtividadeCompetencias.put(ativId, new HashSet<>());
        }

        for (int i = 0; i < request.getCompetencias().size(); i++) {
            CompetenciaMapaDto dto = request.getCompetencias().get(i);
            Competencia competencia = competenciasSalvas.get(i);

            if (dto.getAtividadesCodigos() != null) {
                for (Long ativId : dto.getAtividadesCodigos()) {
                    if (!atividadesDoMapaIds.contains(ativId)) {
                        throw new ErroValidacao(
                                "Atividade %d não pertence ao mapa %d".formatted(ativId, codMapa));
                    }
                    mapAtividadeCompetencias.get(ativId).add(competencia);
                }
            }
        }

        // Apply changes to Atividade entities
        for (Atividade atividade : atividadesAtuais) {
            Set<Competencia> novasCompetencias = mapAtividadeCompetencias.get(atividade.getCodigo());
            atividade.setCompetencias(novasCompetencias);
            for (Competencia competencia : novasCompetencias) {
                competencia.getAtividades().add(atividade);
            }
        }

        atividadeRepo.saveAll(atividadesAtuais);

        validarIntegridadeMapa(codMapa);

        List<Competencia> competenciasFinais = competenciaRepo.findByMapaCodigo(codMapa);

        return mapaCompletoMapper.toDto(mapa, null, competenciasFinais);
    }

    // ========================================================================================
    // Métodos auxiliares
    // ========================================================================================

    /**
     * Valida a integridade de um mapa, verificando se existem atividades ou competências órfãs.
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
