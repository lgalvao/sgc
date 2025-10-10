package sgc.mapa.dto;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T13:02:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class MapaMapperImpl implements MapaMapper {

    @Override
    public MapaDto toDTO(Mapa mapa) {
        if ( mapa == null ) {
            return null;
        }

        Long codigo = null;
        LocalDateTime dataHoraDisponibilizado = null;
        String observacoesDisponibilizacao = null;
        Boolean sugestoesApresentadas = null;
        LocalDateTime dataHoraHomologado = null;
        String sugestoes = null;

        codigo = mapa.getCodigo();
        dataHoraDisponibilizado = mapa.getDataHoraDisponibilizado();
        observacoesDisponibilizacao = mapa.getObservacoesDisponibilizacao();
        sugestoesApresentadas = mapa.getSugestoesApresentadas();
        dataHoraHomologado = mapa.getDataHoraHomologado();
        sugestoes = mapa.getSugestoes();

        MapaDto mapaDto = new MapaDto( codigo, dataHoraDisponibilizado, observacoesDisponibilizacao, sugestoesApresentadas, dataHoraHomologado, sugestoes );

        return mapaDto;
    }

    @Override
    public Mapa toEntity(MapaDto mapaDto) {
        if ( mapaDto == null ) {
            return null;
        }

        Mapa mapa = new Mapa();

        mapa.setCodigo( mapaDto.codigo() );
        mapa.setDataHoraDisponibilizado( mapaDto.dataHoraDisponibilizado() );
        mapa.setObservacoesDisponibilizacao( mapaDto.observacoesDisponibilizacao() );
        mapa.setSugestoes( mapaDto.sugestoes() );
        mapa.setSugestoesApresentadas( mapaDto.sugestoesApresentadas() );
        mapa.setDataHoraHomologado( mapaDto.dataHoraHomologado() );

        return mapa;
    }
}
