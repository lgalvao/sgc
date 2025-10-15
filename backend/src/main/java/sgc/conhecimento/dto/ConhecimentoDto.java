package sgc.conhecimento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referência por id para evitar expor entidades JPA.
 */
public record ConhecimentoDto(
    Long codigo,
    @NotNull(message = "Código da atividade é obrigatório")
    Long atividadeCodigo,
    @NotBlank(message = "Descrição não pode ser vazia")
    String descricao
) {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    public ConhecimentoDto sanitize() {
        String sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(this.descricao);
        return new ConhecimentoDto(this.codigo, this.atividadeCodigo, sanitizedDescricao);
    }
}