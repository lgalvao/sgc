package sgc.subprocesso.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoContextoConsultaService {

    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final HierarquiaService hierarquiaService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;

    public ContextoConsultaBase montar(Subprocesso subprocesso, List<Movimentacao> movimentacoes) {
        ContextoUsuarioAutenticado contextoUsuario = obterContextoUsuarioAutenticado();
        Long unidadeAtivaCodigo = contextoUsuario.unidadeAtivaCodigo();
        Unidade unidadeUsuario = unidadeService.buscarPorCodigoComSuperior(unidadeAtivaCodigo);
        Unidade unidadeAlvo = subprocesso.getUnidade();
        Unidade localizacaoAtual = resolverLocalizacaoAtual(subprocesso, movimentacoes);

        boolean processoFinalizado = processoFinalizado(subprocesso);
        boolean mesmaUnidadeAlvo = Objects.equals(unidadeAtivaCodigo, unidadeAlvo.getCodigo());

        return ContextoConsultaBase.builder()
                .perfil(contextoUsuario.perfil())
                .localizacaoAtual(localizacaoAtual)
                .processoFinalizado(processoFinalizado)
                .mesmaUnidade(!processoFinalizado && Objects.equals(unidadeAtivaCodigo, localizacaoAtual.getCodigo()))
                .mesmaUnidadeAlvo(mesmaUnidadeAlvo)
                .unidadeAlvoNaHierarquiaUsuario(mesmaUnidadeAlvo || hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario))
                .temMapaVigente(!processoFinalizado && unidadeService.temMapaVigente(unidadeAlvo.getCodigo()))
                .build();
    }

    private boolean processoFinalizado(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        return processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO;
    }

    private ContextoUsuarioAutenticado obterContextoUsuarioAutenticado() {
        return Objects.requireNonNull(usuarioFacade.contextoAutenticado(), "contexto autenticado obrigatorio");
    }

    private Unidade resolverLocalizacaoAtual(Subprocesso subprocesso, List<Movimentacao> movimentacoes) {
        return movimentacoes.isEmpty()
                ? localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)
                : movimentacoes.getFirst().getUnidadeDestino();
    }

    @Builder
    public record ContextoConsultaBase(
            Perfil perfil,
            Unidade localizacaoAtual,
            boolean processoFinalizado,
            boolean mesmaUnidade,
            boolean mesmaUnidadeAlvo,
            boolean unidadeAlvoNaHierarquiaUsuario,
            boolean temMapaVigente
    ) {}
}
