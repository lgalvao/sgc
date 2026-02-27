package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
class ProcessoValidador {
    private final OrganizacaoFacade organizacaoFacade;
    private final ConsultasSubprocessoService queryService;

    /**
     * Valida se todas as unidades especificadas possuem mapa vigente.
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
