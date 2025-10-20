package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
@Service
@RequiredArgsConstructor
public class SubprocessoDtoService {

    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo repositorioConhecimento;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final AnaliseService analiseService;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final SubprocessoMapper subprocessoMapper;

    /**
     * Obtém os detalhes completos de um subprocesso, incluindo movimentações,
     * atividades e conhecimentos.
     * <p>
     * O acesso é restrito a usuários ADMIN ou a usuários com perfil GESTOR/CHEFE
     * que pertençam à mesma unidade do subprocesso.
     *
     * @param id             O ID do subprocesso.
     * @param perfil         O perfil do usuário que solicita os detalhes.
     * @param unidadeUsuario O ID da unidade do usuário.
     * @return Um {@link SubprocessoDetalheDto} com os dados completos.
     * @throws ErroDominioAccessoNegado se o usuário não tiver permissão.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long id, String perfil, Long unidadeUsuario) {
        if (perfil == null) {
            throw new ErroDominioAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = repositorioSubprocesso.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(id)));

        if ("GESTOR".equalsIgnoreCase(perfil) || "CHEFE".equalsIgnoreCase(perfil)) {
            if (sp.getUnidade() == null || unidadeUsuario == null || !unidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new ErroDominioAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(perfil)) {
            throw new ErroDominioAccessoNegado("Perfil sem permissão.");
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo())
                : emptyList();
        final Set<Long> idsAtividades = atividades.stream().map(Atividade::getCodigo).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Conhecimento> conhecimentos = repositorioConhecimento.findAll().stream()
                .filter(c -> c.getAtividade() != null && idsAtividades.contains(c.getAtividade().getCodigo()))
                .toList();

        return SubprocessoDetalheDto.of(sp, movimentacoes, atividades.stream().map(atividadeMapper::toDTO).collect(Collectors.toList()), conhecimentos.stream().map(conhecimentoMapper::toDTO).collect(Collectors.toList()), movimentacaoMapper);
    }

    /**
     * Obtém os dados de cadastro de um subprocesso, incluindo a lista de
     * atividades e seus respectivos conhecimentos.
     *
     * @param idSubprocesso O ID do subprocesso.
     * @return Um {@link SubprocessoCadastroDto} com os dados de cadastro.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso", idSubprocesso));

        List<SubprocessoCadastroDto.AtividadeCadastroDTO> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();

            for (Atividade a : atividades) {
                List<Conhecimento> ks = repositorioConhecimento.findByAtividadeCodigo(a.getCodigo());
                List<ConhecimentoDto> ksDto = ks == null
                        ? emptyList()
                        : ks.stream().map(conhecimentoMapper::toDTO).toList();

                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDTO.builder()
                    .id(a.getCodigo())
                    .descricao(a.getDescricao())
                    .conhecimentos(ksDto)
                    .build());
            }
        }

        return SubprocessoCadastroDto.builder()
            .subprocessoId(sp.getCodigo())
            .unidadeSigla(HtmlUtils.escapeHtml(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null))
            .atividades(atividadesComConhecimentos)
            .build();
    }

    /**
     * Obtém as sugestões associadas a um subprocesso.
     *
     * @param idSubprocesso O ID do subprocesso.
     * @return Um {@link SugestoesDto} contendo as sugestões.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        return SugestoesDto.of(sp);
    }

    /**
     * Obtém todos os dados necessários para a tela de ajuste de mapa.
     * <p>
     * Este método agrega informações do subprocesso, da última análise de validação,
     * e de todas as competências, atividades, conhecimentos e seus vínculos
     * associados ao mapa do subprocesso.
     *
     * @param idSubprocesso O ID do subprocesso.
     * @return Um {@link MapaAjusteDto} com os dados consolidados para o ajuste.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException se o subprocesso não possuir um mapa associado.
     */
    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado.");
        }

        Long idMapa = sp.getMapa().getCodigo();

        Analise analise = analiseService.listarPorSubprocesso(idSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst()
                .orElse(null);

        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(idMapa);
        List<Atividade> atividades = atividadeRepo.findByMapaCodigo(idMapa);
        List<Conhecimento> conhecimentos = repositorioConhecimento.findByMapaCodigo(idMapa);
        List<CompetenciaAtividade> competenciaAtividades = competenciaAtividadeRepo.findByMapaCodigo(idMapa);


        return MapaAjusteDto.of(sp, analise, competencias, atividades, conhecimentos, competenciaAtividades);
    }

    /**
     * Lista todos os subprocessos cadastrados no sistema.
     *
     * @return Uma {@link List} de {@link SubprocessoDto}.
     */
    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAll()
            .stream()
            .map(subprocessoMapper::toDTO)
            .toList();
    }
}