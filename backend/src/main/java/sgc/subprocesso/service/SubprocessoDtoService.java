package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.dto.ConhecimentoMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.atividade.modelo.Conhecimento;
import sgc.atividade.modelo.ConhecimentoRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.modelo.Perfil;
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
     * @param codigo            O código do subprocesso.
     * @param perfil            O perfil do usuário que solicita os detalhes.
     * @param codUnidadeUsuario O código da unidade do usuário.
     * @return Um {@link SubprocessoDetalheDto} com os dados completos.
     * @throws ErroAccessoNegado         se o usuário não tiver permissão.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil, Long codUnidadeUsuario) {
        if (perfil == null) {
            throw new ErroAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = repositorioSubprocesso.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codigo)));

        if (perfil == Perfil.GESTOR || perfil == Perfil.CHEFE) {
            if (sp.getUnidade() == null || codUnidadeUsuario == null || !codUnidadeUsuario.equals(sp.getUnidade().getCodigo())) {
                throw new ErroAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
            }
        } else if (perfil != Perfil.ADMIN) {
            throw new ErroAccessoNegado("Perfil sem permissão.");
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        final List<Atividade> atividades = (sp.getMapa() != null && sp.getMapa().getCodigo() != null)
                ? atividadeRepo.findByMapaCodigo(sp.getMapa().getCodigo())
                : emptyList();

        final Set<Long> idsAtividades = atividades.stream().map(Atividade::getCodigo).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Conhecimento> conhecimentos = repositorioConhecimento.findAll().stream()
                .filter(c -> c.getAtividade() != null && idsAtividades.contains(c.getAtividade().getCodigo()))
                .toList();

        return SubprocessoDetalheDto.of(sp, movimentacoes, atividades.stream().map(atividadeMapper::toDto).collect(Collectors.toList()), conhecimentos.stream().map(conhecimentoMapper::toDto).collect(Collectors.toList()), movimentacaoMapper);
    }

    /**
     * Obtém os dados de cadastro de um subprocesso, incluindo a lista de
     * atividades e seus respectivos conhecimentos.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link SubprocessoCadastroDto} com os dados de cadastro.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
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
                .unidadeSigla(HtmlUtils.escapeHtml(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null))
                .atividades(atividadesComConhecimentos)
                .build();
    }

    /**
     * Obtém as sugestões associadas a um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link SugestoesDto} contendo as sugestões.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        return SugestoesDto.of(sp);
    }

    /**
     * Obtém todos os dados necessários para a tela de ajuste de mapa.
     * <p>
     * Este método agrega informações do subprocesso, da última análise de validação,
     * e de todas as competências, atividades, conhecimentos e seus vínculos
     * associados ao mapa do subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link MapaAjusteDto} com os dados consolidados para o ajuste.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     * @throws IllegalStateException     se o subprocesso não possuir um mapa associado.
     */
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
        List<CompetenciaAtividade> competenciaAtividades = competenciaAtividadeRepo.findByMapaCodigo(codMapa);


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