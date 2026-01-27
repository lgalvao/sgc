package sgc.analise;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sgc.analise.dto.CriarAnaliseCommand;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;

/**
 * Serviço para gerenciar as análises de subprocessos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnaliseFacade {
    private final AnaliseRepo analiseRepo;
    private final UnidadeFacade unidadeService;

    /**
     * Lista todas as análises de um determinado tipoAnalise para um subprocesso específico.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param tipoAnalise    O tipoAnalise de análise a ser filtrada (e.g., CADASTRO, VALIDACAO).
     * @return Uma lista de {@link Analise} ordenada pela data e hora em ordem decrescente.
     */
    @Transactional(readOnly = true)
    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipoAnalise) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                .filter(analise -> analise.getTipo() == tipoAnalise)
                .toList();
    }

    /**
     * Cria e persiste uma análise com base nos dados fornecidos.
     *
     * @param subprocesso A entidade do subprocesso.
     * @param command     O comando contendo todas as informações necessárias para criar a análise.
     * @return A entidade {@link Analise} que foi criada e salva no banco de dados.
     */
    @Transactional
    public Analise criarAnalise(Subprocesso subprocesso, CriarAnaliseCommand command) {
        Unidade unidade = null;
        if (command.siglaUnidade() != null) {
            var unidadeDto = unidadeService.buscarPorSigla(command.siglaUnidade());
            unidade = unidadeService.buscarEntidadePorId(unidadeDto.getCodigo());
        }

        Analise analise = Analise.builder()
                .subprocesso(subprocesso)
                .dataHora(LocalDateTime.now())
                .observacoes(command.observacoes())
                .tipo(command.tipo())
                .acao(command.acao())
                .unidadeCodigo(unidade != null ? unidade.getCodigo() : null)
                .usuarioTitulo(command.tituloUsuario())
                .motivo(command.motivo())
                .build();

        return analiseRepo.save(analise);
    }

    /**
     * Remove todas as análises associadas a um subprocesso específico.
     *
     * <p>Para cenários de limpeza de dados, como a exclusão de um subprocesso, garantindo que suas
     * análises dependentes também sejam removidas.
     *
     * <p>Este método deve ser chamado dentro de uma transação existente.
     *
     * @param codSubprocesso O código do subprocesso cujas análises serão removidas.
     */
    public void removerPorSubprocesso(Long codSubprocesso) {
        List<Analise> analises = analiseRepo.findBySubprocessoCodigo(codSubprocesso);
        if (!analises.isEmpty()) analiseRepo.deleteAll(analises);
    }
}
