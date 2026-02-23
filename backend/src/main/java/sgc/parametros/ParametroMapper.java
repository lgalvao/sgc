package sgc.parametros;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.comum.config.CentralMapperConfig;
import sgc.parametros.model.Parametro;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface ParametroMapper {
    /**
     * Atualiza uma entidade Parametro existente com dados do DTO de requisição.
     * Preserva o código da entidade existente.
     */
    @Mapping(target = "codigo", ignore = true)
    void atualizarEntidade(ParametroRequest request, @MappingTarget Parametro parametro);
}
