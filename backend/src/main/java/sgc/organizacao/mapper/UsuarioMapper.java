package sgc.organizacao.mapper;

import sgc.comum.config.CentralMapperConfig;

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
@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public interface UsuarioMapper {

    // ========== Mapeamentos de Unidade ==========

    /**
     * Mapeamento base de Unidade para UnidadeDto.
     * Flag isElegivel é ignorada para ser tratada nos métodos default.
     */
    @Mapping(target = "codigoPai", source = "unidadeSuperior.codigo")
    @Mapping(target = "tipo", expression = "java(unidade.getTipo() != null ? unidade.getTipo().name() : null)")
    @Mapping(target = "subunidades", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "isElegivel", ignore = true)
    @Mapping(target = "sigla", expression = "java(mapUnidadeSiglaParaUsuario(unidade))")
    @Mapping(target = "nome", expression = "java(mapUnidadeNomeParaUsuario(unidade))")
    UnidadeDto toUnidadeDtoBase(Unidade unidade);

    default String mapUnidadeSiglaParaUsuario(Unidade unidade) {
        if (unidade == null) return null;
        return Long.valueOf(1L).equals(unidade.getCodigo()) ? "SEDOC" : unidade.getSigla();
    }

    default String mapUnidadeNomeParaUsuario(Unidade unidade) {
        if (unidade == null) return null;
        return Long.valueOf(1L).equals(unidade.getCodigo()) ? "Secretaria de Documentação" : unidade.getNome();
    }

    /**
     * Converte Unidade para UnidadeDto com flag de elegibilidade customizada.
     */
    default UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel) {
        UnidadeDto dto = toUnidadeDtoBase(unidade);
        if (dto != null) {
            dto.setElegivel(isElegivel);
        }
        return dto;
    }

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
