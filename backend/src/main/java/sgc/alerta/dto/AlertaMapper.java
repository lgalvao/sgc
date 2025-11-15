package sgc.alerta.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.alerta.model.Alerta;

@Mapper(componentModel = "spring")
public interface AlertaMapper {
    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigem")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestino")
    @Mapping(target = "dataHoraLeitura", ignore = true)
    @Mapping(target = "linkDestino", ignore = true)
    AlertaDto toDto(Alerta alerta);
}
