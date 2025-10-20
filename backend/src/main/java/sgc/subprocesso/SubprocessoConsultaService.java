package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubprocessoConsultaService {

    private final SubprocessoRepo subprocessoRepo;

    /**
     * Busca e retorna um subprocesso pelo seu ID.
     *
     * @param id O ID do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    public Subprocesso getSubprocesso(Long id) {
        return subprocessoRepo.findById(id)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + id));
    }

    /**
     * Busca e retorna um subprocesso pelo seu ID, garantindo que ele possua um
     * mapa de competências associado.
     *
     * @param id O ID do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado ou
     *                                  se não possuir um mapa associado.
     */
    public Subprocesso getSubprocessoComMapa(Long id) {
        Subprocesso subprocesso = getSubprocesso(id);
        if (subprocesso.getMapa() == null) {
            throw new ErroDominioNaoEncontrado("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }
}