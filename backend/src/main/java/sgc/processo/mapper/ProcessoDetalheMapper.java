package sgc.processo.mapper;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.UnidadeProcesso;

/**
 * Mapper MapStruct para conversão de entidades relacionadas a ProcessoDetalhe.
 */
@Mapper(componentModel = "spring")
public interface ProcessoDetalheMapper {

    /**
     * Converte Unidade para UnidadeParticipanteDto.
     * Inicializa listas vazias para filhos.
     */
    @Mapping(target = "codUnidade", source = "codigo")
    @Mapping(target = "codUnidadeSuperior", source = "unidadeSuperior.codigo")
    @Mapping(target = "filhos", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "situacaoSubprocesso", ignore = true)
    @Mapping(target = "situacaoLabel", ignore = true)
    @Mapping(target = "dataLimite", ignore = true)
    @Mapping(target = "dataLimiteFormatada", ignore = true)
    @Mapping(target = "codSubprocesso", ignore = true)
    @Mapping(target = "mapaCodigo", ignore = true)
    ProcessoDetalheDto.@Nullable UnidadeParticipanteDto fromUnidade(@Nullable Unidade unidade);

    /**
     * Converte UnidadeProcesso (snapshot) para UnidadeParticipanteDto.
     */
    @Mapping(target = "codUnidade", source = "unidadeCodigo")
    @Mapping(target = "codUnidadeSuperior", source = "unidadeSuperiorCodigo")
    @Mapping(target = "filhos", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "situacaoSubprocesso", ignore = true)
    @Mapping(target = "situacaoLabel", ignore = true)
    @Mapping(target = "dataLimite", ignore = true)
    @Mapping(target = "dataLimiteFormatada", ignore = true)
    @Mapping(target = "codSubprocesso", ignore = true)
    @Mapping(target = "mapaCodigo", ignore = true)
    ProcessoDetalheDto.@Nullable UnidadeParticipanteDto fromSnapshot(@Nullable UnidadeProcesso snapshot);
}
