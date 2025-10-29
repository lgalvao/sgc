package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.analise.modelo.Analise;
import sgc.analise.modelo.AnaliseRepo;
import sgc.analise.modelo.TipoAnalise;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para gerenciar as análises de subprocessos.
 */
@Service
@RequiredArgsConstructor
public class AnaliseService {
    private final AnaliseRepo analiseRepo;
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Lista todas as análises de um determinado tipo para um subprocesso específico.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param tipo           O tipo de análise a ser filtrada (e.g., CADASTRO, VALIDACAO).
     * @return Uma lista de {@link Analise} ordenada pela data e hora em ordem decrescente.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public List<Analise> listarPorSubprocesso(Long codSubprocesso, TipoAnalise tipo) {
        if (subprocessoRepo.findById(codSubprocesso).isEmpty()) {
            throw new ErroDominioNaoEncontrado("Subprocesso", codSubprocesso);
        }
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso)
            .stream()
            .filter(a -> a.getTipo() == tipo)
            .toList();
    }

    /**
     * Cria e persiste uma nova análise com base nos dados fornecidos.
     *
     * @param req O DTO contendo todas as informações necessárias para criar a análise.
     * @return A entidade {@link Analise} que foi criada e salva no banco de dados.
     * @throws ErroDominioNaoEncontrado se o subprocesso associado à análise não for encontrado.
     */
    @Transactional
    public Analise criarAnalise(CriarAnaliseRequestDto req) {
        Subprocesso sp = subprocessoRepo.findById(req.getSubprocessoCodigo())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso", req.getSubprocessoCodigo()));

        Analise a = Analise.builder()
            .subprocesso(sp)
            .dataHora(LocalDateTime.now())
            .observacoes(req.getObservacoes())
            .tipo(req.getTipo())
            .acao(req.getAcao())
            .unidadeSigla(req.getUnidadeSigla())
            .analistaUsuarioTitulo(req.getAnalistaUsuarioTitulo())
            .motivo(req.getMotivo())
            .build();

        return analiseRepo.save(a);
    }

    /**
     * Remove todas as análises associadas a um subprocesso específico.
     * <p>
     * Este método é útil para cenários de limpeza de dados, como a exclusão
     * de um subprocesso, garantindo que suas análises dependentes também sejam removidas.
     *
     * @param subprocessoCodigo O código do subprocesso cujas análises serão removidas.
     */
    @Transactional
    public void removerPorSubprocesso(Long subprocessoCodigo) {
        analiseRepo.deleteBySubprocessoCodigo(subprocessoCodigo);
    }
}
