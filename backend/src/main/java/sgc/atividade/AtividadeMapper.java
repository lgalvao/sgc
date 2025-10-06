package sgc.atividade;

import sgc.mapa.Mapa;

/**
 * Mapper entre Atividade e AtividadeDTO.
 */
public class AtividadeMapper {
    public static AtividadeDTO toDTO(Atividade a) {
        if (a == null) return null;
        Long mapaCodigo = a.getMapa() != null ? a.getMapa().getCodigo() : null;
        return new AtividadeDTO(a.getCodigo(), mapaCodigo, a.getDescricao());
    }

    public static Atividade toEntity(AtividadeDTO dto) {
        if (dto == null) return null;
        Atividade a = new Atividade();
        a.setCodigo(dto.getCodigo());
        if (dto.getMapaCodigo() != null) {
            Mapa m = new Mapa();
            m.setCodigo(dto.getMapaCodigo());
            a.setMapa(m);
        } else {
            a.setMapa(null);
        }
        a.setDescricao(dto.getDescricao());
        return a;
    }
}