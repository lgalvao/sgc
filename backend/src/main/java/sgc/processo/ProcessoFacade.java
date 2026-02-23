package sgc.processo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaFacade;
import sgc.notificacao.EmailService;
import sgc.notificacao.EmailModelosService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.*;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.*;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoFacade {
    private final ProcessoConsultaService processoConsultaService;
    private final ProcessoManutencaoService processoManutencaoService;
    private final UnidadeFacade unidadeService;
    private final SubprocessoFacade subprocessoFacade;
    private final ProcessoDetalheBuilder processoDetalheBuilder;
    private final UsuarioFacade usuarioService;
    private final ProcessoInicializador processoInicializador;
    private final AlertaFacade alertaService;
    private final EmailService emailService;
    private final ProcessoAcessoService processoAcessoService;
    private final ProcessoFinalizador processoFinalizador;
    private final EmailModelosService emailModelosService;

    private Processo buscarPorId(Long id) {
        return processoConsultaService.buscarProcessoCodigo(id);
    }

    public boolean checarAcesso(Authentication authentication, Long codProcesso) {
        return processoAcessoService.checarAcesso(authentication, codProcesso);
    }

    @Transactional
    public Processo criar(CriarProcessoRequest req) {
        return processoManutencaoService.criar(req);
    }

    @Transactional
    public Processo atualizar(Long codigo, AtualizarProcessoRequest requisicao) {
        return processoManutencaoService.atualizar(codigo, requisicao);
    }

    @Transactional
    public void apagar(Long codigo) {
        processoManutencaoService.apagar(codigo);
    }

    @Transactional(readOnly = true)
    public Optional<Processo> obterPorId(Long codigo) {
        return processoConsultaService.buscarProcessoCodigoOpt(codigo);
    }

    @Transactional(readOnly = true)
    public Processo obterEntidadePorId(Long codigo) {
        return processoConsultaService.buscarProcessoComParticipantes(codigo);
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
    public List<Processo> listarFinalizados() {
        return processoConsultaService.processosFinalizados();
    }

    @Transactional(readOnly = true)
    public List<Processo> listarAtivos() {
        return processoConsultaService.processosAndamento();
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
        Unidade unidade = unidadeService.porCodigo(unidadeCodigo);

        // Verifica se unidade participa do processo
        if (processo.getParticipantes().stream().noneMatch(u -> u.getUnidadeCodigo().equals(unidadeCodigo))) {
            throw new ErroProcesso("Unidade não participa deste processo.");
        }

        String dataLimiteText = processo.getDataLimite() != null
                ? processo.getDataLimite().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "N/A";

        String descricao = "Lembrete: Prazo do processo %s encerra em %s"
                .formatted(processo.getDescricao(), dataLimiteText);
        String assunto = "SGC: Lembrete de prazo - %s".formatted(processo.getDescricao());

        String corpoHtml = emailModelosService.criarEmailLembretePrazo(
                unidade.getSigla(), processo.getDescricao(), processo.getDataLimite());

        Subprocesso subprocesso = subprocessoFacade.obterPorProcessoEUnidade(codProcesso, unidadeCodigo);
        subprocessoFacade.registrarMovimentacaoLembrete(subprocesso.getCodigo());

        // Enviar para o titular da unidade
        Usuario titular = usuarioService.buscarPorLogin(unidade.getTituloTitular());
        emailService.enviarEmailHtml(titular.getEmail(), assunto, corpoHtml);
 
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
    public List<Subprocesso> listarTodosSubprocessos(Long codProcesso) {
        return subprocessoFacade.listarEntidadesPorProcesso(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesSubprocessos(Long codProcesso) {
        return subprocessoFacade.listarEntidadesPorProcesso(codProcesso);
    }

    @Transactional
    public void executarAcaoEmBloco(Long codProcesso, AcaoEmBlocoRequest req) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoFacade.listarPorProcessoEUnidades(codProcesso, req.unidadeCodigos());

        if (req.acao() == DISPONIBILIZAR) {
            List<Long> ids = subprocessos.stream().map(Subprocesso::getCodigo).toList();
            DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(
                    req.dataLimite(),
                    "Disponibilização em bloco"
            );
            subprocessoFacade.disponibilizarMapaEmBloco(ids, codProcesso, dispReq, usuario);
            return;
        }

        processarAcoesAceiteHomologacao(req, usuario, subprocessos);
    }

    private void processarAcoesAceiteHomologacao(AcaoEmBlocoRequest req, Usuario usuario, List<Subprocesso> subprocessos) {
        List<Long> idsAceitarCadastro = new ArrayList<>();
        List<Long> idsAceitarValidacao = new ArrayList<>();
        List<Long> idsHomologarCadastro = new ArrayList<>();
        List<Long> idsHomologarValidacao = new ArrayList<>();

        if (subprocessos.isEmpty()) return;

        for (Subprocesso sp : subprocessos) {
            categorizarPorAcao(req, sp, idsAceitarCadastro, idsAceitarValidacao, idsHomologarCadastro, idsHomologarValidacao);
        }

        executarAcoesBatch(usuario, idsAceitarCadastro, idsAceitarValidacao, idsHomologarCadastro, idsHomologarValidacao);
    }

    private void categorizarPorAcao(AcaoEmBlocoRequest req, Subprocesso sp,
                                           List<Long> idsAceitarCad, List<Long> idsAceitarVal,
                                           List<Long> idsHomolCad, List<Long> idsHomolVal) {
        Long codId = sp.getCodigo();
        boolean isCadastro = isSituacaoCadastro(sp.getSituacao());

        if (req.acao() == ACEITAR) {
            if (isCadastro) {
                idsAceitarCad.add(codId);
            } else {
                idsAceitarVal.add(codId);
            }
        } else if (req.acao() == HOMOLOGAR) {
            if (isCadastro) {
                idsHomolCad.add(codId);
            } else {
                idsHomolVal.add(codId);
            }
        }
    }

    private void executarAcoesBatch(Usuario usuario,
                                    List<Long> idsAceitarCadastro,
                                    List<Long> idsAceitarValidacao,
                                    List<Long> idsHomologarCadastro,
                                    List<Long> idsHomologarValidacao) {
 
        if (!idsAceitarCadastro.isEmpty()) {
            subprocessoFacade.aceitarCadastroEmBloco(idsAceitarCadastro, usuario);
        }
        if (!idsAceitarValidacao.isEmpty()) {
            subprocessoFacade.aceitarValidacaoEmBloco(idsAceitarValidacao, usuario);
        }
        if (!idsHomologarCadastro.isEmpty()) {
            subprocessoFacade.homologarCadastroEmBloco(idsHomologarCadastro, usuario);
        }
        if (!idsHomologarValidacao.isEmpty()) {
            subprocessoFacade.homologarValidacaoEmBloco(idsHomologarValidacao, usuario);
        }
    }

    private boolean isSituacaoCadastro(SituacaoSubprocesso situacao) {
        return situacao == MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacao == REVISAO_CADASTRO_DISPONIBILIZADA ||
                situacao == REVISAO_CADASTRO_HOMOLOGADA;
    }
}
