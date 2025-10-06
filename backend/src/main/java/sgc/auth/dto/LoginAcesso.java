package sgc.auth.dto;

/**
 * DTO para enviar requisição ao serviço de autenticação AD.
 * Usado internamente para integração com o sistema Acesso TRE-PE.
 */
public record LoginAcesso(
    String login,      // Título eleitoral do usuário
    String senha,      // Senha do usuário
    Boolean producao   // true para produção, false para homologação
) {}