package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HierarquiaService {
    private final ResponsabilidadeRepo responsabilidadeRepo;

    public boolean isResponsavel(Unidade unidade, Usuario usuario) {
        return responsabilidadeRepo.findById(unidade.getCodigo())
                .map(resp -> Objects.equals(resp.getUsuarioTitulo(), usuario.getTituloEleitoral()))
                .orElse(false);
    }

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

    public boolean isMesmaOuSubordinada(Unidade alvo, Unidade superior) {
        if (Objects.equals(alvo.getCodigo(), superior.getCodigo())) return true;

        return isSubordinada(alvo, superior);
    }

    public boolean isSuperiorImediata(Unidade alvo, Unidade superior) {
        Unidade superiorAlvo = alvo.getUnidadeSuperior();
        if (superiorAlvo == null) return false;

        return Objects.equals(superiorAlvo.getCodigo(), superior.getCodigo());
    }
}
