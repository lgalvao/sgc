package sgc.subprocesso.service.decomposed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoPermissoesService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubprocessoDetalheService {
    private final SubprocessoCrudService crudService;
    private final AtividadeService atividadeService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final UsuarioService usuarioService;
    private final SubprocessoPermissoesService subprocessoPermissoesService;
    private final SubprocessoDetalheMapper subprocessoDetalheMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final AnaliseService analiseService;
    private final CompetenciaService competenciaService;
    private final ConhecimentoService conhecimentoService;
    private final MapaAjusteMapper mapaAjusteMapper;

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
        // ⚡ Bolt: Usando a lista de conhecimentos já carregada na entidade
        List<Conhecimento> conhecimentos = atividade.getConhecimentos();
        if (conhecimentos == null) {
            conhecimentos = emptyList();
        }

        List<ConhecimentoVisualizacaoDto> conhecimentosDto = conhecimentos.stream()
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

    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil, Usuario usuarioAutenticado) {
        if (perfil == null) {
            throw new ErroAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }
        Subprocesso sp = crudService.buscarSubprocesso(codigo);
        verificarPermissaoVisualizacao(sp, perfil, usuarioAutenticado);

        Usuario responsavel = usuarioService.buscarResponsavelAtual(sp.getUnidade().getSigla());
        Usuario titular = null;
        if (sp.getUnidade() != null && sp.getUnidade().getTituloTitular() != null) {
            try {
                titular = usuarioService.buscarPorLogin(sp.getUnidade().getTituloTitular());
            } catch (Exception e) {
                log.warn("Erro ao buscar titular: {}", e.getMessage());
            }
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        SubprocessoPermissoesDto permissoes = subprocessoPermissoesService.calcularPermissoes(sp, usuarioAutenticado);

        return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
    }

    private void verificarPermissaoVisualizacao(Subprocesso sp, Perfil perfil, Usuario usuario) {
        boolean hasPerfil = usuario.getTodasAtribuicoes().stream().anyMatch(a -> a.getPerfil() == perfil);
        if (!hasPerfil) throw new ErroAccessoNegado("Perfil inválido para o usuário.");
        if (perfil == Perfil.ADMIN) return;

        Unidade unidadeAlvo = sp.getUnidade();
        if (unidadeAlvo == null) throw new ErroAccessoNegado("Unidade não identificada.");

        boolean hasPermission = usuario.getTodasAtribuicoes().stream()
                .filter(a -> a.getPerfil() == perfil)
                .anyMatch(a -> {
                    Unidade unidadeUsuario = a.getUnidade();
                    if (perfil == Perfil.GESTOR) {
                        return isMesmaUnidadeOuSubordinada(unidadeAlvo, unidadeUsuario);
                    }
                    return unidadeAlvo.getCodigo().equals(unidadeUsuario.getCodigo());
                });
        if (!hasPermission) throw new ErroAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
    }

    private boolean isMesmaUnidadeOuSubordinada(Unidade alvo, Unidade superior) {
        Unidade atual = alvo;
        while (atual != null) {
            if (atual.getCodigo().equals(superior.getCodigo())) return true;
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }

    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();
            for (Atividade a : atividades) {
                List<ConhecimentoDto> ksDto = a.getConhecimentos() == null ? emptyList() :
                    a.getConhecimentos().stream().map(conhecimentoMapper::toDto).toList();
                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                        .codigo(a.getCodigo())
                        .descricao(a.getDescricao())
                        .conhecimentos(ksDto)
                        .build());
            }
        }
        return SubprocessoCadastroDto.builder()
                .subprocessoCodigo(sp.getCodigo())
                .unidadeSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null)
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
        return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos);
    }

    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = crudService.buscarSubprocesso(codSubprocesso);
        return subprocessoPermissoesService.calcularPermissoes(sp, usuario);
    }
}
