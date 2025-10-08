package sgc.subprocesso.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.subprocesso.modelo.Subprocesso;

/**
 * Mapper (usando MapStruct) entre a entidade Subprocesso e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface SubprocessoMapper {
    @Mapping(source = "processo.codigo", target = "processoCodigo")
    @Mapping(source = "unidade.codigo", target = "unidadeCodigo")
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    SubprocessoDto toDTO(Subprocesso subprocesso);

    @Mapping(target = "processo", expression = "java(dto.getProcessoCodigo() != null ? new sgc.processo.modelo.Processo() {{ setCodigo(dto.getProcessoCodigo()); }} : null)")
    @Mapping(target = "unidade", expression = "java(dto.getUnidadeCodigo() != null ? new sgc.unidade.modelo.Unidade() {{ setCodigo(dto.getUnidadeCodigo()); }} : null)")
    @Mapping(target = "mapa", expression = "java(dto.getMapaCodigo() != null ? new sgc.mapa.modelo.Mapa() {{ setCodigo(dto.getMapaCodigo()); }} : null)")
    Subprocesso toEntity(SubprocessoDto dto);

}
