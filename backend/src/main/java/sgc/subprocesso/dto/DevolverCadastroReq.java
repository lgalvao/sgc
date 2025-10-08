package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de devolução de cadastro (CDU-13 item 9 e CDU-14 item 10).
 */
public record DevolverCadastroReq(
    @NotBlank(message = "Motivo da devolução é obrigatório")
    String motivo,
    
    String observacoes
) {}