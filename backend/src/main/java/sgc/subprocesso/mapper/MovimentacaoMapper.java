package sgc.subprocesso.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Movimentacao;

/**
 * Mapper (usando MapStruct) entre a entidade Movimentacao e seu DTO.
 */
@Mapper(componentModel = "spring")
@FunctionalInterface
public interface MovimentacaoMapper {
    @Mapping(source = "unidadeOrigem.codigo", target = "unidadeOrigemCodigo")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigemSigla")
    @Mapping(source = "unidadeOrigem.nome", target = "unidadeOrigemNome")
    @Mapping(source = "unidadeDestino.codigo", target = "unidadeDestinoCodigo")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestinoSigla")
    @Mapping(source = "unidadeDestino.nome", target = "unidadeDestinoNome")
    MovimentacaoDto toDTO(Movimentacao movimentacao);
}
