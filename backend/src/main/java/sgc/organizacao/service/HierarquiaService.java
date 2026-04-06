package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HierarquiaService {
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final UnidadeHierarquiaService unidadeHierarquiaService;

    public boolean isResponsavel(Unidade unidade, Usuario usuario) {
        return responsabilidadeRepo.findById(unidade.getCodigo())
                .map(resp -> Objects.equals(resp.getUsuarioTitulo(), usuario.getTituloEleitoral()))
                .orElse(false);
    }

    public boolean isSubordinada(Unidade alvo, Unidade superior) {
        List<Long> descendentes = unidadeHierarquiaService.buscarIdsDescendentes(superior.getCodigo());
        if (descendentes.contains(alvo.getCodigo())) {
            return true;
        }

        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(alvo.getCodigo());
        return codigoPai != null && codigoPai.equals(superior.getCodigo());
    }

    public boolean ehMesmaOuSubordinada(Unidade alvo, Unidade superior) {
        if (Objects.equals(alvo.getCodigo(), superior.getCodigo())) return true;

        return isSubordinada(alvo, superior);
    }

    public boolean isSuperiorImediata(Unidade alvo, Unidade superior) {
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(alvo.getCodigo());
        return codigoPai != null && codigoPai.equals(superior.getCodigo());
    }
}
