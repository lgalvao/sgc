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
    private final UnidadeRepo unidadeRepo;

    public boolean isResponsavel(Unidade unidade, Usuario usuario) {
        return responsabilidadeRepo.findById(unidade.getCodigoPersistido())
                .map(resp -> Objects.equals(resp.getUsuarioTitulo(), usuario.getTituloEleitoral()))
                .orElse(false);
    }

    public boolean isSubordinada(Unidade alvo, Unidade superior) {
        Long codigoSuperiorAlvo = superior.getCodigoPersistido();

        Unidade atual = alvo;
        while (atual != null) {
            Unidade proxSuperior = atual.getUnidadeSuperior();
            if (proxSuperior == null) break;

            if (Objects.equals(codigoSuperiorAlvo, proxSuperior.getCodigoPersistido())) {
                return true;
            }
            
            // Busca via repo para garantir que o próximo 'getUnidadeSuperior' funcione 
            // mesmo se estivermos lidando com proxies ou sessões parciais.
            atual = unidadeRepo.findById(proxSuperior.getCodigoPersistido()).orElse(null);
        }
        return false;
    }

    public boolean ehMesmaOuSubordinada(Unidade alvo, Unidade superior) {
        if (Objects.equals(alvo.getCodigoPersistido(), superior.getCodigoPersistido())) return true;

        return isSubordinada(alvo, superior);
    }

    public boolean isSuperiorImediata(Unidade alvo, Unidade superior) {
        Unidade superiorAlvo = alvo.getUnidadeSuperior();
        if (superiorAlvo == null) {
            return false;
        }
        return Objects.equals(superiorAlvo.getCodigoPersistido(), superior.getCodigoPersistido());
    }
}
