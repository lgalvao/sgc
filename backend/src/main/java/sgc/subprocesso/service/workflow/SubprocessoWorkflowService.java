package sgc.subprocesso.service.workflow;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.*;
import sgc.comum.repo.RepositorioComum;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço unificado responsável por todos os workflows de subprocesso.
 * 
 * <p>Consolidação dos serviços:
 * <ul>
 *   <li>SubprocessoCadastroWorkflowService - Workflow de cadastro de atividades</li>
 *   <li>SubprocessoMapaWorkflowService - Workflow de mapa de competências</li>
 *   <li>SubprocessoTransicaoService - Transições e movimentações</li>
 *   <li>SubprocessoWorkflowService (root) - Operações administrativas</li>
 * </ul>
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * ImpactoMapaService e SubprocessoValidacaoService injetados com @Lazy.
 * Dependência circular verificada e tratada.
 */
@Service
@Slf4j
public class SubprocessoWorkflowService {
    // Strategy Pattern: Maps para eliminar if/else por TipoProcesso
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_DISPONIBILIZADO
    ));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_COM_SUGESTOES = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_COM_SUGESTOES,
            TipoProcesso.REVISAO, REVISAO_MAPA_COM_SUGESTOES
    ));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_VALIDADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_VALIDADO
    ));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_HOMOLOGADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_HOMOLOGADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_HOMOLOGADO
    ));

    private static final String ENTIDADE_SUBPROCESSO = "Subprocesso";

    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoCrudService crudService;
    private final AlertaFacade alertaService;
    private final UnidadeFacade unidadeService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseFacade analiseFacade;
    private final SubprocessoValidacaoService validacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final AccessControlService accessControlService;
    private final CompetenciaService competenciaService;
    private final AtividadeService atividadeService;
    private final MapaFacade mapaFacade;
    private final RepositorioComum repo;

    private SubprocessoWorkflowService self;

    /**
     * Construtor manual para garantir injeção correta de dependências lazy.
     */
    public SubprocessoWorkflowService(
            SubprocessoRepo repositorioSubprocesso,
            SubprocessoCrudService crudService,
            AlertaFacade alertaService,
            UnidadeFacade unidadeService,
            MovimentacaoRepo repositorioMovimentacao,
            SubprocessoTransicaoService transicaoService,
            AnaliseFacade analiseFacade,
            @Lazy SubprocessoValidacaoService validacaoService,
            @Lazy ImpactoMapaService impactoMapaService,
            AccessControlService accessControlService,
            CompetenciaService competenciaService,
            AtividadeService atividadeService,
            MapaFacade mapaFacade,
            RepositorioComum repo) {
        this.repositorioSubprocesso = repositorioSubprocesso;
        this.crudService = crudService;
        this.alertaService = alertaService;
        this.unidadeService = unidadeService;
        this.repositorioMovimentacao = repositorioMovimentacao;
        this.transicaoService = transicaoService;
        this.analiseFacade = analiseFacade;
        this.validacaoService = validacaoService;
        this.impactoMapaService = impactoMapaService;
        this.accessControlService = accessControlService;
        this.competenciaService = competenciaService;
        this.atividadeService = atividadeService;
        this.mapaFacade = mapaFacade;
        this.repo = repo;
    }

    @Autowired
    public void setSelf(@Lazy SubprocessoWorkflowService self) {
        this.self = self;
    }

    // ===== OPERAÇÕES ADMINISTRATIVAS =====

    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        int etapa = 1;

        if (s.name().contains("CADASTRO")) {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
             etapa = 1;
        } else if (s.name().contains("MAPA")) {
             sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
             etapa = 2;
        } else {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        repositorioSubprocesso.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repositorioSubprocesso.findByMapaCodigo(mapaCodigo)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso do mapa", mapaCodigo));

        if (subprocesso.getSituacao() == NAO_INICIADO) {
            assert subprocesso.getProcesso() != null :
                    "Invariante violada: Subprocesso deve ter processo quando situação != NAO_INICIADO";
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            }
        }
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return repositorioSubprocesso.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }

    public void reabrirCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.MAPEAMENTO) {
            throw new ErroValidacao("Reabertura de cadastro permitida apenas para processos de Mapeamento.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= MAPEAMENTO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de cadastro.", Map.of());
        }

        sp.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de cadastro");
        enviarAlertasReabertura(sp, justificativa, false);
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);

        if (sp.getProcesso().getTipo() != TipoProcesso.REVISAO) {
            throw new ErroValidacao("Reabertura de revisão permitida apenas para processos de Revisão.", Map.of());
        }
        if (sp.getSituacao().ordinal() <= REVISAO_CADASTRO_EM_ANDAMENTO.ordinal()) {
            throw new ErroValidacao("Subprocesso ainda está em fase de revisão.", Map.of());
        }

        sp.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        registrarMovimentacaoReabertura(sp, "Reabertura de revisão de cadastro");
        enviarAlertasReabertura(sp, justificativa, true);
    }

    private void registrarMovimentacaoReabertura(Subprocesso sp, String descricao) {
        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        Movimentacao mov = new Movimentacao();
        mov.setSubprocesso(sp);
        mov.setDataHora(java.time.LocalDateTime.now());
        mov.setUnidadeOrigem(sedoc);
        mov.setUnidadeDestino(sp.getUnidade());
        mov.setDescricao(descricao);
        repositorioMovimentacao.save(mov);
    }

    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
        try {
            if (isRevisao) {
                 alertaService.criarAlertaReaberturaRevisao(sp.getProcesso(), sp.getUnidade(), justificativa);
            } else {
                 alertaService.criarAlertaReaberturaCadastro(sp.getProcesso(), sp.getUnidade(), justificativa);
            }
            Unidade superior = sp.getUnidade().getUnidadeSuperior();
            while (superior != null) {
                if (isRevisao) {
                    alertaService.criarAlertaReaberturaRevisaoSuperior(sp.getProcesso(), superior, sp.getUnidade());
                } else {
                    alertaService.criarAlertaReaberturaCadastroSuperior(sp.getProcesso(), superior, sp.getUnidade());
                }
                superior = superior.getUnidadeSuperior();
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de reabertura: {}", e.getMessage());
        }
    }

    // ===== CADASTRO WORKFLOW =====

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, usuario, DISPONIBILIZAR_CADASTRO, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, usuario, DISPONIBILIZAR_REVISAO_CADASTRO, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    private void disponibilizar(Long codSubprocesso, Usuario usuario, sgc.seguranca.acesso.Acao acao, SituacaoSubprocesso novaSituacao, TipoTransicao transicao) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, acao, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso, sp);
        
        Unidade origem = sp.getUnidade();
        Unidade destino = origem.getUnidadeSuperior();
        
        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseFacade.removerPorSubprocesso(sp.getCodigo());
        transicaoService.registrar(sp, transicao, origem, destino, usuario);
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso, Subprocesso sp) {
        validacaoService.validarExistenciaAtividades(codSubprocesso);

        if (!validacaoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }

        Mapa mapa = sp.getMapa();
        if (mapa == null || mapa.getCodigo() == null) {
            throw new ErroMapaNaoAssociado("Subprocesso sem mapa associado");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_DEVOLVIDO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                unidadeAnalise,
                unidadeAnalise,
                sp.getUnidade(),
                usuario,
                observacoes,
                null
        ));
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new ErroInvarianteViolada("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                TipoTransicao.CADASTRO_ACEITO,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                unidadeDestino,
                unidadeOrigem,
                unidadeDestino,
                usuario,
                observacoes,
                null
        ));
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        transicaoService.registrar(sp, TipoTransicao.CADASTRO_HOMOLOGADO, sedoc, sedoc, usuario, observacoes);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.DEVOLUCAO_REVISAO,
                unidadeAnalise,
                unidadeAnalise,
                sp.getUnidade(),
                usuario,
                observacoes,
                null
        ));
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            throw new ErroInvarianteViolada("Unidade superior não encontrada para o subprocesso " + codSubprocesso);
        }

        Unidade unidadeDestino = unidadeAnalise.getUnidadeSuperior() != null ? unidadeAnalise.getUnidadeSuperior() : unidadeAnalise;

        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_ACEITA,
                TipoAnalise.CADASTRO,
                TipoAcaoAnalise.ACEITE_REVISAO,
                unidadeAnalise,
                unidadeAnalise,
                unidadeDestino,
                usuario,
                observacoes,
                null
        ));
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, @Nullable String observacoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.isTemImpactos()) {
            Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            repositorioSubprocesso.save(sp);
            transicaoService.registrar(sp, TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA, sedoc, sedoc, usuario, observacoes);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            repositorioSubprocesso.save(sp);
        }
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = repo.buscar(Subprocesso.class, codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_SUBPROCESSO, "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo)));

            self.aceitarCadastro(target.getCodigo(), "De acordo com o cadastro de atividades da unidade (Em Bloco)", usuario);
        });
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = repo.buscar(Subprocesso.class, codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_SUBPROCESSO, "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo)));

            self.homologarCadastro(target.getCodigo(), "Homologação em bloco", usuario);
        });
    }

    // ===== MAPA WORKFLOW =====

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaService.buscarPorCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaFacade.salvarMapaCompleto(codMapa, request);

        if (eraVazio
                && temNovasCompetencias
                && subprocesso.getSituacao()
                == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaService.buscarPorCodMapa(codMapa).isEmpty();

        competenciaService.criarCompetenciaComAtividades(
                subprocesso.getMapa(), request.getDescricao(), request.getAtividadesIds()
        );

        // Alterar situação para MAPA_CRIADO se era vazio e passou a ter competências
        if (eraVazio && subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            repositorioSubprocesso.save(subprocesso);
        }

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaRequest request) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(
                codCompetencia, request.getDescricao(), request.getAtividadesIds());

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(
            Long codSubprocesso, Long codCompetencia) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        competenciaService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio e estava em MAPA_CRIADO, voltar para CADASTRO_HOMOLOGADO
        boolean ficouVazio = competenciaService.buscarPorCodMapa(codMapa).isEmpty();
        if (ficouVazio && subprocesso.getSituacao() == MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            repositorioSubprocesso.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = repo.buscar(Subprocesso.class, codSubprocesso);

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != MAPEAMENTO_CADASTRO_HOMOLOGADO
                && situacao != MAPEAMENTO_MAPA_CRIADO
                && situacao != REVISAO_CADASTRO_HOMOLOGADA
                && situacao != REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s"
                            .formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroEstadoImpossivel(
                    "Subprocesso %d existe mas não possui Mapa associado - violação de invariante"
                    .formatted(subprocesso.getCodigo()));
        }
        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_MAPA, sp);

        validarMapaParaDisponibilizacao(sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        if (request.getDataLimite() == null) {
            throw new ErroValidacao("A data limite para validação é obrigatória.");
        }

        sp.getMapa().setSugestoes(null);
        analiseFacade.removerPorSubprocesso(codSubprocesso);
        
        if (org.springframework.util.StringUtils.hasText(request.getObservacoes())) {
            sp.getMapa().setSugestoes(request.getObservacoes());
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        
        sp.setDataLimiteEtapa2(request.getDataLimite().atStartOfDay());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_DISPONIBILIZADO,
                sedoc,
                sp.getUnidade(),
                usuario,
                request.getObservacoes());
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = competenciaService.buscarPorCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = atividadeService.buscarPorMapaCodigo(codMapa);
        var atividadesAssociadas = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        var atividadesNaoAssociadas = atividadesDoSubprocesso.stream()
                .filter(a -> !atividadesAssociadas.contains(a.getCodigo()))
                .toList();

        if (!atividadesNaoAssociadas.isEmpty()) {
            String nomesAtividades = atividadesNaoAssociadas.stream()
                    .map(Atividade::getDescricao)
                    .collect(Collectors.joining(", "));

            throw new ErroValidacao("""
                    Todas as atividades devem estar associadas a pelo menos uma competência.
                    Atividades pendentes: %s""".
                    formatted(nomesAtividades)
            );
        }
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, APRESENTAR_SUGESTOES, sp);

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }

        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseFacade.removerPorSubprocesso(sp.getCodigo());

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_SUGESTOES_APRESENTADAS,
                sp.getUnidade(),
                sp.getUnidade().getUnidadeSuperior(),
                usuario,
                sugestoes);
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, VALIDAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        transicaoService.registrar(sp, TipoTransicao.MAPA_VALIDADO, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), usuario);
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_MAPA, sp);

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                sp,
                novaSituacao,
                TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA,
                TipoAnalise.VALIDACAO,
                TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO,
                sp.getUnidade().getUnidadeSuperior(), // Analise
                sp.getUnidade().getUnidadeSuperior(), // Origem da Transição
                sp.getUnidade(), // Destino da Transição
                usuario,
                justificativa,
                justificativa
        ));
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_MAPA, sp);
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade = unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        // Se não tem próxima unidade (é o topo ou estrutura rasa), homologar direto
        if (proximaUnidade == null) {
            // Caso especial: Fim da cadeia de validação (Homologação Implícita?)
            Unidade sup = sp.getUnidade().getUnidadeSuperior();
            String siglaUnidade = sup != null ? sup.getSigla() : sp.getUnidade().getSigla();

            analiseFacade.criarAnalise(sp, CriarAnaliseCommand.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes("Aceite da validação")
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(siglaUnidade)
                        .tituloUsuario(usuario.getTituloEleitoral())
                        .motivo(null)
                        .build());

            sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
            repositorioSubprocesso.save(sp);
        } else {
            SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo());
            transicaoService.registrarAnaliseETransicao(new SubprocessoTransicaoService.RegistrarWorkflowReq(
                    sp,
                    novaSituacao,
                    TipoTransicao.MAPA_VALIDACAO_ACEITA,
                    TipoAnalise.VALIDACAO,
                    TipoAcaoAnalise.ACEITE_MAPEAMENTO,
                    unidadeSuperior, // Analise
                    unidadeSuperior, // Origem
                    proximaUnidade, // Destino
                    usuario,
                    "Aceite da validação",
                    null
            ));
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        repositorioSubprocesso.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla("SEDOC");
        transicaoService.registrar(sp, TipoTransicao.MAPA_HOMOLOGADO, sedoc, sedoc, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);
        accessControlService.verificarPermissao(usuario, AJUSTAR_MAPA, sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());
        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataLimiteEtapa2(request.getDataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_DISPONIBILIZADO,
                sp.getUnidade(),
                sp.getUnidade(),
                usuario);
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, DisponibilizarMapaRequest request, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = repo.buscar(Subprocesso.class, codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_SUBPROCESSO, "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo)));

            self.disponibilizarMapa(target.getCodigo(), request, usuario);
        });
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = repo.buscar(Subprocesso.class, codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_SUBPROCESSO, "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo)));

            self.aceitarValidacao(target.getCodigo(), usuario);
        });
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = repo.buscar(Subprocesso.class, codSubprocessoBase);
            Subprocesso target = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_SUBPROCESSO, "processo=%d, unidade=%d".formatted(base.getProcesso().getCodigo(), unidadeCodigo)));

            self.homologarValidacao(target.getCodigo(), usuario);
        });
    }

    // ===== HELPER METHODS =====

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return repo.buscar(Subprocesso.class, codSubprocesso);
    }
}
