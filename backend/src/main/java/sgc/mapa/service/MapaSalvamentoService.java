package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.ComumRepo;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.*;
import sgc.seguranca.sanitizacao.UtilSanitizacao;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço especializado para salvar o mapa completo de competências.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MapaSalvamentoService {
    private final MapaRepo mapaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final ComumRepo repo;

    /**
     * Salva o mapa completo com todas as competências e suas associações.
     */
    public Mapa salvarMapaCompleto(Long codMapa, SalvarMapaRequest request) {
        Mapa mapa = repo.buscar(Mapa.class, codMapa);

        String observacoes = request.observacoes();
        atualizarObservacoes(mapa, observacoes);

        ContextoSalvamento contexto = prepararContexto(codMapa, request);
        removerCompetenciasObsoletas(contexto);

        List<Competencia> competenciasSalvas = salvarCompetencias(contexto, mapa);
        atualizarAssociacoesAtividades(contexto, competenciasSalvas);
        validarIntegridadeMapa(codMapa, contexto.atividadesAtuais, competenciasSalvas);

        return mapa;
    }

    private void atualizarObservacoes(Mapa mapa, @Nullable String observacoes) {
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(observacoes);
        mapa.setObservacoesDisponibilizacao(sanitizedObservacoes);
        mapaRepo.save(mapa);
    }

    private ContextoSalvamento prepararContexto(Long codMapa, SalvarMapaRequest request) {
        List<Competencia> competenciasAtuais = new ArrayList<>(competenciaRepo.findByMapa_Codigo(codMapa));
        List<Atividade> atividadesAtuais = new ArrayList<>(atividadeRepo.findByMapa_Codigo(codMapa));

        Set<Long> atividadesDoMapaIds = atividadesAtuais.stream()
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        Set<Long> codigosNovos = request.competencias().stream()
                .map(SalvarMapaRequest.CompetenciaRequest::codigo)
                .collect(Collectors.toSet());

        return new ContextoSalvamento(
                codMapa,
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

    private List<Competencia> salvarCompetencias(ContextoSalvamento contexto, Mapa mapa) {
        Map<Long, Competencia> mapaCompetenciasExistentes = contexto.competenciasAtuais.stream()
                .collect(Collectors.toMap(Competencia::getCodigo, c -> c));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();

        for (SalvarMapaRequest.CompetenciaRequest compDto : contexto.request.competencias()) {
            Competencia competencia = processarCompetenciaDto(compDto, mapaCompetenciasExistentes, mapa);
            competenciasParaSalvar.add(competencia);
        }

        return competenciaRepo.saveAll(competenciasParaSalvar);
    }

    private Competencia processarCompetenciaDto(
            SalvarMapaRequest.CompetenciaRequest compDto,
            Map<Long, Competencia> mapaCompetenciasExistentes,
            Mapa mapa) {

        Long codigo = compDto.codigo();
        Competencia competencia;

        if (codigo == null || codigo == 0) {
            competencia = Competencia.builder()
                    .mapa(mapa)
                    .atividades(new HashSet<>())
                    .build();
        } else {
            competencia = mapaCompetenciasExistentes.get(codigo);
            if (competencia == null) {
                competencia = repo.buscar(Competencia.class, codigo);
            }
        }

        competencia.setDescricao(compDto.descricao());
        return competencia;
    }

    private void atualizarAssociacoesAtividades(ContextoSalvamento contexto, List<Competencia> competenciasSalvas) {
        Map<Long, Set<Competencia>> mapAtividadeCompetencias = construirMapaAssociacoes(
                contexto, competenciasSalvas);

        aplicarAssociacoes(contexto.atividadesAtuais, mapAtividadeCompetencias);
        atividadeRepo.saveAll(contexto.atividadesAtuais);
    }

    private Map<Long, Set<Competencia>> construirMapaAssociacoes(
            ContextoSalvamento contexto,
            List<Competencia> competenciasSalvas) {

        Map<Long, Set<Competencia>> mapAtividadeCompetencias = new HashMap<>();
        for (Long ativId : contexto.atividadesDoMapaIds) {
            mapAtividadeCompetencias.put(ativId, new HashSet<>());
        }

        Iterator<Competencia> itSalvas = competenciasSalvas.iterator();
        for (SalvarMapaRequest.CompetenciaRequest dto : contexto.request.competencias()) {
            if (!itSalvas.hasNext()) break;
            Competencia competencia = itSalvas.next();

            for (Long ativId : dto.atividadesCodigos()) {
                validarAtividadePertenceAoMapa(ativId, contexto.atividadesDoMapaIds, contexto.codMapa);
                mapAtividadeCompetencias.get(ativId).add(competencia);
            }
        }

        return mapAtividadeCompetencias;
    }

    private void validarAtividadePertenceAoMapa(Long ativId, Set<Long> atividadesDoMapaIds, @Nullable Long codMapa) {
        if (!atividadesDoMapaIds.contains(ativId)) {
            throw new ErroValidacao(
                    "Atividade %d não pertence ao mapa %s".formatted(ativId, Objects.toString(codMapa)));
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

    private void validarIntegridadeMapa(Long codMapa, List<Atividade> atividades, List<Competencia> competencias) {
        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                log.warn("Atividade {} não vinculada a nenhuma competência no mapa {}", atividade.getCodigo(), codMapa);
            }
        }

        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                log.warn("Competência {} sem atividades vinculadas no mapa {}", competencia.getCodigo(), codMapa);
            }
        }
    }

    private record ContextoSalvamento(
            Long codMapa,
            List<Competencia> competenciasAtuais,
            List<Atividade> atividadesAtuais,
            Set<Long> atividadesDoMapaIds,
            Set<Long> codigosNovos,
            SalvarMapaRequest request) {
    }
}