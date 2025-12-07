package sgc.atividade.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import sgc.atividade.model.Atividade;

/** Mapper (usando MapStruct) entre a entidade Atividade e seu DTO. */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
@Mapper(componentModel = "spring")
public abstract class AtividadeMapper {

    /**
     * Converte uma entidade {@link Atividade} em um DTO {@link AtividadeDto}.
     *
     * @param atividade A entidade a ser convertida.
     * @return O DTO correspondente.
     */
    @Mapping(source = "mapa.codigo", target = "mapaCodigo")
    public abstract AtividadeDto toDto(Atividade atividade);

    /**
     * Converte um DTO {@link AtividadeDto} em uma entidade {@link Atividade}.
     *
     * @param atividadeDto O DTO a ser convertido.
     * @return A entidade correspondente.
     */
    @Mapping(target = "mapa", ignore = true)
    @Mapping(target = "conhecimentos", ignore = true)
    @Mapping(target = "competencias", ignore = true)
    public abstract Atividade toEntity(AtividadeDto atividadeDto);

}
