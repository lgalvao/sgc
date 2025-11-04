package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubprocessoConsultaService {
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Busca e retorna um subprocesso pelo seu código.
     *
     * @param codigo O código do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    public Subprocesso getSubprocesso(Long codigo) {
        return subprocessoRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    /**
     * Busca e retorna um subprocesso pelo seu código, garantindo que ele possua um
     * mapa de competências associado.
     *
     * @param codigo O código do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado ou
     *                                  se não possuir um mapa associado.
     */
    public Subprocesso getSubprocessoComMapa(Long codigo) {
        Subprocesso subprocesso = getSubprocesso(codigo);
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }
}