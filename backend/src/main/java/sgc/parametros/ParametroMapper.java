package sgc.parametros;

import org.mapstruct.*;
import sgc.comum.config.*;
import sgc.parametros.model.*;

@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface ParametroMapper {
    /**
     * Atualiza uma entidade Parametro existente com dados do DTO de requisição.
     * Preserva o código da entidade existente.
     */
    @Mapping(target = "codigo", ignore = true)
    void atualizarEntidade(ParametroRequest request, @MappingTarget Parametro parametro);
}
