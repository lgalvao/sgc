package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.Mapa;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.UnidadeProcesso;
import sgc.subprocesso.SubprocessoDtoMapper;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoVisualizacaoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .podeAprovarConsenso(usuarioEhServidorAvaliado)
                .habilitarAprovarConsenso(usuarioEhServidorAvaliado && situacao == SituacaoAvaliacaoServidor.CONSENSO_CRIADO)
                .build();
    }

    public DiagnosticoEquipeDto obterEquipe(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        String responsavelTitulo = buscarResponsavelTitulo(subprocesso.getUnidade().getCodigo());

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
                                    situacao != SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA
                                            && situacao != SituacaoAvaliacaoServidor.CONSENSO_APROVADO
                            )
                            .podePermitirAvaliacao(situacao == SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
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
                                    .importancia(resolverConsensoImportancia(a))
                                    .dominio(resolverConsensoDominio(a))
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
            default -> throw new IllegalStateException("Situação de subprocesso incompatível com diagnóstico: " + subprocesso.getSituacao());
        };
    }

    private UnidadeProcesso resolverUnidadeSnapshot(Subprocesso subprocesso) {
        return subprocesso.getProcesso()
                .buscarParticipante(subprocesso.getUnidade().getCodigo())
                .orElse(null);
    }

    private String buscarResponsavelTitulo(Long unidadeCodigo) {
        UnidadeResponsavelDto responsavel = responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeCodigo)
                .orElse(null);
        if (responsavel == null) {
            return null;
        }
        return responsavel.substitutoTitulo() != null && !responsavel.substitutoTitulo().isBlank()
                ? responsavel.substitutoTitulo()
                : responsavel.titularTitulo();
    }

    private Integer resolverConsensoImportancia(AvaliacaoServidor avaliacao) {
        return consensoEspelhadoDaAutoavaliacao(avaliacao) ? null : avaliacao.getConsensoImportancia();
    }

    private Integer resolverConsensoDominio(AvaliacaoServidor avaliacao) {
        return consensoEspelhadoDaAutoavaliacao(avaliacao) ? null : avaliacao.getConsensoDominio();
    }

    private Integer resolverImportanciaServidorParaConsenso(AvaliacaoServidor avaliacao) {
        return autoavaliacaoDisponivelParaConsenso(avaliacao) ? avaliacao.getAutoimportancia() : null;
    }

    private Integer resolverDominioServidorParaConsenso(AvaliacaoServidor avaliacao) {
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
