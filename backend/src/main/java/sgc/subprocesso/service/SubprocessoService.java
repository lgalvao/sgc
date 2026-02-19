package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseFacade;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.model.*;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.seguranca.acesso.Acao;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static sgc.subprocesso.model.SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO;
import static sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubprocessoService {

    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repositorioComum;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final MapaManutencaoService mapaManutencaoService;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final AccessControlService accessControlService;
    private final AnaliseFacade analiseFacade;
    private final MapaAjusteMapper mapaAjusteMapper;

    // --- CRUD Operations ---

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.findByIdWithMapaAndAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioComum.buscar(Subprocesso.class, "mapa.codigo", codMapa);
    }

    private void processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {
        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder()
                    .codigo(cod)
                    .build();
            Long codAtual = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                subprocesso.setMapa(m);
            }
        });

        if (!Objects.equals(subprocesso.getDataLimiteEtapa1(), request.dataLimiteEtapa1())) {
            subprocesso.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa1(), request.dataFimEtapa1())) {
            subprocesso.setDataFimEtapa1(request.dataFimEtapa1());
        }
        if (!Objects.equals(subprocesso.getDataFimEtapa2(), request.dataFimEtapa2())) {
            subprocesso.setDataFimEtapa2(request.dataFimEtapa2());
        }
    }

    public void excluir(Long codigo) {
        buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidades() {
        return subprocessoRepo.findAllComFetch();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Map<String, Object> filtros = Map.of(
                "processo.codigo", codProcesso,
                "unidade.codigo", codUnidade
        );
        return repositorioComum.buscar(Subprocesso.class, filtros);
    }

    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        return criar(request);
    }

    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, @Nullable List<Long> codUnidades) {
        if (codUnidades == null || codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    // --- Validation Operations ---

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            return emptyList();
        }
        return atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            throw new ErroValidacao("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        }
    }

    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        List<ErroValidacaoDto> erros = new ArrayList<>();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos().isEmpty()) {
                    erros.add(ErroValidacaoDto.builder()
                            .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                            .atividadeCodigo(atividade.getCodigo())
                            .descricaoAtividade(atividade.getDescricao())
                            .mensagem("Esta atividade não possui conhecimentos associados.")
                            .build());
                }
            }
        }

        return ValidacaoCadastroDto.builder()
                .valido(erros.isEmpty())
                .erros(erros)
                .build();
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, Set<SituacaoSubprocesso> permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }

        if (permitidas.isEmpty()) {
            throw new IllegalArgumentException("Conjunto de situações permitidas não pode ser vazio");
        }

        if (!permitidas.contains(subprocesso.getSituacao())) {
            String permitidasStr = String.join(", ",
                permitidas.stream().map(SituacaoSubprocesso::name).toList());
            throw new ErroValidacao(
                "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s"
                    .formatted(subprocesso.getSituacao(), permitidasStr));
        }
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, SituacaoSubprocesso... permitidas) {
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        validarSituacaoPermitida(subprocesso, Set.of(permitidas));
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, String mensagem, SituacaoSubprocesso... permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }

        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }

        if (!Set.of(permitidas).contains(subprocesso.getSituacao())) {
            throw new ErroValidacao(mensagem);
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }

        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(
                "Subprocesso não atingiu a situação mínima necessária. Situação atual: %s. Mínima exigida: %s"
                    .formatted(subprocesso.getSituacao(), minima));
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima, String mensagem) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }

        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(mensagem);
        }
    }

    // --- Factory Operations ---

    public Subprocesso criar(CriarSubprocessoRequest request) {
        Processo processo = Processo.builder()
                .codigo(request.codProcesso())
                .build();

        Unidade unidade = Unidade.builder()
                .codigo(request.codUnidade())
                .build();

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .mapa(null)
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);

        Mapa mapa = Mapa.builder()
                .subprocesso(subprocessoSalvo)
                .build();
        Mapa mapaSalvo = mapaRepo.save(mapa);

        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.OPERACIONAL
                          || u.getTipo() == TipoUnidade.INTEROPERACIONAL
                          || u.getTipo() == TipoUnidade.RAIZ)
                .toList();

        if (unidadesElegiveis.isEmpty()) return;

        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .mapa(null)
                        .situacao(NAO_INICIADO)
                        .dataLimiteEtapa1(processo.getDataLimite())
                        .build())
                .map(Subprocesso.class::cast)
                .toList();

        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        List<Mapa> mapas = subprocessosSalvos.stream()
                .<Mapa>map(sp -> Mapa.builder()
                        .subprocesso(sp)
                        .build())
                .toList();

        List<Mapa> mapasSalvos = mapaRepo.saveAll(mapas);
        for (int i = 0; i < subprocessosSalvos.size(); i++) {
            subprocessosSalvos.get(i).setMapa(mapasSalvos.get(i));
        }

        List<Movimentacao> movimentacoes = new ArrayList<>();
        for (Subprocesso sp : subprocessosSalvos) {
            movimentacoes.add(Movimentacao.builder()
                    .subprocesso(sp)
                    .unidadeOrigem(unidadeOrigem)
                    .unidadeDestino(sp.getUnidade())
                    .usuario(usuario)
                    .descricao("Processo iniciado")
                    .build());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);

        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de revisão iniciado")
                .build());
        log.info("Subprocesso para revisão criado para unidade {}", unidade.getSigla());
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        mapaCopiado.setSubprocesso(subprocessoSalvo);

        Mapa mapaSalvo = mapaRepo.save(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de diagnóstico iniciado")
                .build());
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }

    // --- Atividade Operations ---

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = repositorioComum.buscar(Subprocesso.class, codSubprocessoDestino);
        Subprocesso spOrigem = repositorioComum.buscar(Subprocesso.class, codSubprocessoOrigem);

        // Importar atividades diretamente (sem evento assíncrono)
        servicoDeCopiaDeMapa.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo());

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                default ->
                    log.debug("Tipo de processo {} não requer atualização automática de situação no import.",
                            tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build());

        log.info("Evento de importação de atividades publicado: subprocesso {} -> {}",
                codSubprocessoOrigem, codSubprocessoDestino);
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocessoComMapa(codSubprocesso);
        List<Atividade> todasAtividades = mapaManutencaoService
                .buscarAtividadesPorMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(new ArrayList<>(atividade.getConhecimentos()))
                .build();
    }

    // --- Ajuste Mapa Operations ---

    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        Subprocesso sp = repositorioComum.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocessoComMapa(codSubprocesso);
        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = analiseFacade.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);

        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa);
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.listarConhecimentosPorMapa(codMapa);
        Map<Long, Set<Long>> associacoes = mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa);

        return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos, associacoes);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }
    }

    private void atualizarDescricoesAtividades(List<CompetenciaAjusteDto> competencias) {
        Map<Long, String> atividadeDescricoes = new HashMap<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                atividadeDescricoes.put(ativDto.codAtividade(), ativDto.nome());
            }
        }
        if (!atividadeDescricoes.isEmpty()) {
            mapaManutencaoService.atualizarDescricoesAtividadeEmLote(atividadeDescricoes);
        }
    }

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        // Carregar todas as competências envolvidas
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.buscarCompetenciasPorCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.buscarAtividadesPorCodigos(todasAtividadesIds)
                .stream()
                .collect(Collectors.toMap(Atividade::getCodigo, Function.identity()));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            Competencia competencia = mapaCompetencias.get(compDto.getCodCompetencia());
            if (competencia != null) {
                competencia.setDescricao(compDto.getNome());

                Set<Atividade> atividadesSet = new HashSet<>();
                for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                    Atividade ativ = mapaAtividades.get(ativDto.codAtividade());
                    atividadesSet.add(ativ);
                }
                competencia.setAtividades(atividadesSet);
                competenciasParaSalvar.add(competencia);
            }
        }
        mapaManutencaoService.salvarTodasCompetencias(competenciasParaSalvar);
    }

    // --- Permissions Operations ---

    public boolean podeExecutar(Usuario usuario, Acao acao, Subprocesso subprocesso) {
        return accessControlService.podeExecutar(usuario, acao, subprocesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return calcularPermissoes(sp, usuario);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        boolean isRevisao = subprocesso.getProcesso().getTipo() == TipoProcesso.REVISAO;

        Acao acaoEditarCadastro = isRevisao
                ? Acao.EDITAR_REVISAO_CADASTRO
                : Acao.EDITAR_CADASTRO;

        Acao acaoDisponibilizarCadastro = isRevisao
                ? Acao.DISPONIBILIZAR_REVISAO_CADASTRO
                : Acao.DISPONIBILIZAR_CADASTRO;

        Acao acaoDevolverCadastro = isRevisao
                ? Acao.DEVOLVER_REVISAO_CADASTRO
                : Acao.DEVOLVER_CADASTRO;

        Acao acaoAceitarCadastro = isRevisao
                ? Acao.ACEITAR_REVISAO_CADASTRO
                : Acao.ACEITAR_CADASTRO;

        Acao acaoHomologarCadastro = isRevisao
                ? Acao.HOMOLOGAR_REVISAO_CADASTRO
                : Acao.HOMOLOGAR_CADASTRO;

        return SubprocessoPermissoesDto.builder()
                .podeVerPagina(podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                .podeEditarMapa(podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso))
                .podeEditarCadastro(podeExecutar(usuario, acaoEditarCadastro, subprocesso))
                .podeVisualizarMapa(podeExecutar(usuario, Acao.VISUALIZAR_MAPA, subprocesso))
                .podeDisponibilizarMapa(podeExecutar(usuario, Acao.DISPONIBILIZAR_MAPA, subprocesso))
                .podeDisponibilizarCadastro(podeExecutar(usuario, acaoDisponibilizarCadastro, subprocesso))
                .podeDevolverCadastro(podeExecutar(usuario, acaoDevolverCadastro, subprocesso))
                .podeAceitarCadastro(podeExecutar(usuario, acaoAceitarCadastro, subprocesso))
                .podeHomologarCadastro(podeExecutar(usuario, acaoHomologarCadastro, subprocesso))
                .podeVisualizarDiagnostico(podeExecutar(usuario, Acao.VISUALIZAR_DIAGNOSTICO, subprocesso))
                .podeAlterarDataLimite(podeExecutar(usuario, Acao.ALTERAR_DATA_LIMITE, subprocesso))
                .podeVisualizarImpacto(podeExecutar(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso))
                .podeRealizarAutoavaliacao(podeExecutar(usuario, Acao.REALIZAR_AUTOAVALIACAO, subprocesso))
                .podeReabrirCadastro(podeExecutar(usuario, Acao.REABRIR_CADASTRO, subprocesso))
                .podeReabrirRevisao(podeExecutar(usuario, Acao.REABRIR_REVISAO, subprocesso))
                .podeEnviarLembrete(podeExecutar(usuario, Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso))
                .podeApresentarSugestoes(podeExecutar(usuario, Acao.APRESENTAR_SUGESTOES, subprocesso))
                .podeValidarMapa(podeExecutar(usuario, Acao.VALIDAR_MAPA, subprocesso))
                .podeAceitarMapa(podeExecutar(usuario, Acao.ACEITAR_MAPA, subprocesso))
                .podeDevolverMapa(podeExecutar(usuario, Acao.DEVOLVER_MAPA, subprocesso))
                .podeHomologarMapa(podeExecutar(usuario, Acao.HOMOLOGAR_MAPA, subprocesso))
                .build();
    }

    // --- Context Operations ---

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = buscarSubprocesso(codigo);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    @Transactional(readOnly = true)
    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        accessControlService.verificarPermissao(usuarioAutenticado, Acao.VISUALIZAR_SUBPROCESSO, sp);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());
        Usuario titular = null;
        try {
            titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
        } catch (Exception e) {
            log.warn("Erro ao buscar titular: {}", e.getMessage());
        }

        List<Movimentacao> movimentacoes = movimentacaoRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        SubprocessoPermissoesDto permissoes = calcularPermissoes(sp, usuarioAutenticado);

        return new SubprocessoDetalheResponse(
            sp, responsavel, titular, movimentacoes, permissoes
        );
    }

    @Transactional(readOnly = true)
    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        SubprocessoDetalheResponse detalhes = obterDetalhes(subprocesso, usuario);

        Unidade unidade = subprocesso.getUnidade();
        List<AtividadeDto> atividades = listarAtividadesSubprocesso(codSubprocesso);

        return new ContextoEdicaoResponse(
                unidade,
                subprocesso,
                detalhes,
                mapaFacade.obterPorCodigo(subprocesso.getMapa().getCodigo()),
                atividades
        );
    }
}
