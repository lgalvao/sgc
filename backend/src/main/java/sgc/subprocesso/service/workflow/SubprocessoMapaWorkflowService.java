package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeDeveriaExistir;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço responsável pelo workflow do mapa de competências de um subprocesso.
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * MapaFacade injetado normalmente. Dependência circular verificada e refutada.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoMapaWorkflowService {
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
    private static final String DETALHE_SUBPROCESSO_FMT = "processo=%d, unidade=%d";
    private static final String MSG_ERRO_WORKFLOW_BLOCO = "Workflow em bloco - subprocesso deveria ter sido criado no início do processo";

    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaService competenciaService;
    private final AtividadeService atividadeService;
    private final MapaFacade mapaFacade;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseService analiseService;
    private final UnidadeService unidadeService;
    private final SubprocessoValidacaoService validacaoService;
    private final AccessControlService accessControlService;

    private SubprocessoMapaWorkflowService self;

    @Autowired
    public void setSelf(@Lazy SubprocessoMapaWorkflowService self) {
        this.self = self;
    }

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaService.buscarPorCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaFacade.salvarMapaCompleto(codMapa, request);

        if (eraVazio
                && temNovasCompetencias
                && subprocesso.getSituacao()
                == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
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
        if (eraVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
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
        if (ficouVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            subprocessoRepo.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO
                && situacao != SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO
                && situacao != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && situacao != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s"
                            .formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new sgc.comum.erros.ErroEstadoImpossivel(
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
        analiseService.removerPorSubprocesso(codSubprocesso);
        
        if (org.springframework.util.StringUtils.hasText(request.getObservacoes())) {
            sp.getMapa().setSugestoes(request.getObservacoes());
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        
        sp.setDataLimiteEtapa2(request.getDataLimite().atStartOfDay());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorId(unidadeService.buscarPorSigla("SEDOC").getCodigo());

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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, APRESENTAR_SUGESTOES, sp);

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }

        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, VALIDAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        transicaoService.registrar(sp, TipoTransicao.MAPA_VALIDADO, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), usuario);
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_MAPA, sp);
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade = unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        // Se não tem próxima unidade (é o topo ou estrutura rasa), homologar direto
        if (proximaUnidade == null) {
            // Caso especial: Fim da cadeia de validação (Homologação Implícita?)
            Unidade sup = sp.getUnidade().getUnidadeSuperior();
            String siglaUnidade = sup != null ? sup.getSigla() : sp.getUnidade().getSigla();

            analiseService.criarAnalise(sp, CriarAnaliseCommand.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes("Aceite da validação")
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(siglaUnidade)
                        .tituloUsuario(usuario.getTituloEleitoral())
                        .motivo(null)
                        .build());

            sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
            subprocessoRepo.save(sp);
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorId(unidadeService.buscarPorSigla("SEDOC").getCodigo());
        transicaoService.registrar(sp, TipoTransicao.MAPA_HOMOLOGADO, sedoc, sedoc, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, AJUSTAR_MAPA, sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());
        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataLimiteEtapa2(request.getDataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

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
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeDeveriaExistir(
                            ENTIDADE_SUBPROCESSO,
                            DETALHE_SUBPROCESSO_FMT.formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            MSG_ERRO_WORKFLOW_BLOCO));

            self.disponibilizarMapa(target.getCodigo(), request, usuario);
        });
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeDeveriaExistir(
                            ENTIDADE_SUBPROCESSO,
                            DETALHE_SUBPROCESSO_FMT.formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            MSG_ERRO_WORKFLOW_BLOCO));

            self.aceitarValidacao(target.getCodigo(), usuario);
        });
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeDeveriaExistir(
                            ENTIDADE_SUBPROCESSO,
                            DETALHE_SUBPROCESSO_FMT.formatted(base.getProcesso().getCodigo(), unidadeCodigo),
                            MSG_ERRO_WORKFLOW_BLOCO));

            self.homologarValidacao(target.getCodigo(), usuario);
        });
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return subprocessoRepo.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: " + codSubprocesso));
    }
}
