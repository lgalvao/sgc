package sgc.sgrh.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

@Mapper(componentModel = "spring")
public interface SgrhMapper {

    @Mapping(target = "codigo", source = "unidade.codigo")
    @Mapping(target = "nome", source = "unidade.nome")
    @Mapping(target = "sigla", source = "unidade.sigla")
    @Mapping(target = "codigoPai", expression = "java(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null)")
    @Mapping(target = "tipo", expression = "java(unidade.getTipo() != null ? unidade.getTipo().name() : null)")
    @Mapping(target = "subunidades", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isElegivel", source = "isElegivel")
    UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel);

    // Overload for when eligibility is defaulted to true or handled externally
    @Mapping(target = "codigo", source = "unidade.codigo")
    @Mapping(target = "nome", source = "unidade.nome")
    @Mapping(target = "sigla", source = "unidade.sigla")
    @Mapping(target = "codigoPai", expression = "java(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null)")
    @Mapping(target = "tipo", expression = "java(unidade.getTipo() != null ? unidade.getTipo().name() : null)")
    @Mapping(target = "subunidades", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isElegivel", constant = "true")
    UnidadeDto toUnidadeDto(Unidade unidade);

    @Mapping(target = "codigo", expression = "java(String.valueOf(usuario.getTituloEleitoral()))")
    @Mapping(target = "nome", source = "nome")
    @Mapping(target = "tituloEleitoral", expression = "java(String.valueOf(usuario.getTituloEleitoral()))")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "unidadeCodigo", source = "unidadeLotacao.codigo")
    ServidorDto toServidorDto(Usuario usuario);
}
