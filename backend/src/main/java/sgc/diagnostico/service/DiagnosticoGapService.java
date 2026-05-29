package sgc.diagnostico.service;


import org.springframework.stereotype.Service;
import sgc.diagnostico.model.AvaliacaoServidor;

@Service
public class DiagnosticoGapService {
    public void recalcularGap(AvaliacaoServidor avaliacao) {
        Integer importancia = avaliacao.getImportancia();
        Integer dominio = avaliacao.getDominio();
        if (importancia == null || dominio == null || importancia == 0 || dominio == 0) {
            avaliacao.setGap(null);
            return;
        }
        avaliacao.setGap(importancia - dominio);
    }
}