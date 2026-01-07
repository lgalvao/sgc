package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
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

    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaService competenciaService;
    private final AtividadeService atividadeService;
    private final MapaService mapaService;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseService analiseService;
    private final UnidadeService unidadeService;
    private final SubprocessoService subprocessoService;

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaService.buscarPorMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(codMapa, request, tituloUsuario);

        if (eraVazio
                && temNovasCompetencias
                && subprocesso.getSituacao()
                == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaReq request, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaService.buscarPorMapa(codMapa).isEmpty();

        competenciaService.adicionarCompetencia(
                subprocesso.getMapa(), request.getDescricao(), request.getAtividadesIds()
        );

        // Alterar situação para MAPA_CRIADO se era vazio e passou a ter competências
        if (eraVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaReq request,
            String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(
                codCompetencia, request.getDescricao(), request.getAtividadesIds());

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(
            Long codSubprocesso, Long codCompetencia, String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        competenciaService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio e estava em MAPA_CRIADO, voltar para CADASTRO_HOMOLOGADO
        boolean ficouVazio = competenciaService.buscarPorMapa(codMapa).isEmpty();
        if (ficouVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            subprocessoRepo.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
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
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        validarMapaParaDisponibilizacao(sp);
        
        // Validação adicional de associações (do service antigo)
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        if (request.getDataLimite() == null) {
            throw new ErroValidacao("A data limite para validação é obrigatória.");
        }

        sp.getMapa().setSugestoes(null);
        analiseService.removerPorSubprocesso(codSubprocesso);
        
        if (request.getObservacoes() != null && !request.getObservacoes().isBlank()) {
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
        var competencias = competenciaService.buscarPorMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        // Recuperar atividades via mapa, já que não temos AtividadeRepo aqui e não há método findBySubprocessoCodigo no service
        // (Assumindo que findBySubprocessoCodigo(codSubprocesso) é equivalente a findByMapaCodigo(sub.getMapa().getCodigo()))
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
    public void apresentarSugestoes(Long codSubprocesso, String sugestoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

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

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_VALIDADO,
                sp.getUnidade(),
                sp.getUnidade().getUnidadeSuperior(),
                usuario);
    }

    private final SubprocessoWorkflowExecutor workflowExecutor;

    @Transactional
    public void devolverValidacao(Long codSubprocesso, String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());

        sp.setDataFimEtapa2(null);

        workflowExecutor.registrarAnaliseETransicao(
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
        );
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade =
                unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        // Se não tem próxima unidade (é o topo ou estrutura rasa), homologar direto

        if (proximaUnidade == null) {
            // Caso especial: Fim da cadeia de validação (Homologação Implícita?)

            String siglaUnidade = sp.getUnidade().getUnidadeSuperior() != null ?
                    sp.getUnidade().getUnidadeSuperior().getSigla() : sp.getUnidade().getSigla();

            analiseService.criarAnalise(
                sp,
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes("Aceite da validação")
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(siglaUnidade)
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(null)
                        .build());

            sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
            subprocessoRepo.save(sp);
            // Sem transição registrada no código original para este caso.
        } else {
            SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo());

            workflowExecutor.registrarAnaliseETransicao(
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
            );
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorId(unidadeService.buscarPorSigla("SEDOC").getCodigo());

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_HOMOLOGADO,
                sedoc,
                sedoc,
                usuario);
    }

    @Transactional
    public void submeterMapaAjustado(
            Long codSubprocesso, SubmeterMapaAjustadoReq request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

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
    public void disponibilizarMapaEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, DisponibilizarMapaRequest request, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para unidade " + unidadeCodigo));

            disponibilizarMapa(target.getCodigo(), request, usuario);
        });
    }

    @Transactional
    public void aceitarValidacaoEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para unidade " + unidadeCodigo));

            aceitarValidacao(target.getCodigo(), usuario);
        });
    }

    @Transactional
    public void homologarValidacaoEmBloco(java.util.List<Long> unidadeCodigos, Long codSubprocessoBase, Usuario usuario) {
        unidadeCodigos.forEach(unidadeCodigo -> {
            Subprocesso base = buscarSubprocesso(codSubprocessoBase);
            Subprocesso target = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(base.getProcesso().getCodigo(), unidadeCodigo)
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para unidade " + unidadeCodigo));

            homologarValidacao(target.getCodigo(), usuario);
        });
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return subprocessoRepo
                .findById(codSubprocesso)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Subprocesso não encontrado: " + codSubprocesso));
    }
}
