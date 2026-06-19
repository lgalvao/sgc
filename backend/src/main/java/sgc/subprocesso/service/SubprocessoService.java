package sgc.subprocesso.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.service.DiagnosticoFluxoService;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static sgc.organizacao.model.TipoUnidade.INTEROPERACIONAL;
import static sgc.organizacao.model.TipoUnidade.OPERACIONAL;
import static sgc.seguranca.AcaoPermissao.CONSULTAR_PARA_IMPORTACAO;
import static sgc.seguranca.AcaoPermissao.EDITAR_CADASTRO;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SubprocessoService {
    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private final ComumRepo repo;
    private final UnidadeService unidadeService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService copiaMapaService;
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoConsultaService consultaService;
    private final DiagnosticoFluxoService diagnosticoFluxoService;

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

    private void processarVinculos(Subprocesso sp, @Nullable AtualizarVinculosSubprocessoCommand command) {
        if (command == null) return;
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

    private void processarPrazos(Subprocesso sp, @Nullable AtualizarPrazosSubprocessoCommand command) {
        if (command == null) return;
        Optional.of(command.dataLimiteEtapa1()).flatMap(Function.identity()).ifPresent(sp::setDataLimiteEtapa1);
        Optional.of(command.dataFimEtapa1()).flatMap(Function.identity()).ifPresent(sp::setDataFimEtapa1);
        Optional.of(command.dataLimiteEtapa2()).flatMap(Function.identity()).ifPresent(sp::setDataLimiteEtapa2);
        Optional.of(command.dataFimEtapa2()).flatMap(Function.identity()).ifPresent(sp::setDataFimEtapa2);
    }

    @Transactional
    public void criarParaMapeamento(CriarSubprocessosMapeamentoCommand command) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Processo processo = command.processo();
        Unidade unidadeOrigem = command.unidadeOrigem();
        List<Unidade> unidadesElegiveis = listarUnidadesElegiveisParaMapeamento(command.unidades());

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
                .map(subprocesso -> criarMovimentacaoInicial(subprocesso, unidadeOrigem, usuario, Mensagens.HIST_PROCESSO_INICIADO))
                .toList();
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(CriarSubprocessoComMapaCommand command) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        criarSubprocessoComMapa(ContextoCriacaoSubprocesso.builder()
                .processo(command.processo())
                .unidade(command.unidade())
                .unidadeMapa(command.unidadeMapa())
                .unidadeOrigem(command.unidadeOrigem())
                .usuario(usuario)
                .situacaoInicial(NAO_INICIADO)
                .descMovimentacao(Mensagens.HIST_PROCESSO_INICIADO)
                .build());
        log.info("Criado subprocesso para unidade {}", command.unidade().getSigla());
    }

    public void criarParaDiagnostico(CriarSubprocessoComMapaCommand command) {
        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        Subprocesso subprocessoSalvo = criarSubprocessoComMapa(ContextoCriacaoSubprocesso.builder()
                .processo(command.processo())
                .unidade(command.unidade())
                .unidadeMapa(command.unidadeMapa())
                .unidadeOrigem(command.unidadeOrigem())
                .usuario(usuario)
                .situacaoInicial(NAO_INICIADO)
                .descMovimentacao("Processo de diagnóstico iniciado")
                .build());
        diagnosticoFluxoService.inicializarDiagnostico(subprocessoSalvo);
        log.info("Criado subprocesso {} para unidade {}", subprocessoSalvo.getCodigo(), command.unidade().getSigla());
    }

    private Subprocesso criarSubprocessoComMapa(ContextoCriacaoSubprocesso contexto) {
        Long codMapaVigente = obterCodigoMapaVigenteObrigatorio(contexto.unidadeMapa(), contexto.unidade());
        Subprocesso subprocesso = criarSubprocessoInicial(contexto.processo(), contexto.unidade(), contexto.situacaoInicial());

        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente, subprocessoSalvo);

        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(criarMovimentacaoInicial(subprocessoSalvo, contexto.unidadeOrigem(), contexto.usuario(), contexto.descMovimentacao()));

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

    @SuppressWarnings("unused")
    private Long obterCodigoMapaVigenteObrigatorio(UnidadeMapa unidadeMapa, Unidade ignorado) {
        return unidadeMapa.getMapaVigente().getCodigo();
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
    public void adicionarCompetencia(Long codSubprocesso, CriarCompetenciaRequest request) {
        ContextoEdicaoMapa contexto = carregarContextoEdicaoMapa(codSubprocesso);
        validarCompetenciaParaCriacao(request);
        mapaManutencaoService.criarCompetenciaComAtividades(
                contexto.mapa(), request.descricao(), request.atividadesCodigos());
        atualizarSituacaoAposPreenchimentoMapa(contexto.subprocesso(), contexto.mapaEstavaVazio());
    }

    public void atualizarCompetencia(Long codSubprocesso, Long codCompetencia, AtualizarCompetenciaRequest request) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesCodigos());

        Mapa mapa = obterMapaObrigatorio(sp);
        mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    private void validarCompetenciaParaCriacao(CriarCompetenciaRequest request) {
        if (request.atividadesCodigos().isEmpty()) {
            throw new ErroValidacao(Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE);
        }
    }

    public void removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        ContextoEdicaoMapa contexto = carregarContextoEdicaoMapa(codSubprocesso);
        mapaManutencaoService.removerCompetencia(codCompetencia);

        boolean ficouVazio = mapaManutencaoService.competenciasCodMapa(contexto.codMapa()).isEmpty();
        mapaManutencaoService.reconciliarSituacaoSubprocesso(contexto.subprocesso());
        atualizarSituacaoAposEsvaziamentoMapa(contexto.subprocesso(), ficouVazio);
        mapaManutencaoService.mapaCodigo(contexto.codMapa());
    }

    private ContextoEdicaoMapa carregarContextoEdicaoMapa(Long codSubprocesso) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = Optional.ofNullable(subprocesso.getMapa())
                .orElseGet(() -> obterMapaObrigatorio(subprocesso));
        Long codMapa = Optional.ofNullable(mapa.getCodigo())
                .orElseGet(() -> obterCodigoMapaObrigatorio(subprocesso));
        boolean mapaEstavaVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        return ContextoEdicaoMapa.builder()
                .subprocesso(subprocesso)
                .mapa(mapa)
                .codMapa(codMapa)
                .mapaEstavaVazio(mapaEstavaVazio)
                .build();
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
                "Mapa só pode ser editado nas situações 'Mapa criado', 'Cadastro homologado', 'Mapa ajustado', 'Revisão do cadastro homologada' ou 'Mapa com sugestões'. Situação atual: %s".formatted(sp.getSituacao()),
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_CADASTRO_HOMOLOGADA,
                REVISAO_MAPA_AJUSTADO,
                REVISAO_MAPA_COM_SUGESTOES);
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
    public ImportacaoAtividadesResultado importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem, List<Long> codigosAtividades) {
        ImportacaoAtividadesContexto contexto = montarContextoImportacaoAtividades(codSubprocessoDestino, codSubprocessoOrigem);
        int totalSolicitado = codigosAtividades.size();

        log.info("Importando {} atividades do mapa #{} para o mapa #{}",
                totalSolicitado, contexto.codMapaOrigem(), contexto.codMapaDestino());

        int totalImportado = copiaMapaService.importarAtividadesDeOutroMapa(
                contexto.codMapaOrigem(), contexto.codMapaDestino(), codigosAtividades);

        atualizarSituacaoDestinoAposImportacao(contexto.subprocessoDestino());

        return new ImportacaoAtividadesResultado(
                contexto.subprocessoDestino().getCodigo(),
                houveDuplicidadeNaImportacao(totalSolicitado, totalImportado)
        );
    }

    private ImportacaoAtividadesContexto montarContextoImportacaoAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        Subprocesso subprocessoDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        validarDestinoParaImportacao(subprocessoDestino);

        Usuario usuario = usuarioAplicacaoService.usuarioAutenticado();
        validarPermissaoImportacaoNoDestino(usuario, subprocessoDestino);

        Subprocesso subprocessoOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        validarPermissaoConsultaNaOrigem(usuario, subprocessoOrigem);

        return ImportacaoAtividadesContexto.builder()
                .subprocessoDestino(subprocessoDestino)
                .subprocessoOrigem(subprocessoOrigem)
                .usuario(usuario)
                .codMapaDestino(obterCodigoMapaObrigatorio(subprocessoDestino))
                .codMapaOrigem(obterCodigoMapaObrigatorio(subprocessoOrigem))
                .build();
    }

    private Mapa obterMapaObrigatorio(Subprocesso subprocesso) {
        return subprocesso.getMapa();
    }

    private Long obterCodigoMapaObrigatorio(Subprocesso subprocesso) {
        return Optional.ofNullable(obterMapaObrigatorio(subprocesso).getCodigo())
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Subprocesso %s com mapa sem código associado".formatted(subprocesso.getCodigo())));
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

    private boolean houveDuplicidadeNaImportacao(int totalSolicitado, int totalImportado) {
        return totalSolicitado > 0 && totalImportado < totalSolicitado;
    }

    public record CriarSubprocessosMapeamentoCommand(
            Processo processo,
            Collection<Unidade> unidades,
            Unidade unidadeOrigem
    ) {
        public CriarSubprocessosMapeamentoCommand {
            unidades = List.copyOf(unidades);
        }

        @Override
        public Collection<Unidade> unidades() {
            return List.copyOf(unidades);
        }
    }

    public record CriarSubprocessoComMapaCommand(
            Processo processo,
            Unidade unidade,
            UnidadeMapa unidadeMapa,
            Unidade unidadeOrigem
    ) {
    }

    public record ImportacaoAtividadesResultado(
            Long codigoSubprocessoDestino,
            boolean temDuplicatas
    ) {
    }

    @Builder
    private record ContextoEdicaoMapa(
            Subprocesso subprocesso,
            Mapa mapa,
            Long codMapa,
            boolean mapaEstavaVazio
    ) {
    }

    @Builder
    private record ImportacaoAtividadesContexto(
            Subprocesso subprocessoDestino,
            Subprocesso subprocessoOrigem,
            Usuario usuario,
            Long codMapaDestino,
            Long codMapaOrigem
    ) {
    }

    @Builder
    private record ContextoCriacaoSubprocesso(
            Processo processo,
            Unidade unidade,
            UnidadeMapa unidadeMapa,
            Unidade unidadeOrigem,
            Usuario usuario,
            SituacaoSubprocesso situacaoInicial,
            String descMovimentacao
    ) {
    }
}
