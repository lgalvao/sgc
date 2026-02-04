package sgc.mapa.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class MapaMapperImpl implements MapaMapper {

    @Override
    public MapaDto toDto(Mapa mapa) {
        if ( mapa == null ) {
            return null;
        }

        MapaDto.MapaDtoBuilder mapaDto = MapaDto.builder();

        mapaDto.codigo( mapa.getCodigo() );
        mapaDto.dataHoraDisponibilizado( mapa.getDataHoraDisponibilizado() );
        mapaDto.observacoesDisponibilizacao( mapa.getObservacoesDisponibilizacao() );
        mapaDto.dataHoraHomologado( mapa.getDataHoraHomologado() );
        mapaDto.sugestoes( mapa.getSugestoes() );

        return mapaDto.build();
    }

    @Override
    public Mapa toEntity(MapaDto mapaDto) {
        if ( mapaDto == null ) {
            return null;
        }

        Mapa.MapaBuilder<?, ?> mapa = Mapa.builder();

        mapa.codigo( mapaDto.codigo() );
        mapa.dataHoraDisponibilizado( mapaDto.dataHoraDisponibilizado() );
        mapa.observacoesDisponibilizacao( mapaDto.observacoesDisponibilizacao() );
        mapa.sugestoes( mapaDto.sugestoes() );
        mapa.dataHoraHomologado( mapaDto.dataHoraHomologado() );

        return mapa.build();
    }
}
