package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.eventos.EventoMapaAlterado;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.*;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MapaManutencaoService {
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final MapaRepo mapaRepo;
    private final ComumRepo repo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Atividade> listarAtividades() {
        return atividadeRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Atividade obterAtividadePorCodigo(Long codAtividade) {
        return repo.buscar(Atividade.class, codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorCodigos(List<Long> codigos) {
        return atividadeRepo.findAllById(codigos);
    }

    public Atividade criarAtividade(CriarAtividadeRequest request) {
        Mapa mapa = repo.buscar(Mapa.class, request.mapaCodigo());
        notificarAlteracaoMapa(request.mapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(request);
        entidade.setMapa(mapa);

        return atividadeRepo.save(entidade);
    }

    public void atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade existente = repo.buscar(Atividade.class, codigo);
        notificarAlteracaoMapa(existente.getMapa().getCodigo());

        var entidadeParaAtualizar = atividadeMapper.toEntity(request);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());
        atividadeRepo.save(existente);
    }

    public void atualizarDescricoesAtividadeEmLote(Map<Long, String> descricoesPorId) {
        List<Atividade> atividades = atividadeRepo.findAllById(descricoesPorId.keySet());
        Set<Long> mapasAfetados = new HashSet<>();

        for (Atividade atividade : atividades) {
            String novaDescricao = descricoesPorId.get(atividade.getCodigo());
            if (novaDescricao != null) {
                atividade.setDescricao(novaDescricao);
            }
            var mapa = atividade.getMapa();
            mapasAfetados.add(mapa.getCodigo());
        }

        atividadeRepo.saveAll(atividades);

        for (Long codMapa : mapasAfetados) {
            notificarAlteracaoMapa(codMapa);
        }

    }

    public void excluirAtividade(Long codAtividade) {
        Atividade atividade = repo.buscar(Atividade.class, codAtividade);
        excluirAtividadeEConhecimentos(atividade);
    }

    private void excluirAtividadeEConhecimentos(Atividade atividade) {
        var mapa = atividade.getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());

        // Remove conhecimentos associados
        List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividade_Codigo(atividade.getCodigo());
        conhecimentoRepo.deleteAll(conhecimentos);

        atividadeRepo.delete(atividade);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapa_Codigo(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigoSemRelacionamentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo);
    }

    @Transactional(readOnly = true)
    public List<Atividade> buscarAtividadesPorMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaCodigo);
    }

    public Competencia buscarCompetenciaPorCodigo(Long codCompetencia) {
        return repo.buscar(Competencia.class, codCompetencia);
    }

    public List<Competencia> buscarCompetenciasPorCodMapa(Long codMapa) {
        return competenciaRepo.findByMapa_Codigo(codMapa);
    }

    public List<Competencia> buscarCompetenciasPorCodMapaSemRelacionamentos(Long codMapa) {
        return competenciaRepo.findByMapaCodigoSemFetch(codMapa);
    }

    public Map<Long, Set<Long>> buscarIdsAssociacoesCompetenciaAtividade(Long codMapa) {
        List<Object[]> rows = competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(codMapa);
        Map<Long, Set<Long>> result = new HashMap<>();
        for (Object[] row : rows) {
            Long compId = (Long) row[0];
            Long ativId = (Long) row[2];
            result.computeIfAbsent(compId, k -> new HashSet<>()).add(ativId);
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
        Competencia competencia = repo.buscar(Competencia.class, codCompetencia);
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
        Competencia competencia = repo.buscar(Competencia.class, codCompetencia);

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

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosPorAtividade(Long codAtividade) {
        repo.buscar(Atividade.class, codAtividade);
        return conhecimentoRepo.findByAtividade_Codigo(codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosEntidadesPorAtividade(Long codAtividade) {
        return conhecimentoRepo.findByAtividade_Codigo(codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosPorMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    public Conhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        var atividade = repo.buscar(Atividade.class, codAtividade);
        var mapa = atividade.getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());

        var conhecimento = conhecimentoMapper.toEntity(request);
        conhecimento.setAtividade(atividade);
        atividade.getConhecimentos().add(conhecimento);
        return conhecimentoRepo.save(conhecimento);
    }

    public void atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        var existente = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));

        var mapa = existente.getAtividade().getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());

        var paraAtualizar = conhecimentoMapper.toEntity(request);
        existente.setDescricao(paraAtualizar.getDescricao());
        conhecimentoRepo.save(existente);
    }

    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        var conhecimento = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));
        executarExclusaoConhecimento(conhecimento);
    }

    private void executarExclusaoConhecimento(Conhecimento conhecimento) {
        var mapa = conhecimento.getAtividade().getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());
        conhecimento.getAtividade().getConhecimentos().remove(conhecimento);
        conhecimentoRepo.delete(conhecimento);
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        eventPublisher.publishEvent(new EventoMapaAlterado(mapaCodigo));
    }

    @Transactional(readOnly = true)
    public List<Mapa> listarTodosMapas() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaVigentePorUnidade(Long unidadeCodigo) {
        return mapaRepo.findMapaVigenteByUnidade(unidadeCodigo);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaPorSubprocessoCodigo(Long subprocessoCodigo) {
        return mapaRepo.findBySubprocessoCodigo(subprocessoCodigo);
    }

    @Transactional(readOnly = true)
    public Mapa buscarMapaPorCodigo(Long codigo) {
        return repo.buscar(Mapa.class, codigo);
    }

    public Mapa salvarMapa(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    @Transactional(readOnly = true)
    public boolean mapaExiste(Long codigo) {
        return mapaRepo.existsById(codigo);
    }

    public void excluirMapa(Long codigo) {
        mapaRepo.deleteById(codigo);
    }
}
