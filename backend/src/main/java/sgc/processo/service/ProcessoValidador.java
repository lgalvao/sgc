package sgc.processo.service;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Optional;

import static sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO;
import static sgc.subprocesso.model.SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO;

/**
 * Serviço responsável pelas validações de regras de negócio de Processo.
 * 
 * <p>Centraliza todas as validações complexas relacionadas a processos,
 * incluindo validação de unidades, subprocessos e regras de finalização.</p>
 * 
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * SubprocessoFacade é injetado com @Lazy para quebrar dependência circular:
 * ProcessoFacade → ProcessoValidador → SubprocessoFacade → ... → ProcessoFacade
 */
@Service
@Slf4j
class ProcessoValidador {
    
    private final UnidadeFacade unidadeService;
    private final SubprocessoFacade subprocessoFacade;

    /**
     * Constructor com @Lazy para quebrar dependência circular.
     */
    public ProcessoValidador(
            UnidadeFacade unidadeService,
            @Lazy SubprocessoFacade subprocessoFacade) {
        this.unidadeService = unidadeService;
        this.subprocessoFacade = subprocessoFacade;
    }

    /**
     * Valida se todas as unidades especificadas possuem mapa vigente.
     * 
     * @param codigosUnidades lista de códigos de unidades a validar
     * @return Optional com mensagem de erro se alguma unidade não possuir mapa vigente
     */
    public Optional<String> getMensagemErroUnidadesSemMapa(@Nullable List<Long> codigosUnidades) {
        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            return Optional.empty();
        }

        List<Unidade> unidades = unidadeService.buscarEntidadesPorIds(codigosUnidades);
        List<Long> unidadesSemMapa = unidades.stream()
                .map(Unidade::getCodigo)
                .filter(codigo -> !unidadeService.verificarExistenciaMapaVigente(codigo))
                .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = unidadeService.buscarSiglasPorIds(unidadesSemMapa);
            return Optional.of(("As seguintes unidades não possuem mapa vigente e não podem participar"
                    + " de um processo de revisão: %s").formatted(String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }

    /**
     * Valida se o processo pode ser finalizado.
     * 
     * @param processo processo a validar
     * @throws ErroProcesso se o processo não estiver em situação válida para finalização
     */
    @Transactional(readOnly = true)
    public void validarFinalizacaoProcesso(Processo processo) {
        if (processo.getSituacao() != SituacaoProcesso.EM_ANDAMENTO) {
            throw new ErroProcesso("Apenas processos 'EM ANDAMENTO' podem ser finalizados.");
        }
        validarTodosSubprocessosHomologados(processo);
    }

    /**
     * Valida se todos os subprocessos de um processo estão homologados.
     * 
     * @param processo processo a validar
     * @throws ErroProcesso se algum subprocesso não estiver homologado
     */
    public void validarTodosSubprocessosHomologados(Processo processo) {
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(processo.getCodigo());
        List<String> pendentes = subprocessos.stream().filter(sp -> sp.getSituacao() != MAPEAMENTO_MAPA_HOMOLOGADO
                && sp.getSituacao() != REVISAO_MAPA_HOMOLOGADO)
                .map(sp -> {
                    String identificador = sp.getUnidade() != null ? sp.getUnidade().getSigla()
                            : String.format("Subprocesso %d", sp.getCodigo());
                    return String.format("%s (Situação: %s)", identificador, sp.getSituacao());
                })
                .toList();

        if (!pendentes.isEmpty()) {
            String mensagem = String.format("Não é possível encerrar o processo. Unidades pendentes de"
                    + " homologação:%n- %s",
                    String.join("%n- ", pendentes));
            log.warn("Validação de finalização falhou: {} subprocessos não homologados.", pendentes.size());
            throw new ErroProcesso(mensagem);
        }
        log.info("Homologados {} subprocessos.", subprocessos.size());
    }
}
