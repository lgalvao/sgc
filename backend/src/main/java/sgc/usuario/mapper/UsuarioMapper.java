package sgc.usuario.mapper;

import org.mapstruct.Mapper;
import sgc.unidade.dto.AtribuicaoTemporariaDto;
import sgc.unidade.dto.UnidadeDto;
import sgc.unidade.model.AtribuicaoTemporaria;
import sgc.unidade.model.Unidade;
import sgc.usuario.dto.UsuarioDto;
import sgc.usuario.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    default UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel) {
        UnidadeDto dto = UnidadeDto.builder().build();
        if (unidade == null) {
            dto.setSubunidades(new java.util.ArrayList<>());
            dto.setElegivel(isElegivel);
            return dto;
        }

        Unidade unidadeSuperior = unidade.getUnidadeSuperior();

        dto.setCodigo(unidade.getCodigo())
                .setNome(unidade.getNome())
                .setSigla(unidade.getSigla())
                .setCodigoPai(unidadeSuperior != null ? unidadeSuperior.getCodigo() : null)
                .setTipo(unidade.getTipo() != null ? unidade.getTipo().name() : null)
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
        if (usuario == null) {
            return UsuarioDto.builder().build();
        }

        Object tituloObj = usuario.getTituloEleitoral();
        String titulo = tituloObj != null ? tituloObj.toString() : null;

        return UsuarioDto.builder()
                .nome(usuario.getNome())
                .tituloEleitoral(titulo)
                .email(usuario.getEmail())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : null)
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
