package sgc.auth.dto;

import java.util.List;

/**
 * DTO para resposta do login bem-sucedido.
 * Contém o token JWT, perfis do usuário e informações do servidor.
 */
public record LoginResponse(
    String token,
    List<PerfilDto> perfis,
    ServidorDto servidor
) {}