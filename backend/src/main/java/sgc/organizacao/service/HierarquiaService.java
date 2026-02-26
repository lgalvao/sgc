package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Serviço centralizado para gerenciar hierarquia de unidades organizacionais.
 * Consolida a lógica de verificação de subordinação.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HierarquiaService {
    private final ResponsabilidadeRepo responsabilidadeRepo;

    /**
     * Verifica se o usuário é o responsável pela unidade (Titular, Substituto ou Atribuição Temporária).
     *
     * @param unidade A unidade
     * @param usuario O usuário
     * @return true se o usuário é o responsável
     */
    public boolean isResponsavel(Unidade unidade, Usuario usuario) {
        return responsabilidadeRepo.findById(unidade.getCodigo())
                .map(resp -> Objects.equals(resp.getUsuarioTitulo(), usuario.getTituloEleitoral()))
                .orElse(false);
    }

    /**
     * Verifica se uma unidade é subordinada a outra na hierarquia.
     *
     * @param alvo     A unidade que pode ser subordinada
     * @param superior A unidade que pode ser superior
     * @return true se 'alvo' é subordinada a 'superior' (direta ou indiretamente)
     */
    public boolean isSubordinada(Unidade alvo, Unidade superior) {
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
    public boolean isMesmaOuSubordinada(Unidade alvo, Unidade superior) {
        if (Objects.equals(alvo.getCodigo(), superior.getCodigo())) return true;

        return isSubordinada(alvo, superior);
    }

    /**
     * Verifica se uma unidade é a superior imediata de outra.
     *
     * @param alvo     A unidade alvo
     * @param superior A potencial unidade superior imediata
     * @return true se 'superior' é a unidade superior imediata de 'alvo'
     */
    public boolean isSuperiorImediata(Unidade alvo, Unidade superior) {
        Unidade superiorAlvo = alvo.getUnidadeSuperior();
        if (superiorAlvo == null) return false;

        return Objects.equals(superiorAlvo.getCodigo(), superior.getCodigo());
    }
}
