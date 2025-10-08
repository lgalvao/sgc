package sgc.processo.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;

/**
 * Mapper (usando MapStruct) para converter a entidade Processo e suas associações para ProcessoDetalheDto.
 */
@Mapper(componentModel = "spring")
public interface ProcessoDetalheMapperInterface {

    @Mapping(target = "unidades", ignore = true) // Mapeamento customizado necessário
    @Mapping(target = "resumoSubprocessos", ignore = true)
        // Mapeamento customizado necessário
    ProcessoDetalheDto toDetailDTO(Processo processo);

    // Mapeamento de entidade para DTO de participante
    @Mapping(target = "filhos", ignore = true)
    // Assumindo que filhos não vem diretamente de UnidadeProcesso
    ProcessoDetalheDto.UnidadeParticipanteDTO unidadeProcessoToUnidadeParticipanteDTO(UnidadeProcesso unidadeProcesso);

    // Mapeamento de subprocesso para DTO de resumo
    ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso);

    // Mapeamento de subprocesso para DTO de participante (usado quando não há UnidadeProcesso correspondente)
    @Mapping(target = "unidadeCodigo", source = "unidade.codigo")
    @Mapping(target = "nome", source = "unidade.nome")
    @Mapping(target = "sigla", source = "unidade.sigla")
    @Mapping(target = "unidadeSuperiorCodigo", source = "unidade.unidadeSuperior.codigo")
    @Mapping(target = "situacaoSubprocesso", source = "situacaoId")
    @Mapping(target = "dataLimite", source = "dataLimiteEtapa1")
    @Mapping(target = "filhos", ignore = true)
    ProcessoDetalheDto.UnidadeParticipanteDTO subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso);
}