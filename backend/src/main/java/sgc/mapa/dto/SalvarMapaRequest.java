package sgc.mapa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.jspecify.annotations.Nullable;
import java.util.List;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request DTO para salvar um mapa completo. Usado para criar ou atualizar o mapa com todas as suas
 * competências e vínculos com atividades de uma vez.
 */
@Getter
@Builder
@AllArgsConstructor
public class SalvarMapaRequest {
    /**
     * Observações gerais sobre o mapa.
     */
    @Nullable
    @SanitizarHtml
    private final String observacoes;

    /**
     * Lista de competências do mapa.
     */
    @NotEmpty(message = "Lista de competências não pode ser vazia")
    @Valid
    private final List<CompetenciaMapaDto> competencias;
}
