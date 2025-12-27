package sgc.analise;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.model.Unidade;
import sgc.unidade.service.UnidadeService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciar as análises de subprocessos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnaliseService {
    private final AnaliseRepo analiseRepo;
    private final SubprocessoService subprocessoService;
    private final UnidadeService unidadeService;

    /**
     * Lista todas as análises de um determinado tipoAnalise para um subprocesso específico.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param tipoAnalise    O tipoAnalise de análise a ser filtrada (e.g., CADASTRO, VALIDACAO).
     * @return Uma lista de {@link Analise} ordenada pela data e hora em ordem decrescente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipoAnalise) {
        // Verifica existência (lança exceção se não encontrar)
        subprocessoService.buscarSubprocesso(codSubprocesso);

        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                .filter(analise -> analise.getTipo() == tipoAnalise)
                .toList();
    }

    /**
     * Cria e persiste uma nova análise com base nos dados fornecidos.
     *
     * @param req O DTO contendo todas as informações necessárias para criar a análise.
     * @return A entidade {@link Analise} que foi criada e salva no banco de dados.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso associado à análise não for encontrado.
     */
    @Transactional
    public Analise criarAnalise(CriarAnaliseRequest req) {
        Subprocesso sp = subprocessoService.buscarSubprocesso(req.getCodSubprocesso());

        Unidade unidade = null;
        if (req.getSiglaUnidade() != null) {
            // Requer UnidadeService com método que retorne Entidade por Sigla
            // Adicionar este método em UnidadeService ou usar buscarEntidadePorId se tivermos o ID
            // Como temos sigla, UnidadeService precisa de buscarEntidadePorSigla
            // Por enquanto, usamos buscarPorSigla(DTO) e depois buscarEntidadePorId(DTO.getId()) se necessário,
            // ou melhor, UnidadeService.buscarEntidadePorSigla.
            // Vou assumir que UnidadeService.buscarPorSigla retorna DTO.
            // Para manter consistência com refatoração, UnidadeService deve expor entidade se necessário ou Analise deve usar DTO.
            // Analise entity precisa de unidadeCodigo (Long). UnidadeDTO tem codigo.
            // Mas o código antigo usava unidadeRepo.findBySigla -> Unidade Entity.
            // Analise builder usa unidadeCodigo.

            var unidadeDto = unidadeService.buscarPorSigla(req.getSiglaUnidade());
            unidade = unidadeService.buscarEntidadePorId(unidadeDto.getCodigo());
        }

        Analise analise = Analise.builder()
                .subprocesso(sp)
                .dataHora(LocalDateTime.now())
                .observacoes(req.getObservacoes())
                .tipo(req.getTipo())
                .acao(req.getAcao())
                .unidadeCodigo(unidade != null ? unidade.getCodigo() : null)
                .usuarioTitulo(req.getTituloUsuario())
                .motivo(req.getMotivo())
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
