package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
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
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Collections.*;
import static sgc.organizacao.model.TipoUnidade.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoService {

    private static final String SIGLA_ADMIN = "ADMIN";
    private static final String NOME_ENTIDADE = "Subprocesso";
    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
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
    public Map<String, Object> obterSugestoes() {
        return Map.of("sugestoes", "");
    }

    @Transactional(readOnly = true)
    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.findByIdWithMapaAndAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    @Transactional(readOnly = true)
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
        return subprocessoRepo.findByMapa_Codigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "Mapa ID: " + codMapa));
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidades() {
        return subprocessoRepo.findAllComFetch();
    }

    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoWithFetch(codProcesso, codUnidade)
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

    public List<Subprocesso> listarPorProcessoESituacoes(Long processoId, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoInWithUnidade(processoId, situacoes);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long processoId, Long unidadeId, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInWithUnidade(processoId, unidadeId, situacoes);
    }

    private void processarAlteracoes(Subprocesso sp, AtualizarSubprocessoRequest request) {
        Optional.of(request.codMapa()).ifPresent(cod -> {
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
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) return emptyList();

        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(mapa.getCodigo());
        return atividades.isEmpty()
                ? emptyList()
                : atividades.stream().filter(a -> a.getConhecimentos().isEmpty()).toList();
    }


    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
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
        if (subprocesso.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();

            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
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
                        .situacao(SituacaoSubprocesso.NAO_INICIADO)
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
        log.info("Criando subprocesso para unidade {} no processo de revisão {}", unidade.getSigla(), processo.getCodigo());
        criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                SituacaoSubprocesso.NAO_INICIADO, "Processo iniciado");
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
                "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(sp.getSituacao()),
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
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codSubprocesso));
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
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
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
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codigo).orElseThrow();
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        String localizacaoAtual = siglaUnidade;

        Usuario responsavel = usuarioFacade.buscarResponsavelAtual(siglaUnidade);
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

    @Transactional(readOnly = true)
    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow();
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

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        Processo processo = sp.getProcesso();

        if (processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO) {
            return PermissoesSubprocessoDto.builder().build();
        }

        Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();
        Long codUnidadeLocalizacao = obterUnidadeLocalizacao(sp).getCodigo();
        boolean mesmaUnidade = Objects.equals(codUnidadeUsuario, codUnidadeLocalizacao);

        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();
        boolean temMapaVigente = unidadeService.verificarMapaVigente(sp.getUnidade().getCodigo());

        return construirPermissoes(mesmaUnidade, perfil, situacao, temMapaVigente);
    }

    private PermissoesSubprocessoDto construirPermissoes(boolean mesmaUnidade, Perfil perfil, SituacaoSubprocesso situacao, boolean temMapaVigente) {
        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(mesmaUnidade && isChefe && Set.of(SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDisponibilizarCadastro(mesmaUnidade && isChefe && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDevolverCadastro(mesmaUnidade && (isGestor || isAdmin) && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeAceitarCadastro(mesmaUnidade && isGestor && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeHomologarCadastro(mesmaUnidade && isAdmin && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeEditarMapa(verificarEditarMapa(mesmaUnidade, isAdmin, situacao))
                .podeDisponibilizarMapa(mesmaUnidade && isAdmin && Set.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
                .podeValidarMapa(mesmaUnidade && isChefe && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeApresentarSugestoes(mesmaUnidade && isChefe && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeDevolverMapa(verificarGerirMapa(mesmaUnidade, isGestor || isAdmin, situacao))
                .podeAceitarMapa(verificarGerirMapa(mesmaUnidade, isGestor, situacao))
                .podeHomologarMapa(verificarGerirMapa(mesmaUnidade, isAdmin, situacao))
                .podeVisualizarImpacto(verificarVisualizarImpacto(temMapaVigente, mesmaUnidade, isChefe, isGestor, isAdmin, situacao))
                .podeAlterarDataLimite(isAdmin)
                .podeReabrirCadastro(isAdmin && situacao.ordinal() >= SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO.ordinal() && situacao.name().startsWith("MAPEAMENTO"))
                .podeReabrirRevisao(isAdmin && situacao.ordinal() >= SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO.ordinal() && situacao.name().startsWith("REVISAO"))
                .podeEnviarLembrete(isAdmin)
                .build();
    }

    private boolean verificarVisualizarImpacto(boolean temMapaVigente, boolean mesmaUnidade, boolean isChefe, boolean isGestor, boolean isAdmin, SituacaoSubprocesso situacao) {
        return temMapaVigente && (
                (mesmaUnidade && isChefe && situacao == SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) ||
                (mesmaUnidade && isGestor && situacao == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) ||
                (isAdmin && Set.of(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).contains(situacao))
        );
    }

    private boolean verificarEditarMapa(boolean mesmaUnidade, boolean isAdmin, SituacaoSubprocesso situacao) {
        return mesmaUnidade && isAdmin && Set.of(
                SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO).contains(situacao);
    }

    private boolean verificarGerirMapa(boolean mesmaUnidade, boolean isPermitido, SituacaoSubprocesso situacao) {
        return mesmaUnidade && isPermitido && Set.of(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO).contains(situacao);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem, List<Long> codigosAtividades) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        if (!permissionEvaluator.checkPermission(usuario, spDestino, "EDITAR_CADASTRO")) {
            throw new ErroAcessoNegado("Usuário não tem permissão para importar atividades.");
        }
        validarSituacaoParaImportacao(spDestino);

        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        if (!permissionEvaluator.checkPermission(usuario, spOrigem, "CONSULTAR_PARA_IMPORTACAO")) {
            throw new ErroAcessoNegado("Usuário não tem permissão para consultar o subprocesso de origem.");
        }

        Long codMapaOrigem = spOrigem.getMapa().getCodigo();
        Long codMapaDestino = spDestino.getMapa().getCodigo();
        log.info("Importando {} atividades do mapa #{} para o mapa #{}", 
                codigosAtividades != null ? codigosAtividades.size() : "todas as", codMapaOrigem, codMapaDestino);
        copiaMapaService.importarAtividadesDeOutroMapa(codMapaOrigem, codMapaDestino, codigosAtividades);

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();
            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
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
        Subprocesso subprocesso = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow();

        Long codMapa = subprocesso.getMapa().getCodigo();
        List<Atividade> todasAtividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);

        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private void validarSituacaoParaImportacao(Subprocesso sp) {
        SituacaoSubprocesso situacaoSp = sp.getSituacao();

        if (!SITUACOES_PERMITIDAS_IMPORTACAO.contains(situacaoSp)) {
            String msg = "Situação do subprocesso não permite importação. Situação atual: %s".formatted(situacaoSp);
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
        UnidadeDto unidade = UnidadeDto.fromEntity(unidadeService.buscarPorId(analise.getUnidadeCodigo()));

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


}