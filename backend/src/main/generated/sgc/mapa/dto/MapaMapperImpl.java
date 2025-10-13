package sgc.mapa.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-13T10:53:07-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class MapaMapperImpl implements MapaMapper {

    @Override
    public MapaDto toDTO(Mapa mapa) {
        if ( mapa == null ) {
            return null;
        }

        MapaDto.MapaDtoBuilder mapaDto = MapaDto.builder();

        mapaDto.codigo( mapa.getCodigo() );
        mapaDto.dataHoraDisponibilizado( mapa.getDataHoraDisponibilizado() );
        mapaDto.observacoesDisponibilizacao( mapa.getObservacoesDisponibilizacao() );
        mapaDto.sugestoesApresentadas( mapa.getSugestoesApresentadas() );
        mapaDto.dataHoraHomologado( mapa.getDataHoraHomologado() );
        mapaDto.sugestoes( mapa.getSugestoes() );

        return mapaDto.build();
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
