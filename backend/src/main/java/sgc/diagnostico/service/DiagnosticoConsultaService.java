package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.UnidadeProcesso;
import sgc.subprocesso.SubprocessoDtoMapper;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoConsultaService {
    private final ComumRepo repo;
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final OcupacaoCriticaRepo ocupacaoRepo;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final SubprocessoDtoMapper subprocessoDtoMapper;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;

    public DiagnosticoContextoDto obterContexto(Long codSubprocesso) {
        Subprocesso sp = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        Diagnostico diagnostico = repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso));
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
                .situacaoDiagnostico(diagnostico.getSituacao().name())
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
        List<AvaliacaoCompetenciaDto> competencias = avaliacoes.stream()
                .map(a -> AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(a.getCompetencia().getCodigo())
                        .importancia(a.getConsensoImportancia())
                        .dominio(a.getConsensoDominio())
                        .build())
                .toList();
        List<ConsensoCompetenciaDto> competenciasDetalhadas = avaliacoes.stream()
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
                .competenciasDetalhadas(competenciasDetalhadas)
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
        var ocupacoes = ocupacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        var movimentacoes = subprocessoConsultaService.listarMovimentacoes(subprocesso);
        UnidadeProcesso unidadeSnapshot = resolverUnidadeSnapshot(subprocesso);

        UnidadeResumoDto unidade = UnidadeResumoDto.builder()
                .unidadeCodigo(unidadeSnapshot != null ? unidadeSnapshot.getUnidadeCodigoPersistido() : subprocesso.getUnidade().getCodigo())
                .unidadeSigla(unidadeSnapshot != null ? unidadeSnapshot.getSigla() : subprocesso.getUnidade().getSigla())
                .unidadeNome(unidadeSnapshot != null ? unidadeSnapshot.getNome() : subprocesso.getUnidade().getNome())
                .situacaoSubprocesso(subprocesso.getSituacao().name())
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

        List<OcupacaoCriticaDto> ocupacoesCriticas = ocupacoes.stream()
                .map(o -> OcupacaoCriticaDto.builder()
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
                .situacaoDiagnostico(diagnostico.getSituacao().name())
                .servidores(servidores)
                .ocupacoesCriticas(ocupacoesCriticas)
                .movimentacoes(movimentacoesDto)
                .build();
    }

    private UnidadeProcesso resolverUnidadeSnapshot(Subprocesso subprocesso) {
        return subprocesso.getProcesso()
                .buscarParticipante(subprocesso.getUnidade().getCodigo())
                .orElse(null);
    }

}
