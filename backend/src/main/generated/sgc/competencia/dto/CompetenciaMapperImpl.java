package sgc.competencia.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-13T10:00:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class CompetenciaMapperImpl implements CompetenciaMapper {

    @Override
    public CompetenciaDto toDTO(Competencia competencia) {
        if ( competencia == null ) {
            return null;
        }

        Long mapaCodigo = null;
        Long codigo = null;
        String descricao = null;

        mapaCodigo = competenciaMapaCodigo( competencia );
        codigo = competencia.getCodigo();
        descricao = competencia.getDescricao();

        CompetenciaDto competenciaDto = new CompetenciaDto( codigo, mapaCodigo, descricao );

        return competenciaDto;
    }

    @Override
    public Competencia toEntity(CompetenciaDto competenciaDTO) {
        if ( competenciaDTO == null ) {
            return null;
        }

        Competencia competencia = new Competencia();

        competencia.setMapa( map( competenciaDTO.mapaCodigo() ) );
        competencia.setCodigo( competenciaDTO.codigo() );
        competencia.setDescricao( competenciaDTO.descricao() );

        return competencia;
    }

    private Long competenciaMapaCodigo(Competencia competencia) {
        Mapa mapa = competencia.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }
}
