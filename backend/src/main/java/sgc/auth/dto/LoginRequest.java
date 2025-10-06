package sgc.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login.
 * Recebe o título (CPF) e senha do usuário.
 */
public record LoginRequest(
    @NotBlank(message = "Título é obrigatório")
    String titulo,
    
    @NotBlank(message = "Senha é obrigatória")
    String senha
) {}