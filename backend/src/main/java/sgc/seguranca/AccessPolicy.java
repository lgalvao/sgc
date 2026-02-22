package sgc.seguranca;

import sgc.organizacao.model.Usuario;

/**
 * Interface base para todas as políticas de acesso do SGC.
 * Define o contrato para verificação de permissões e obtenção de motivos de negação.
 *
 * @param <T> O tipo do recurso protegido pela política.
 */
public interface AccessPolicy<T> {
    /**
     * Verifica se um usuário pode executar uma determinada ação em um recurso.
     *
     * @param usuario O usuário autenticado
     * @param acao    A ação a ser verificada
     * @param recurso O recurso sobre o qual a ação será executada
     * @return true se o acesso for concedido, false caso contrário
     */
    boolean canExecute(Usuario usuario, Acao acao, T recurso);

    /**
     * Obtém o motivo detalhado da última negação de acesso.
     * Útil para fornecer feedback informativo ao usuário.
     *
     * @return String contendo o motivo da negação
     */
    String getMotivoNegacao();
}
