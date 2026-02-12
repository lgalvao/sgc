package sgc.subprocesso.service.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoMapaWorkflowService {

    private static final String SIGLA_ADMIN = "ADMIN";

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
        if (eraVazio && temNovasCompetencias) {
            if (subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
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

        // Alterar situação para MAPA_CRIADO/AJUSTADO se era vazio e passou a ter competências
        if (eraVazio) {
            if (subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
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

        // Se o mapa ficou vazio, voltar para situação anterior
        boolean ficouVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        if (ficouVazio) {
            if (subprocesso.getSituacao() == MAPEAMENTO_MAPA_CRIADO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                subprocessoRepo.save(subprocesso);
                log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_MAPA_AJUSTADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
                subprocessoRepo.save(subprocesso);
                log.info("Situação do subprocesso {} alterada para REVISAO_CADASTRO_HOMOLOGADA (mapa ficou vazio)", codSubprocesso);
            }
        }

        return mapaFacade.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);

        validacaoService.validarSituacaoPermitida(subprocesso,
            "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(subprocesso.getSituacao()),
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO);

        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_MAPA, sp);

        validarMapaParaDisponibilizacao(sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.getMapa().setSugestoes(null);
        analiseFacade.removerPorSubprocesso(codSubprocesso);

        if (StringUtils.hasText(request.observacoes())) {
            sp.getMapa().setSugestoes(request.observacoes());
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.observacoes())
                .build());
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

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_SUGESTOES_APRESENTADAS)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .observacoes(sugestoes)
                .build());
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, VALIDAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_VALIDADO)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_MAPA, sp);

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior == null) {
            unidadeSuperior = sp.getUnidade();
        }

        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeSuperior)
                .unidadeOrigemTransicao(unidadeSuperior)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(justificativa)
                .observacoes(justificativa)
                .build());
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, usuario);
    }

    private void executarAceiteValidacao(Long codSubprocesso, Usuario usuario) {
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
            transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                    .sp(sp)
                    .novaSituacao(novaSituacao)
                    .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                    .tipoAnalise(TipoAnalise.VALIDACAO)
                    .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .unidadeAnalise(unidadeSuperior)
                    .unidadeOrigemTransicao(unidadeSuperior)
                    .unidadeDestinoTransicao(proximaUnidade)
                    .usuario(usuario)
                    .motivoAnalise("Aceite da validação")
                    .build());
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        executarHomologacaoValidacao(codSubprocesso, usuario);
    }

    private void executarHomologacaoValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);
        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, AJUSTAR_MAPA, sp);
        validacaoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa1(LocalDateTime.now());

        if (request.dataLimiteEtapa2() != null) {
            sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }

        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(sp.getUnidade())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.justificativa())
                .build());
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos,
            DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarDisponibilizacaoMapa(codSubprocesso, request, usuario));
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteValidacao(codSubprocesso, usuario));
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarHomologacaoValidacao(codSubprocesso, usuario));
    }
}