package sgc.auth.dto;

/**
 * DTO que representa um perfil do usuário em uma unidade específica.
 * Um usuário pode ter múltiplos perfis em diferentes unidades.
 * <p> <p>
 * Perfis possíveis: ADMIN, GESTOR, CHEFE, SERVIDOR
 */
public record PerfilDto(
    String perfil,    // ADMIN, GESTOR, CHEFE, SERVIDOR
    String unidade    // Código da unidade
) {}