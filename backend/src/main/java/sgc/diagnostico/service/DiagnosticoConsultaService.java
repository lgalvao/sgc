package sgc.diagnostico.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.dto.UnidadeResumoDto;
import sgc.diagnostico.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoConsultaService {
    private final ComumRepo repo;
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SubprocessoDtoMapper subprocessoDtoMapper;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;
    private final SubprocessoVisualizacaoService subprocessoVisualizacaoService;
    private final ResponsavelUnidadeService responsavelUnidadeService;
    private final UnidadeService unidadeService;

    public DiagnosticoContextoDto obterContexto(Long codSubprocesso) {
        Subprocesso sp = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        UnidadeProcesso unidadeSnapshot = resolverUnidadeSnapshot(sp);
        List<CompetenciaResumoDto> competencias = resolverMapaDiagnostico(sp).getCompetencias().stream()
                .map(c -> CompetenciaResumoDto.builder()
                        .competenciaCodigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();
        return DiagnosticoContextoDto.builder()
                .processoCodigo(sp.getProcesso().getCodigo())
                .subprocessoCodigo(sp.getCodigo())
                .unidadeCodigo(unidadeSnapshot.getUnidadeCodigoPersistido())
                .unidadeSigla(unidadeSnapshot.getSigla())
                .unidadeNome(unidadeSnapshot.getNome())
                .situacaoSubprocesso(sp.getSituacao().name())
                .situacaoDiagnostico(resolverSituacaoDiagnostico(sp))
                .competencias(competencias)
                .build();
    }

    private Mapa resolverMapaDiagnostico(Subprocesso subprocesso) {
        return unidadeService.buscarMapaVigente(subprocesso.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Processo de diagnóstico sem mapa vigente para a unidade %s".formatted(subprocesso.getUnidade().getSigla())
                ));
    }

    public AutoavaliacaoDto obterAutoavaliacao(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        List<AvaliacaoServidor> avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());
        List<AvaliacaoCompetenciaDto> competencias = avaliacoes.stream()
                .map(a -> AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(a.getCompetencia().getCodigo())
                        .competenciaDescricao(a.getCompetencia().getDescricao())
                        .importancia(a.getAutoimportancia())
                        .dominio(a.getAutodominio())
                        .build())
                .toList();
        String situacaoServidor = avaliacoes.stream()
                .findFirst()
                .map(a -> a.getSituacaoServidor().name())
                .orElse(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA.name());
        SituacaoAvaliacaoServidor situacao = SituacaoAvaliacaoServidor.valueOf(situacaoServidor);
        return AutoavaliacaoDto.builder()
                .competencias(competencias)
                .situacaoServidor(situacaoServidor)
                .podeEditar(situacao == SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA)
                .podeConcluirAutoavaliacao(
                        situacao == SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA
                                || situacao == SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA
                )
                .habilitarConcluirAutoavaliacao(situacao == SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA)
                .build();
    }

    public ConsensoDto obterConsenso(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        return obterConsenso(codSubprocesso, usuario.getTituloEleitoral());
    }

    public ConsensoDto obterConsenso(Long codSubprocesso, String servidorTitulo) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        List<AvaliacaoServidor> avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);
        String servidorNome = avaliacoes.stream()
                .findFirst()
                .map(AvaliacaoServidor::getServidorNomeDiagnostico)
                .orElse(servidorTitulo);
        List<ConsensoCompetenciaDto> competencias = avaliacoes.stream()
                .map(a -> ConsensoCompetenciaDto.builder()
                        .competenciaCodigo(a.getCompetencia().getCodigo())
                        .competenciaDescricao(a.getCompetencia().getDescricao())
                        .servidorImportancia(resolverImportanciaServidorParaConsenso(a))
                        .servidorDominio(resolverDominioServidorParaConsenso(a))
                        .chefiaImportancia(a.getChefiaImportancia())
                        .chefiaDominio(a.getChefiaDominio())
                        .consensoImportancia(resolverConsensoImportancia(a))
                        .consensoDominio(resolverConsensoDominio(a))
                        .build())
                .toList();
        String situacaoServidor = avaliacoes.stream()
                .findFirst()
                .map(a -> a.getSituacaoServidor().name())
                .orElse(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA.name());
        SituacaoAvaliacaoServidor situacao = SituacaoAvaliacaoServidor.valueOf(situacaoServidor);
        boolean usuarioEhServidorAvaliado = usuario.getTituloEleitoral().equals(servidorTitulo);
        return ConsensoDto.builder()
                .servidorNome(servidorNome)
                .competencias(competencias)
                .situacaoServidor(situacaoServidor)
                .podeEditar(!usuarioEhServidorAvaliado && situacao != SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .podeConcluirAvaliacao(!usuarioEhServidorAvaliado)
                .habilitarConcluirAvaliacao(
                        !usuarioEhServidorAvaliado
                                && situacao != SituacaoAvaliacaoServidor.CONSENSO_CRIADO
                                && situacao != SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                )
                .podeAprovarConsenso(usuarioEhServidorAvaliado && situacao != SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .habilitarAprovarConsenso(usuarioEhServidorAvaliado && situacao == SituacaoAvaliacaoServidor.CONSENSO_CRIADO)
                .build();
    }

    public DiagnosticoEquipeDto obterEquipe(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        String responsavelTitulo = buscarResponsavelTitulo(subprocesso.getUnidade().getCodigo());
        boolean diagnosticoEmAndamento = subprocesso.getSituacao() == SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO;

        Map<String, SituacaoAvaliacaoServidor> situacoes = new HashMap<>();
        Map<String, String> nomes = new HashMap<>();
        for (AvaliacaoServidor a : avaliacoes) {
            String titulo = a.getServidor().getTituloEleitoral();
            if (titulo.equals(responsavelTitulo)) {
                continue;
            }
            situacoes.put(titulo, a.getSituacaoServidor());
            nomes.put(titulo, a.getServidorNomeDiagnostico());
        }

        List<DiagnosticoEquipeDto.Item> itens = situacoes.entrySet().stream()
                .map(e -> {
                    SituacaoAvaliacaoServidor situacao = e.getValue();
                    return DiagnosticoEquipeDto.Item.builder()
                            .servidorTitulo(e.getKey())
                            .servidorNome(nomes.get(e.getKey()))
                            .situacaoServidor(situacao.name())
                            .podeManterConsenso(situacao != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
                            .podeImpossibilitar(
                                    diagnosticoEmAndamento
                                            && situacao != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                                            && situacao != SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                            )
                            .podePermitirAvaliacao(
                                    diagnosticoEmAndamento
                                            && situacao == SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
                            .build();
                })
                .toList();

        return DiagnosticoEquipeDto.builder()
                .servidores(itens)
                .build();
    }

    public DiagnosticoUnidadeDto obterDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        var situacoes = situacaoCapacitacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        var movimentacoes = subprocessoConsultaService.listarMovimentacoes(subprocesso);
        UnidadeProcesso unidadeSnapshot = resolverUnidadeSnapshot(subprocesso);

        String responsavelTitulo = buscarResponsavelTitulo(subprocesso.getUnidade().getCodigo());
        UnidadeResumoDto unidade = UnidadeResumoDto.builder()
                .unidadeCodigo(unidadeSnapshot.getUnidadeCodigoPersistido())
                .unidadeSigla(unidadeSnapshot.getSigla())
                .unidadeNome(unidadeSnapshot.getNome())
                .situacaoSubprocesso(subprocesso.getSituacao().name())
                .responsavelTitulo(responsavelTitulo)
                .build();

        Map<String, List<AvaliacaoServidor>> porServidor = avaliacoes.stream()
                .filter(a -> !a.getServidor().getTituloEleitoral().equals(responsavelTitulo))
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getServidor().getTituloEleitoral(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<ServidorDiagnosticoDto> servidores = porServidor.values().stream()
                .map(lista -> {
                    AvaliacaoServidor primeiro = lista.getFirst();
                    List<AvaliacaoCompetenciaDto> consenso = lista.stream()
                            .map(a -> AvaliacaoCompetenciaDto.builder()
                                    .competenciaCodigo(a.getCompetencia().getCodigo())
                                    .competenciaDescricao(a.getCompetencia().getDescricao())
                                    .importancia(resolverImportanciaParaDiagnosticoUnidade(a))
                                    .dominio(resolverDominioParaDiagnosticoUnidade(a))
                                    .build())
                            .toList();
                    return ServidorDiagnosticoDto.builder()
                            .servidorTitulo(primeiro.getServidor().getTituloEleitoral())
                            .servidorNome(primeiro.getServidorNomeDiagnostico())
                            .situacaoServidor(primeiro.getSituacaoServidor().name())
                            .consenso(consenso)
                            .podeManterConsenso(
                                    primeiro.getSituacaoServidor() != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                            )
                            .podeImpossibilitar(
                                    primeiro.getSituacaoServidor() != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                                            && primeiro.getSituacaoServidor() != SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                            )
                            .podePermitirAvaliacao(
                                    primeiro.getSituacaoServidor() == SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                            )
                            .build();
                })
                .toList();

        List<SituacaoCapacitacaoDto> situacoesCapacitacao = situacoes.stream()
                .filter(o -> !o.getServidor().getTituloEleitoral().equals(responsavelTitulo))
                .map(o -> SituacaoCapacitacaoDto.builder()
                        .competenciaCodigo(o.getCompetencia().getCodigo())
                        .servidorTitulo(o.getServidor().getTituloEleitoral())
                        .servidorNome(o.getServidorNomeDiagnostico())
                        .situacaoCapacitacao(o.getSituacaoCapacitacao() != null ? o.getSituacaoCapacitacao().name() : null)
                        .build())
                .toList();

        List<MovimentacaoDto> movimentacoesDto = movimentacoes.stream()
                .map(subprocessoDtoMapper::paraMovimentacao)
                .toList();

        return DiagnosticoUnidadeDto.builder()
                .unidade(unidade)
                .situacaoDiagnostico(resolverSituacaoDiagnostico(subprocesso))
                .servidores(servidores)
                .situacoesCapacitacao(situacoesCapacitacao)
                .movimentacoes(movimentacoesDto)
                .build();
    }

    public List<AnaliseHistoricoDto> listarHistoricoDiagnostico(Long codSubprocesso) {
        return subprocessoVisualizacaoService.listarHistoricoDiagnostico(codSubprocesso);
    }

    private String resolverSituacaoDiagnostico(Subprocesso subprocesso) {
        return switch (subprocesso.getSituacao()) {
            case DIAGNOSTICO_CONCLUIDO -> subprocessoVisualizacaoService.possuiAnalise(
                    subprocesso.getCodigo(),
                    TipoAnalise.DIAGNOSTICO,
                    TipoAcaoAnalise.ACEITE_DIAGNOSTICO
            ) ? "VALIDADO" : "CONCLUIDO";
            case DIAGNOSTICO_HOMOLOGADO -> "HOMOLOGADO";
            case NAO_INICIADO, DIAGNOSTICO_EM_ANDAMENTO -> "EM_ANDAMENTO";
            default ->
                    throw new IllegalStateException("Situação de subprocesso incompatível com diagnóstico: " + subprocesso.getSituacao());
        };
    }

    private UnidadeProcesso resolverUnidadeSnapshot(Subprocesso subprocesso) {
        return subprocesso.getProcesso()
                .buscarParticipante(subprocesso.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Snapshot da unidade %d ausente no processo %d".formatted(
                                subprocesso.getUnidade().getCodigo(),
                                subprocesso.getProcesso().getCodigo()
                        )));
    }

    private @Nullable String buscarResponsavelTitulo(Long unidadeCodigo) {
        UnidadeResponsavelDto responsavel = responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeCodigo)
                .orElse(null);
        if (responsavel == null) {
            return null;
        }
        return responsavel.substitutoTitulo() != null && !responsavel.substitutoTitulo().isBlank()
                ? responsavel.substitutoTitulo()
                : responsavel.titularTitulo();
    }

    private @Nullable Integer resolverConsensoImportancia(AvaliacaoServidor avaliacao) {
        return consensoEspelhadoDaAutoavaliacao(avaliacao) ? null : avaliacao.getConsensoImportancia();
    }

    private @Nullable Integer resolverConsensoDominio(AvaliacaoServidor avaliacao) {
        return consensoEspelhadoDaAutoavaliacao(avaliacao) ? null : avaliacao.getConsensoDominio();
    }

    private @Nullable Integer resolverImportanciaParaDiagnosticoUnidade(AvaliacaoServidor avaliacao) {
        Integer consenso = resolverConsensoImportancia(avaliacao);
        return consenso != null ? consenso : resolverImportanciaServidorParaConsenso(avaliacao);
    }

    private @Nullable Integer resolverDominioParaDiagnosticoUnidade(AvaliacaoServidor avaliacao) {
        Integer consenso = resolverConsensoDominio(avaliacao);
        return consenso != null ? consenso : resolverDominioServidorParaConsenso(avaliacao);
    }

    private @Nullable Integer resolverImportanciaServidorParaConsenso(AvaliacaoServidor avaliacao) {
        return autoavaliacaoDisponivelParaConsenso(avaliacao) ? avaliacao.getAutoimportancia() : null;
    }

    private @Nullable Integer resolverDominioServidorParaConsenso(AvaliacaoServidor avaliacao) {
        return autoavaliacaoDisponivelParaConsenso(avaliacao) ? avaliacao.getAutodominio() : null;
    }

    private boolean autoavaliacaoDisponivelParaConsenso(AvaliacaoServidor avaliacao) {
        return switch (avaliacao.getSituacaoServidor()) {
            case AUTOAVALIACAO_CONCLUIDA, CONSENSO_CRIADO, CONSENSO_APROVADO -> true;
            case AUTOAVALIACAO_NAO_INICIADA, AVALIACAO_IMPOSSIBILITADA -> false;
        };
    }

    private boolean consensoEspelhadoDaAutoavaliacao(AvaliacaoServidor avaliacao) {
        return avaliacao.getChefiaImportancia() == null
                && avaliacao.getChefiaDominio() == null
                && java.util.Objects.equals(avaliacao.getConsensoImportancia(), avaliacao.getAutoimportancia())
                && java.util.Objects.equals(avaliacao.getConsensoDominio(), avaliacao.getAutodominio());
    }
}
