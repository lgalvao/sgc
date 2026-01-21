package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import java.time.Instant;

/**
 * Serviço responsável por auditar todas as decisões de acesso no sistema.
 * Registra tanto acessos concedidos quanto negados para fins de compliance e segurança.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccessAuditService {

    /**
     * Registra um acesso concedido.
     * 
     * @param usuario O usuário que teve acesso concedido
     * @param acao A ação que foi autorizada
     * @param recurso O recurso acessado
     */
    public void logAccessGranted(Usuario usuario, Acao acao, Object recurso) {
        log.info("ACCESS_GRANTED: user={}, action={}, resource={}, timestamp={}",
                usuario.getTituloEleitoral(),
                acao,
                getResourceId(recurso),
                Instant.now()
        );
    }

    /**
     * Registra um acesso negado.
     * 
     * @param usuario O usuário que teve acesso negado
     * @param acao A ação que foi negada
     * @param recurso O recurso que foi tentado acessar
     * @param motivo O motivo da negação
     */
    public void logAccessDenied(Usuario usuario, Acao acao, Object recurso, String motivo) {
        log.warn("ACCESS_DENIED: user={}, action={}, resource={}, reason={}, timestamp={}",
                usuario.getTituloEleitoral(),
                acao,
                getResourceId(recurso),
                motivo,
                Instant.now()
        );
        
        // Futura implementação: persistir em tabela de auditoria
        // auditRepo.save(new AuditRecord(...));
    }

    /**
     * Obtém um identificador do recurso para logging.
     * 
     * @param recurso O recurso acessado
     * @return String identificadora do recurso
     */
    private String getResourceId(Object recurso) {
        return switch (recurso) {
            case Subprocesso sp -> "Subprocesso:" + sp.getCodigo();
            case Processo p -> "Processo:" + p.getCodigo();
            default ->

                // Para outros tipos de recursos
                    recurso.getClass().getSimpleName();
        };

    }
}
