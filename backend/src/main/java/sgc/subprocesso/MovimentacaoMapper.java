package sgc.subprocesso;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper (usando MapStruct) entre a entidade Movimentacao e seu DTO.
 */
@Mapper(componentModel = "spring")
public interface MovimentacaoMapper {

    @Mapping(source = "unidadeOrigem.codigo", target = "unidadeOrigemCodigo")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigemSigla")
    @Mapping(source = "unidadeOrigem.nome", target = "unidadeOrigemNome")
    @Mapping(source = "unidadeDestino.codigo", target = "unidadeDestinoCodigo")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestinoSigla")
    @Mapping(source = "unidadeDestino.nome", target = "unidadeDestinoNome")
    MovimentacaoDTO toDTO(Movimentacao movimentacao);

}
