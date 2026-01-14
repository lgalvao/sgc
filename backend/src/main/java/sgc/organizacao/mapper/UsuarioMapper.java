package sgc.organizacao.mapper;

import org.mapstruct.Mapper;
import sgc.organizacao.dto.AtribuicaoTemporariaDto;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.AtribuicaoTemporaria;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    default UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel) {
        Unidade unidadeSuperior = unidade.getUnidadeSuperior();

        UnidadeDto dto = UnidadeDto.builder().build();

        dto.setCodigo(unidade.getCodigo())
                .setNome(unidade.getNome())
                .setSigla(unidade.getSigla())
                .setCodigoPai(unidadeSuperior != null ? unidadeSuperior.getCodigo() : null)
                .setTipo(unidade.getTipo().name())
                .setSubunidades(new java.util.ArrayList<>())
                .setElegivel(isElegivel)
                .setTituloTitular(unidade.getTituloTitular());

        return dto;
    }

    default UnidadeDto toUnidadeDto(Unidade unidade) {
        return toUnidadeDto(unidade, true);
    }

    /**
     * Converte uma entidade Usuario para UsuarioDto.
     *
     * @param usuario A entidade de usuário.
     * @return O DTO de usuário.
     */
    default UsuarioDto toUsuarioDto(Usuario usuario) {
        return UsuarioDto.builder()
                .nome(usuario.getNome())
                .tituloEleitoral(usuario.getTituloEleitoral())
                .email(usuario.getEmail())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(usuario.getUnidadeLotacao().getCodigo())
                .build();
    }

    /**
     * Converte uma entidade AtribuicaoTemporaria para AtribuicaoTemporariaDto.
     *
     * @param atribuicao A entidade de atribuição temporária.
     * @return O DTO de atribuição temporária.
     */
    default AtribuicaoTemporariaDto toAtribuicaoTemporariaDto(AtribuicaoTemporaria atribuicao) {
        if (atribuicao == null) {
            return null;
        }

        return AtribuicaoTemporariaDto.builder()
                .codigo(atribuicao.getCodigo())
                .unidade(toUnidadeDto(atribuicao.getUnidade()))
                .usuario(toUsuarioDto(atribuicao.getUsuario()))
                .dataInicio(atribuicao.getDataInicio())
                .dataTermino(atribuicao.getDataTermino())
                .justificativa(atribuicao.getJustificativa())
                .build();
    }
}
