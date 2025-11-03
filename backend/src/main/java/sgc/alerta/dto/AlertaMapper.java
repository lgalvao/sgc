package sgc.alerta.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.alerta.modelo.Alerta;

@Mapper(componentModel = "spring")
public interface AlertaMapper {

    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigem")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestino")
    AlertaDto toDto(Alerta alerta);
}
