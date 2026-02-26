package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.organizacao.model.*;

import java.time.*;

@Builder
public record AtribuicaoDto(
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
