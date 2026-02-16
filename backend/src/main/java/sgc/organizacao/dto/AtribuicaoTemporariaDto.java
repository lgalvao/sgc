package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.organizacao.model.OrganizacaoViews;
import sgc.organizacao.model.Usuario;

import java.time.LocalDateTime;

@Builder
public record AtribuicaoTemporariaDto(
        @JsonView(OrganizacaoViews.Publica.class)
        Long codigo,
        @JsonView(OrganizacaoViews.Publica.class)
        Long unidadeCodigo,
        @JsonView(OrganizacaoViews.Publica.class)
        String unidadeSigla,
        @JsonView(OrganizacaoViews.Publica.class)
        Usuario usuario,
        @JsonView(OrganizacaoViews.Publica.class)
        LocalDateTime dataInicio,
        @JsonView(OrganizacaoViews.Publica.class)
        LocalDateTime dataTermino,
        @JsonView(OrganizacaoViews.Publica.class)
        String justificativa) {
}
