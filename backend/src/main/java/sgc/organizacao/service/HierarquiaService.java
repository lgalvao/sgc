package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.organizacao.model.Unidade;

import java.util.Objects;

/**
 * Serviço centralizado para gerenciar hierarquia de unidades organizacionais.
 * Consolida a lógica de verificação de subordinação.
 */
@Service
@RequiredArgsConstructor
@org.jspecify.annotations.NullMarked
public class ServicoHierarquia {
    /**
     * Verifica se uma unidade é subordinada a outra na hierarquia.
     * 
     * @param alvo     A unidade que pode ser subordinada
     * @param superior A unidade que pode ser superior
     * @return true se 'alvo' é subordinada a 'superior' (direta ou indiretamente)
     */
    public boolean isSubordinada(@Nullable Unidade alvo, @Nullable Unidade superior) {
        if (alvo == null || superior == null) {
            return false;
        }

        Unidade atual = alvo.getUnidadeSuperior();
        while (atual != null) {
            if (Objects.equals(superior.getCodigo(), atual.getCodigo())) {
                return true;
            }
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }

    /**
     * Verifica se uma unidade é a mesma ou subordinada a outra.
     * 
     * @param alvo     A unidade que pode ser subordinada ou igual
     * @param superior A unidade que pode ser superior ou igual
     * @return true se 'alvo' é a mesma unidade ou subordinada a 'superior'
     */
    public boolean isMesmaOuSubordinada(@Nullable Unidade alvo, @Nullable Unidade superior) {
        if (alvo == null || superior == null) {
            return false;
        }

        if (Objects.equals(alvo.getCodigo(), superior.getCodigo())) {
            return true;
        }

        return isSubordinada(alvo, superior);
    }

    /**
     * Verifica se uma unidade é a superior imediata de outra.
     * 
     * @param alvo     A unidade alvo
     * @param superior A potencial unidade superior imediata
     * @return true se 'superior' é a unidade superior imediata de 'alvo'
     */
    public boolean isSuperiorImediata(@Nullable Unidade alvo, @Nullable Unidade superior) {
        if (alvo == null || superior == null) {
            return false;
        }

        Unidade superiorAlvo = alvo.getUnidadeSuperior();
        if (superiorAlvo == null) {
            return false;
        }

        return Objects.equals(superiorAlvo.getCodigo(), superior.getCodigo());
    }
}
