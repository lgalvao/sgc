package sgc.seguranca.acesso;

import sgc.organizacao.model.Usuario;

/**
 * Interface base para políticas de acesso específicas de cada tipo de recurso.
 * Cada implementação define as regras de autorização para um tipo de recurso
 * (Processo, Subprocesso, Atividade, etc.).
 */
public interface AccessPolicy<T> {
    boolean canExecute(Usuario usuario, Acao acao, T recurso);

    String getMotivoNegacao();
}
