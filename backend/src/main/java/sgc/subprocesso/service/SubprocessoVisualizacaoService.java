package sgc.subprocesso.service;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
public class SubprocessoVisualizacaoService {

    private final UsuarioFacade usuarioFacade;
    private final MapaManutencaoService mapaManutencaoService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final ImpactoMapaService impactoMapaService;
    private final SubprocessoAcessoService acessoService;
    private final AnaliseRepo analiseRepo;
    private final AnaliseHistoricoService analiseHistoricoService;

    public MapaVisualizacaoResponse mapaParaVisualizacao(Subprocesso sp) {
        return mapaVisualizacaoService.obterMapaParaVisualizacao(sp);
    }

    public ImpactoMapaResponse verificarImpactos(Subprocesso sp) {
        return impactoMapaService.verificarImpactos(sp);
    }

    public SubprocessoDetalheResponse construirDetalhe(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto, List<Movimentacao> movimentacoes) {
        Subprocesso subprocesso = contexto.subprocesso();
        Unidade unidadeAlvo = contexto.unidadeAlvo();
        Usuario titular = buscarTitularUnidade(unidadeAlvo);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(subprocesso))
                .responsavel(usuarioFacade.buscarResponsabilidadeDetalhadaAtual(unidadeAlvo.getCodigo()))
                .titular(UsuarioResumoDto.fromEntity(titular))
                .movimentacoes(listarMovimentacoesDto(movimentacoes))
                .localizacaoAtual(contexto.localizacaoAtual().getSigla())
                .permissoes(acessoService.resolverPermissoes(contexto))
                .build();
    }

    public SubprocessoDetalheResponse construirDetalheCadastro(SubprocessoConsultaService.ContextoConsultaSubprocesso contexto) {
        Subprocesso subprocesso = contexto.subprocesso();

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(subprocesso))
                .responsavel(null)
                .titular(null)
                .movimentacoes(List.of())
                .localizacaoAtual(contexto.localizacaoAtual().getSigla())
                .permissoes(acessoService.resolverPermissoes(contexto))
                .build();
    }

    public ContextoEdicaoResponse montarContextoEdicao(Subprocesso subprocesso, SubprocessoDetalheResponse detalhes) {
        Mapa mapaCompleto = mapaManutencaoService.mapaCompletoSubprocesso(subprocesso.getCodigo());

        return new ContextoEdicaoResponse(
                subprocesso.getUnidade(),
                SubprocessoResumoDto.fromEntity(subprocesso),
                detalhes,
                MapaCompletoDto.fromEntity(mapaCompleto)
        );
    }

    public ContextoCadastroAtividadesResponse montarContextoCadastroAtividades(
            Subprocesso subprocesso,
            SubprocessoDetalheResponse detalhes,
            List<AtividadeDto> atividadesDisponiveis
    ) {
        return new ContextoCadastroAtividadesResponse(
                subprocesso.getUnidade(),
                detalhes,
                MapaResumoDto.fromEntity(subprocesso.getMapa()),
                atividadesDisponiveis,
                obterAssinaturaCadastroReferencia(subprocesso, atividadesDisponiveis)
        );
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.CADASTRO);
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.VALIDACAO);
    }

    public MapaAjusteDto obterMapaParaAjuste(Subprocesso sp) {
        Long codMapa = sp.getMapa().getCodigo();

        return MapaAjusteDto.of(
                sp,
                obterAnaliseMaisRecentePorTipo(sp.getCodigo()),
                mapaManutencaoService.competenciasCodMapaSemRels(codMapa),
                mapaManutencaoService.atividadesMapaCodigoSemRels(codMapa),
                mapaManutencaoService.conhecimentosCodMapa(codMapa)
        );
    }

    private List<AnaliseHistoricoDto> listarHistoricoPorTipo(Long codSubprocesso, TipoAnalise tipo) {
        return analiseHistoricoService.converterLista(listarAnalisesPorTipo(codSubprocesso, tipo));
    }

    private List<Analise> listarAnalisesPorTipo(Long codSubprocesso, TipoAnalise tipo) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                .filter(analise -> analise.getTipo() == tipo)
                .toList();
    }

    private @Nullable Analise obterAnaliseMaisRecentePorTipo(Long codSubprocesso) {
        return listarAnalisesPorTipo(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
    }

    private String obterAssinaturaCadastroReferencia(Subprocesso subprocesso, List<AtividadeDto> atividadesDisponiveis) {
        List<AtividadeDto> atividadesReferencia = atividadesDisponiveis;

        if (subprocesso.getProcesso().getTipo() == TipoProcesso.REVISAO) {
            atividadesReferencia = mapaManutencaoService.mapaVigenteUnidade(subprocesso.getUnidade().getCodigo())
                    .map(Mapa::getCodigo)
                    .map(mapaManutencaoService::atividadesMapaCodigoComConhecimentos)
                    .orElseGet(List::of)
                    .stream()
                    .map(AtividadeDto::fromEntity)
                    .toList();
        }

        return calcularAssinaturaCadastro(atividadesReferencia);
    }

    private String calcularAssinaturaCadastro(List<AtividadeDto> atividades) {
        return atividades.stream()
                .map(atividade -> {
                    String descricao = atividade.descricao().trim();
                    String conhecimentos = atividade.conhecimentos().stream()
                            .map(conhecimento -> conhecimento.descricao().trim())
                            .sorted()
                            .collect(Collectors.joining("\u0001"));
                    return descricao + "\u0002" + conhecimentos;
                })
                .sorted()
                .collect(Collectors.joining("\u0003"));
    }

    private @Nullable Usuario buscarTitularUnidade(Unidade unidade) {
        String tituloTitular = unidade.getTituloTitular();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            return null;
        }
        return usuarioFacade.buscarUsuarioSemAtribuicoes(tituloTitular);
    }

    private List<MovimentacaoDto> listarMovimentacoesDto(List<Movimentacao> movimentacoes) {
        return movimentacoes.stream()
                .map(MovimentacaoDto::from)
                .toList();
    }
}
