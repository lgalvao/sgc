package sgc.sgrh.mapper;

import org.mapstruct.Mapper;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;

@Mapper(componentModel = "spring")
public interface SgrhMapper {
    default UnidadeDto toUnidadeDto(Unidade unidade, boolean isElegivel) {
        UnidadeDto dto = UnidadeDto.builder().build();
        if (unidade == null) {
            dto.setSubunidades(new java.util.ArrayList<>());
            dto.setElegivel(isElegivel);
            return dto;
        }

        dto.setCodigo(unidade.getCodigo())
                .setNome(unidade.getNome())
                .setSigla(unidade.getSigla())
                .setCodigoPai(unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null)
                .setTipo(unidade.getTipo() != null ? unidade.getTipo().name() : null)
                .setSubunidades(new java.util.ArrayList<>())
                .setElegivel(isElegivel);

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
                .codigo(titulo)
                .nome(usuario.getNome())
                .tituloEleitoral(titulo)
                .email(usuario.getEmail())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : null)
                .build();
    }
}
