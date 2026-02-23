package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.ComumRepo;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.dto.AtividadeMapper;
import sgc.mapa.dto.ConhecimentoMapper;
import sgc.mapa.model.*;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapaManutencaoService {
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final MapaRepo mapaRepo;
    private final ComumRepo repo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final SubprocessoAdminWorkflowService subprocessoAdminService;

    public List<Atividade> listarAtividades() {
        return atividadeRepo.findAll();
    }

    public Atividade obterAtividadePorCodigo(Long codAtividade) {
        return repo.buscar(Atividade.class, codAtividade);
    }

    public List<Atividade> buscarAtividadesPorCodigos(List<Long> codigos) {
        return atividadeRepo.findAllById(codigos);
    }

    public List<Atividade> buscarAtividadesPorMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapa_Codigo(mapaCodigo);
    }

    public List<Atividade> buscarAtividadesPorMapaCodigoSemRelacionamentos(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo);
    }

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

    public List<Conhecimento> listarConhecimentosPorAtividade(Long codAtividade) {
        repo.buscar(Atividade.class, codAtividade);
        return conhecimentoRepo.findByAtividade_Codigo(codAtividade);
    }

    public List<Conhecimento> listarConhecimentosEntidadesPorAtividade(Long codAtividade) {
        return conhecimentoRepo.findByAtividade_Codigo(codAtividade);
    }

    public List<Conhecimento> listarConhecimentosPorMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    public List<Competencia> buscarCompetenciasPorCodigos(List<Long> codigos) {
        return competenciaRepo.findAllById(codigos);
    }

    public List<Mapa> listarTodosMapas() {
        return mapaRepo.findAll();
    }

    public Optional<Mapa> buscarMapaVigentePorUnidade(Long unidadeCodigo) {
        return mapaRepo.findMapaVigenteByUnidade(unidadeCodigo);
    }

    public Optional<Mapa> buscarMapaPorSubprocessoCodigo(Long subprocessoCodigo) {
        return mapaRepo.findBySubprocessoCodigo(subprocessoCodigo);
    }

    public Mapa buscarMapaCompletoPorSubprocesso(Long subprocessoCodigo) {
        return mapaRepo.findFullBySubprocessoCodigo(subprocessoCodigo)
                .orElseThrow(() -> new sgc.comum.erros.ErroEntidadeNaoEncontrada("Mapa", "S:" + subprocessoCodigo));
    }

    public Mapa buscarMapaPorCodigo(Long codigo) {
        return repo.buscar(Mapa.class, codigo);
    }

    public boolean mapaExiste(Long codigo) {
        return mapaRepo.existsById(codigo);
    }

    @Transactional
    public Atividade criarAtividade(CriarAtividadeRequest request) {
        validarDescricaoAtividadeUnica(request.mapaCodigo(), request.descricao());
        Mapa mapa = repo.buscar(Mapa.class, request.mapaCodigo());
        notificarAlteracaoMapa(request.mapaCodigo());

        Atividade entidade = atividadeMapper.toEntity(request);
        entidade.setMapa(mapa);

        return atividadeRepo.save(entidade);
    }

    @Transactional
    public void atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade existente = repo.buscar(Atividade.class, codigo);
        if (!existente.getDescricao().equalsIgnoreCase(request.descricao())) {
            validarDescricaoAtividadeUnica(existente.getMapa().getCodigo(), request.descricao());
        }
        notificarAlteracaoMapa(existente.getMapa().getCodigo());

        var entidadeParaAtualizar = atividadeMapper.toEntity(request);
        existente.setDescricao(entidadeParaAtualizar.getDescricao());
        atividadeRepo.save(existente);
    }

    @Transactional
    public void atualizarDescricoesAtividadeEmLote(Map<Long, String> descricoesPorId) {
        List<Atividade> atividades = atividadeRepo.findAllById(descricoesPorId.keySet());
        Set<Long> mapasAfetados = new HashSet<>();

        atividades.forEach(atividade -> {
            String novaDescricao = descricoesPorId.get(atividade.getCodigo());
            if (novaDescricao != null) {
                atividade.setDescricao(novaDescricao);
            }
            var mapa = atividade.getMapa();
            mapasAfetados.add(mapa.getCodigo());
        });

        atividadeRepo.saveAll(atividades);
        mapasAfetados.forEach(this::notificarAlteracaoMapa);
    }

    @Transactional
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

    @Transactional
    public void salvarCompetencia(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

    @Transactional
    public void salvarTodasCompetencias(List<Competencia> competencias) {
        competenciaRepo.saveAll(competencias);
    }

    @Transactional
    public void criarCompetenciaComAtividades(Mapa mapa, String descricao, List<Long> codigosAtividades) {
        Competencia competencia = Competencia.builder()
                .descricao(descricao)
                .mapa(mapa)
                .build();

        prepararCompetenciasAtividades(codigosAtividades, competencia);
        competenciaRepo.save(competencia);
        atividadeRepo.saveAll(competencia.getAtividades());
    }

    @Transactional
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

    @Transactional
    public void removerCompetencia(Long codCompetencia) {
        Competencia competencia = repo.buscar(Competencia.class, codCompetencia);

        List<Atividade> atividadesAssociadas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAssociadas.forEach(atividade -> atividade.getCompetencias().remove(competencia));

        atividadeRepo.saveAll(atividadesAssociadas);
        competenciaRepo.delete(competencia);
    }

    @Transactional
    public Conhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        validarDescricaoConhecimentoUnica(codAtividade, request.descricao());
        var atividade = repo.buscar(Atividade.class, codAtividade);
        var mapa = atividade.getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());

        var conhecimento = conhecimentoMapper.toEntity(request);
        conhecimento.setAtividade(atividade);
        atividade.getConhecimentos().add(conhecimento);
        return conhecimentoRepo.save(conhecimento);
    }

    @Transactional
    public void atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        var existente = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));

        if (!existente.getDescricao().equalsIgnoreCase(request.descricao())) {
            validarDescricaoConhecimentoUnica(codAtividade, request.descricao());
        }

        var mapa = existente.getAtividade().getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());

        var paraAtualizar = conhecimentoMapper.toEntity(request);
        existente.setDescricao(paraAtualizar.getDescricao());
        conhecimentoRepo.save(existente);
    }

    @Transactional
    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        var conhecimento = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));
        executarExclusaoConhecimento(conhecimento);
    }

    @Transactional
    public Mapa salvarMapa(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    @Transactional
    public void excluirMapa(Long codigo) {
        mapaRepo.deleteById(codigo);
    }

    private void executarExclusaoConhecimento(Conhecimento conhecimento) {
        var mapa = conhecimento.getAtividade().getMapa();
        notificarAlteracaoMapa(mapa.getCodigo());
        conhecimento.getAtividade().getConhecimentos().remove(conhecimento);
        conhecimentoRepo.delete(conhecimento);
    }

    private void prepararCompetenciasAtividades(List<Long> codigosAtividades, Competencia competencia) {
        if (codigosAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codigosAtividades);
        competencia.setAtividades(new HashSet<>(atividades));

        atividades.forEach(atividade -> atividade.getCompetencias().add(competencia));
    }

    private void notificarAlteracaoMapa(Long mapaCodigo) {
        subprocessoAdminService.atualizarParaEmAndamento(mapaCodigo);
    }

    private void validarDescricaoAtividadeUnica(Long mapaCodigo, String descricao) {
        boolean existe = atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo).stream()
                .anyMatch(a -> a.getDescricao().equalsIgnoreCase(descricao));
        if (existe) {
            throw new ErroValidacao("Já existe uma atividade com esta descrição neste mapa.");
        }
    }

    private void validarDescricaoConhecimentoUnica(Long codAtividade, String descricao) {
        boolean existe = conhecimentoRepo.findByAtividade_Codigo(codAtividade).stream()
                .anyMatch(c -> c.getDescricao().equalsIgnoreCase(descricao));
        if (existe) {
            throw new ErroValidacao("Já existe um conhecimento com esta descrição nesta atividade.");
        }
    }
}
