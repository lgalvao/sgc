package sgc.mapa.service;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MapaManutencaoService {
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final MapaRepo mapaRepo;
    private final ComumRepo repo;
    private final SubprocessoService subprocessoService;

    public MapaManutencaoService(
            AtividadeRepo atividadeRepo,
            CompetenciaRepo competenciaRepo,
            ConhecimentoRepo conhecimentoRepo,
            MapaRepo mapaRepo,
            ComumRepo repo,
            @Lazy SubprocessoService subprocessoService) {

        this.atividadeRepo = atividadeRepo;
        this.competenciaRepo = competenciaRepo;
        this.conhecimentoRepo = conhecimentoRepo;
        this.mapaRepo = mapaRepo;
        this.repo = repo;
        this.subprocessoService = subprocessoService;
    }

    public List<Atividade> listarAtividades() {
        return atividadeRepo.findAll();
    }

    public Atividade atividadeCodigo(Long codAtividade) {
        return repo.buscar(Atividade.class, codAtividade);
    }

    public List<Atividade> atividadesCodigos(List<Long> codigos) {
        return atividadeRepo.findAllById(codigos);
    }

    public List<Atividade> atividadesMapaCodigo(Long mapaCodigo) {
        return atividadeRepo.findByMapa_Codigo(mapaCodigo);
    }

    public List<Atividade> atividadesMapaCodigoSemRels(Long mapaCodigo) {
        return atividadeRepo.findByMapaCodigoSemFetch(mapaCodigo);
    }

    public List<Atividade> atividadesMapaCodigoComConhecimentos(Long mapaCodigo) {
        return atividadeRepo.findWithConhecimentosByMapa_Codigo(mapaCodigo);
    }

    public Competencia competenciaCodigo(Long codCompetencia) {
        return repo.buscar(Competencia.class, codCompetencia);
    }

    public List<Competencia> competenciasCodMapa(Long codMapa) {
        return competenciaRepo.findByMapa_Codigo(codMapa);
    }

    public List<Competencia> competenciasCodMapaSemRels(Long codMapa) {
        return competenciaRepo.findByMapaCodigoSemFetch(codMapa);
    }

    public Map<Long, Set<Long>> codigosAssociacoesCompetenciaAtividade(Long codMapa) {
        List<Object[]> rows = competenciaRepo.findCompetenciaAndAtividadeIdsByMapaCodigo(codMapa);
        Map<Long, Set<Long>> result = new HashMap<>();

        for (Object[] row : rows) {
            Long codCompetencia = (Long) row[0];
            Long codAtividade = (Long) row[2];
            result.computeIfAbsent(codCompetencia, k -> new HashSet<>()).add(codAtividade);
        }

        return result;
    }

    public List<Conhecimento> conhecimentosCodigoAtividade(Long codAtividade) {
        repo.buscar(Atividade.class, codAtividade);
        return conhecimentoRepo.findByAtividade_Codigo(codAtividade);
    }

    public List<Conhecimento> conhecimentosCodMapa(Long codMapa) {
        return conhecimentoRepo.findByMapaCodigo(codMapa);
    }

    public List<Competencia> competenciasCodigos(List<Long> codigos) {
        return competenciaRepo.findAllById(codigos);
    }

    public List<Mapa> mapas() {
        return mapaRepo.findAll();
    }

    public Optional<Mapa> mapaVigenteUnidade(Long unidadeCodigo) {
        return mapaRepo.buscarMapaVigentePorUnidade(unidadeCodigo);
    }

    public Optional<Mapa> mapaSubprocesso(Long subprocessoCodigo) {
        return mapaRepo.buscarPorSubprocesso(subprocessoCodigo);
    }

    public Mapa mapaCompletoSubprocesso(Long subprocessoCodigo) {
        return mapaRepo.buscarCompletoPorSubprocesso(subprocessoCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Mapa", "S:" + subprocessoCodigo));
    }

    public Mapa mapaCodigo(Long codigo) {
        return repo.buscar(Mapa.class, codigo);
    }

    public boolean mapaExiste(Long codigo) {
        return mapaRepo.existsById(codigo);
    }

    @Transactional
    public Atividade criarAtividade(CriarAtividadeRequest request) {
        validarDescricaoAtividadeUnica(request.mapaCodigo(), request.descricao());
        Mapa mapa = repo.buscar(Mapa.class, request.mapaCodigo());

        Atividade entidade = Atividade.criarDe(request);
        entidade.setMapa(mapa);

        log.info("Atividade criada no mapa {}", request.mapaCodigo());
        Atividade atividadeSalva = atividadeRepo.save(entidade);
        notificarAlteracaoMapa(request.mapaCodigo());
        return atividadeSalva;
    }

    @Transactional
    public void atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade existente = repo.buscar(Atividade.class, codigo);
        if (!existente.getDescricao().equalsIgnoreCase(request.descricao())) {
            validarDescricaoAtividadeUnica(existente.getMapa().getCodigo(), request.descricao());
        }

        existente.atualizarDe(request);
        atividadeRepo.save(existente);
        notificarAlteracaoMapa(existente.getMapa().getCodigo());
        log.info("Atividade {} atualizada", codigo);
    }

    @Transactional
    public void atualizarDescricoesAtividadeEmBloco(Map<Long, String> descricoesPorCodigo) {
        List<Atividade> atividades = atividadeRepo.findAllById(descricoesPorCodigo.keySet());
        Set<Long> mapasAfetados = new HashSet<>();

        atividades.forEach(atividade -> {
            String novaDescricao = descricoesPorCodigo.get(atividade.getCodigo());
            if (novaDescricao != null) {
                atividade.setDescricao(novaDescricao);
            }
            var mapa = atividade.getMapa();
            mapasAfetados.add(mapa.getCodigo());
        });

        atividadeRepo.saveAll(atividades);
        mapasAfetados.forEach(this::notificarAlteracaoMapa);
        log.info("Atualizando descrições de {} atividades em lote", descricoesPorCodigo.size());
    }

    @Transactional
    public void excluirAtividade(Long codAtividade) {
        Atividade atividade = repo.buscar(Atividade.class, codAtividade);
        excluirAtividadeComConhecimentos(atividade);
        log.info("Atividade {} excluída", codAtividade);
    }

    private void excluirAtividadeComConhecimentos(Atividade atividade) {
        var mapa = atividade.getMapa();

        List<Conhecimento> conhecimentos = conhecimentoRepo.findByAtividade_Codigo(atividade.getCodigo());
        conhecimentoRepo.deleteAll(conhecimentos);

        atividadeRepo.delete(atividade);
        notificarAlteracaoMapa(mapa.getCodigo());
    }

    @Transactional
    public Mapa salvarMapa(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    @Transactional
    public List<Mapa> salvarMapas(List<Mapa> mapas) {
        return mapaRepo.saveAll(mapas);
    }

    @Transactional
    public void salvarCompetencia(Competencia competencia) {
        competenciaRepo.save(competencia);
    }

    @Transactional
    public void salvarCompetencias(List<Competencia> competencias) {
        log.info("Salvando lote de {} competências", competencias.size());
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

        log.info("Competência criada");
    }

    @Transactional
    public void atualizarCompetencia(Long codigo, String desc, List<Long> atividadesCodigos) {
        Competencia competencia = repo.buscar(Competencia.class, codigo);
        competencia.setDescricao(desc);

        List<Atividade> atividadesAntigas = atividadeRepo.listarPorCompetencia(competencia);
        atividadesAntigas.forEach(atividade -> atividade.getCompetencias().remove(competencia));
        atividadeRepo.saveAll(atividadesAntigas);

        competencia.getAtividades().clear();
        prepararCompetenciasAtividades(atividadesCodigos, competencia);
        competenciaRepo.save(competencia);

        atividadeRepo.saveAll(competencia.getAtividades());

        log.info("Competência atualizada");
    }

    @Transactional
    public void removerCompetencia(Long codCompetencia) {
        log.info("Removendo competência {}", codCompetencia);
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

        var conhecimento = Conhecimento.criarDe(request);
        conhecimento.setAtividade(atividade);
        atividade.getConhecimentos().add(conhecimento);
        log.info("Conhecimento criado na atividade {}", codAtividade);

        Conhecimento conhecimentoSalvo = conhecimentoRepo.save(conhecimento);
        notificarAlteracaoMapa(mapa.getCodigo());
        return conhecimentoSalvo;
    }

    @Transactional
    public void atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        String descricao = request.descricao();

        Conhecimento existente = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));
        if (!existente.getDescricao().equalsIgnoreCase(descricao)) {
            validarDescricaoConhecimentoUnica(codAtividade, descricao);
        }

        var mapa = existente.getAtividade().getMapa();

        existente.atualizarDe(request);
        conhecimentoRepo.save(existente);
        notificarAlteracaoMapa(mapa.getCodigo());

        log.info("Conhecimento atualizado na atividade {}",codAtividade);
    }

    @Transactional
    public void excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Conhecimento conhecimento = repo.buscar(Conhecimento.class, Map.of("codigo", codConhecimento, "atividade.codigo", codAtividade));
        executarExclusaoConhecimento(conhecimento);

        log.info("Conhecimento excluído da atividade {}", codAtividade);
    }

    @Transactional
    public void excluirMapa(Long codigo) {
        mapaRepo.deleteById(codigo);
    }

    private void executarExclusaoConhecimento(Conhecimento conhecimento) {
        Mapa mapa = conhecimento.getAtividade().getMapa();
        conhecimento.getAtividade().getConhecimentos().remove(conhecimento);
        conhecimentoRepo.delete(conhecimento);
        notificarAlteracaoMapa(mapa.getCodigo());
    }

    private void prepararCompetenciasAtividades(List<Long> codigosAtividades, Competencia competencia) {
        if (codigosAtividades.isEmpty()) return;

        List<Atividade> atividades = atividadeRepo.findAllById(codigosAtividades);
        competencia.setAtividades(new HashSet<>(atividades));

        atividades.forEach(atividade -> atividade.getCompetencias().add(competencia));
    }

    private void notificarAlteracaoMapa(Long codMapa) {
        subprocessoService.atualizarParaEmAndamento(codMapa);
    }

    private void validarDescricaoAtividadeUnica(Long codMapa, String desc) {
        boolean existe = atividadeRepo.findByMapaCodigoSemFetch(codMapa).stream()
                .anyMatch(a -> a.getDescricao().equalsIgnoreCase(desc));

        if (existe) throw new ErroValidacao(SgcMensagens.DESCRICAO_ATIVIDADE_DUPLICADA);
    }

    private void validarDescricaoConhecimentoUnica(Long codAtividade, String descricao) {
        boolean existe = conhecimentoRepo.findByAtividade_Codigo(codAtividade).stream()
                .anyMatch(c -> c.getDescricao().equalsIgnoreCase(descricao));

        if (existe) throw new ErroValidacao(SgcMensagens.DESCRICAO_CONHECIMENTO_DUPLICADA);
    }
}
