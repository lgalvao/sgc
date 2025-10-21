package sgc.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 *
 * @param codigo O código da atividade.
 * @param mapaCodigo O código do mapa ao qual a atividade pertence.
 * @param descricao A descrição da atividade.
 */
public record AtividadeDto(
    Long codigo,
    Long mapaCodigo,

    @NotBlank(message = "Descrição não pode ser vazia")
    String descricao
) {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    /**
     * Sanitiza a descrição da atividade para remover HTML potencialmente malicioso.
     * @return Um novo DTO com a descrição sanitizada.
     */
    public AtividadeDto sanitize() {
        String sanitizedDescricao = HTML_SANITIZER_POLICY.sanitize(this.descricao);
        return new AtividadeDto(this.codigo, this.mapaCodigo, sanitizedDescricao);
    }
}
