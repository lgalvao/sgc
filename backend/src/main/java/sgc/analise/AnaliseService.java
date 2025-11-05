package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciar as análises de subprocessos.
 */
@Service
@RequiredArgsConstructor
public class AnaliseService {
    private final AnaliseRepo analiseRepo;
    private final SubprocessoRepo codSubprocesso;

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
        Subprocesso sp = codSubprocesso.findById(req.getCodSubprocesso())
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", req.getCodSubprocesso()));

        Analise a = Analise.builder()
                .subprocesso(sp)
                .dataHora(LocalDateTime.now())
                .observacoes(req.getObservacoes())
                .tipo(req.getTipo())
                .acao(req.getAcao())
                .unidadeSigla(req.getSiglaUnidade())
                .analistaUsuarioTitulo(req.getTituloUsuario())
                .motivo(req.getMotivo())
                .build();

        return analiseRepo.save(a);
    }

    /**
     * Remove todas as análises associadas a um subprocesso específico.
     * <p>
     * Para cenários de limpeza de dados, como a exclusão de um subprocesso,
     * garantindo que suas análises dependentes também sejam removidas.
     *
     * @param codSubprocesso O código do subprocesso cujas análises serão removidas.
     */
    @Transactional
    public void removerPorSubprocesso(Long codSubprocesso) {
        analiseRepo.deleteBySubprocessoCodigo(codSubprocesso);
    }
}
