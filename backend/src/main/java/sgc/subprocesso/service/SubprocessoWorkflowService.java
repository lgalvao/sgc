package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.emptyList;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Service unificado para operações de subprocesso.
 *
 * <p>Consolida:
 * <ul>
 *   <li>CRUD (buscar, criar, atualizar, excluir)</li>
 *   <li>Validação (situação, atividades, associações)</li>
 *   <li>Cadastro workflow (disponibilizar, devolver, aceitar, homologar)</li>
 *   <li>Admin workflow (alterar data limite, reabrir, lembrete)</li>
 *   <li>Factory (criação de subprocessos para mapeamento/revisão/diagnóstico)</li>
 * </ul>
 *
 * <p>Transições e movimentações são delegadas ao {@link SubprocessoTransicaoService}.
 *
 * @see SubprocessoTransicaoService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoWorkflowService {
    private static final String SIGLA_ADMIN = "ADMIN";

    // Repos
    @Setter @Autowired @Lazy
    private SubprocessoRepo subprocessoRepo;
    @Setter @Autowired @Lazy
    private MovimentacaoRepo movimentacaoRepo;
    private final ComumRepo repo;

    // Services internos do módulo
    private final SubprocessoTransicaoService transicaoService;

    // Services de outros módulos
    private final AlertaFacade alertaService;
    private final AnaliseFacade analiseFacade;
    private final OrganizacaoFacade organizacaoFacade;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    @Setter @Autowired @Lazy
    private CopiaMapaService servicoDeCopiaDeMapa;
    private final EmailService emailService;

    // @Lazy para quebrar ciclo: WorkflowService → MapaManutencao → Facade → WorkflowService
    @Lazy @Autowired @Setter
    private MapaManutencaoService mapaManutencaoService;

    // ========================================================================
    // CRUD — Buscar, criar, atualizar, excluir
    // ========================================================================

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
        return repo.buscar(Subprocesso.class, "mapa.codigo", codMapa);
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
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    private void processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {
        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder().codigo(cod).build();
            Long codAtual = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                subprocesso.setMapa(m);
            }
        });

        if (request.dataLimiteEtapa1() != null && !Objects.equals(subprocesso.getDataLimiteEtapa1(), request.dataLimiteEtapa1())) {
            subprocesso.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        }
        if (request.dataFimEtapa1() != null && !Objects.equals(subprocesso.getDataFimEtapa1(), request.dataFimEtapa1())) {
            subprocesso.setDataFimEtapa1(request.dataFimEtapa1());
        }
        if (request.dataLimiteEtapa2() != null && !Objects.equals(subprocesso.getDataLimiteEtapa2(), request.dataLimiteEtapa2())) {
            subprocesso.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }
        if (request.dataFimEtapa2() != null && !Objects.equals(subprocesso.getDataFimEtapa2(), request.dataFimEtapa2())) {
            subprocesso.setDataFimEtapa2(request.dataFimEtapa2());
        }
    }

    // ========================================================================
    // VALIDAÇÃO — Situação, atividades, associações
    // ========================================================================

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            return emptyList();
        }
        return atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            throw new ErroValidacao("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        }
    }

    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        List<ErroValidacaoDto> erros = new ArrayList<>();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos().isEmpty()) {
                    erros.add(ErroValidacaoDto.builder()
                            .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                            .atividadeCodigo(atividade.getCodigo())
                            .descricaoAtividade(atividade.getDescricao())
                            .mensagem("Esta atividade não possui conhecimentos associados.")
                            .build());
                }
            }
        }

        return ValidacaoCadastroDto.builder()
                .valido(erros.isEmpty())
                .erros(erros)
                .build();
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, Set<SituacaoSubprocesso> permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.isEmpty()) {
            throw new IllegalArgumentException("Conjunto de situações permitidas não pode ser vazio");
        }
        if (!permitidas.contains(subprocesso.getSituacao())) {
            String permitidasStr = String.join(", ",
                permitidas.stream().map(SituacaoSubprocesso::name).toList());
            throw new ErroValidacao(
                "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s"
                    .formatted(subprocesso.getSituacao(), permitidasStr));
        }
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, SituacaoSubprocesso... permitidas) {
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        validarSituacaoPermitida(subprocesso, Set.of(permitidas));
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, String mensagem, SituacaoSubprocesso... permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        if (!Set.of(permitidas).contains(subprocesso.getSituacao())) {
            throw new ErroValidacao(mensagem);
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(
                "Subprocesso não atingiu a situação mínima necessária. Situação atual: %s. Mínima exigida: %s"
                    .formatted(subprocesso.getSituacao(), minima));
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima, String mensagem) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(mensagem);
        }
    }

    // ========================================================================
    // CADASTRO WORKFLOW — Disponibilizar, devolver, aceitar, homologar
    // ========================================================================

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario);
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
            TipoTransicao transicao, Usuario usuario) {

        validarRequisitosNegocioParaDisponibilizacao(sp.getCodigo());

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
        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso) {
        validarExistenciaAtividades(codSubprocesso);

        if (!obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
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

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceiteCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarAceiteCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeAtual;
        }

        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeAtual)
                .unidadeOrigemTransicao(unidadeAtual)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
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
        executarHomologacaoCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarHomologacaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
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

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeAtual;
        }

        transicaoService.registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_DISPONIBILIZADA)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_ACEITA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_REVISAO)
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
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.temImpactos()) {
            Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);
            transicaoService.registrar(RegistrarTransicaoCommand.builder()
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

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteCadastro(codSubprocesso, usuario,
                "De acordo com o cadastro de atividades da unidade (Em Bloco)"));
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(
                codSubprocesso -> executarHomologacaoCadastro(codSubprocesso, usuario, "Homologação em bloco"));
    }

    // ========================================================================
    // ADMIN WORKFLOW — Reabertura, data limite, lembrete, situação
    // ========================================================================

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_REABERTO, false);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_REABERTA, true);
    }

    private void executarReabertura(Long codigo, String justificativa, SituacaoSubprocesso situacaoMinima,
                                    SituacaoSubprocesso novaSituacao, TipoTransicao tipoTransicao, boolean isRevisao) {
        Subprocesso sp = buscarSubprocesso(codigo);

        validarSituacaoMinima(sp, situacaoMinima,
            "Subprocesso ainda está em fase de " + (isRevisao ? "revisão" : "cadastro") + ".");

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        transicaoService.registrar(RegistrarTransicaoCommand.builder()
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

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();

        if (s.name().contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        } else if (s.name().contains("MAPA")) {
            sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
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

            emailService.enviarEmail(sp.getUnidade().getSigla(), assunto, corpo);

            int etapa = s.name().contains("MAPA") ? 2 : 1;
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    @Transactional
    public void atualizarParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repo.buscar(Subprocesso.class, "mapa.codigo", mapaCodigo);
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                log.info("Atualizando sit. SP{} para MAPEAMENTO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                log.info("Atualizando sit. SP{} para REVISAO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    @Transactional
    public void registrarMovimentacaoLembrete(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        var unidadeAdmin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeAdmin)
                .unidadeDestino(subprocesso.getUnidade())
                .descricao("Lembrete de prazo enviado")
                .usuario(usuario)
                .build());
        subprocesso.setLocalizacaoAtualCache(subprocesso.getUnidade());
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoRepo.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }

    // ========================================================================
    // FACTORY — Criação de subprocessos
    // ========================================================================

    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.OPERACIONAL
                              || u.getTipo() == TipoUnidade.INTEROPERACIONAL
                              || u.getTipo() == TipoUnidade.RAIZ)
                .toList();

        if (unidadesElegiveis.isEmpty()) return;

        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .mapa(null)
                        .situacao(NAO_INICIADO)
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
            sp.setLocalizacaoAtualCache(sp.getUnidade());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de revisão iniciado")
                .build());
        subprocessoSalvo.setLocalizacaoAtualCache(unidade);
        log.info("Subprocesso para revisão criado para unidade {}", unidade.getSigla());
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(codMapaVigente);
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        mapaCopiado.setSubprocesso(subprocessoSalvo);

        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de diagnóstico iniciado")
                .build());
        subprocessoSalvo.setLocalizacaoAtualCache(unidade);
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }
}
