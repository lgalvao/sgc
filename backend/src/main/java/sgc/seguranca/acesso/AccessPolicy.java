package sgc.seguranca.acesso;

import sgc.organizacao.model.Usuario;

/**
 * Interface base para políticas de acesso específicas de cada tipo de recurso.
 * Cada implementação define as regras de autorização para um tipo de recurso
 * (Processo, Subprocesso, Atividade, etc.).
 * 
 * @param <T> O tipo de recurso gerenciado por esta política
 */
public interface AccessPolicy<T> {
    
    /**
     * Verifica se um usuário pode executar uma ação em um recurso.
     * 
     * @param usuario O usuário autenticado
     * @param acao A ação a ser executada
     * @param recurso O recurso alvo da ação
     * @return true se a ação é permitida, false caso contrário
     */
    boolean canExecute(Usuario usuario, Acao acao, T recurso);
    
    /**
     * Retorna o motivo da última negação de acesso.
     * Útil para fornecer feedback claro ao usuário.
     * 
     * @return Mensagem explicando por que o acesso foi negado
     */
    String getMotivoNegacao();
}
