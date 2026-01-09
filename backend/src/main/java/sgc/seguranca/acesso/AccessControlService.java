package sgc.seguranca.acesso;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

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
    private final SubprocessoAccessPolicy subprocessoAccessPolicy;
    private final ProcessoAccessPolicy processoAccessPolicy;
    private final AtividadeAccessPolicy atividadeAccessPolicy;
    private final MapaAccessPolicy mapaAccessPolicy;

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
    public <T> boolean podeExecutar(@Nullable Usuario usuario, Acao acao, T recurso) {
        if (usuario == null) {
            log.warn("Tentativa de verificação de permissão com usuário nulo: acao={}, recurso={}", acao, recurso);
            return false;
        }
        
        log.debug("Verificando permissão: usuario={}, acao={}, recurso={}", 
                usuario.getTituloEleitoral(), acao, recurso);
        
        // Delega para a policy apropriada baseado no tipo do recurso
        if (recurso instanceof Subprocesso subprocesso) {
            return subprocessoAccessPolicy.canExecute(usuario, acao, subprocesso);
        }
        
        if (recurso instanceof Processo processo) {
            return processoAccessPolicy.canExecute(usuario, acao, processo);
        }
        
        if (recurso instanceof Atividade atividade) {
            return atividadeAccessPolicy.canExecute(usuario, acao, atividade);
        }
        
        if (recurso instanceof Mapa mapa) {
            return mapaAccessPolicy.canExecute(usuario, acao, mapa);
        }
        
        // Tipo de recurso não reconhecido - nega acesso por segurança
        log.warn("Tipo de recurso não reconhecido para verificação de acesso: {}", 
                recurso != null ? recurso.getClass().getName() : "null");
        return false;
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
    private <T> String obterMotivoNegacao(@Nullable Usuario usuario, Acao acao, T recurso) {
        if (usuario == null) {
            return "Usuário não autenticado não pode executar a ação: " + acao.getDescricao();
        }
        
        // Obtém o motivo da policy apropriada
        if (recurso instanceof Subprocesso) {
            return subprocessoAccessPolicy.getMotivoNegacao();
        }
        
        if (recurso instanceof Processo) {
            return processoAccessPolicy.getMotivoNegacao();
        }
        
        if (recurso instanceof Atividade) {
            return atividadeAccessPolicy.getMotivoNegacao();
        }
        
        if (recurso instanceof Mapa) {
            return mapaAccessPolicy.getMotivoNegacao();
        }
        
        // Mensagem genérica para outros tipos
        return String.format("Usuário '%s' não tem permissão para executar a ação '%s'", 
                usuario.getTituloEleitoral(), acao.getDescricao());
    }
}
