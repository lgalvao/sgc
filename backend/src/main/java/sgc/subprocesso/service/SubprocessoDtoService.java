package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoDtoService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final CompetenciaRepo competenciaRepo;
    private final AnaliseService analiseService;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final SubprocessoMapper subprocessoMapper;
    private final SubprocessoPermissoesService subprocessoPermissoesService;
    private final SgrhService sgrhService;

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil, Long codUnidadeUsuario) {
        log.debug("Obtendo detalhes para o subprocesso {}", codigo);
        if (perfil == null) {
            log.warn("Perfil inválido para acesso aos detalhes do subprocesso.");
            throw new ErroAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = repositorioSubprocesso.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codigo)));
        log.debug("Subprocesso encontrado: {}", sp.getCodigo());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        log.debug("Usuário autenticado: {}", username);
        Usuario usuario = sgrhService.buscarUsuarioPorLogin(username);
        log.debug("Usuário encontrado: {}", usuario);
        log.debug("Atribuições do usuário (from SGRH): {}", usuario.getTodasAtribuicoes().stream().map(a -> a.getPerfil() + "-" + a.getUnidade().getSigla()).toList());
        log.debug("Perfil solicitado (from request): {}", perfil);

        verificarPermissaoVisualizacao(sp, perfil, usuario);

        Usuario responsavel = sgrhService.buscarResponsavelVigente(sp.getUnidade().getSigla());
        log.debug("Responsável encontrado: {}", responsavel);

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        SubprocessoPermissoesDto permissoes = subprocessoPermissoesService.calcularPermissoes(sp, usuario);
        log.debug("Permissões calculadas: {}", permissoes);

        return SubprocessoDetalheDto.of(sp, responsavel, movimentacoes, movimentacaoMapper, permissoes);
    }

    private void verificarPermissaoVisualizacao(Subprocesso sp, Perfil perfil, Usuario usuario) {
        boolean hasPerfil = usuario.getTodasAtribuicoes().stream()
                .anyMatch(a -> a.getPerfil() == perfil);

        if (!hasPerfil) {
            log.warn("Usuário não possui o perfil solicitado.");
            throw new ErroAccessoNegado("Perfil inválido para o usuário.");
        }

        if (perfil == Perfil.ADMIN) {
            return;
        }

        Unidade unidadeAlvo = sp.getUnidade();

        if (unidadeAlvo == null) {
            throw new ErroAccessoNegado("Unidade não identificada.");
        }

        boolean hasPermission = usuario.getTodasAtribuicoes().stream()
                .filter(a -> a.getPerfil() == perfil)
                .anyMatch(a -> {
                    Unidade unidadeUsuario = a.getUnidade();
                    if (perfil == Perfil.GESTOR) {
                        return isMesmaUnidadeOuSubordinada(unidadeAlvo, unidadeUsuario);
                    } else if (perfil == Perfil.CHEFE || perfil == Perfil.SERVIDOR) {
                        return unidadeAlvo.getCodigo().equals(unidadeUsuario.getCodigo());
                    }
                    return false;
                });

        if (!hasPermission) {
            log.warn("Acesso negado para perfil {} na unidade {}", perfil, unidadeAlvo.getSigla());
            throw new ErroAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
        }
    }

    private boolean isMesmaUnidadeOuSubordinada(Unidade alvo, Unidade superior) {
        sgc.unidade.model.Unidade atual = alvo;
        while (atual != null) {
            if (atual.getCodigo().equals(superior.getCodigo())) {
                return true;
            }
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();

            for (Atividade a : atividades) {
                List<Conhecimento> ks = repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                List<ConhecimentoDto> ksDto = ks == null
                        ? emptyList()
                        : ks.stream().map(conhecimentoMapper::toDto).toList();

                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                        .codigo(a.getCodigo())
                        .descricao(a.getDescricao())
                        .conhecimentos(ksDto)
                        .build());
            }
        }

        return SubprocessoCadastroDto.builder()
                .subprocessoId(sp.getCodigo())
                .unidadeSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null)
                .atividades(atividadesComConhecimentos)
                .build();
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        return SugestoesDto.of(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codSubprocesso));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado.");
        }

        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = analiseService.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst()
                .orElse(null);

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(codMapa);
        List<Conhecimento> conhecimentos = repositorioConhecimento.findByMapaCodigo(codMapa);


        return MapaAjusteDto.of(sp, analise, competencias, atividades, conhecimentos);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAll()
                .stream()
                .map(subprocessoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Subprocesso sp = repositorioSubprocesso.findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o processo %d e unidade %d".formatted(codProcesso, codUnidade)));
        return subprocessoMapper.toDTO(sp);
    }
}
