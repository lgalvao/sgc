package sgc.diagnostico.dto;

import lombok.*;

@Builder
public record ConsensoCompetenciaDto(
        Long competenciaCodigo,
        String competenciaDescricao,
        Integer servidorImportancia,
        Integer servidorDominio,
        Integer chefiaImportancia,
        Integer chefiaDominio,
        Integer consensoImportancia,
        Integer consensoDominio
) {
}
