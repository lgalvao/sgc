package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;
import sgc.seguranca.sanitizacao.UtilSanitizacao;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado para salvar o mapa completo de competências.
 *
 * <p>
 * Responsável por processar requisições de salvamento que incluem:
 * <ul>
 * <li>Atualização das observações do mapa</li>
 * <li>Criação, atualização e remoção de competências</li>
 * <li>Atualização das associações entre atividades e competências</li>
 * <li>Validação de integridade do mapa</li>
 * </ul>
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaSalvamentoService {
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaCompletoMapper mapaCompletoMapper;

    /**
     * Salva o mapa completo com todas as competências e suas associações.
     *
     * @param codMapa O código do mapa a ser salvo.
     * @param request A requisição contendo as competências e observações.
     * @return O DTO do mapa completo atualizado.
     * @throws ErroEntidadeNaoEncontrada se o mapa não for encontrado.
     * @throws ErroValidacao             se houver atividades inválidas na
     *                                   requisição.
     */
    public MapaCompletoDto salvarMapaCompleto(
            Long codMapa, SalvarMapaRequest request) {

        Mapa mapa = mapaRepo.findById(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa não encontrado: %d".formatted(codMapa)));

        atualizarObservacoes(mapa, request.getObservacoes());

        ContextoSalvamento contexto = prepararContexto(codMapa, request);
        removerCompetenciasObsoletas(contexto);
        List<Competencia> competenciasSalvas = salvarCompetencias(mapa, contexto);
        atualizarAssociacoesAtividades(contexto, competenciasSalvas);

        validarIntegridadeMapa(codMapa, contexto.atividadesAtuais, competenciasSalvas);

        return mapaCompletoMapper.toDto(mapa, null, competenciasSalvas);
    }

    private void atualizarObservacoes(Mapa mapa, String observacoes) {
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(observacoes);
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapaRepo.save(mapa);
    }

    private ContextoSalvamento prepararContexto(Long codMapa, SalvarMapaRequest request) {
        List<Competencia> competenciasAtuais = new ArrayList<>(competenciaRepo.findByMapaCodigo(codMapa));
        List<Atividade> atividadesAtuais = new ArrayList<>(atividadeRepo.findByMapaCodigo(codMapa));

        Set<Long> atividadesDoMapaIds = atividadesAtuais.stream()
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> codigosNovos = request.getCompetencias().stream()
                .map(CompetenciaMapaDto::getCodigo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return new ContextoSalvamento(
                competenciasAtuais,
                atividadesAtuais,
                atividadesDoMapaIds,
                codigosNovos,
                request);
    }

    private void removerCompetenciasObsoletas(ContextoSalvamento contexto) {
        List<Competencia> paraRemover = contexto.competenciasAtuais.stream()
                .filter(c -> !contexto.codigosNovos.contains(c.getCodigo()))
                .toList();

        if (!paraRemover.isEmpty()) {
            for (Atividade atividade : contexto.atividadesAtuais) {
                paraRemover.forEach(atividade.getCompetencias()::remove);
            }
            competenciaRepo.deleteAll(paraRemover);
            contexto.competenciasAtuais.removeAll(paraRemover);
        }
    }

    private List<Competencia> salvarCompetencias(Mapa mapa, ContextoSalvamento contexto) {
        Map<Long, Competencia> mapaCompetenciasExistentes = contexto.competenciasAtuais.stream()
                .collect(Collectors.toMap(Competencia::getCodigo, c -> c));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();

        for (CompetenciaMapaDto compDto : contexto.request.getCompetencias()) {
            Competencia competencia = processarCompetenciaDto(compDto, mapa, mapaCompetenciasExistentes);
            competenciasParaSalvar.add(competencia);
        }

        return competenciaRepo.saveAll(competenciasParaSalvar);
    }

    private Competencia processarCompetenciaDto(
            CompetenciaMapaDto compDto,
            Mapa mapa,
            Map<Long, Competencia> mapaCompetenciasExistentes) {

        if (compDto.getCodigo() == null) {
            return new Competencia(compDto.getDescricao(), mapa);
        }

        Competencia competencia = mapaCompetenciasExistentes.get(compDto.getCodigo());
        if (competencia == null) {
            throw new sgc.comum.erros.ErroEntidadeDeveriaExistir(
                    "Competência", compDto.getCodigo(),
                    "MapaSalvamentoService.atualizarCompetenciaExistente - competência deveria estar no mapa");
        }
        competencia.setDescricao(compDto.getDescricao());
        return competencia;
    }

    // ===================================================================================
    // Métodos de associação
    // ===================================================================================

    private void atualizarAssociacoesAtividades(ContextoSalvamento contexto, List<Competencia> competenciasSalvas) {
        Long codMapa = contexto.atividadesAtuais.isEmpty() ? null
                : contexto.atividadesAtuais.getFirst().getMapa().getCodigo();

        Map<Long, Set<Competencia>> mapAtividadeCompetencias = construirMapaAssociacoes(
                contexto, competenciasSalvas, codMapa);

        aplicarAssociacoes(contexto.atividadesAtuais, mapAtividadeCompetencias);
        atividadeRepo.saveAll(contexto.atividadesAtuais);
    }

    private Map<Long, Set<Competencia>> construirMapaAssociacoes(
            ContextoSalvamento contexto,
            List<Competencia> competenciasSalvas,
            Long codMapa) {

        Map<Long, Set<Competencia>> mapAtividadeCompetencias = new HashMap<>();

        // Inicializar com conjuntos vazios para todas as atividades
        for (Long ativId : contexto.atividadesDoMapaIds) {
            mapAtividadeCompetencias.put(ativId, new HashSet<>());
        }

        List<CompetenciaMapaDto> competenciasDto = contexto.request.getCompetencias();
        for (int i = 0; i < competenciasDto.size(); i++) {
            CompetenciaMapaDto dto = competenciasDto.get(i);
            Competencia competencia = competenciasSalvas.get(i);

            if (dto.getAtividadesCodigos() != null) {
                for (Long ativId : dto.getAtividadesCodigos()) {
                    validarAtividadePertenceAoMapa(ativId, contexto.atividadesDoMapaIds, codMapa);
                    mapAtividadeCompetencias.get(ativId).add(competencia);
                }
            }
        }

        return mapAtividadeCompetencias;
    }

    private void validarAtividadePertenceAoMapa(Long ativId, Set<Long> atividadesDoMapaIds, Long codMapa) {
        if (!atividadesDoMapaIds.contains(ativId)) {
            throw new ErroValidacao("Atividade %d não pertence ao mapa %d".formatted(ativId, codMapa));
        }
    }

    private void aplicarAssociacoes(List<Atividade> atividades, Map<Long, Set<Competencia>> mapAtividadeCompetencias) {
        for (Atividade atividade : atividades) {
            Set<Competencia> novasCompetencias = mapAtividadeCompetencias.get(atividade.getCodigo());
            atividade.setCompetencias(novasCompetencias);
            for (Competencia competencia : novasCompetencias) {
                competencia.getAtividades().add(atividade);
            }
        }
    }

    // ===================================================================================
    // Métodos de validação
    // ===================================================================================

    private void validarIntegridadeMapa(Long codMapa, List<Atividade> atividades, List<Competencia> competencias) {
        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}",
                        atividade.getCodigo(), codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}",
                        competencia.getCodigo(), codMapa);
            }
        }
    }

    /**
     * Classe auxiliar para manter o contexto durante o salvamento.
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static class ContextoSalvamento {
        final List<Competencia> competenciasAtuais;
        final List<Atividade> atividadesAtuais;
        final Set<Long> atividadesDoMapaIds;
        final Set<Long> codigosNovos;
        final SalvarMapaRequest request;

        ContextoSalvamento(
                List<Competencia> competenciasAtuais,
                List<Atividade> atividadesAtuais,
                Set<Long> atividadesDoMapaIds,
                Set<Long> codigosNovos,
                SalvarMapaRequest request) {
            this.competenciasAtuais = competenciasAtuais;
            this.atividadesAtuais = atividadesAtuais;
            this.atividadesDoMapaIds = atividadesDoMapaIds;
            this.codigosNovos = codigosNovos;
            this.request = request;
        }
    }
}
