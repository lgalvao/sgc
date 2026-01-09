package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseReq;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoWorkflowExecutor {
    private final SubprocessoRepo repositorioSubprocesso;
    private final SubprocessoTransicaoService transicaoService;
    private final AnaliseService analiseService;

    @Transactional
    public void registrarAnaliseETransicao(
            Subprocesso sp,
            SituacaoSubprocesso novaSituacao,
            TipoTransicao tipoTransicao,
            TipoAnalise tipoAnalise,
            TipoAcaoAnalise tipoAcaoAnalise,
            Unidade unidadeAnalise, // Quem realizou a análise (ex: Unidade Superior)
            Unidade unidadeOrigemTransicao, // Origem da transição
            Unidade unidadeDestinoTransicao, // Destino da transição
            Usuario usuario,
            @Nullable String observacoes,
            @Nullable String motivoAnalise
    ) {
        // 1. Criar Análise
        analiseService.criarAnalise(
                sp,
                CriarAnaliseReq.builder()
                        .codSubprocesso(sp.getCodigo())
                        .observacoes(observacoes)
                        .tipo(tipoAnalise)
                        .acao(tipoAcaoAnalise)
                        .siglaUnidade(unidadeAnalise.getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(motivoAnalise)
                        .build());

        // 2. Atualizar Estado
        sp.setSituacao(novaSituacao);
        repositorioSubprocesso.save(sp);

        // 3. Registrar Transição
        transicaoService.registrar(
                sp,
                tipoTransicao,
                unidadeOrigemTransicao,
                unidadeDestinoTransicao,
                usuario,
                observacoes);

        log.info("Workflow executado: Subprocesso {} -> {}, Transição {}",
                sp.getCodigo(), novaSituacao, tipoTransicao);
    }
}
