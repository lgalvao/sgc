package sgc.organizacao.dto;

import lombok.*;

import java.util.*;

@Builder
public record UsuarioConsultaDto(
        String tituloEleitoral,
        String matricula,
        String nome,
        String email,
        String ramal,
        UnidadeResumoDto unidade,
        List<String> perfis) {
}
