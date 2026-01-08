package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.organizacao.model.Usuario;

/**
 * Serviço central de controle de acesso do SGC.
 * Responsável por verificar se um usuário pode executar uma ação em um recurso,
 * delegando para as políticas de acesso específicas e auditando todas as decisões.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccessControlService {

    private final AccessAuditService auditService;
    private final HierarchyService hierarchyService;

    /**
     * Verifica se o usuário pode executar uma ação em um recurso.
     * Lança exceção se não tiver permissão.
     * 
     * @param usuario O usuário autenticado
     * @param acao A ação a ser executada
     * @param recurso O recurso alvo
     * @param <T> Tipo do recurso
     * @throws ErroAccessoNegado se não tiver permissão
     */
    public <T> void verificarPermissao(Usuario usuario, Acao acao, T recurso) {
        if (!podeExecutar(usuario, acao, recurso)) {
            String motivo = obterMotivoNegacao(usuario, acao, recurso);
            auditService.logAccessDenied(usuario, acao, recurso, motivo);
            throw new ErroAccessoNegado(motivo);
        }
        
        auditService.logAccessGranted(usuario, acao, recurso);
    }

    /**
     * Verifica se o usuário PODE executar uma ação (sem lançar exceção).
     * 
     * @param usuario O usuário autenticado
     * @param acao A ação a ser executada
     * @param recurso O recurso alvo
     * @param <T> Tipo do recurso
     * @return true se pode executar, false caso contrário
     */
    public <T> boolean podeExecutar(Usuario usuario, Acao acao, T recurso) {
        // Por enquanto, retorna true para não quebrar funcionalidade existente
        // As policies serão implementadas nos próximos sprints
        log.debug("Verificando permissão: usuario={}, acao={}, recurso={}", 
                usuario.getTituloEleitoral(), acao, recurso);
        return true;
    }

    /**
     * Obtém o motivo da negação de acesso.
     * 
     * @param usuario O usuário
     * @param acao A ação
     * @param recurso O recurso
     * @param <T> Tipo do recurso
     * @return Mensagem explicativa
     */
    private <T> String obterMotivoNegacao(Usuario usuario, Acao acao, T recurso) {
        // Implementação básica - será expandida com as policies
        return String.format("Usuário '%s' não tem permissão para executar a ação '%s'", 
                usuario.getTituloEleitoral(), acao.getDescricao());
    }
}
