package sgc.seguranca.dto;

import lombok.*;

@Builder
public record PermissoesSessaoResponse(
        boolean mostrarCriarProcesso,
        boolean mostrarArvoreCompletaUnidades,
        boolean mostrarCtaPainelVazio,
        boolean mostrarRelatorios,
        boolean mostrarDiagnosticoOrganizacional,
        boolean mostrarMenuConfiguracoes,
        boolean mostrarMenuAdministradores,
        boolean mostrarCriarAtribuicaoTemporaria) {
}
