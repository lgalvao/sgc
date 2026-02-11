package sgc.subprocesso.mapper;

import sgc.comum.config.CentralMapperConfig;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.comum.util.FormatadorData;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Movimentacao;

/**
 * Mapper (usando MapStruct) entre a entidade Movimentacao e seu DTO.
 */
@Mapper(componentModel = "spring", config = CentralMapperConfig.class, imports = FormatadorData.class)
public interface MovimentacaoMapper {
    @Mapping(source = "unidadeOrigem.codigo", target = "unidadeOrigemCodigo")
    @Mapping(target = "unidadeOrigemSigla", expression = "java(mapUnidadeSiglaParaUsuario(movimentacao.getUnidadeOrigem()))")
    @Mapping(source = "unidadeOrigem.nome", target = "unidadeOrigemNome")
    @Mapping(source = "unidadeDestino.codigo", target = "unidadeDestinoCodigo")
    @Mapping(target = "unidadeDestinoSigla", expression = "java(mapUnidadeSiglaParaUsuario(movimentacao.getUnidadeDestino()))")
    @Mapping(source = "unidadeDestino.nome", target = "unidadeDestinoNome")
    @Mapping(target = "dataHoraFormatada", expression = "java(FormatadorData.formatarDataHora(movimentacao.getDataHora()))")
    @Nullable MovimentacaoDto toDto(@Nullable Movimentacao movimentacao);

    /**
     * Mapeia sigla da unidade, substituindo RAIZ (id=1) por "SEDOC" para o usuário.
     * A unidade RAIZ é interna/técnica e nunca deve aparecer na UI.
     * 
     * @param unidade a unidade a mapear
     * @return a sigla para exibição ao usuário ("SEDOC" se for RAIZ, senão a sigla original)
     */
    default String mapUnidadeSiglaParaUsuario(sgc.organizacao.model.Unidade unidade) {
        // Se é unidade RAIZ (id=1), exibe "SEDOC" para o usuário
        return unidade.getCodigo() == 1L ? "SEDOC" : unidade.getSigla();
    }
}
