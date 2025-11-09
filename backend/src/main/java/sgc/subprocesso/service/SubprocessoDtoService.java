package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
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
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.model.Perfil;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
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
    private final AnaliseService analiseService;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;
    private final MovimentacaoMapper movimentacaoMapper;
    private final SubprocessoMapper subprocessoMapper;

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
}
