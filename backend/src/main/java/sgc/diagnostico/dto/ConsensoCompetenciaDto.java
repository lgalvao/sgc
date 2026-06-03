package sgc.diagnostico.dto;

import lombok.Builder;

@Builder
public record ConsensoCompetenciaDto(
        Long competenciaCodigo,
        Integer autoimportancia,
        Integer autodominio,
        Integer chefiaImportancia,
        Integer chefiaDominio,
        Integer consensoImportancia,
        Integer consensoDominio
) {
}
