package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.MsgValidacao;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import static sgc.seguranca.AcaoPermissao.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static sgc.organizacao.model.TipoUnidade.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoService {
    private static final String SIGLA_ADMIN = "ADMIN";
    private static final String NOME_ENTIDADE = "Subprocesso";
    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private final ComumRepo repo;
    private final AnaliseRepo analiseRepo;
    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final SubprocessoValidacaoService validacaoService;
    @Setter
    @Autowired
    @Lazy
    private SubprocessoRepo subprocessoRepo;
    @Setter
    @Autowired
    @Lazy
    private MovimentacaoRepo movimentacaoRepo;
    @Setter
    @Autowired
    @Lazy
    private CopiaMapaService copiaMapaService;
    @Lazy
    @Autowired
    @Setter
    private MapaManutencaoService mapaManutencaoService;
    private final HierarquiaService hierarquiaService;

    @Transactional(readOnly = true)
    public MapaVisualizacaoResponse mapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return mapaVisualizacaoService.obterMapaParaVisualizacao(sp);
    }

    @Transactional(readOnly = true)
    public ImpactoMapaResponse verificarImpactos(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return impactoMapaService.verificarImpactos(sp, usuario);
    }

    @Transactional
    public Mapa salvarMapa(Long codSubprocesso, SalvarMapaRequest request) {
        return salvarMapaSubprocesso(codSubprocesso, request);
    }

    @Transactional(readOnly = true)
    public Mapa mapaCompletoPorSubprocesso(Long codSubprocesso) {
        return mapaManutencaoService.mapaCompletoSubprocesso(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterSugestoes(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        String sugestoes = Optional.ofNullable(subprocesso.getMapa())
                .map(Mapa::getSugestoes)
                .orElse("");
        return Map.of("sugestoes", sugestoes);
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoComUnidade(codProcesso);
    }

    @Transactional
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        reconciliarSituacaoCadastro(subprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return subprocessoRepo.findByMapa_Codigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "Mapa ID: " + codMapa));
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarTodos() {
        return subprocessoRepo.listarTodosComFetch();
    }

    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.buscarPorProcessoEUnidadeComFetch(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "P:%d U:%d".formatted(codProcesso, codUnidade)));
    }

    @Transactional
    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        Processo processo = Processo.builder().codigo(request.codProcesso()).build();
        Unidade unidade = Unidade.builder().codigo(request.codUnidade()).build();

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .mapa(null)
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);
        Mapa mapa = Mapa.builder().subprocesso(subprocessoSalvo).build();
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapa);
        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    @Transactional
    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional
    public void excluir(Long codigo) {
        buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    public List<Subprocesso> listarPorProcessoESituacoes(Long codProcesso, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoInWithUnidade(codProcesso, situacoes);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoEUnidadeCodigosESituacoes(Long codProcesso, List<Long> codigosUnidades, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codigosUnidades).stream()
                .filter(sp -> situacoes.contains(sp.getSituacao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInComUnidade(codProcesso, codUnidade, situacoes);
    }

    private void processarAlteracoes(Subprocesso sp, AtualizarSubprocessoRequest request) {
        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder().codigo(cod).build();
            Mapa mapa = sp.getMapa();
            Long codAtual = mapa != null ? mapa.getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                sp.setMapa(m);
            }
        });

        LocalDateTime dataLimiteEtapa1 = request.dataLimiteEtapa1();
        if (dataLimiteEtapa1 != null) sp.setDataLimiteEtapa1(dataLimiteEtapa1);

        LocalDateTime dataFimEtapa1 = request.dataFimEtapa1();
        if (dataFimEtapa1 != null) sp.setDataFimEtapa1(dataFimEtapa1);

        LocalDateTime dataLimiteEtapa2 = request.dataLimiteEtapa2();
        if (dataLimiteEtapa2 != null) sp.setDataLimiteEtapa2(dataLimiteEtapa2);

        LocalDateTime dataFimEtapa2 = request.dataFimEtapa2();
        if (dataFimEtapa2 != null) sp.setDataFimEtapa2(dataFimEtapa2);
    }

    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    public Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();
        if (sp.getCodigo() == null) {
            return sp.getUnidade();
        }
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (movs.isEmpty()) {
            return sp.getUnidade();
        }
        Unidade destino = movs.getFirst().getUnidadeDestino();
        return (destino != null) ? destino : sp.getUnidade();
    }


    @Transactional
    public void atualizarParaEmAndamento(Long mapaCodigo) {
        var subprocesso = subprocessoRepo.findByMapa_Codigo(mapaCodigo).orElseThrow();
        boolean temAtividades = !mapaManutencaoService.atividadesMapaCodigoSemRels(mapaCodigo).isEmpty();
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();
        TipoProcesso tipoProcesso = subprocesso.getProcesso().getTipo();

        if (tipoProcesso == TipoProcesso.REVISAO) {
            if (situacaoAtual == NAO_INICIADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
            return;
        }

        if (!temAtividades && situacaoAtual == MAPEAMENTO_CADASTRO_EM_ANDAMENTO) {
            subprocesso.setSituacaoForcada(NAO_INICIADO);
            subprocessoRepo.save(subprocesso);
            return;
        }

        if (temAtividades && situacaoAtual == NAO_INICIADO) {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);
        }
    }

    @Transactional
    public void registrarMovimentacaoLembrete(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        var unidadeAdmin = unidadeService.buscarPorSigla(SIGLA_ADMIN);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeAdmin)
                .unidadeDestino(subprocesso.getUnidade())
                .descricao("Lembrete de prazo enviado")
                .usuario(usuario)
                .build());
        subprocesso.setLocalizacaoAtual(subprocesso.getUnidade());
    }

    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        log.info("Iniciando criação de subprocessos para mapeamento no processo {}", processo.getCodigo());
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> {
                    TipoUnidade tipo = u.getTipo();
                    return tipo == OPERACIONAL || tipo == INTEROPERACIONAL || tipo == RAIZ;
                }).toList();

        if (unidadesElegiveis.isEmpty()) {
            log.warn("Nenhuma unidade elegível encontrada para o processo {}", processo.getCodigo());
            return;
        }

        log.info("Criando {} subprocessos para o processo {}", unidadesElegiveis.size(), processo.getCodigo());

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

        List<Mapa> mapasSalvos = mapaManutencaoService.salvarMapas(mapas);
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
            sp.setLocalizacaoAtual(sp.getUnidade());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        log.info("Criando subprocesso para unidade {} no processo {}", unidade.getSigla(), processo.getCodigo());
        criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                NAO_INICIADO, "Processo iniciado");
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Subprocesso subprocessoSalvo = criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, "Processo de diagnóstico iniciado");
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }

    private Subprocesso criarSubprocessoComMapa(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa,
                                                Unidade unidadeOrigem, Usuario usuario,
                                                SituacaoSubprocesso situacaoInicial, String descricaoMovimentacao) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(situacaoInicial)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente);
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao(descricaoMovimentacao)
                .build());

        subprocessoSalvo.setLocalizacaoAtual(unidade);
        return subprocessoSalvo;
    }

    public Mapa salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();

        boolean eraVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        Mapa mapa = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        if (eraVazio && temNovasCompetencias) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }

        return mapa;
    }

    public Mapa adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        Long codMapa = mapa.getCodigo();

        boolean eraVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();

        mapaManutencaoService.criarCompetenciaComAtividades(mapa, request.descricao(), request.atividadesIds());

        if (eraVazio) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }

        return mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    public Mapa atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaRequest request) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = sp.getMapa();
        return mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        mapaManutencaoService.removerCompetencia(codCompetencia);

        boolean ficouVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        if (ficouVazio) {
            atualizarSituacaoMapaVazio(subprocesso, true);
        }

        return mapaManutencaoService.mapaCodigo(subprocesso.getMapa().getCodigo());
    }

    private void atualizarSituacaoMapaVazio(Subprocesso subprocesso, boolean ficouVazio) {
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();

        if (ficouVazio) {
            if (situacaoAtual == SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO) {
                subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", subprocesso.getCodigo());
            } else if (situacaoAtual == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para REVISAO_CADASTRO_HOMOLOGADA (mapa ficou vazio)", subprocesso.getCodigo());
            }
        } else {
            if (situacaoAtual == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (situacaoAtual == SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                "Mapa só pode ser editado nas situações 'Mapa criado', 'Cadastro homologado', 'Mapa ajustado' ou 'Revisão do cadastro homologada'. Se o mapa estiver com sugestões, valide as sugestões primeiro para retornar à situação editável. Situação atual: %s".formatted(sp.getSituacao()),
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        return sp;
    }


    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        log.info("Salvando ajustes no mapa do subprocesso {} ({} competências)", codSubprocesso, competencias.size());
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        log.info("Recuperando mapa para ajustes do subprocesso {}", codSubprocesso);
        Subprocesso sp = subprocessoRepo.buscarPorCodigoComMapaEAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codSubprocesso));
        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = listarAnalisesPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);

        List<Competencia> competencias = mapaManutencaoService.competenciasCodMapaSemRels(codMapa);
        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoSemRels(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.conhecimentosCodMapa(codMapa);

        return MapaAjusteDto.of(sp, analise, competencias, atividades, conhecimentos);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroValidacao(
                    MsgValidacao.AJUSTES_ESTADOS_ESPECIFICOS
                            .formatted(sp.getSituacao()));
        }
    }

    private void atualizarDescricoesAtividades(List<CompetenciaAjusteDto> competencias) {
        Map<Long, String> atividadeDescricoes = new HashMap<>();
        competencias.forEach(compDto -> compDto.getAtividades().forEach(
                ativDto -> atividadeDescricoes.put(ativDto.codAtividade(), ativDto.nome()))
        );

        if (!atividadeDescricoes.isEmpty()) {
            mapaManutencaoService.atualizarDescricoesAtividadeEmBloco(atividadeDescricoes);
        }
    }

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.competenciasCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.atividadesCodigos(todasAtividadesIds)
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
        mapaManutencaoService.salvarCompetencias(competenciasParaSalvar);
    }

    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo).orElseThrow();
        reconciliarSituacaoCadastro(sp);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        String localizacaoAtual = siglaUnidade;

        sgc.organizacao.dto.ResponsavelDto responsavel = usuarioFacade.buscarResponsabilidadeDetalhadaAtual(siglaUnidade);
        Usuario titular = usuarioFacade.buscarPorLogin(sp.getUnidade().getTituloTitular());

        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (!movs.isEmpty()) {
            Unidade destino = movs.getFirst().getUnidadeDestino();
            if (destino != null) {
                localizacaoAtual = destino.getSigla();
            }
        }

        List<MovimentacaoDto> movimentacoes = movs.stream()
                .map(MovimentacaoDto::from)
                .toList();

        PermissoesSubprocessoDto permissoes = obterPermissoesUI(sp, usuarioAutenticado);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(sp)
                .responsavel(responsavel)
                .titular(titular)
                .movimentacoes(movimentacoes)
                .localizacaoAtual(localizacaoAtual)
                .permissoes(permissoes)
                .build();
    }

    @Transactional
    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = subprocessoRepo.buscarPorCodigoComMapaEAtividades(codSubprocesso).orElseThrow();
        reconciliarSituacaoCadastro(sp);
        SubprocessoDetalheResponse detalhes = obterDetalhes(sp, usuario);

        Unidade unidadeSp = sp.getUnidade();
        List<AtividadeDto> atividades = listarAtividadesSubprocesso(codSubprocesso);

        return new ContextoEdicaoResponse(
                unidadeSp,
                sp,
                detalhes,
                mapaManutencaoService.mapaCompletoSubprocesso(codSubprocesso),
                atividades
        );
    }

    private void reconciliarSituacaoCadastro(Subprocesso subprocesso) {
        if (subprocesso.getMapa() == null) {
            return;
        }

        TipoProcesso tipoProcesso = subprocesso.getProcesso().getTipo();
        if (tipoProcesso != TipoProcesso.MAPEAMENTO) {
            return;
        }

        boolean temAtividades = !mapaManutencaoService.atividadesMapaCodigoSemRels(subprocesso.getMapa().getCodigo()).isEmpty();
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();

        if (temAtividades && situacaoAtual == NAO_INICIADO) {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);
            return;
        }

        if (!temAtividades && situacaoAtual == MAPEAMENTO_CADASTRO_EM_ANDAMENTO) {
            subprocesso.setSituacaoForcada(NAO_INICIADO);
            subprocessoRepo.save(subprocesso);
        }
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        Processo processo = sp.getProcesso();

        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();

        if (processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO) {
            // Para processos finalizados, bloqueia toda edição mas mantém acesso de visualização (CDU-18, CDU-17)
            Unidade unidadeUsuario = unidadeService.buscarPorCodigo(usuario.getUnidadeAtivaCodigo());
            return PermissoesSubprocessoDto.builder()
                    .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario))
                    .habilitarAcessoMapa(verificarAcessoMapaHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario))
                    .build();
        }

        Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();
        Long codUnidadeLocalizacao = obterUnidadeLocalizacao(sp).getCodigo();
        boolean mesmaUnidade = Objects.equals(codUnidadeUsuario, codUnidadeLocalizacao);

        boolean temMapaVigente = unidadeService.verificarMapaVigente(sp.getUnidade().getCodigo());

        return construirPermissoes(mesmaUnidade, usuario, sp, temMapaVigente);
    }

    private PermissoesSubprocessoDto construirPermissoes(boolean mesmaUnidade, Usuario usuario, Subprocesso sp, boolean temMapaVigente) {
        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();
        Unidade unidadeUsuario = unidadeService.buscarPorCodigo(usuario.getUnidadeAtivaCodigo());

        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario);
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario);

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(isChefe && Set.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDisponibilizarCadastro(isChefe && Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDevolverCadastro((isGestor || isAdmin) && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeAceitarCadastro(isGestor && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeHomologarCadastro(isAdmin && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeEditarMapa(verificarEditarMapa(isAdmin, situacao))
                .podeDisponibilizarMapa(isAdmin && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
                .podeValidarMapa(isChefe && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeApresentarSugestoes(isChefe && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeVerSugestoes((isGestor || isAdmin) && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
                .podeDevolverMapa(verificarGerirMapa(isGestor || isAdmin, situacao))
                .podeAceitarMapa(verificarGerirMapa(isGestor, situacao))
                .podeHomologarMapa(verificarGerirMapa(isAdmin, situacao))
                .podeVisualizarImpacto(verificarVisualizarImpacto(temMapaVigente, mesmaUnidade, isChefe, isGestor, isAdmin, situacao))
                .podeAlterarDataLimite(isAdmin)
                .podeReabrirCadastro(isAdmin && situacao.ordinal() >= SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO.ordinal() && situacao.name().startsWith("MAPEAMENTO"))
                .podeReabrirRevisao(isAdmin && situacao.ordinal() >= SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO.ordinal() && situacao.name().startsWith("REVISAO"))
                .podeEnviarLembrete(isAdmin)
                .mesmaUnidade(mesmaUnidade)
                .habilitarAcessoCadastro(habilitarAcessoCadastro)
                .habilitarAcessoMapa(habilitarAcessoMapa)
                .build();
    }

    private boolean verificarAcessoCadastroHabilitado(Perfil perfil, SituacaoSubprocesso situacao, Unidade unidadeAlvo, Unidade unidadeUsuario) {
        if (perfil == Perfil.CHEFE) {
            return Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
        }

        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(situacao);
        if (perfil == Perfil.ADMIN) {
            return cadastroDisponibilizado;
        }
        if (perfil == Perfil.GESTOR) {
            return cadastroDisponibilizado && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        }
        return cadastroDisponibilizado && Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
    }

    private boolean verificarAcessoMapaHabilitado(Perfil perfil, SituacaoSubprocesso situacao, Unidade unidadeAlvo, Unidade unidadeUsuario) {
        if (perfil == Perfil.ADMIN) {
            return verificarMapaHabilitadoParaAdmin(situacao);
        }

        if (perfil == Perfil.GESTOR) {
            return verificarMapaDisponibilizadoParaVisualizacao(situacao)
                    && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        }

        if (perfil == Perfil.CHEFE || perfil == Perfil.SERVIDOR) {
            return verificarMapaDisponibilizadoParaVisualizacao(situacao)
                    && Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
        }
        return false;
    }

    private boolean verificarCadastroDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        if (situacao.name().startsWith("MAPEAMENTO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO.ordinal();
        }
        if (situacao.name().startsWith("REVISAO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA.ordinal();
        }
        return false;
    }

    private boolean verificarMapaDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        if (situacao.name().startsWith("MAPEAMENTO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO.ordinal();
        }
        if (situacao.name().startsWith("REVISAO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO.ordinal();
        }
        return false;
    }

    private boolean verificarMapaHabilitadoParaAdmin(SituacaoSubprocesso situacao) {
        if (situacao.name().startsWith("MAPEAMENTO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO.ordinal();
        }
        if (situacao.name().startsWith("REVISAO")) {
            return situacao.ordinal() >= SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA.ordinal();
        }
        return false;
    }

    private boolean verificarVisualizarImpacto(boolean temMapaVigente, boolean mesmaUnidade, boolean isChefe, boolean isGestor, boolean isAdmin, SituacaoSubprocesso situacao) {
        return temMapaVigente && (
                (mesmaUnidade && isChefe && situacao == REVISAO_CADASTRO_EM_ANDAMENTO) ||
                        (mesmaUnidade && isGestor && situacao == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) ||
                        (isAdmin && Set.of(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).contains(situacao))
        );
    }

    private boolean verificarEditarMapa(boolean isAdmin, SituacaoSubprocesso situacao) {
        return isAdmin && Set.of(
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO).contains(situacao);
    }

    private boolean verificarGerirMapa(boolean isPermitido, SituacaoSubprocesso situacao) {
        return isPermitido && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO).contains(situacao);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem, List<Long> codigosAtividades) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        if (!permissionEvaluator.verificarPermissao(usuario, spDestino, EDITAR_CADASTRO)) {
            throw new ErroAcessoNegado(MsgValidacao.SEM_PERMISSAO_IMPORTAR);
        }
        validarSituacaoParaImportacao(spDestino);

        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        if (!permissionEvaluator.verificarPermissao(usuario, spOrigem, CONSULTAR_PARA_IMPORTACAO)) {
            throw new ErroAcessoNegado(MsgValidacao.SEM_PERMISSAO_CONSULTAR_ORIGEM);
        }

        Long codMapaOrigem = spOrigem.getMapa().getCodigo();
        Long codMapaDestino = spDestino.getMapa().getCodigo();
        log.info("Importando {} atividades do mapa #{} para o mapa #{}", codigosAtividades != null ? codigosAtividades.size() : "todas as", codMapaOrigem, codMapaDestino);
        int importadas = copiaMapaService.importarAtividadesDeOutroMapa(codMapaOrigem, codMapaDestino, codigosAtividades);

        if (codigosAtividades != null && importadas == 0 && !codigosAtividades.isEmpty()) {
            throw new ErroValidacao(MsgValidacao.IMPORTACAO_ATIVIDADES_DUPLICADAS);
        }

        if (spDestino.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();
            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                default -> log.debug("Tipo de processo {} não requer atualização de situação.", tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build();

        movimentacaoRepo.save(movimentacao);
        spDestino.setLocalizacaoAtual(spDestino.getUnidade());
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.buscarPorCodigoComMapaEAtividades(codSubprocesso).orElseThrow();

        Long codMapa = subprocesso.getMapa().getCodigo();
        List<Atividade> todasAtividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);

        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesParaImportacao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.buscarPorCodigoComMapaEAtividades(codSubprocesso).orElseThrow();
        if (subprocesso.getProcesso() == null || subprocesso.getProcesso().getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao(MsgValidacao.IMPORTACAO_SO_PROCESSOS_FINALIZADOS);
        }
        return listarAtividadesSubprocesso(codSubprocesso);
    }

    private void validarSituacaoParaImportacao(Subprocesso sp) {
        SituacaoSubprocesso situacaoSp = sp.getSituacao();

        if (!SITUACOES_PERMITIDAS_IMPORTACAO.contains(situacaoSp)) {
            String msg = MsgValidacao.SITUACAO_IMPEDE_IMPORTACAO.formatted(situacaoSp);
            throw new ErroValidacao(msg);
        }
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(new ArrayList<>(atividade.getConhecimentos()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso, TipoAnalise tipo) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                .filter(a -> a.getTipo() == tipo)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarAnalisesPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.CADASTRO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return listarAnalisesPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.VALIDACAO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    public AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        UnidadeDto unidade = UnidadeDto.fromEntity(unidadeService.buscarPorCodigo(analise.getUnidadeCodigo()));

        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .unidadeSigla(unidade.getSigla())
                .unidadeNome(unidade.getNome())
                .analistaUsuarioTitulo(analise.getUsuarioTitulo())
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoComUnidade(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoComUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public Unidade obterLocalizacaoAtual(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();
        Unidade loc = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .filter(m -> m.getUnidadeDestino() != null)
                .map(Movimentacao::getUnidadeDestino)
                .orElse(sp.getUnidade());
        sp.setLocalizacaoAtual(loc);
        return loc;
    }
}
