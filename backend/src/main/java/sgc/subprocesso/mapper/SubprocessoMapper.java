package sgc.subprocesso.mapper;

import sgc.comum.config.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.Subprocesso;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface SubprocessoMapper {
    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidade.codigo", target = "codUnidade")
    @Mapping(source = "mapa.codigo", target = "codMapa")
    SubprocessoDto toDto(Subprocesso subprocesso);

    @Mapping(source = "codigo", target = "codSubprocesso")
    @Mapping(source = "unidade.nome", target = "unidadeNome")
    @Mapping(source = "unidade.sigla", target = "unidadeSigla")
    sgc.processo.dto.SubprocessoElegivelDto toElegivelDto(Subprocesso subprocesso);
}
