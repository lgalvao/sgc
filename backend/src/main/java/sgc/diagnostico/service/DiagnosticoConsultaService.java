package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;

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
    private final DiagnosticoRepo diagnosticoRepo;
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final OcupacaoCriticaRepo ocupacaoRepo;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final UsuarioFacade usuarioFacade;

    public DiagnosticoContextoDto obterContexto(Long codSubprocesso) {
        Subprocesso sp = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        List<CompetenciaResumoDto> competencias = sp.getMapa().getCompetencias().stream()
                .map(c -> new CompetenciaResumoDto(c.getCodigo(), c.getDescricao()))
                .toList();
        return new DiagnosticoContextoDto(
                sp.getProcesso().getCodigo(),
                sp.getCodigo(),
                sp.getUnidade().getCodigo(),
                sp.getUnidade().getSigla(),
                sp.getUnidade().getNome(),
                sp.getSituacao().name(),
                competencias
        );
    }

    public AutoavaliacaoDto obterAutoavaliacao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        List<AvaliacaoCompetenciaDto> competencias = avaliacaoRepo.buscarAvaliacoesDoServidor(
                        diagnostico.getCodigo(), usuario.getTituloEleitoral())
                .stream()
                .map(a -> new AvaliacaoCompetenciaDto(a.getCompetencia().getCodigo(), a.getImportancia(), a.getDominio()))
                .toList();
        return new AutoavaliacaoDto(competencias, obterSituacaoServidor(diagnostico, usuario.getTituloEleitoral()));
    }

    public ConsensoDto obterConsenso(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        List<AvaliacaoCompetenciaDto> competencias = avaliacaoRepo.buscarAvaliacoesDoServidor(
                        diagnostico.getCodigo(), usuario.getTituloEleitoral())
                .stream()
                .map(a -> new AvaliacaoCompetenciaDto(a.getCompetencia().getCodigo(), a.getImportancia(), a.getDominio()))
                .toList();
        return new ConsensoDto(competencias, obterSituacaoServidor(diagnostico, usuario.getTituloEleitoral()));
    }

    public DiagnosticoEquipeDto obterEquipe(Long codSubprocesso) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());

        Map<String, SituacaoAvaliacaoServidor> situacoes = new HashMap<>();
        Map<String, String> nomes = new HashMap<>();
        for (AvaliacaoServidor a : avaliacoes) {
            String titulo = a.getServidor().getTituloEleitoral();
            situacoes.put(titulo, a.getSituacaoServidor());
            nomes.put(titulo, a.getServidor().getNome());
        }

        List<DiagnosticoEquipeDto.Item> itens = situacoes.entrySet().stream()
                .map(e -> new DiagnosticoEquipeDto.Item(e.getKey(), nomes.get(e.getKey()), e.getValue().name()))
                .toList();

        return new DiagnosticoEquipeDto(itens);
    }

    public DiagnosticoUnidadeDto obterDiagnosticoUnidade(Long codSubprocesso) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);
        var avaliacoes = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        var ocupacoes = ocupacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        var movimentacoes = subprocessoConsultaService.listarMovimentacoes(
                subprocessoConsultaService.buscarSubprocesso(codSubprocesso));

        UnidadeResumoDto unidade = new UnidadeResumoDto(
                subprocesso.getUnidade().getCodigo(),
                subprocesso.getUnidade().getSigla(),
                subprocesso.getUnidade().getNome(),
                subprocesso.getSituacao().name()
        );

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
                            .map(a -> new AvaliacaoCompetenciaDto(
                                    a.getCompetencia().getCodigo(),
                                    a.getImportancia(),
                                    a.getDominio()
                            ))
                            .toList();
                    return new ServidorDiagnosticoDto(
                            primeiro.getServidor().getTituloEleitoral(),
                            primeiro.getServidor().getNome(),
                            primeiro.getSituacaoServidor().name(),
                            consenso
                    );
                })
                .toList();

        List<OcupacaoCriticaDto> ocupacoesCriticas = ocupacoes.stream()
                .map(o -> new OcupacaoCriticaDto(
                        o.getCompetencia().getCodigo(),
                        o.getServidor().getTituloEleitoral(),
                        o.getSituacaoCapacitacao().name()
                ))
                .toList();

        List<MovimentacaoDto> movimentacoesDto = movimentacoes.stream()
                .map(MovimentacaoDto::from)
                .toList();

        return new DiagnosticoUnidadeDto(unidade, servidores, ocupacoesCriticas, movimentacoesDto);
    }

    private String obterSituacaoServidor(Diagnostico diagnostico, String servidorTitulo) {
        return avaliacaoRepo.buscarAvaliacoesDoServidor(diagnostico.getCodigo(), servidorTitulo).stream()
                .findFirst()
                .map(a -> a.getSituacaoServidor().name())
                .orElse(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_REALIZADA.name());
    }
}