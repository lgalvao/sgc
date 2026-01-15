package sgc.subprocesso.service.detalhe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubprocessoDetalheService {
    private final SubprocessoCrudService crudService;
    private final AtividadeService atividadeService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final UsuarioService usuarioService;
    private final SubprocessoDetalheMapper subprocessoDetalheMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final AnaliseService analiseService;
    private final CompetenciaService competenciaService;
    private final ConhecimentoService conhecimentoService;
    private final MapaAjusteMapper mapaAjusteMapper;
    private final sgc.seguranca.acesso.AccessControlService accessControlService;

    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocesso(codSubprocesso);
        if (subprocesso.getMapa() == null) {
            return List.of();
        }
        // ⚡ Bolt: Usando 'buscarPorMapaCodigoComConhecimentos' para evitar N+1 queries
        // ao carregar conhecimentos para cada atividade
        List<Atividade> todasAtividades = atividadeService.buscarPorMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private AtividadeVisualizacaoDto mapAtividadeToDto(Atividade atividade) {
        List<ConhecimentoVisualizacaoDto> conhecimentosDto = atividade.getConhecimentos().stream()
                .map(c -> ConhecimentoVisualizacaoDto.builder()
                        .codigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();
        return AtividadeVisualizacaoDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }

    public SubprocessoDetalheDto obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = crudService.buscarSubprocesso(codigo);
        
        // Centralized security check
        accessControlService.verificarPermissao(usuarioAutenticado, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, sp);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());
        Usuario titular = null;
        if (sp.getUnidade().getTituloTitular() != null) {
            try {
                titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
            } catch (Exception e) {
                log.warn("Erro ao buscar titular: {}", e.getMessage());
            }
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        SubprocessoPermissoesDto permissoes = calcularPermissoes(sp, usuarioAutenticado);

        return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
    }

    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null) {
            List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
            for (Atividade a : atividades) {
                List<ConhecimentoDto> ksDto = a.getConhecimentos().stream().map(conhecimentoMapper::toDto).toList();
                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                        .codigo(a.getCodigo())
                        .descricao(a.getDescricao())
                        .conhecimentos(ksDto)
                        .build());
            }
        }
        return SubprocessoCadastroDto.builder()
                .subprocessoCodigo(sp.getCodigo())
                .unidadeSigla(sp.getUnidade().getSigla())
                .atividades(atividadesComConhecimentos)
                .build();
    }

    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return SugestoesDto.of(sp);
    }

    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocessoComMapa(codSubprocesso);
        Long codMapa = sp.getMapa().getCodigo();
        Analise analise = analiseService.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
        List<Competencia> competencias = competenciaService.buscarPorCodMapa(codMapa);
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigo(codMapa);
        List<Conhecimento> conhecimentos = conhecimentoService.listarPorMapa(codMapa);
        @Nullable Analise analiseVal = analise;
        return mapaAjusteMapper.toDto(sp, analiseVal, competencias, atividades, conhecimentos);
    }

    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return calcularPermissoes(sp, usuario);
    }

    public SubprocessoPermissoesDto calcularPermissoes(Subprocesso subprocesso, Usuario usuario) {
        // Determina as ações baseado no tipo de processo
        boolean isRevisao = subprocesso.getProcesso() != null 
                && subprocesso.getProcesso().getTipo() == sgc.processo.model.TipoProcesso.REVISAO;
        
        sgc.seguranca.acesso.Acao acaoDisponibilizarCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.DISPONIBILIZAR_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.DISPONIBILIZAR_CADASTRO;
        
        sgc.seguranca.acesso.Acao acaoDevolverCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.DEVOLVER_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.DEVOLVER_CADASTRO;
        
        sgc.seguranca.acesso.Acao acaoAceitarCadastro = isRevisao 
                ? sgc.seguranca.acesso.Acao.ACEITAR_REVISAO_CADASTRO 
                : sgc.seguranca.acesso.Acao.ACEITAR_CADASTRO;
        
        return SubprocessoPermissoesDto.builder()
                .podeVerPagina(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                .podeEditarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.EDITAR_MAPA, subprocesso))
                .podeVisualizarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_MAPA, subprocesso))
                .podeDisponibilizarMapa(podeExecutar(usuario, sgc.seguranca.acesso.Acao.DISPONIBILIZAR_MAPA, subprocesso))
                .podeDisponibilizarCadastro(podeExecutar(usuario, acaoDisponibilizarCadastro, subprocesso))
                .podeDevolverCadastro(podeExecutar(usuario, acaoDevolverCadastro, subprocesso))
                .podeAceitarCadastro(podeExecutar(usuario, acaoAceitarCadastro, subprocesso))
                .podeVisualizarDiagnostico(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_DIAGNOSTICO, subprocesso))
                .podeAlterarDataLimite(podeExecutar(usuario, sgc.seguranca.acesso.Acao.ALTERAR_DATA_LIMITE, subprocesso))
                .podeVisualizarImpacto(podeExecutar(usuario, sgc.seguranca.acesso.Acao.VERIFICAR_IMPACTOS, subprocesso))
                .podeRealizarAutoavaliacao(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REALIZAR_AUTOAVALIACAO, subprocesso))
                .podeReabrirCadastro(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REABRIR_CADASTRO, subprocesso))
                .podeReabrirRevisao(podeExecutar(usuario, sgc.seguranca.acesso.Acao.REABRIR_REVISAO, subprocesso))
                .podeEnviarLembrete(podeExecutar(usuario, sgc.seguranca.acesso.Acao.ENVIAR_LEMBRETE_PROCESSO, subprocesso))
                .build();
    }

    private boolean podeExecutar(Usuario usuario, sgc.seguranca.acesso.Acao acao, Subprocesso subprocesso) {
        return accessControlService.podeExecutar(usuario, acao, subprocesso);
    }
}
