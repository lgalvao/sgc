package sgc.analise;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.api.CriarAnaliseRequest;
import sgc.analise.internal.model.Analise;
import sgc.analise.internal.model.AnaliseRepo;
import sgc.analise.internal.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.api.model.Unidade;
import sgc.unidade.api.model.UnidadeRepo;

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
    private final SubprocessoRepo codSubprocesso;
    private final UnidadeRepo unidadeRepo;

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
        if (this.codSubprocesso.findById(codSubprocesso).isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso);
        }

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
        Subprocesso sp = codSubprocesso
                .findById(req.getCodSubprocesso())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", req.getCodSubprocesso()));

        Unidade unidade = null;
        if (req.getSiglaUnidade() != null) {
            unidade = unidadeRepo.findBySigla(req.getSiglaUnidade())
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", req.getSiglaUnidade()));
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
