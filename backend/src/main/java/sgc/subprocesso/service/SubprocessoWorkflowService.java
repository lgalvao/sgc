package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoEmailService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.seguranca.acesso.Acao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubprocessoWorkflowService {

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
    private final SubprocessoService subprocessoService;
    private final AlertaFacade alertaService;
    private final UnidadeFacade unidadeService;
    private final AnaliseFacade analiseFacade;
    private final UsuarioFacade usuarioServiceFacade;
    private final ImpactoMapaService impactoMapaService;
    private final AccessControlService accessControlService;
    private final MapaManutencaoService mapaManutencaoService;
    private final MapaFacade mapaFacade;
    private final NotificacaoEmailService notificacaoEmailService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SubprocessoEmailService emailService;
    private final ComumRepo repo;

    // --- Cadastro Workflow Operations ---

    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_REABERTO, false);
    }

    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_REABERTA, true);
    }

    private void executarReabertura(Long codigo, String justificativa, SituacaoSubprocesso situacaoMinima,
                                    SituacaoSubprocesso novaSituacao, TipoTransicao tipoTransicao, boolean isRevisao) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codigo);

        subprocessoService.validarSituacaoMinima(sp, situacaoMinima,
            "Subprocesso ainda está em fase de " + (isRevisao ? "revisão" : "cadastro") + ".");

        Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);
        Usuario usuario = usuarioServiceFacade.obterUsuarioAutenticadoOuNull();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(justificativa)
                .build());

        enviarAlertasReabertura(sp, justificativa, isRevisao);
    }

    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
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
    }

    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO,
                DISPONIBILIZAR_CADASTRO, usuario);
    }

    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        disponibilizar(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA,
                TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, DISPONIBILIZAR_REVISAO_CADASTRO, usuario);
    }

    private void disponibilizar(Long codSubprocesso, SituacaoSubprocesso novaSituacao,
            TipoTransicao transicao, Acao acaoPermissao, Usuario usuario) {

        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, acaoPermissao, sp);
        validarRequisitosNegocioParaDisponibilizacao(codSubprocesso);

        Unidade origem = sp.getUnidade();

        Unidade destino = origem.getUnidadeSuperior();
        if (destino == null) {
            log.warn("Unidade {} não possui superior. Usando a própria unidade como destino.", origem.getSigla());
            destino = origem;
        }

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        analiseFacade.removerPorSubprocesso(sp.getCodigo());

        final Unidade destinoFinal = destino;
        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso) {
        subprocessoService.validarExistenciaAtividades(codSubprocesso);

        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
    }

    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();

        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceiteCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarAceiteCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_CADASTRO, sp);

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeOrigem;
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeDestino)
                .unidadeOrigemTransicao(unidadeOrigem)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacaoCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarHomologacaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_CADASTRO, sp);

        Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(sp);

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_REVISAO_CADASTRO, sp);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        Unidade superiorAnalise = unidadeAnalise.getUnidadeSuperior();
        Unidade unidadeDestino = (superiorAnalise != null) ? superiorAnalise : unidadeAnalise;

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_DISPONIBILIZADA)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_ACEITA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_REVISAO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_REVISAO_CADASTRO, sp);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.temImpactos()) {
            Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);
            registrar(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA)
                    .origem(admin)
                    .destino(admin)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            subprocessoRepo.save(sp);
        }
    }

    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteCadastro(codSubprocesso, usuario,
                "De acordo com o cadastro de atividades da unidade (Em Bloco)"));
    }

    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(
                codSubprocesso -> executarHomologacaoCadastro(codSubprocesso, usuario, "Homologação em bloco"));
    }

    // --- Mapa Workflow Operations ---

    public Mapa salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        Mapa mapa = mapaFacade.salvarMapaCompleto(codMapa, request);
        if (eraVazio && temNovasCompetencias) {
            if (subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }

        return mapa;
    }

    public Mapa adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
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

        return mapaFacade.obterPorCodigo(mapa.getCodigo());
    }

    public Mapa atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaRequest request) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = subprocesso.getMapa();
        return mapaFacade.obterPorCodigo(mapa.getCodigo());
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
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

        return mapaFacade.obterPorCodigo(subprocesso.getMapa().getCodigo());
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoService.buscarSubprocesso(codSubprocesso);

        subprocessoService.validarSituacaoPermitida(subprocesso,
            "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(subprocesso.getSituacao()),
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO);

        return subprocesso;
    }

    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DISPONIBILIZAR_MAPA, sp);

        validarMapaParaDisponibilizacao(sp);
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

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

        registrar(RegistrarTransicaoCommand.builder()
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

    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
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

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_SUGESTOES_APRESENTADAS)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .observacoes(sugestoes)
                .build());
    }

    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, VALIDAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_VALIDADO)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .build());
    }

    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, DEVOLVER_MAPA, sp);

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior == null) {
            unidadeSuperior = sp.getUnidade();
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
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

    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, usuario);
    }

    private void executarAceiteValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, ACEITAR_MAPA, sp);
        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade = unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        if (proximaUnidade == null) {
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
            registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
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

    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        executarHomologacaoValidacao(codSubprocesso, usuario);
    }

    private void executarHomologacaoValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, HOMOLOGAR_MAPA, sp);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade admin = unidadeService.buscarEntidadePorSigla(SIGLA_ADMIN);
        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .build());
    }

    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        accessControlService.verificarPermissao(usuario, AJUSTAR_MAPA, sp);
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa1(LocalDateTime.now());

        if (request.dataLimiteEtapa2() != null) {
            sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }

        subprocessoRepo.save(sp);

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(sp.getUnidade())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.justificativa())
                .build());
    }

    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos,
            DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarDisponibilizacaoMapa(codSubprocesso, request, usuario));
    }

    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteValidacao(codSubprocesso, usuario));
    }

    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarHomologacaoValidacao(codSubprocesso, usuario));
    }

    // --- Admin Workflow Operations ---

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        int etapa = 1;

        if (s.name().contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        } else if (s.name().contains("MAPA")) {
            sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
            etapa = 2;
        } else {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        subprocessoRepo.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String assunto = "SGC: Data limite alterada";
            String corpo = ("Prezado(a) responsável pela %s," + "%n%n" +
                    "A data limite da etapa atual no processo %s foi alterada para %s.%n")
                    .formatted(sp.getUnidade().getSigla(), sp.getProcesso().getDescricao(), novaDataStr);

            notificacaoEmailService.enviarEmail(sp.getUnidade().getSigla(), assunto, corpo);
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }


    @Transactional
    public void registrarMovimentacaoLembrete(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoService.buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioServiceFacade.obterUsuarioAutenticado();
        var unidadeAdmin = unidadeService.buscarEntidadePorSigla("ADMIN");

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeAdmin)
                .unidadeDestino(subprocesso.getUnidade())
                .descricao("Lembrete de prazo enviado")
                .usuario(usuario)
                .build());
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoRepo.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }

    // --- Transicao Operations ---

    @Transactional
    public void registrar(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioServiceFacade.obterUsuarioAutenticado();

        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(cmd.sp())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescricaoMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        notificarTransicao(cmd.sp(), cmd.tipo(), cmd.origem(), cmd.destino(), cmd.observacoes());
    }

    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();

        analiseFacade.criarAnalise(
                sp,
                CriarAnaliseCommand.builder()
                        .codSubprocesso(sp.getCodigo())
                        .observacoes(cmd.observacoes())
                        .tipo(cmd.tipoAnalise())
                        .acao(cmd.tipoAcaoAnalise())
                        .siglaUnidade(cmd.unidadeAnalise().getSigla())
                        .tituloUsuario(cmd.usuario().getTituloEleitoral())
                        .motivo(cmd.motivoAnalise())
                        .build());

        sp.setSituacao(cmd.novaSituacao());

        registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(cmd.usuario())
                .observacoes(cmd.observacoes())
                .build());

        log.info("Workflow SP{}: {} -> {}", sp.getCodigo(), cmd.novaSituacao(), cmd.tipoTransicao());
    }

    private void notificarTransicao(Subprocesso sp, TipoTransicao tipo,
                                     sgc.organizacao.model.Unidade origem,
                                     sgc.organizacao.model.Unidade destino,
                                     String observacoes) {
        try {
            if (tipo.geraAlerta()) {
                String sigla = sp.getUnidade().getSigla();
                String descricao = tipo.formatarAlerta(sigla);
                alertaService.criarAlertaTransicao(sp.getProcesso(), descricao, origem, destino);
            }

            if (tipo.enviaEmail()) {
                emailService.enviarEmailTransicaoDireta(sp, tipo, origem, destino, observacoes);
            }
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de transição {}: {}", tipo, e.getMessage(), e);
        }
    }
}
