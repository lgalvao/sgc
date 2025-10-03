package sgc.mapper;

import sgc.dto.CompetenciaDTO;
import sgc.model.Competencia;
import sgc.model.Mapa;

/**
 * Mapper entre Competencia e CompetenciaDTO.
 */
public class CompetenciaMapper {

    public static CompetenciaDTO toDTO(Competencia c) {
        if (c == null) return null;
        Long mapaCodigo = c.getMapa() != null ? c.getMapa().getCodigo() : null;
        return new CompetenciaDTO(c.getCodigo(), mapaCodigo, c.getDescricao());
    }

    public static Competencia toEntity(CompetenciaDTO dto) {
        if (dto == null) return null;
        Competencia c = new Competencia();
        c.setCodigo(dto.getCodigo());
        if (dto.getMapaCodigo() != null) {
            Mapa m = new Mapa();
            m.setCodigo(dto.getMapaCodigo());
            c.setMapa(m);
        } else {
            c.setMapa(null);
        }
        c.setDescricao(dto.getDescricao());
        return c;
    }
}