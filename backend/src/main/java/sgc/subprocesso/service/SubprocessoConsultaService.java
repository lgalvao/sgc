package sgc.subprocesso.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoConsultaService {

    private static final String NOME_ENTIDADE = "Subprocesso";

    private final SubprocessoRepo subprocessoRepo;
    private final MapaManutencaoService mapaManutencaoService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoContextoConsultaService contextoConsultaService;
    private final SubprocessoAcessoService acessoService;
    private final SubprocessoVisualizacaoService visualizacaoService;
    private final sgc.mapa.MapaDtoMapper mapaDtoMapper;
    private final sgc.subprocesso.SubprocessoDtoMapper subprocessoDtoMapper;

    public MapaVisualizacaoResponse mapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return visualizacaoService.mapaParaVisualizacao(sp);
    }

    public ImpactoMapaResponse verificarImpactos(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return visualizacaoService.verificarImpactos(sp);
    }

    public MapaCompletoDto mapaCompletoDtoPorSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getProcesso().getTipo() == TipoProcesso.DIAGNOSTICO
                ? mapaManutencaoService.mapaCompletoVigenteUnidade(subprocesso.getUnidade().getCodigo())
                : mapaManutencaoService.mapaCompletoSubprocesso(codSubprocesso);
        return mapaDtoMapper.paraMapaCompletoDto(mapa);
    }

    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        return (mapa == null) ? SugestoesDto.vazia() : SugestoesDto.de(mapa.getSugestoes());
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapa(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.listarPorProcessoComUnidade(codProcesso);
    }

    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return obterStatus(subprocesso);
    }

    public SubprocessoSituacaoDto obterStatus(Subprocesso subprocesso) {
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao().name())
                .build();
    }

    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return subprocessoRepo.findByMapa_Codigo(codMapa)
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Subprocesso não encontrado para mapa %s".formatted(codMapa)));
    }

    public List<Subprocesso> listarTodos() {
        return subprocessoRepo.listarTodosComFetch();
    }

    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return obterCadastro(subprocesso);
    }

    public SubprocessoCadastroDto obterCadastro(Subprocesso subprocesso) {
        return subprocessoDtoMapper.paraCadastro(subprocesso, listarAtividadesSubprocesso(subprocesso));
    }

    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.buscarPorProcessoEUnidadeComFetch(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "P:%d U:%d".formatted(codProcesso, codUnidade)));
    }

    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) return List.of();
        return subprocessoRepo.listarPorProcessoEUnidadesComUnidade(codProcesso, codUnidades);
    }

    public List<Subprocesso> listarPorProcessoESituacoes(Long codProcesso, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.listarPorProcessoESituacoesComUnidade(codProcesso, situacoes);
    }

    public List<Subprocesso> listarPorProcessoEUnidadeCodigosESituacoes(Long codProcesso, List<Long> codigosUnidades, List<SituacaoSubprocesso> situacoes) {
        if (codigosUnidades.isEmpty() || situacoes.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.listarPorProcessoEUnidadesComUnidade(codProcesso, codigosUnidades).stream()
                .filter(sp -> situacoes.contains(sp.getSituacao()))
                .toList();
    }

    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.listarPorProcessoUnidadeESituacoesComUnidade(codProcesso, codUnidade, situacoes);
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    public SubprocessoDetalheResponse obterDetalhes(Long codigo) {
        Subprocesso sp = buscarSubprocesso(codigo);
        return obterDetalhes(sp);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp) {
        List<Movimentacao> movimentacoes = listarMovimentacoes(sp);
        return visualizacaoService.construirDetalhe(montarContextoConsulta(sp, movimentacoes), movimentacoes);
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocessoComMapa(codSubprocesso);
        return obterContextoEdicao(subprocesso);
    }

    public ContextoEdicaoResponse obterContextoEdicao(Subprocesso subprocesso) {
        return visualizacaoService.montarContextoEdicao(subprocesso, obterDetalhes(subprocesso));
    }

    public ContextoCadastroAtividadesResponse obterContextoCadastroAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocessoComMapa(codSubprocesso);
        return visualizacaoService.montarContextoCadastroAtividades(
                subprocesso,
                visualizacaoService.construirDetalheCadastro(montarContextoConsultaLeve(subprocesso)),
                listarAtividadesSubprocesso(subprocesso)
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp) {
        return acessoService.resolverPermissoes(montarContextoConsultaLeve(sp));
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return obterPermissoesUI(subprocesso);
    }

    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return listarAtividadesSubprocesso(subprocesso);
    }

    public List<AtividadeDto> listarAtividadesSubprocesso(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        return mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa).stream()
                .map(mapaDtoMapper::paraAtividadeDto)
                .toList();
    }

    public List<AtividadeDto> listarAtividadesParaImportacao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        if (subprocesso.getProcesso() == null || subprocesso.getProcesso().getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao("SGC-MSG-100230");
        }
        return listarAtividadesSubprocesso(subprocesso);
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return visualizacaoService.listarHistoricoCadastro(codSubprocesso);
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return visualizacaoService.listarHistoricoValidacao(codSubprocesso);
    }

    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return visualizacaoService.obterMapaParaAjuste(sp);
    }

    public List<Movimentacao> listarMovimentacoes(Subprocesso sp) {
        return movimentacaoRepo.listarPorSubprocessoPaginado(sp.getCodigo(), PageRequest.of(0, 15));
    }

    public List<Movimentacao> listarMovimentacoesOrdenadas(Long subprocessoCodigo) {
        return movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocessoCodigo);
    }

    private ContextoConsultaSubprocesso montarContextoConsultaLeve(Subprocesso sp) {
        return montarContextoConsulta(sp, List.of());
    }

    private ContextoConsultaSubprocesso montarContextoConsulta(Subprocesso sp, List<Movimentacao> movimentacoes) {
        SubprocessoContextoConsultaService.ContextoConsultaBase contextoBase =
                contextoConsultaService.montar(sp, movimentacoes);

        return ContextoConsultaSubprocesso.builder()
                .subprocesso(sp)
                .perfil(contextoBase.perfil())
                .localizacaoAtual(contextoBase.localizacaoAtual())
                .processoFinalizado(contextoBase.processoFinalizado())
                .mesmaUnidade(contextoBase.mesmaUnidade())
                .mesmaUnidadeAlvo(contextoBase.mesmaUnidadeAlvo())
                .unidadeAlvoNaHierarquiaUsuario(contextoBase.unidadeAlvoNaHierarquiaUsuario())
                .temMapaVigente(contextoBase.temMapaVigente())
                .build();
    }

    @Builder
    public record ContextoConsultaSubprocesso(
            Subprocesso subprocesso,
            Perfil perfil,
            Unidade localizacaoAtual,
            boolean processoFinalizado,
            boolean mesmaUnidade,
            boolean mesmaUnidadeAlvo,
            boolean unidadeAlvoNaHierarquiaUsuario,
            boolean temMapaVigente
    ) {

        public Unidade unidadeAlvo() {
            return subprocesso.getUnidade();
        }

        public SituacaoSubprocesso situacao() {
            return subprocesso.getSituacao();
        }

        public boolean isMesmaUnidadeAlvo() {
            return mesmaUnidadeAlvo;
        }

        public boolean isUnidadeAlvoNaHierarquiaUsuario() {
            return unidadeAlvoNaHierarquiaUsuario;
        }

        public boolean isChefe() {
            return perfil == Perfil.CHEFE;
        }

        public boolean isGestor() {
            return perfil == Perfil.GESTOR;
        }

        public boolean isAdmin() {
            return perfil == Perfil.ADMIN;
        }

        public boolean isGestorOuAdmin() {
            return isGestor() || isAdmin();
        }
    }
}
