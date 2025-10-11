package sgc.analise.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar o histórico de análises de cadastro (CDU-13, item 7).
 */
public record AnaliseCadastroDto(
    LocalDateTime dataHora,
    String unidadeSigla,
    String resultado,
    String observacoes
) {
}