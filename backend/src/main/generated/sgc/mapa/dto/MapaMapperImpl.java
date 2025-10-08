package sgc.mapa.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:14-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class MapaMapperImpl implements MapaMapper {

    @Override
    public MapaDto toDTO(Mapa mapa) {
        if ( mapa == null ) {
            return null;
        }

        MapaDto mapaDto = new MapaDto();

        mapaDto.setCodigo( mapa.getCodigo() );
        mapaDto.setDataHoraDisponibilizado( mapa.getDataHoraDisponibilizado() );
        mapaDto.setObservacoesDisponibilizacao( mapa.getObservacoesDisponibilizacao() );
        mapaDto.setSugestoesApresentadas( mapa.getSugestoesApresentadas() );
        mapaDto.setDataHoraHomologado( mapa.getDataHoraHomologado() );
        mapaDto.setSugestoes( mapa.getSugestoes() );

        return mapaDto;
    }

    @Override
    public Mapa toEntity(MapaDto mapaDto) {
        if ( mapaDto == null ) {
            return null;
        }

        Mapa mapa = new Mapa();

        mapa.setCodigo( mapaDto.getCodigo() );
        mapa.setDataHoraDisponibilizado( mapaDto.getDataHoraDisponibilizado() );
        mapa.setObservacoesDisponibilizacao( mapaDto.getObservacoesDisponibilizacao() );
        mapa.setSugestoes( mapaDto.getSugestoes() );
        mapa.setSugestoesApresentadas( mapaDto.getSugestoesApresentadas() );
        mapa.setDataHoraHomologado( mapaDto.getDataHoraHomologado() );

        return mapa;
    }
}
