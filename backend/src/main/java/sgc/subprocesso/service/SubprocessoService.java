package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.util.*;
import sgc.alerta.*;
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
import java.time.format.*;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO,
            TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO));
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_COM_SUGESTOES = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
            TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES));
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_VALIDADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
            TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO));
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_HOMOLOGADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
            TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO));
    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    private final ComumRepo repo;
    private final AnaliseRepo analiseRepo;
    private final AlertaFacade alertaService;
    private final UnidadeService unidadeService;
    private final HierarquiaService hierarquiaService;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    private final EmailService emailService;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final SubprocessoNotificacaoService notificacaoService;
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
        return mapaManutencaoService.buscarMapaCompletoPorSubprocesso(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterSugestoes() {
        return Map.of("sugestoes", "");
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.findByIdWithMapaAndAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
    }

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
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso (por mapa)", codMapa));
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidades() {
        return subprocessoRepo.findAllComFetch();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoWithFetch(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", "P:%d U:%d".formatted(codProcesso, codUnidade)));
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

    public void excluir(Long codigo) {
        buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    @Transactional(readOnly = true)
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

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) return emptyList();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        return atividades.isEmpty()
                ? emptyList()
                : atividades.stream().filter(a -> a.getConhecimentos().isEmpty()).toList();
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        validacaoService.validarExistenciaAtividades(subprocesso);
    }

    public void validarAssociacoesMapa(Long mapaId) {
        validacaoService.validarAssociacoesMapa(mapaId);
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, Set<SituacaoSubprocesso> permitidas) {
        validacaoService.validarSituacaoPermitida(subprocesso, permitidas);
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, SituacaoSubprocesso... permitidas) {
        validacaoService.validarSituacaoPermitida(subprocesso, permitidas);
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, String mensagem, SituacaoSubprocesso... permitidas) {
        validacaoService.validarSituacaoPermitida(subprocesso, mensagem, permitidas);
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima, String mensagem) {
        validacaoService.validarSituacaoMinima(subprocesso, minima, mensagem);
    }

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario);
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
                                TipoTransicao transicao, Usuario usuario) {

        List<Atividade> atividadesSemConhecimento = obterAtividadesSemConhecimento(sp.getCodigo());
        validacaoService.validarRequisitosNegocioParaDisponibilizacao(sp, atividadesSemConhecimento);

        Unidade origem = sp.getUnidade();
        Unidade destino = origem.getUnidadeSuperior();
        if (destino == null) {
            log.warn("Unidade {} não possui superior. Usando a própria unidade como destino.", origem.getSigla());
            destino = origem;
        }

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());

        removerAnalisesPorSubprocesso(sp.getCodigo());

        final Unidade destinoFinal = destino;
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, false);
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, false);
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
    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, false);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarDevolucao(codSubprocesso, usuario, observacoes, true);
    }

    private void executarDevolucao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validarSituacaoPermitida(sp, situacaoAtual);

        Unidade unidadeAnalise = obterUnidadeLocalizacao(sp);
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        Unidade unidadeDevolucao = movs.stream()
                .filter(m -> Objects.equals(m.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElse(sp.getUnidade());

        SituacaoSubprocesso novaSituacao = situacaoAtual; // Defaults back to the same status waiting for another unit
        if (Objects.equals(unidadeDevolucao.getCodigo(), sp.getUnidade().getCodigo())) {
            novaSituacao = isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
            sp.setDataFimEtapa1(null);
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(isRevisao ? TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA : TipoTransicao.CADASTRO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(isRevisao ? TipoAcaoAnalise.DEVOLUCAO_REVISAO : TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceite(codSubprocesso, usuario, observacoes, true);
    }

    private void executarAceite(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validarSituacaoPermitida(sp, situacaoAtual);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeAtual;
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(situacaoAtual)
                .tipoTransicao(isRevisao ? TipoTransicao.REVISAO_CADASTRO_ACEITA : TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(isRevisao ? TipoAcaoAnalise.ACEITE_REVISAO : TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeAtual)
                .unidadeOrigemTransicao(unidadeAtual)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacao(codSubprocesso, usuario, observacoes, true);
    }

    private void executarHomologacao(Long codSubprocesso, Usuario usuario, @Nullable String observacoes, boolean isRevisao) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso situacaoAtual = isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO;
        validarSituacaoPermitida(sp, situacaoAtual);

        boolean deveHomologar = true;
        if (isRevisao) {
            var impactos = impactoMapaService.verificarImpactos(sp, usuario);
            if (!impactos.temImpactos()) {
                sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
                subprocessoRepo.save(sp);
                deveHomologar = false;
            }
        }

        if (deveHomologar) {
            Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
            sp.setSituacao(isRevisao ? SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

            registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(isRevisao ? TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA : TipoTransicao.CADASTRO_HOMOLOGADO)
                    .origem(admin)
                    .destino(admin)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        }
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceite(codSubprocesso, usuario,
                "Avaliação em bloco", false));
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(
                codSubprocesso -> executarHomologacao(codSubprocesso, usuario, "Homologação em bloco", false));
    }

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_REABERTO, false);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_REABERTA, true);
    }

    private void executarReabertura(Long codigo, String justificativa, SituacaoSubprocesso situacaoMinima,
                                    SituacaoSubprocesso novaSituacao, TipoTransicao tipoTransicao, boolean isRevisao) {

        Subprocesso sp = buscarSubprocesso(codigo);
        validarSituacaoMinima(sp, situacaoMinima, "Subprocesso ainda está em fase de " + (isRevisao ? "revisão" : "cadastro") + ".");

        Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);

        registrarTransicao(RegistrarTransicaoCommand.builder()
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
        Processo processo = sp.getProcesso();
        Unidade unidade = sp.getUnidade();

        if (isRevisao) {
            alertaService.criarAlertaReaberturaRevisao(processo, unidade, justificativa);
        } else {
            alertaService.criarAlertaReaberturaCadastro(processo, unidade, justificativa);
        }

        Unidade superior = unidade.getUnidadeSuperior();
        while (superior != null) {
            if (isRevisao) {
                alertaService.criarAlertaReaberturaRevisaoSuperior(processo, superior, unidade);
            } else {
                alertaService.criarAlertaReaberturaCadastroSuperior(processo, superior, unidade);
            }
            superior = superior.getUnidadeSuperior();
        }
    }

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();
        String situacaoSp = s.name();

        LocalDateTime dataLimiteEtapa1 = novaDataLimite.atStartOfDay();
        if (situacaoSp.contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(dataLimiteEtapa1);
        } else if (situacaoSp.contains("MAPA")) {
            sp.setDataLimiteEtapa2(dataLimiteEtapa1);
        } else {
            sp.setDataLimiteEtapa1(dataLimiteEtapa1);
        }

        subprocessoRepo.save(sp);

        String novaDataStr = novaDataLimite.format(DATE_FORMATTER);
        String assunto = "SGC: Data limite alterada";
        String corpo = ("Prezado(a) responsável pela %s," + "%n%n" +
                "A data limite da etapa atual no processo %s foi alterada para %s.%n")
                .formatted(sp.getUnidade().getSigla(), sp.getProcesso().getDescricao(), novaDataStr);

        emailService.enviarEmail(sp.getUnidade().getSigla(), assunto, corpo);

        int etapa = situacaoSp.contains("MAPA") ? 2 : 1;
        alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
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
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> {
                    TipoUnidade tipo = u.getTipo();
                    return tipo == OPERACIONAL || tipo == INTEROPERACIONAL || tipo == RAIZ;
                }).toList();

        if (unidadesElegiveis.isEmpty()) return;

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
        criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                SituacaoSubprocesso.NAO_INICIADO, "Processo iniciado");
        log.info("Subprocesso criado para unidade {}", unidade.getSigla());
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

        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
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

        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();

        mapaManutencaoService.criarCompetenciaComAtividades(mapa, request.descricao(), request.atividadesIds());

        if (eraVazio) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }

        return mapaManutencaoService.buscarMapaPorCodigo(mapa.getCodigo());
    }

    public Mapa atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaRequest request) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = sp.getMapa();
        return mapaManutencaoService.buscarMapaPorCodigo(mapa.getCodigo());
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        mapaManutencaoService.removerCompetencia(codCompetencia);

        boolean ficouVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        if (ficouVazio) {
            atualizarSituacaoMapaVazio(subprocesso, true);
        }

        return mapaManutencaoService.buscarMapaPorCodigo(subprocesso.getMapa().getCodigo());
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
        validarSituacaoPermitida(sp,
                "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(sp.getSituacao()),
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

        return sp;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        validarSituacaoPermitida(sp,
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES);

        validarMapaParaDisponibilizacao(sp);

        Mapa mapa = sp.getMapa();
        validarAssociacoesMapa(mapa.getCodigo());

        mapa.setSugestoes(null);
        removerAnalisesPorSubprocesso(codSubprocesso);

        String observacoes = request.observacoes();
        if (StringUtils.hasText(observacoes)) {
            mapa.setSugestoes(observacoes);
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());

        Unidade unidadeRaiz = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        RegistrarTransicaoCommand transicaoCommand = RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(unidadeRaiz)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(observacoes)
                .build();

        registrarTransicao(transicaoCommand);
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO,
                SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);

        sp.getMapa().setSugestoes(sugestoes);
        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());

        removerAnalisesPorSubprocesso(sp.getCodigo());

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_VALIDADO)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES,
                SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        Unidade unidadeAnalise = obterUnidadeLocalizacao(sp);
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        Unidade unidadeDevolucao = movs.stream()
                .filter(m -> Objects.equals(m.getUnidadeDestino().getCodigo(), unidadeAnalise.getCodigo()))
                .map(Movimentacao::getUnidadeOrigem)
                .filter(unidadeOrigem -> hierarquiaService.isSubordinada(unidadeOrigem, unidadeAnalise))
                .findFirst()
                .orElse(sp.getUnidade());

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        RegistrarWorkflowCommand workflowCommand = RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(justificativa)
                .observacoes(justificativa)
                .build();

        registrarAnaliseETransicao(workflowCommand);
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, usuario);
    }

    private void executarAceiteValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade proximaUnidade = unidadeAtual.getUnidadeSuperior();

        if (proximaUnidade == null) {
            String siglaUnidade = unidadeAtual.getSigla();

            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Aceite da validação")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .siglaUnidade(siglaUnidade)
                    .tituloUsuario(usuario.getTituloEleitoral())
                    .motivo(null)
                    .build();

            criarAnalise(sp, request, TipoAnalise.VALIDACAO);

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
                    .unidadeAnalise(unidadeAtual)
                    .unidadeOrigemTransicao(unidadeAtual)
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));

        Unidade admin = unidadeService.buscarPorSigla(SIGLA_ADMIN);
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa1(LocalDateTime.now());

        if (request.dataLimiteEtapa2() != null) {
            sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
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
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = listarAnalisesPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);

        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa);
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.listarConhecimentosPorMapa(codMapa);

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
            mapaManutencaoService.atualizarDescricoesAtividadeEmLote(atividadeDescricoes);
        }
    }

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.buscarCompetenciasPorCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.buscarAtividadesPorCodigos(todasAtividadesIds)
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
        mapaManutencaoService.salvarTodasCompetencias(competenciasParaSalvar);
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
                mapaManutencaoService.buscarMapaPorCodigo(sp.getMapa().getCodigo()),
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

        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        boolean temMapaVigente = unidadeService.verificarMapaVigente(sp.getUnidade().getCodigo());

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(mesmaUnidade && isChefe && Set.of(
                        SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDisponibilizarCadastro(mesmaUnidade && isChefe && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
                .podeDevolverCadastro(mesmaUnidade && (isGestor || isAdmin) && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeAceitarCadastro(mesmaUnidade && isGestor && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeHomologarCadastro(mesmaUnidade && isAdmin && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
                .podeEditarMapa(mesmaUnidade && isAdmin && Set.of(
                        SituacaoSubprocesso.NAO_INICIADO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO,
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                        SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA,
                        SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES,
                        SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO).contains(situacao))
                .podeDisponibilizarMapa(mesmaUnidade && isAdmin && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO, SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                        SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
                .podeValidarMapa(mesmaUnidade && isChefe && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeApresentarSugestoes(mesmaUnidade && isChefe && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
                .podeDevolverMapa(mesmaUnidade && (isGestor || isAdmin) && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO).contains(situacao))
                .podeAceitarMapa(mesmaUnidade && isGestor && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO).contains(situacao))
                .podeHomologarMapa(mesmaUnidade && isAdmin && Set.of(
                        SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO).contains(situacao))
                .podeVisualizarImpacto(temMapaVigente && (
                        (mesmaUnidade && isChefe && situacao == SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO) ||
                                (mesmaUnidade && isGestor && situacao == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) ||
                                (isAdmin && Set.of(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).contains(situacao))
                ))
                .podeAlterarDataLimite(isAdmin)
                .podeReabrirCadastro(isAdmin)
                .podeReabrirRevisao(isAdmin)
                .podeEnviarLembrete(isAdmin || isGestor)
                .build();
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
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
        copiaMapaService.importarAtividadesDeOutroMapa(codMapaOrigem, codMapaDestino);

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
        List<Atividade> todasAtividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(codMapa);

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

    @Transactional
    public Analise salvarAnalise(Analise analise) {
        return analiseRepo.save(analise);
    }

    @Transactional
    public Analise criarAnalise(Subprocesso sp, CriarAnaliseRequest request, TipoAnalise tipo) {
        Unidade unidadeEntidade = unidadeService.buscarPorSigla(request.siglaUnidade());
        var unidadeDto = UnidadeDto.fromEntity(unidadeEntidade);

        Analise analise = Analise.builder()
                .subprocesso(sp)
                .dataHora(LocalDateTime.now())
                .observacoes(request.observacoes())
                .tipo(tipo)
                .acao(request.acao())
                .unidadeCodigo(unidadeDto.getCodigo())
                .usuarioTitulo(request.tituloUsuario())
                .motivo(request.motivo())
                .build();

        return salvarAnalise(analise);
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarAnalisesPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.CADASTRO)
                .map(this::paraHistoricoDto)
                .toList();
    }

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

    @Transactional
    public void removerAnalisesPorSubprocesso(Long codSubprocesso) {
        List<Analise> analises = analiseRepo.findBySubprocessoCodigo(codSubprocesso);
        if (!analises.isEmpty()) {
            analiseRepo.deleteAll(analises);
        }
    }

    @Transactional
    public void registrarTransicao(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioFacade.usuarioAutenticado();

        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(cmd.sp())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        cmd.sp().setLocalizacaoAtual(cmd.destino() != null ? cmd.destino() : cmd.sp().getUnidade());
        subprocessoRepo.save(cmd.sp());
        
        notificacaoService.notificarTransicao(NotificacaoCommand.builder()
                .subprocesso(cmd.sp())
                .tipoTransicao(cmd.tipo())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .observacoes(cmd.observacoes())
                .build());
    }

    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioFacade.usuarioAutenticado();

        CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                .observacoes(cmd.observacoes())
                .acao(cmd.tipoAcaoAnalise())
                .siglaUnidade(cmd.unidadeAnalise().getSigla())
                .tituloUsuario(usuario.getTituloEleitoral())
                .motivo(cmd.motivoAnalise())
                .build();

        criarAnalise(sp, request, cmd.tipoAnalise());

        sp.setSituacao(cmd.novaSituacao());

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(usuario)
                .observacoes(cmd.observacoes())
                .build());
    }
}