package sesel.sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SgrhService {
    @Transactional
    public void sincronizarDados() {
        // TODO: Implementar integração real com SGRH
        // Por enquanto, apenas simula a sincronização
        log.info("Sincronizando dados com SGRH...");
    }
}