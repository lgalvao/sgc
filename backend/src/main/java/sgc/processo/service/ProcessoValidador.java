package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pelas validações de regras de negócio de Processo.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class ProcessoValidador {

    private final UnidadeFacade unidadeService;
    private final ConsultasSubprocessoService queryService;

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
                .filter(codigo -> !unidadeService.verificarMapaVigente(codigo))
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
        var resultado = queryService.validarSubprocessosParaFinalizacao(processo.getCodigo());

        if (!resultado.valido()) {
            log.warn("Validação de finalização falhou para processo {}: {}",
                    processo.getCodigo(), resultado.mensagem());
            throw new ErroProcesso(resultado.mensagem());
        }

        log.info("Todos os subprocessos do processo {} estão homologados", processo.getCodigo());
    }
}
