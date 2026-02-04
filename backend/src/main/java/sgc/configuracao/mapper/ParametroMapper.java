package sgc.configuracao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.dto.ParametroResponse;
import sgc.configuracao.model.Parametro;

@Mapper(componentModel = "spring")
public interface ParametroMapper {
    ParametroResponse toResponse(Parametro parametro);
    
    /**
     * Atualiza uma entidade Parametro existente com dados do DTO de requisição.
     * Preserva o código da entidade existente.
     */
    @Mapping(target = "codigo", ignore = true)
    void atualizarEntidade(ParametroRequest request, @MappingTarget Parametro parametro);
}
