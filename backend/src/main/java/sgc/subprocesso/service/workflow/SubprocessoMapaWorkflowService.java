package sgc.subprocesso.service.workflow;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import static sgc.seguranca.acesso.Acao.ACEITAR_MAPA;
import static sgc.seguranca.acesso.Acao.AJUSTAR_MAPA;
import static sgc.seguranca.acesso.Acao.APRESENTAR_SUGESTOES;
import static sgc.seguranca.acesso.Acao.DEVOLVER_MAPA;
import static sgc.seguranca.acesso.Acao.DISPONIBILIZAR_MAPA;
import static sgc.seguranca.acesso.Acao.HOMOLOGAR_MAPA;
import static sgc.seguranca.acesso.Acao.VALIDAR_MAPA;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_VALIDADO;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoMapaWorkflowService {

    private static final String SIGLA_SEDOC = "SEDOC";

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_DISPONIBILIZADO));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_COM_SUGESTOES = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_COM_SUGESTOES,
            TipoProcesso.REVISAO, REVISAO_MAPA_COM_SUGESTOES));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_VALIDADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_VALIDADO));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_HOMOLOGADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_HOMOLOGADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_HOMOLOGADO));

    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoCrudService crudService;
    private final MapaManutencaoService mapaManutencaoService;
    private final MapaFacade mapaFacade;
    private final AccessControlService accessControlService;
    @Lazy private final SubprocessoValidacaoService validacaoService;
    private final AnaliseFacade analiseFacade;
    private final SubprocessoTransicaoService transicaoService;
    private final UnidadeFacade unidadeService;

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        MapaCompletoDto mapaDto = mapaFacade.salvarMapaCompleto(codMapa, request);
        if (eraVazio && temNovasCompetencias && subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        Long codMapa = mapa.getCodigo();
        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();

        mapaManutencaoService.criarCompetenciaComAtividades(
                mapa, request.descricao(), request.atividadesIds());

        // Alterar situação para MAPA_CRIADO se era vazio e passou a ter competências
        if (eraVazio && subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaFacade.obterMapaCompleto(mapa.getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaRequest request) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = subprocesso.getMapa();
        return mapaFacade.obterMapaCompleto(mapa.getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        mapaManutencaoService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio e estava em MAPA_CRIADO, voltar para CADASTRO_HOMOLOGADO
        boolean ficouVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        if (ficouVazio && subprocesso.getSituacao() == MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            subprocessoRepo.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != MAPEAMENTO_CADASTRO_HOMOLOGADO
                && situacao != MAPEAMENTO_MAPA_CRIADO
                && situacao != REVISAO_CADASTRO_HOMOLOGADA
                && situacao != REVISAO_MAPA_AJUSTADO) {

            throw new ErroMapaEmSituacaoInvalida("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(situacao));
        }

        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        if (request.dataLimite() == null) {
            throw new ErroValidacao("Data limite é obrigatória.");
        }
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_MAPA, sp);

        validarMapaParaDisponibilizacao(sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.getMapa().setSugestoes(null);
        analiseFacade.removerPorSubprocesso(codSubprocesso);

        if (org.springframework.util.StringUtils.hasText(request.observacoes())) {
            sp.getMapa().setSugestoes(request.observacoes());
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);

        transicaoService.registrar(
                sp,
                TipoTransicao.MAPA_DISPONIBILIZADO,
                sedoc,
                sp.getUnidade(),
                usuario,
                request.observacoes());
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = mapaManutencaoService.buscarAtividadesPorMapaCodigo(codMapa);
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

            throw new ErroValidacao(
                    "Todas as atividades devem estar associadas a pelo menos uma competência.%nAtividades pendentes: %s"
                            .formatted(nomesAtividades));
        }
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, APRESENTAR_SUGESTOES, sp);

        sp.getMapa().setSugestoes(sugestoes);
        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());
        subprocessoRepo.save(sp);

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
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, VALIDAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        transicaoService.registrar(sp, TipoTransicao.MAPA_VALIDADO, sp.getUnidade(),
                sp.getUnidade().getUnidadeSuperior(), usuario);
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
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
                justificativa));
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
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
                    null));
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeService.buscarEntidadePorSigla(SIGLA_SEDOC);
        transicaoService.registrar(sp, TipoTransicao.MAPA_HOMOLOGADO, sedoc, sedoc, usuario);
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, AJUSTAR_MAPA, sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
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
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos,
            DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> disponibilizarMapa(codSubprocesso, request, usuario));
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> aceitarValidacao(codSubprocesso, usuario));
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> homologarValidacao(codSubprocesso, usuario));
    }
}