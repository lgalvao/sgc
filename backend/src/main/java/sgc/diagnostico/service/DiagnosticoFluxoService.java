package sgc.diagnostico.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoFluxoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final ComumRepo repo;
    private final DiagnosticoValidacaoService validacaoService;
    private final DiagnosticoNotificacaoService notificacaoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SubprocessoTransicaoService transicaoService;
    private final SubprocessoValidacaoService subprocessoValidacaoService;
    private final SubprocessoFluxoContextoService fluxoContextoService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final UnidadeService unidadeService;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;
    private final UsuarioService usuarioService;
    private final ResponsavelUnidadeService responsavelUnidadeService;

    public void inicializarDiagnostico(Subprocesso subprocesso) {
        Diagnostico diagnostico = Diagnostico.builder()
                .subprocesso(subprocesso)
                .build();

        List<ServidorProcesso> servidoresSnapshot = subprocesso.getProcesso()
                .buscarServidoresParticipantes(subprocesso.getUnidade().getCodigo());
        String responsavelTitulo = buscarResponsavelTitulo(subprocesso.getUnidade().getCodigo());
        if (responsavelTitulo != null) {
            servidoresSnapshot = servidoresSnapshot.stream()
                    .filter(snapshot -> !Objects.equals(snapshot.getUsuarioTitulo(), responsavelTitulo))
                    .toList();
        }
        List<Usuario> servidores = carregarServidoresSnapshot(servidoresSnapshot);
        List<AvaliacaoServidor> avaliacoes = new ArrayList<>();
        List<SituacaoCapacitacao> situacaoCapacitacoes = new ArrayList<>();

        Mapa mapa = resolverMapaDiagnostico(subprocesso);
        UnidadeProcesso unidadeSnapshot = subprocesso.getProcesso()
                .buscarParticipante(subprocesso.getUnidade().getCodigo())
                .orElse(null);
        for (Usuario servidor : servidores) {
            ServidorProcesso servidorSnapshot = localizarSnapshotObrigatorio(servidoresSnapshot, servidor.getTituloEleitoral());
            for (Competencia competencia : mapa.getCompetencias()) {
                AvaliacaoServidor avaliacao = AvaliacaoServidor.builder()
                        .diagnostico(diagnostico)
                        .servidor(servidor)
                        .servidorNomeSnapshot(servidorSnapshot.getNome())
                        .competencia(competencia)
                        .situacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA)
                        .build();
                avaliacoes.add(avaliacao);

                situacaoCapacitacoes.add(SituacaoCapacitacao.builder()
                        .diagnostico(diagnostico)
                        .servidor(servidor)
                        .servidorNomeSnapshot(servidorSnapshot.getNome())
                        .unidadeCodigoSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getUnidadeCodigoPersistido() : subprocesso.getUnidade().getCodigo())
                        .unidadeSiglaSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getSigla() : subprocesso.getUnidade().getSigla())
                        .unidadeNomeSnapshot(unidadeSnapshot != null ? unidadeSnapshot.getNome() : subprocesso.getUnidade().getNome())
                        .competencia(competencia)
                        .build());
            }
        }

        diagnostico.setAvaliacaoServidores(avaliacoes);
        diagnostico.setSituacaoCapacitacoes(situacaoCapacitacoes);
        diagnosticoRepo.save(diagnostico);
    }

    private Mapa resolverMapaDiagnostico(Subprocesso subprocesso) {
        return unidadeService.buscarMapaVigente(subprocesso.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Processo de diagnóstico sem mapa vigente para a unidade %s".formatted(subprocesso.getUnidade().getSigla())
                ));
    }

    private List<Usuario> carregarServidoresSnapshot(List<ServidorProcesso> servidoresSnapshot) {
        List<String> titulos = servidoresSnapshot.stream()
                .map(ServidorProcesso::getUsuarioTitulo)
                .distinct()
                .toList();
        if (titulos.isEmpty()) {
            return List.of();
        }
        var usuariosPorTitulo = new HashMap<String, Usuario>();
        usuarioService.buscarPorTitulos(titulos).forEach(usuario -> usuariosPorTitulo.put(usuario.getTituloEleitoral(), usuario));
        return titulos.stream()
                .map(titulo -> {
                    Usuario usuario = usuariosPorTitulo.get(titulo);
                    if (usuario == null) {
                        throw new sgc.comum.erros.ErroInconsistenciaInterna(
                                "Servidor %s do snapshot do processo não foi encontrado para inicializar o diagnóstico".formatted(titulo)
                        );
                    }
                    return usuario;
                })
                .toList();
    }

    private ServidorProcesso localizarSnapshotObrigatorio(List<ServidorProcesso> snapshots, String tituloEleitoral) {
        return snapshots.stream()
                .filter(snapshot -> Objects.equals(snapshot.getUsuarioTitulo(), tituloEleitoral))
                .findFirst()
                .orElseThrow(() -> new sgc.comum.erros.ErroInconsistenciaInterna(
                        "Servidor %s não encontrado no snapshot do processo".formatted(tituloEleitoral)
                ));
    }

    private @Nullable String buscarResponsavelTitulo(Long unidadeCodigo) {
        return responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeCodigo)
                .map(responsavel -> responsavel.substitutoTitulo() != null && !responsavel.substitutoTitulo().isBlank()
                        ? responsavel.substitutoTitulo()
                        : responsavel.titularTitulo())
                .orElse(null);
    }

    public void concluirDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        validacaoService.validarConclusaoUnidade(diagnostico.getCodigo());

        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);

        diagnostico.setDataConclusao(LocalDateTime.now());

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setDataFimEtapa1(LocalDateTime.now());

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Unidade unidadeOrigem = subprocesso.getUnidade();
        Unidade unidadeDestino = obterSuperiorImediatoObrigatorio(unidadeOrigem, "conclusão de diagnóstico");

        transicaoService.registrarTransicaoSemComunicacoes(RegistrarTransicaoCommand.builder()
                .sp(subprocesso)
                .tipo(TipoTransicao.DIAGNOSTICO_CONCLUIDO)
                .origem(unidadeOrigem)
                .destino(unidadeDestino)
                .usuario(usuario)
                .observacoes(null)
                .build());

        notificacaoService.notificarDiagnosticoConcluido(subprocesso, unidadeDestino);
    }

    public void validarConclusaoDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        validacaoService.validarConclusaoUnidade(diagnostico.getCodigo());
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
    }

    public void devolverDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, java.util.Map.of("subprocesso.codigo", codSubprocesso));
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeDevolucao = fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(subprocesso, unidadeAnalise);

        SituacaoSubprocesso novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO;
        if (Objects.equals(unidadeDevolucao.getCodigo(), subprocesso.getUnidade().getCodigo())) {
            novaSituacao = SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO;
            diagnostico.setDataConclusao(null);
            diagnostico.getAvaliacaoServidores().forEach(avaliacao -> {
                avaliacao.setSituacaoServidorAnterior(null);
                if (avaliacao.getSituacaoServidor() != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA) {
                    avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
                }
            });
        }

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        transicaoService.registrarWorkflowComDestino(RegistrarWorkflowAnaliseCommand.builder()
                .sp(subprocesso)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_DIAGNOSTICO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeDestino(unidadeDevolucao)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(observacao)
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());

        notificacaoService.notificarDiagnosticoDevolvido(subprocesso, unidadeAnalise, unidadeDevolucao, observacao);
    }

    public void validarDevolucaoDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(subprocesso, unidadeAnalise);
    }

    public void validarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeSuperior = obterSuperiorImediatoObrigatorio(unidadeAnalise, "aceite de diagnóstico");

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        transicaoService.registrarWorkflowComDestino(RegistrarWorkflowAnaliseCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_ACEITO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_DIAGNOSTICO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeDestino(unidadeSuperior)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(observacao)
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());

        notificacaoService.notificarDiagnosticoAceito(subprocesso, unidadeAnalise, unidadeSuperior);
    }

    public void aceitarDiagnosticosEmBloco(List<Long> subprocessoCodigos) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        List<Subprocesso> subprocessos = subprocessoCodigos.stream()
                .map(subprocessoConsultaService::buscarSubprocesso)
                .toList();
        Unidade unidadeAnalise = null;
        Unidade unidadeSuperior = null;

        for (Subprocesso subprocesso : subprocessos) {
            Unidade[] contexto = validarDiagnosticoEmBloco(subprocesso, usuario);
            if (unidadeAnalise == null) {
                unidadeAnalise = contexto[0];
                unidadeSuperior = contexto[1];
            }
        }

        if (!subprocessos.isEmpty()) {
            notificacaoService.notificarDiagnosticosAceitosEmBloco(subprocessos, unidadeAnalise, unidadeSuperior);
            notificacaoService.criarAlertaDiagnosticosAceitosEmBloco(
                    subprocessos.getFirst().getProcesso(),
                    unidadeAnalise,
                    unidadeSuperior
            );
        }
    }

    public void validarAceiteDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
    }

    public void homologarDiagnostico(Long codSubprocesso, @Nullable String observacao) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        validacaoService.validarDiagnosticoHomologavel(codSubprocesso);

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        transicaoService.registrarWorkflowDentroDoAdmin(RegistrarWorkflowAnaliseCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_HOMOLOGADO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO)
                .usuario(usuario)
                .observacoes(observacao)
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());

        notificacaoService.notificarDiagnosticoHomologado(subprocesso);
    }

    public void homologarDiagnosticosEmBloco(List<Long> subprocessoCodigos) {
        List<Subprocesso> subprocessos = subprocessoCodigos.stream()
                .map(subprocessoConsultaService::buscarSubprocesso)
                .toList();

        for (Subprocesso subprocesso : subprocessos) {
            homologarDiagnosticoEmBloco(subprocesso);
        }
    }

    public void validarHomologacaoDiagnostico(Long codSubprocesso) {
        var subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        validacaoService.validarDiagnosticoHomologavel(codSubprocesso);
    }

    private Unidade[] validarDiagnosticoEmBloco(Subprocesso subprocesso, Usuario usuario) {
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        Unidade unidadeAnalise = localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso);
        Unidade unidadeSuperior = obterSuperiorImediatoObrigatorio(unidadeAnalise, "aceite em bloco de diagnóstico");

        transicaoService.registrarWorkflowComDestino(RegistrarWorkflowAnaliseCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_ACEITO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_DIAGNOSTICO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeDestino(unidadeSuperior)
                .usuario(usuario)
                .motivoAnalise(null)
                .observacoes(null)
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());
        return new Unidade[]{unidadeAnalise, unidadeSuperior};
    }

    private Unidade obterSuperiorImediatoObrigatorio(Unidade unidadeOrigem, String contexto) {
        Unidade unidadeSuperior = fluxoContextoService.buscarSuperiorImediato(unidadeOrigem.getCodigo());
        if (unidadeSuperior == null) {
            throw new ErroInconsistenciaInterna(
                    "Unidade superior imediata obrigatória ausente para %s da unidade %s"
                            .formatted(contexto, unidadeOrigem.getSigla())
            );
        }
        return unidadeSuperior;
    }

    private void homologarDiagnosticoEmBloco(Subprocesso subprocesso) {
        subprocessoValidacaoService.validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        validacaoService.validarDiagnosticoHomologavel(subprocesso.getCodigo());

        subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);

        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        transicaoService.registrarWorkflowDentroDoAdmin(RegistrarWorkflowAnaliseCommand.builder()
                .sp(subprocesso)
                .novaSituacao(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO)
                .tipoTransicao(TipoTransicao.DIAGNOSTICO_HOMOLOGADO)
                .tipoAnalise(TipoAnalise.DIAGNOSTICO)
                .tipoAcaoAnalise(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO)
                .usuario(usuario)
                .observacoes(null)
                .modoComunicacao(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES)
                .build());
    }
}
