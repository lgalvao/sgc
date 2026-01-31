package sgc.organizacao.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.AtribuicaoTemporaria;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

/**
 * Mapper MapStruct para conversão de entidades de organização para DTOs.
 *
 * <p>Centraliza todos os mapeamentos de Unidade, Usuario e AtribuicaoTemporaria.
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // ========== Mapeamentos de Unidade ==========

    /**
     * Converte Unidade para UnidadeDto com flag de elegibilidade customizada.
     */
    @Mapping(target = "codigoPai", source = "unidade.unidadeSuperior.codigo")
    @Mapping(target = "tipo", expression = "java(unidade.getTipo() != null ? unidade.getTipo().name() : null)")
    @Mapping(target = "subunidades", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isElegivel", source = "isElegivel")
    UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel);

    /**
     * Converte Unidade para UnidadeDto (elegível por padrão).
     */
    default UnidadeDto toUnidadeDto(Unidade unidade) {
        return toUnidadeDto(unidade, true);
    }

    /**
     * Converte Unidade para UnidadeDto calculando elegibilidade pelo tipo.
     * Unidades INTERMEDIARIAS não são elegíveis.
     */
    @Named("toUnidadeDtoComElegibilidadeCalculada")
    default UnidadeDto toUnidadeDtoComElegibilidadeCalculada(Unidade unidade) {
        if (unidade == null) return null;
        boolean elegivel = unidade.getTipo() != TipoUnidade.INTERMEDIARIA;
        return toUnidadeDto(unidade, elegivel);
    }

    // ========== Mapeamentos de Usuario ==========

    /**
     * Converte Usuario para UsuarioDto.
     */
    @Mapping(target = "unidadeCodigo", source = "unidadeLotacao.codigo")
    UsuarioDto toUsuarioDto(Usuario usuario);

    // ========== Mapeamentos de AtribuicaoTemporaria ==========

    /**
     * Converte AtribuicaoTemporaria para AtribuicaoTemporariaDto.
     */
    @Mapping(target = "unidade", source = "unidade", qualifiedByName = "toUnidadeDtoSimples")
    @Mapping(target = "usuario", source = "usuario")
    AtribuicaoTemporariaDto toAtribuicaoTemporariaDto(AtribuicaoTemporaria atribuicao);

    /**
     * Mapeamento simplificado de Unidade para uso em AtribuicaoTemporariaDto.
     */
    @Named("toUnidadeDtoSimples")
    default UnidadeDto toUnidadeDtoSimples(Unidade unidade) {
        return toUnidadeDto(unidade, true);
    }
}
