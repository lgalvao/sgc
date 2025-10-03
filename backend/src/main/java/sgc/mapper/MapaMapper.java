package sgc.mapper;

import sgc.dto.MapaDTO;
import sgc.model.Mapa;

/**
 * Mapper entre Mapa e MapaDTO.
 */
public class MapaMapper {

    public static MapaDTO toDTO(Mapa m) {
        if (m == null) return null;
        return new MapaDTO(
                m.getCodigo(),
                m.getDataHoraDisponibilizado(),
                m.getObservacoesDisponibilizacao(),
                m.getSugestoesApresentadas(),
                m.getDataHoraHomologado()
        );
    }

    public static Mapa toEntity(MapaDTO dto) {
        if (dto == null) return null;
        Mapa m = new Mapa();
        m.setCodigo(dto.getCodigo());
        m.setDataHoraDisponibilizado(dto.getDataHoraDisponibilizado());
        m.setObservacoesDisponibilizacao(dto.getObservacoesDisponibilizacao());
        m.setSugestoesApresentadas(dto.getSugestoesApresentadas());
        m.setDataHoraHomologado(dto.getDataHoraHomologado());
        return m;
    }
}