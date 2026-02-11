package sgc.subprocesso.mapper;

import sgc.comum.config.CentralMapperConfig;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sgc.comum.util.FormatadorData;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.model.Movimentacao;

/**
 * Mapper (usando MapStruct) entre a entidade Movimentacao e seu DTO.
 */
@Mapper(componentModel = "spring", config = CentralMapperConfig.class, imports = FormatadorData.class)
public interface MovimentacaoMapper {
    @Mapping(source = "unidadeOrigem.codigo", target = "unidadeOrigemCodigo")
    @Mapping(source = "unidadeOrigem", target = "unidadeOrigemSigla")
    @Mapping(source = "unidadeOrigem.nome", target = "unidadeOrigemNome")
    @Mapping(source = "unidadeDestino.codigo", target = "unidadeDestinoCodigo")
    @Mapping(source = "unidadeDestino", target = "unidadeDestinoSigla")
    @Mapping(source = "unidadeDestino.nome", target = "unidadeDestinoNome")
    @Mapping(target = "dataHoraFormatada", expression = "java(FormatadorData.formatarDataHora(movimentacao.getDataHora()))")
    MovimentacaoDto toDto(Movimentacao movimentacao);

    /**
     * Mapeia sigla da unidade, substituindo RAIZ (id=1) por "SEDOC" para o usuário.
     * A unidade RAIZ é interna/técnica e nunca deve aparecer na UI.
     * 
     * @param unidade a unidade a mapear (garantida não nula pelo MapStruct)
     * @return a sigla para exibição ao usuário ("SEDOC" se for RAIZ, senão a sigla original)
     */
    default String mapUnidadeSiglaParaUsuario(Unidade unidade) {
        return Objects.equals(unidade.getCodigo(), 1L) ? "SEDOC" : unidade.getSigla();
    }
}
