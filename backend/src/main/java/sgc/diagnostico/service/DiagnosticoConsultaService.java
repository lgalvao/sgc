package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.UnidadeProcesso;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.service.ResponsavelUnidadeService;
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

    public DiagnosticoContextoDto obterContexto(Long codSubprocesso) {
        Subprocesso sp = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        UnidadeProcesso unidadeSnapshot = resolverUnidadeSnapshot(sp);
        List<CompetenciaResumoDto> competencias = sp.getMapa().getCompetencias().stream()
                .map(c -> CompetenciaResumoDto.builder()
                        .competenciaCodigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();
        return DiagnosticoContextoDto.builder()
                .processoCodigo(sp.getProcesso().getCodigo())
                .subprocessoCodigo(sp.getCodigo())
                .unidadeCodigo(unidadeSnapshot != null ? unidadeSnapshot.getUnidadeCodigoPersistido() : sp.getUnidade().getCodigo())
                .unidadeSigla(unidadeSnapshot != null ? unidadeSnapshot.getSigla() : sp.getUnidade().getSigla())
                .unidadeNome(unidadeSnapshot != null ? unidadeSnapshot.getNome() : sp.getUnidade().getNome())
                .situacaoSubprocesso(sp.getSituacao().name())
                .situacaoDiagnostico(resolverSituacaoDiagnostico(sp))
                .competencias(competencias)
                .build();
    }

    public AutoavaliacaoDto obterAutoavaliacao(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        List<AvaliacaoServidor> avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());
        List<AvaliacaoCompetenciaDto> competencias = avaliacoes.stream()
                .map(a -> AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(a.getCompetencia().getCodigo())
                        .importancia(a.getAutoimportancia())
                        .dominio(a.getAutodominio())
                        .build())
                .toList();
        String situacaoServidor = avaliacoes.stream()
                .findFirst()
                .map(a -> a.getSituacaoServidor().name())
                .orElse(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA.name());
        return AutoavaliacaoDto.builder()
                .competencias(competencias)
                .situacaoServidor(situacaoServidor)
                .build();
    }

    public ConsensoDto obterConsenso(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        return obterConsenso(codSubprocesso, usuario.getTituloEleitoral());
    }

    public ConsensoDto obterConsenso(Long codSubprocesso, String servidorTitulo) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        List<AvaliacaoServidor> avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);
        List<ConsensoCompetenciaDto> competencias = avaliacoes.stream()
                .map(a -> ConsensoCompetenciaDto.builder()
                        .competenciaCodigo(a.getCompetencia().getCodigo())
                        .autoimportancia(a.getAutoimportancia())
                        .autodominio(a.getAutodominio())
                        .chefiaImportancia(a.getChefiaImportancia())
                        .chefiaDominio(a.getChefiaDominio())
                        .consensoImportancia(a.getConsensoImportancia())
                        .consensoDominio(a.getConsensoDominio())
                        .build())
                .toList();
        String situacaoServidor = avaliacoes.stream()
                .findFirst()
                .map(a -> a.getSituacaoServidor().name())
                .orElse(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA.name());
        return ConsensoDto.builder()
                .competencias(competencias)
                .situacaoServidor(situacaoServidor)
                .build();
    }

    public DiagnosticoEquipeDto obterEquipe(Long codSubprocesso) {
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());

        Map<String, SituacaoAvaliacaoServidor> situacoes = new HashMap<>();
        Map<String, String> nomes = new HashMap<>();
        for (AvaliacaoServidor a : avaliacoes) {
            String titulo = a.getServidor().getTituloEleitoral();
            situacoes.put(titulo, a.getSituacaoServidor());
            nomes.put(titulo, a.getServidorNomeDiagnostico());
        }

        List<DiagnosticoEquipeDto.Item> itens = situacoes.entrySet().stream()
                .map(e -> DiagnosticoEquipeDto.Item.builder()
                        .servidorTitulo(e.getKey())
                        .servidorNome(nomes.get(e.getKey()))
                        .situacaoServidor(e.getValue().name())
                        .build())
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
                .unidadeCodigo(unidadeSnapshot != null ? unidadeSnapshot.getUnidadeCodigoPersistido() : subprocesso.getUnidade().getCodigo())
                .unidadeSigla(unidadeSnapshot != null ? unidadeSnapshot.getSigla() : subprocesso.getUnidade().getSigla())
                .unidadeNome(unidadeSnapshot != null ? unidadeSnapshot.getNome() : subprocesso.getUnidade().getNome())
                .situacaoSubprocesso(subprocesso.getSituacao().name())
                .responsavelTitulo(responsavelTitulo)
                .build();

        Map<String, List<AvaliacaoServidor>> porServidor = avaliacoes.stream()
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
                                    .importancia(a.getConsensoImportancia())
                                    .dominio(a.getConsensoDominio())
                                    .build())
                            .toList();
                    return ServidorDiagnosticoDto.builder()
                            .servidorTitulo(primeiro.getServidor().getTituloEleitoral())
                            .servidorNome(primeiro.getServidorNomeDiagnostico())
                            .situacaoServidor(primeiro.getSituacaoServidor().name())
                            .consenso(consenso)
                            .build();
                })
                .toList();

        List<SituacaoCapacitacaoDto> situacoesCapacitacao = situacoes.stream()
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
}
