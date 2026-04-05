package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import static sgc.organizacao.model.TipoUnidade.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SubprocessoService {
    private final ComumRepo repo;
    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService copiaMapaService;
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoConsultaService consultaService;

    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);

    @Transactional
    public Mapa salvarMapa(Long codSubprocesso, SalvarMapaRequest request) {
        return salvarMapaSubprocesso(codSubprocesso, request);
    }

    @Transactional
    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        Processo processo = repo.buscar(Processo.class, request.codProcesso());
        Unidade unidade = unidadeService.buscarPorCodigo(request.codUnidade());

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);
        Mapa mapa = Mapa.builder().subprocesso(subprocessoSalvo).build();
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapa);
        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    @Transactional
    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoCommand command) {
        Subprocesso subprocesso = consultaService.buscarSubprocesso(codigo);
        processarVinculos(subprocesso, command.vinculos());
        processarPrazos(subprocesso, command.prazos());
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional
    public void excluir(Long codigo) {
        consultaService.buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    private void processarVinculos(Subprocesso sp, AtualizarVinculosSubprocessoCommand command) {
        Optional.ofNullable(command.codUnidade())
                .map(unidadeService::buscarPorCodigo)
                .ifPresent(sp::setUnidade);

        Optional.ofNullable(command.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder().codigo(cod).build();
            Mapa mapa = sp.getMapa();
            Long codAtual = mapa != null ? mapa.getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                sp.setMapa(m);
            }
        });
    }

    private void processarPrazos(Subprocesso sp, AtualizarPrazosSubprocessoCommand command) {
        command.dataLimiteEtapa1().ifPresent(sp::setDataLimiteEtapa1);
        command.dataFimEtapa1().ifPresent(sp::setDataFimEtapa1);
        command.dataLimiteEtapa2().ifPresent(sp::setDataLimiteEtapa2);
        command.dataFimEtapa2().ifPresent(sp::setDataFimEtapa2);
    }

    @Transactional
    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        List<Unidade> unidadesElegiveis = listarUnidadesElegiveisParaMapeamento(unidades);

        if (unidadesElegiveis.isEmpty()) {
            log.warn("Nenhuma unidade elegível encontrada para o processo {}", processo.getCodigo());
            return;
        }

        log.info("Criando {} subprocessos para o processo {}", unidadesElegiveis.size(), processo.getCodigo());
        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> criarSubprocessoInicial(processo, unidade, NAO_INICIADO))
                .toList();

        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        List<Mapa> mapas = subprocessosSalvos.stream()
                .<Mapa>map(sp -> Mapa.builder()
                        .subprocesso(sp)
                        .build())
                .toList();

        List<Mapa> mapasSalvos = mapaManutencaoService.salvarMapas(mapas);
        associarMapasASubprocessos(subprocessosSalvos, mapasSalvos);

        List<Movimentacao> movimentacoes = subprocessosSalvos.stream()
                .map(subprocesso -> criarMovimentacaoInicial(subprocesso, unidadeOrigem, usuario, "Processo iniciado"))
                .toList();
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario, NAO_INICIADO, "Processo iniciado");
        log.info("Criado subprocesso para unidade {}", unidade.getSigla());
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Subprocesso subprocessoSalvo = criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, "Processo de diagnóstico iniciado");
        log.info("Criado subprocesso {} para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }

    private Subprocesso criarSubprocessoComMapa(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa,
                                                Unidade unidadeOrigem, Usuario usuario,
                                                SituacaoSubprocesso situacaoInicial, String descMovimentacao) {
        Long codMapaVigente = obterCodigoMapaVigenteObrigatorio(unidadeMapa, unidade);
        Subprocesso subprocesso = criarSubprocessoInicial(processo, unidade, situacaoInicial);

        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente, subprocessoSalvo);

        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(criarMovimentacaoInicial(subprocessoSalvo, unidadeOrigem, usuario, descMovimentacao));

        return subprocessoSalvo;
    }

    private List<Unidade> listarUnidadesElegiveisParaMapeamento(Collection<Unidade> unidades) {
        return unidades.stream()
                .filter(unidade -> {
                    TipoUnidade tipo = unidade.getTipo();
                    return tipo == OPERACIONAL || tipo == INTEROPERACIONAL;
                })
                .toList();
    }

    private Subprocesso criarSubprocessoInicial(Processo processo, Unidade unidade, SituacaoSubprocesso situacaoInicial) {
        return Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .situacao(situacaoInicial)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
    }

    private void associarMapasASubprocessos(List<Subprocesso> subprocessos, List<Mapa> mapas) {
        for (int i = 0; i < subprocessos.size(); i++) {
            subprocessos.get(i).setMapa(mapas.get(i));
        }
    }

    private Long obterCodigoMapaVigenteObrigatorio(UnidadeMapa unidadeMapa, Unidade unidade) {
        Mapa mapaVigente = unidadeMapa.getMapaVigente();
        if (mapaVigente == null) {
            throw new IllegalStateException("Unidade %s sem mapa vigente para revisão/diagnóstico".formatted(unidade.getSigla()));
        }
        return mapaVigente.getCodigo();
    }

    private Movimentacao criarMovimentacaoInicial(Subprocesso subprocesso, Unidade unidadeOrigem, Usuario usuario, String descricao) {
        return Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(subprocesso.getUnidade())
                .usuario(usuario)
                .descricao(descricao)
                .build();
    }

    @Transactional
    public Mapa salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        ContextoEdicaoMapa contexto = carregarContextoEdicaoMapa(codSubprocesso);
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        Mapa mapa = mapaSalvamentoService.salvarMapaCompleto(contexto.codMapa(), request);
        mapaManutencaoService.reconciliarSituacaoSubprocesso(contexto.subprocesso());
        atualizarSituacaoAposPreenchimentoMapa(contexto.subprocesso(), contexto.mapaEstavaVazio() && temNovasCompetencias);

        return mapa;
    }

    @Transactional
    public Mapa adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        ContextoEdicaoMapa contexto = carregarContextoEdicaoMapa(codSubprocesso);
        validarCompetenciaParaCriacao(request);
        mapaManutencaoService.criarCompetenciaComAtividades(
                contexto.mapa(), request.descricao(), request.atividadesIds());
        atualizarSituacaoAposPreenchimentoMapa(contexto.subprocesso(), contexto.mapaEstavaVazio());
        return mapaManutencaoService.mapaCodigo(contexto.codMapa());
    }

    public Mapa atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaRequest request) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = consultaService.obterMapaObrigatorio(sp);
        return mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    private void validarCompetenciaParaCriacao(CompetenciaRequest request) {
        if (request.atividadesIds().isEmpty()) {
            throw new ErroValidacao(Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE);
        }
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        ContextoEdicaoMapa contexto = carregarContextoEdicaoMapa(codSubprocesso);
        mapaManutencaoService.removerCompetencia(codCompetencia);

        boolean ficouVazio = mapaManutencaoService.competenciasCodMapa(contexto.codMapa()).isEmpty();
        mapaManutencaoService.reconciliarSituacaoSubprocesso(contexto.subprocesso());
        atualizarSituacaoAposEsvaziamentoMapa(contexto.subprocesso(), ficouVazio);

        return mapaManutencaoService.mapaCodigo(contexto.codMapa());
    }

    private ContextoEdicaoMapa carregarContextoEdicaoMapa(Long codSubprocesso) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = Optional.ofNullable(subprocesso.getMapa())
                .orElseGet(() -> consultaService.obterMapaObrigatorio(subprocesso));
        Long codMapa = Optional.ofNullable(mapa.getCodigo())
                .orElseGet(() -> consultaService.obterCodigoMapaObrigatorio(subprocesso));
        boolean mapaEstavaVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        return new ContextoEdicaoMapa(subprocesso, mapa, codMapa, mapaEstavaVazio);
    }

    private void atualizarSituacaoAposPreenchimentoMapa(Subprocesso subprocesso, boolean deveAtualizarSituacao) {
        if (deveAtualizarSituacao) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }
    }

    private void atualizarSituacaoAposEsvaziamentoMapa(Subprocesso subprocesso, boolean mapaFicouVazio) {
        if (mapaFicouVazio) {
            atualizarSituacaoMapaVazio(subprocesso, true);
        }
    }

    private void atualizarSituacaoMapaVazio(Subprocesso subprocesso, boolean ficouVazio) {
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();

        if (ficouVazio) {
            if (situacaoAtual == MAPEAMENTO_MAPA_CRIADO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", subprocesso.getCodigo());
            } else if (situacaoAtual == REVISAO_MAPA_AJUSTADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para REVISAO_CADASTRO_HOMOLOGADA (mapa ficou vazio)", subprocesso.getCodigo());
            }
        } else {
            if (situacaoAtual == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (situacaoAtual == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    @Transactional(readOnly = true)
    public Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                "Mapa só pode ser editado nas situações 'Mapa criado', 'Cadastro homologado', 'Mapa ajustado' ou 'Revisão do cadastro homologada'. Se o mapa estiver com sugestões, valide as sugestões primeiro para retornar à situação editável. Situação atual: %s".formatted(sp.getSituacao()),
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                REVISAO_CADASTRO_HOMOLOGADA,
                REVISAO_MAPA_AJUSTADO);
        return sp;
    }

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        log.info("Salvando ajustes no mapa do subprocesso {} ({} competências)", codSubprocesso, competencias.size());
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != REVISAO_MAPA_AJUSTADO) {
            throw new ErroValidacao(
                    Mensagens.AJUSTES_ESTADOS_ESPECIFICOS
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

    @Transactional
    public boolean importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem, List<Long> codigosAtividades) {
        ImportacaoAtividadesContexto contexto = montarContextoImportacaoAtividades(codSubprocessoDestino, codSubprocessoOrigem);
        int totalSolicitado = codigosAtividades.size();

        log.info("Importando {} atividades do mapa #{} para o mapa #{}",
                totalSolicitado, contexto.codMapaOrigem(), contexto.codMapaDestino());

        int totalImportado = copiaMapaService.importarAtividadesDeOutroMapa(
                contexto.codMapaOrigem(), contexto.codMapaDestino(), codigosAtividades);

        atualizarSituacaoDestinoAposImportacao(contexto.subprocessoDestino());
        registrarMovimentacaoImportacao(contexto);

        return houveDuplicidadeNaImportacao(totalSolicitado, totalImportado);
    }

    private ImportacaoAtividadesContexto montarContextoImportacaoAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        Subprocesso subprocessoDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        validarDestinoParaImportacao(subprocessoDestino);

        Usuario usuario = usuarioFacade.usuarioAutenticado();
        validarPermissaoImportacaoNoDestino(usuario, subprocessoDestino);

        Subprocesso subprocessoOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        validarPermissaoConsultaNaOrigem(usuario, subprocessoOrigem);

        return new ImportacaoAtividadesContexto(
                subprocessoDestino,
                subprocessoOrigem,
                usuario,
                consultaService.obterCodigoMapaObrigatorio(subprocessoDestino),
                consultaService.obterCodigoMapaObrigatorio(subprocessoOrigem));
    }

    private void validarDestinoParaImportacao(Subprocesso subprocessoDestino) {
        validacaoService.validarSituacaoPermitida(subprocessoDestino,
                Mensagens.SITUACAO_IMPEDE_IMPORTACAO.formatted(subprocessoDestino.getSituacao()),
                SITUACOES_PERMITIDAS_IMPORTACAO.toArray(new SituacaoSubprocesso[0]));
    }

    private void validarPermissaoImportacaoNoDestino(Usuario usuario, Subprocesso subprocessoDestino) {
        if (!permissionEvaluator.verificarPermissao(usuario, subprocessoDestino, EDITAR_CADASTRO)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_IMPORTAR);
        }
    }

    private void validarPermissaoConsultaNaOrigem(Usuario usuario, Subprocesso subprocessoOrigem) {
        if (!permissionEvaluator.verificarPermissao(usuario, subprocessoOrigem, CONSULTAR_PARA_IMPORTACAO)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_CONSULTAR_ORIGEM);
        }
    }

    private void atualizarSituacaoDestinoAposImportacao(Subprocesso subprocessoDestino) {
        if (subprocessoDestino.getSituacao() != NAO_INICIADO) {
            return;
        }

        switch (subprocessoDestino.getProcesso().getTipo()) {
            case MAPEAMENTO -> subprocessoDestino.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            case REVISAO -> subprocessoDestino.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            default -> {
                log.debug("Tipo de processo {} não requer atualização de situação.",
                        subprocessoDestino.getProcesso().getTipo());
                return;
            }
        }

        subprocessoRepo.save(subprocessoDestino);
    }

    private void registrarMovimentacaoImportacao(ImportacaoAtividadesContexto contexto) {
        Unidade unidadeOrigem = contexto.subprocessoOrigem().getUnidade();
        String descricao = Mensagens.HIST_IMPORTACAO_ATIVIDADES.formatted(
                contexto.subprocessoOrigem().getCodigo(),
                unidadeOrigem.getSigla());

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(contexto.subprocessoDestino())
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(contexto.subprocessoDestino().getUnidade())
                .descricao(descricao)
                .usuario(contexto.usuario())
                .build());
    }

    private boolean houveDuplicidadeNaImportacao(int totalSolicitado, int totalImportado) {
        return totalSolicitado > 0 && totalImportado < totalSolicitado;
    }

    private record ContextoEdicaoMapa(
            Subprocesso subprocesso,
            Mapa mapa,
            Long codMapa,
            boolean mapaEstavaVazio
    ) {
    }

    private record ImportacaoAtividadesContexto(
            Subprocesso subprocessoDestino,
            Subprocesso subprocessoOrigem,
            Usuario usuario,
            Long codMapaDestino,
            Long codMapaOrigem
    ) {
    }
}
