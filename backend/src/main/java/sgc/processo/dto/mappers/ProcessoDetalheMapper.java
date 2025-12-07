package sgc.processo.dto.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

/**
 * Mapper (usando MapStruct) para converter a entidade Processo e suas associações para
 * ProcessoDetalheDto.
 */
@Mapper(componentModel = "spring")
@DecoratedWith(ProcessoDetalheMapperCustom.class)
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
    @Mapping(target = "situacaoSubprocesso", ignore = true)
    @Mapping(target = "dataLimite", ignore = true)
    @Mapping(target = "codUnidade", source = "codigo")
    @Mapping(target = "codUnidadeSuperior", source = "unidadeSuperior.codigo")
    @Mapping(target = "mapaCodigo", source = "mapaVigente.codigo")
    @Mapping(target = "codSubprocesso", ignore = true)
    ProcessoDetalheDto.UnidadeParticipanteDto unidadeToUnidadeParticipanteDTO(Unidade unidade);

    // Mapeamento de subprocesso para DTO de resumo
    @Mapping(target = "codigo", source = "processo.codigo")
    @Mapping(target = "descricao", source = "processo.descricao")
    @Mapping(target = "situacao", source = "processo.situacao")
    @Mapping(target = "tipo", source = "processo.tipo")
    @Mapping(target = "dataLimite", source = "processo.dataLimite")
    @Mapping(target = "dataCriacao", source = "processo.dataCriacao")
    @Mapping(target = "unidadeCodigo", source = "unidade.codigo")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    @Mapping(target = "linkDestino", ignore = true)
    @Mapping(target = "unidadesParticipantes", ignore = true)
    ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso);

    // Mapeamento de subprocesso para DTO de participante (usado quando não há
    // UnidadeProcesso correspondente)
    @Mapping(target = "codUnidade", source = "unidade.codigo")
    @Mapping(target = "nome", source = "unidade.nome")
    @Mapping(target = "sigla", source = "unidade.sigla")
    @Mapping(target = "codUnidadeSuperior", source = "unidade.unidadeSuperior.codigo")
    @Mapping(target = "situacaoSubprocesso", source = "situacao")
    @Mapping(target = "dataLimite", source = "dataLimiteEtapa1")
    @Mapping(target = "mapaCodigo", source = "mapa.codigo")
    @Mapping(target = "codSubprocesso", source = "codigo")
    @Mapping(target = "filhos", ignore = true)
    ProcessoDetalheDto.UnidadeParticipanteDto subprocessoToUnidadeParticipanteDTO(
            Subprocesso subprocesso);
}
