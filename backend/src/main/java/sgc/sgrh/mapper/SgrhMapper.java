package sgc.sgrh.mapper;

import org.mapstruct.Mapper;
import sgc.sgrh.dto.ServidorDto;
import sgc.sgrh.dto.UnidadeDto;
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

    default ServidorDto toServidorDto(Usuario usuario) {
        ServidorDto dto = new ServidorDto();
        if (usuario == null) return dto;

        Object tituloObj = usuario.getTituloEleitoral();
        String titulo = tituloObj != null ? tituloObj.toString() : null;
        dto.setCodigo(titulo);

        dto.setNome(usuario.getNome());
        dto.setTituloEleitoral(titulo);
        dto.setEmail(usuario.getEmail());
        dto.setUnidadeCodigo(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : null);

        return dto;
    }
}
