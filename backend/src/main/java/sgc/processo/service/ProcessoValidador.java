package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.TipoUnidade;
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

    private final OrganizacaoFacade organizacaoFacade;
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

        List<Long> unidadesSemMapa = codigosUnidades.stream()
                .filter(codigo -> !organizacaoFacade.verificarMapaVigente(codigo))
                .toList();

        if (!unidadesSemMapa.isEmpty()) {
            List<String> siglasUnidadesSemMapa = organizacaoFacade.siglasUnidadesPorCodigos(unidadesSemMapa);
            return Optional.of(("As seguintes unidades não possuem mapa vigente e não podem participar"
                    + " de um processo de revisão: %s").formatted(String.join(", ", siglasUnidadesSemMapa)));
        }
        return Optional.empty();
    }

    /**
     * Valida se o processo pode ser finalizado.
     *
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

    /**
     * Valida se as unidades participantes são elegíveis (não são INTERMEDIARIA).
     *
     * @param unidades lista de unidades a validar
     * @return Optional com mensagem de erro se houver unidade inválida
     */
    public Optional<String> validarTiposUnidades(List<Unidade> unidades) {
        if (unidades.isEmpty()) {
            return Optional.empty();
        }

        List<String> unidadesInvalidas = unidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.INTERMEDIARIA)
                .map(Unidade::getSigla)
                .toList();

        if (!unidadesInvalidas.isEmpty()) {
            return Optional.of("Unidades do tipo INTERMEDIARIA não podem participar de processos: "
                    + String.join(", ", unidadesInvalidas));
        }

        return Optional.empty();
    }
}
