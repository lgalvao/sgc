package sgc.organizacao.dto;

import lombok.Builder;

import java.util.List;

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
