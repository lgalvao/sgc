package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static sgc.processo.model.AcaoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;
/**
 * Orquestra operações de Processo.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoFacade {
    private final ProcessoConsultaService processoConsultaService;
    private final ProcessoManutencaoService processoManutencaoService;
    private final UnidadeFacade unidadeService;
    private final SubprocessoFacade subprocessoFacade;
    private final ProcessoMapper processoMapper;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final SubprocessoMapper subprocessoMapper;
    private final UsuarioFacade usuarioService;
    private final ProcessoInicializador processoInicializador;
    private final AlertaFacade alertaService;
    private final NotificacaoEmailService notificacaoEmailService;

    private final ProcessoAcessoService processoAcessoService;
    private final ProcessoFinalizador processoFinalizador;

    /**
     * Busca um processo por ID ou lança exceção se não encontrado.
     */
    private Processo buscarPorId(Long id) {
        return processoConsultaService.buscarProcessoCodigo(id);
    }

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        return processoAcessoService.checarAcesso(authentication, codProcesso);
    }

    @Transactional
    public ProcessoDto criar(CriarProcessoRequest req) {
        Processo processoSalvo = processoManutencaoService.criar(req);
        return processoMapper.toDto(processoSalvo);
    }

    @Transactional
    public ProcessoDto atualizar(Long codigo, AtualizarProcessoRequest requisicao) {
        Processo processoAtualizado = processoManutencaoService.atualizar(codigo, requisicao);
        return processoMapper.toDto(processoAtualizado);
    }

    @Transactional
    public void apagar(Long codigo) {
        processoManutencaoService.apagar(codigo);
    }

    @Transactional(readOnly = true)
    public Optional<ProcessoDto> obterPorId(Long codigo) {
        return processoConsultaService.buscarProcessoCodigoOpt(codigo).map(processoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProcessoDto obterDtoPorId(Long codigo) {
        return processoMapper.toDto(buscarPorId(codigo));
    }

    @Transactional(readOnly = true)
    public Processo buscarEntidadePorId(Long codigo) {
        return buscarPorId(codigo);
    }

    @Transactional(readOnly = true)
    public ProcessoDetalheDto obterContextoCompleto(Long codigo, Usuario usuario) {
        ProcessoDetalheDto detalhes = obterDetalhes(codigo, usuario);
        List<SubprocessoElegivelDto> elegiveis = listarSubprocessosElegiveis(codigo);

        detalhes.getElegiveis().addAll(elegiveis);
        return detalhes;
    }

    @Transactional(readOnly = true)
    public ProcessoDetalheDto obterDetalhes(Long codProcesso, Usuario usuario) {
        Processo processo = buscarPorId(codProcesso);

        return processoDetalheBuilder.build(processo, usuario);
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarFinalizados() {
        return processoConsultaService.processosFinalizados().stream()
                .flatMap(p -> Stream.ofNullable(processoMapper.toDto(p)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessoDto> listarAtivos() {
        return processoConsultaService.processosAndamento().stream()
                .flatMap(p -> Stream.ofNullable(processoMapper.toDto(p)))
                .toList();
    }

    public Page<Processo> listarTodos(Pageable pageable) {
        return processoConsultaService.processos(pageable);
    }

    public Page<Processo> listarPorParticipantesIgnorandoCriado(List<Long> unidadeIds, Pageable pageable) {
        return processoConsultaService.processosIniciadosPorParticipantes(unidadeIds, pageable);
    }

    @Transactional
    public List<String> iniciarProcessoMapeamento(Long codigo, List<Long> codsUnidades) {
        return iniciarProcesso(codigo, codsUnidades);
    }

    @Transactional
    public List<String> iniciarProcessoRevisao(Long codigo, List<Long> codigosUnidades) {
        return iniciarProcesso(codigo, codigosUnidades);
    }

    @Transactional
    public List<String> iniciarProcessoDiagnostico(Long codigo, List<Long> codsUnidades) {
        return iniciarProcesso(codigo, codsUnidades);
    }

    private List<String> iniciarProcesso(Long codigo, List<Long> codsUnidades) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        return processoInicializador.iniciar(codigo, codsUnidades, usuario);
    }

    @Transactional
    public void finalizar(Long codigo) {
        processoFinalizador.finalizar(codigo);
    }

    @Transactional
    public void enviarLembrete(Long codProcesso, Long unidadeCodigo) {
        Processo processo = buscarEntidadePorId(codProcesso);
        Unidade unidade = unidadeService.buscarEntidadePorId(unidadeCodigo);

        // Verifica se unidade participa do processo
        if (processo.getParticipantes().stream().noneMatch(u -> u.getUnidadeCodigo().equals(unidadeCodigo))) {
            throw new ErroProcesso("Unidade não participa deste processo.");
        }

        String dataLimite = processo.getDataLimite() != null 
                ? processo.getDataLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "N/A";
        String descricao = "Lembrete: Prazo do processo %s encerra em %s"
                .formatted(processo.getDescricao(), dataLimite);
        String assunto = "SGC: Lembrete de prazo - %s".formatted(processo.getDescricao());
        String corpo = ("Prezado(a) responsável pela %s," + "%n%n" +
                "Este é um lembrete de que o prazo para a conclusão da etapa atual do processo %s encerra em %s." + "%n%n" +
                "Por favor, acesse o sistema para concluir suas pendências: /painel.%n")
                .formatted(unidade.getSigla(), processo.getDescricao(), dataLimite);

        SubprocessoDto subprocesso = subprocessoFacade.obterPorProcessoEUnidade(codProcesso, unidadeCodigo);
        subprocessoFacade.registrarMovimentacaoLembrete(subprocesso.getCodigo());
        notificacaoEmailService.enviarEmail(unidade.getSigla(), assunto, corpo);

        alertaService.criarAlertaAdmin(processo, unidade, descricao);
    }

    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        return processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.valueOf(tipo));
    }

    @Transactional(readOnly = true)
    public Set<Long> buscarIdsUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return processoConsultaService.buscarIdsUnidadesComProcessosAtivos(codProcessoIgnorar);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        return processoConsultaService.subprocessosElegiveis(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoFacade.listarEntidadesPorProcesso(codProcesso).stream()
                .map(subprocessoMapper::toDto)
                .toList();
    }

    @Transactional
    public void executarAcaoEmBloco(Long codProcesso, AcaoEmBlocoRequest req) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        if (req.acao() == DISPONIBILIZAR) {
            DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(
                    req.dataLimite(),
                    "Disponibilização em bloco"
            );
            subprocessoFacade.disponibilizarMapaEmBloco(req.unidadeCodigos(), codProcesso, dispReq, usuario);
            return;
        }

        processarAcoesAceiteHomologacao(codProcesso, req, usuario);
    }

    private void processarAcoesAceiteHomologacao(Long codProcesso, AcaoEmBlocoRequest req, Usuario usuario) {
        List<Long> unidadesAceitarCadastro = new ArrayList<>();
        List<Long> unidadesAceitarValidacao = new ArrayList<>();
        List<Long> unidadesHomologarCadastro = new ArrayList<>();
        List<Long> unidadesHomologarValidacao = new ArrayList<>();

        if (req.unidadeCodigos().isEmpty()) {
            return;
        }

        List<SubprocessoDto> subprocessos = subprocessoFacade.listarPorProcessoEUnidades(codProcesso, req.unidadeCodigos());

        for (SubprocessoDto spDto : subprocessos) {
            categorizarUnidadePorAcao(req, spDto, unidadesAceitarCadastro, unidadesAceitarValidacao, unidadesHomologarCadastro, unidadesHomologarValidacao);
        }

        executarAcoesBatch(codProcesso, usuario, unidadesAceitarCadastro, unidadesAceitarValidacao, unidadesHomologarCadastro, unidadesHomologarValidacao);
    }

    private void categorizarUnidadePorAcao(AcaoEmBlocoRequest req, SubprocessoDto spDto,
                                           List<Long> unitsAceitarCad, List<Long> unitsAceitarVal,
                                           List<Long> unitsHomolCad, List<Long> unitsHomolVal) {
        Long codUnidade = spDto.getCodUnidade();
        boolean isCadastro = isSituacaoCadastro(spDto.getSituacao());

        if (req.acao() == ACEITAR) {
            if (isCadastro) {
                unitsAceitarCad.add(codUnidade);
            } else {
                unitsAceitarVal.add(codUnidade);
            }
        } else if (req.acao() == HOMOLOGAR) {
            if (isCadastro) {
                unitsHomolCad.add(codUnidade);
            } else {
                unitsHomolVal.add(codUnidade);
            }
        }
    }

    private void executarAcoesBatch(Long codProcesso,
                                    Usuario usuario,
                                    List<Long> unidadesAceitarCadastro,
                                    List<Long> unidadesAceitarValidacao,
                                    List<Long> unidadesHomologarCadastro,
                                    List<Long> unidadesHomologarValidacao) {

        if (!unidadesAceitarCadastro.isEmpty()) {
            subprocessoFacade.aceitarCadastroEmBloco(unidadesAceitarCadastro, codProcesso, usuario);
        }
        if (!unidadesAceitarValidacao.isEmpty()) {
            subprocessoFacade.aceitarValidacaoEmBloco(unidadesAceitarValidacao, codProcesso, usuario);
        }
        if (!unidadesHomologarCadastro.isEmpty()) {
            subprocessoFacade.homologarCadastroEmBloco(unidadesHomologarCadastro, codProcesso, usuario);
        }
        if (!unidadesHomologarValidacao.isEmpty()) {
            subprocessoFacade.homologarValidacaoEmBloco(unidadesHomologarValidacao, codProcesso, usuario);
        }
    }

    private boolean isSituacaoCadastro(SituacaoSubprocesso situacao) {
        return situacao == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacao == REVISAO_CADASTRO_DISPONIBILIZADA ||
                situacao == REVISAO_CADASTRO_HOMOLOGADA;
    }
}
