package sgc.mapa.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.evento.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.Mapa;

/**
 * Serviço unificado responsável pela manutenção da estrutura do Mapa de Competências.
 * 
 * <p>Consolida as operações de {@link Atividade}, {@link Competencia} e {@link Conhecimento},
 * eliminando dependências circulares e garantindo consistência transacional.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MapaManutencaoService {

    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    
    private final RepositorioComum repo;
    
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    
    private final ApplicationEventPublisher eventPublisher;

    // ============================================================================================
    // SEÇÃO: ATIVIDADE
    // ============================================================================================

    @Transactional(readOnly = true)
    public List<AtividadeResponse> listarAtividades() {
        return atividadeRepo.findAll().stream().map(atividadeMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AtividadeResponse obterAtividadeResponse(Long codAtividade) {
        return atividadeMapper.toResponse(obterAtividadePorCodigo(codAtividade));
    }

    @Transactional(readOnly = true)
    public Atividade obterAtividadePorCodigo(Long codAtividade) {
        return repo.buscar(Atividade.class, codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorCodigos(List<Long> codigos) {
        return atividadeRepo.findAllById(codigos);
    }

    @Transactional(readOnly = true)
    public int contarAtividadesPorMapa(Long codMapa) {
        return (int) atividadeRepo.countByMapaCodigo(codMapa);
    }

    public AtividadeResponse criarAtividade(CriarAtividadeRequest request) {
        Mapa mapa = repo.buscar(Mapa.class, request.mapaCodigo());

        notificarAlteracaoMapa(request.mapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(request);
        entidade.setMapa(mapa);

        Atividade salvo = atividadeRepo.save(entidade);
        return atividadeMapper.toResponse(salvo);
    }

    public void atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade existente = repo.buscar(Atividade.class, codigo);

        if (existente.getMapa() != null) {
            notificarAlteracaoMapa(existente.getMapa().getCodigo());
        }

        var entidadeParaAtualizar = atividadeMapper.toEntity(request);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());

        atividadeRepo.save(existente);
    }

    public List<Atividade> atualizarDescricoesAtividadeEmLote(Map<Long, String> descricoesPorId) {
        List<Atividade> atividades = atividadeRepo.findAllById(descricoesPorId.keySet());
        Set<Long> mapasAfetados = new HashSet<>();

        for (Atividade atividade : atividades) {
            String novaDescricao = descricoesPorId.get(atividade.getCodigo());
            if (novaDescricao != null) {
                atividade.setDescricao(novaDescricao);
            }
            var mapa = atividade.getMapa();
            if (mapa != null) {
                mapasAfetados.add(mapa.getCodigo());
            }
        }

        atividadeRepo.saveAll(atividades);

        for (Long codMapa : mapasAfetados) {
            notificarAlteracaoMapa(codMapa);
        }

        return atividades;
    }

    public void excluirAtividade(Long codAtividade) {
        Atividade atividade = repo.buscar(Atividade.class, codAtividade);
        excluirAtividadeEConhecimentos(atividade);
    }

    private void excluirAtividadeEConhecimentos(Atividade atividade) {
        var mapa = atividade.getMapa();
        if (mapa != null) {
            notificarAlteracaoMapa(mapa.getCodigo());
        }
        
        // Remove conhecimentos associados
        List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
        conhecimentoRepo.deleteAll(conhecimentos);
        
        atividadeRepo.delete(atividade);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigo(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigoSemRelacionamentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findWithConhecimentosByMapaCodigo(mapaCodigo);
    }

    // ============================================================================================
    // SEÇÃO: COMPETÊNCIA
    // ============================================================================================

    public Competencia buscarCompetenciaPorCodigo(Long codCompetencia) {
        return competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));
    }

    public List<Competencia> buscarCompetenciasPorCodMapa(Long codMapa) {
        return competenciaRepo.findByMapaCodigo(codMapa);
    }

    public List<Competencia> buscarCompetenciasPorCodMapaSemRelacionamentos(Long codMapa) {
        return competenciaRepo.findByMapaCodigoSemFetch(codMapa);
    }

    public Map<Long, Set<Long>> buscarIdsAssociacoesCompetenciaAtividade(Long codMapa) {
        List<Object[]> rows = competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(codMapa);
        Map<Long, Set<Long>> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            Long compId = (Long) row[0];
            Long ativId = (Long) row[2];
            if (ativId != null) {
                result.computeIfAbsent(compId, k -> new HashSet<>()).add(ativId);
            }
        }
        return result;
    }

    public void salvarCompetencia(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

    public void salvarTodasCompetencias(List<Competencia> competencias) {
        competenciaRepo.saveAll(competencias);
    }

    public List<Competencia> buscarCompetenciasPorCodigos(List<Long> codigos) {
        return competenciaRepo.findAllById(codigos);
    }

    public void criarCompetenciaComAtividades(Mapa mapa, String descricao, List<Long> codigosAtividades) {
        Competencia competencia = Competencia.builder()
                .descricao(descricao)
                .mapa(mapa)
                .build();
        prepararCompetenciasAtividades(codigosAtividades, competencia);
        competenciaRepo.save(competencia);

        atividadeRepo.saveAll(competencia.getAtividades());
    }

    public void atualizarCompetencia(Long codCompetencia, String descricao, List<Long> atividadesIds) {
        Competencia competencia = competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));

        competencia.setDescricao(descricao);

        List<Atividade> atividadesAntigas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAntigas.forEach(atividade -> atividade.getCompetencias().remove(competencia));
        atividadeRepo.saveAll(atividadesAntigas);

        competencia.getAtividades().clear();
        prepararCompetenciasAtividades(atividadesIds, competencia);
        competenciaRepo.save(competencia);

        atividadeRepo.saveAll(competencia.getAtividades());
    }

    public void removerCompetencia(Long codCompetencia) {
        Competencia competencia = competenciaRepo.findById(codCompetencia)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência", codCompetencia));

        List<Atividade> atividadesAssociadas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAssociadas.forEach(atividade -> atividade.getCompetencias().remove(competencia));

        atividadeRepo.saveAll(atividadesAssociadas);
        competenciaRepo.delete(competencia);
    }

    private void prepararCompetenciasAtividades(List<Long> codigosAtividades, Competencia competencia) {
        if (codigosAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codigosAtividades);
        competencia.setAtividades(new HashSet<>(atividades));

        atividades.forEach(atividade -> atividade.getCompetencias().add(competencia));
    }

    // ============================================================================================
    // SEÇÃO: CONHECIMENTO
    // ============================================================================================

    @Transactional(readOnly = true)
    public List<ConhecimentoResponse> listarConhecimentosPorAtividade(Long codAtividade) {
        if (!atividadeRepo.existsById(codAtividade)) {
            throw new ErroEntidadeNaoEncontrada("Atividade", codAtividade);
        }
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade).stream()
                .map(conhecimentoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosEntidadesPorAtividade(Long codAtividade) {
        return conhecimentoRepo.findByAtividadeCodigo(codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosPorMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    public ConhecimentoResponse criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        return atividadeRepo.findById(codAtividade)
                .map(atividade -> {
                    var mapa = atividade.getMapa();
                    if (mapa != null) {
                        notificarAlteracaoMapa(mapa.getCodigo());
                    }
                    var conhecimento = conhecimentoMapper.toEntity(request);
                    conhecimento.setAtividade(atividade);
                    var salvo = conhecimentoRepo.save(conhecimento);
                    return conhecimentoMapper.toResponse(salvo);
                })
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
    }

    public void atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(
                        existente -> {
                            var mapa = existente.getAtividade().getMapa();
                            if (mapa != null) {
                                notificarAlteracaoMapa(mapa.getCodigo());
                            }
                            var paraAtualizar = conhecimentoMapper.toEntity(request);
                            existente.setDescricao(paraAtualizar.getDescricao());
                            conhecimentoRepo.save(existente);
                        },
                        () -> {
                            throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                        });
    }

    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        conhecimentoRepo.findById(codConhecimento)
                .filter(conhecimento -> conhecimento.getCodigoAtividade().equals(codAtividade))
                .ifPresentOrElse(
                        this::executarExclusaoConhecimento,
                        () -> {
                            throw new ErroEntidadeNaoEncontrada("Conhecimento", codConhecimento);
                        });
    }
    
    // Auxiliares

    private void executarExclusaoConhecimento(Conhecimento conhecimento) {
        var mapa = conhecimento.getAtividade().getMapa();
        if (mapa != null) {
            notificarAlteracaoMapa(mapa.getCodigo());
        }
        conhecimentoRepo.delete(conhecimento);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }
}
