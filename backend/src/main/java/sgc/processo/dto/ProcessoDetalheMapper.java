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
public interface ProcessoDetalheMapper {
    @Mapping(target = "unidades", ignore = true) // Mapeamento customizado necessário
    @Mapping(target = "resumoSubprocessos", ignore = true)
    @Mapping(target = "podeFinalizar", ignore = true)
    @Mapping(target = "podeHomologarCadastro", ignore = true)
    @Mapping(target = "podeHomologarMapa", ignore = true)

    // Mapeamento customizado
    ProcessoDetalheDto toDetailDTO(Processo processo);

    // Mapeamento de entidade para DTO de participante
    @Mapping(target = "filhos", ignore = true)

    // Presume que filhos não vêm diretamente de UnidadeProcesso
    @Mapping(target = "situacaoSubprocesso", ignore = true)
    @Mapping(target = "dataLimite", ignore = true)
    @Mapping(target = "codUnidade", source = "codUnidade")
    @Mapping(target = "codUnidadeSuperior", source = "codUnidadeSuperior")
    ProcessoDetalheDto.UnidadeParticipanteDto unidadeProcessoToUnidadeParticipanteDTO(UnidadeProcesso unidadeProcesso);

    // Mapeamento de subprocesso para DTO de resumo
    @Mapping(target = "codigo", source = "processo.codigo")
    @Mapping(target = "descricao", source = "processo.descricao")
    @Mapping(target = "situacao", source = "processo.situacao")
    @Mapping(target = "tipo", source = "processo.tipo")
    @Mapping(target = "dataLimite", source = "processo.dataLimite")
    @Mapping(target = "dataCriacao", source = "processo.dataCriacao")
    @Mapping(target = "unidadeCodigo", source = "unidade.codigo")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso);

    // Mapeamento de subprocesso para DTO de participante (usado quando não há UnidadeProcesso correspondente)
    @Mapping(target = "codUnidade", source = "unidade.codigo")
    @Mapping(target = "nome", source = "unidade.nome")
    @Mapping(target = "sigla", source = "unidade.sigla")
    @Mapping(target = "codUnidadeSuperior", source = "unidade.unidadeSuperior.codigo")
    @Mapping(target = "situacaoSubprocesso", source = "situacao")
    @Mapping(target = "dataLimite", source = "dataLimiteEtapa1")
    @Mapping(target = "filhos", ignore = true)
    ProcessoDetalheDto.UnidadeParticipanteDto subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso);
}