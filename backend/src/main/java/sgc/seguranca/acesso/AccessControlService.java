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
     */
    public <T> boolean podeExecutar(@Nullable Usuario usuario, Acao acao, T recurso) {
        if (usuario == null) {
            log.warn("Tentativa de verificação de permissão com usuário nulo: acao={}, recurso={}", acao, recurso);
            return false;
        }
        
        log.debug("Verificando permissão: usuario={}, acao={}, recurso={}", 
                usuario.getTituloEleitoral(), acao, recurso);
        
        // Delega para a policy apropriada baseado no tipo do recurso
        switch (recurso) {
            case Subprocesso sp -> {
                return subprocessoAccessPolicy.canExecute(usuario, acao, sp);
            }
            case Processo p -> {
                return processoAccessPolicy.canExecute(usuario, acao, p);
            }
            case Atividade a -> {
                return atividadeAccessPolicy.canExecute(usuario, acao, a);
            }
            case Mapa m -> {
                return mapaAccessPolicy.canExecute(usuario, acao, m);
            }
            // Não executa nada para outros tipos ou null
            default -> {
                // Não executa nada para outros tipos ou null
            }
        }

        // Tipo de recurso não reconhecido - nega acesso por segurança
        log.warn("Tipo de recurso não reconhecido para verificação de acesso: {}",
                recurso.getClass().getName());
        return false;
    }

    /**
     * Obtém o motivo da negação de acesso.
     */
    private <T> String obterMotivoNegacao(@Nullable Usuario usuario, Acao acao, T recurso) {
        if (usuario == null) {
            return "Usuário não autenticado não pode executar a ação: " + acao.getDescricao();
        }
        
        // Obtém o motivo da policy apropriada
        return switch (recurso) {
            case Subprocesso s -> subprocessoAccessPolicy.getMotivoNegacao();
            case Processo p -> processoAccessPolicy.getMotivoNegacao();
            case Atividade a -> atividadeAccessPolicy.getMotivoNegacao();
            case Mapa m -> mapaAccessPolicy.getMotivoNegacao();
            // Mensagem genérica para outros tipos
            default -> String.format("Usuário '%s' não tem permissão para executar a ação '%s'",
                    usuario.getTituloEleitoral(), acao.getDescricao());
        };

    }
}
