package sgc.subprocesso;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper (usando MapStruct) entre a entidade Subprocesso e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface SubprocessoMapper {
    @Mapping(source = "processo.codigo", target = "processoCodigo")
    @Mapping(source = "unidade.codigo", target = "unidadeCodigo")
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    SubprocessoDTO toDTO(Subprocesso subprocesso);

    @Mapping(target = "processo", expression = "java(dto.getProcessoCodigo() != null ? new sgc.processo.Processo() {{ setCodigo(dto.getProcessoCodigo()); }} : null)")
    @Mapping(target = "unidade", expression = "java(dto.getUnidadeCodigo() != null ? new sgc.unidade.Unidade() {{ setCodigo(dto.getUnidadeCodigo()); }} : null)")
    @Mapping(target = "mapa", expression = "java(dto.getMapaCodigo() != null ? new sgc.mapa.Mapa() {{ setCodigo(dto.getMapaCodigo()); }} : null)")
    Subprocesso toEntity(SubprocessoDTO dto);

}
