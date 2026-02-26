package sgc.subprocesso.dto;

import org.mapstruct.*;
import sgc.comum.config.*;
import sgc.comum.util.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

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

    MovimentacaoDto toDto(Movimentacao movimentacao);

    /**
     * Mapeia sigla da unidade, substituindo ADMIN (id=1) por "ADMIN" para o usuário.
     * A unidade ADMIN é interna/técnica.
     * 
     * @param unidade a unidade a mapear (garantida não nula pelo MapStruct)
     * @return a sigla para exibição ao usuário ("ADMIN" se for a unidade raiz, senão a sigla original)
     */
    default String mapUnidadeSiglaParaUsuario(Unidade unidade) {
        return Objects.equals(unidade.getCodigo(), 1L) ? "ADMIN" : unidade.getSigla();
    }
}
