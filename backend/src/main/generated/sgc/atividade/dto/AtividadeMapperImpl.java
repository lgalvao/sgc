package sgc.atividade.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-09T08:37:38-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class AtividadeMapperImpl implements AtividadeMapper {

    @Override
    public AtividadeDto toDTO(Atividade atividade) {
        if ( atividade == null ) {
            return null;
        }

        AtividadeDto atividadeDto = new AtividadeDto();

        atividadeDto.setMapaCodigo( atividadeMapaCodigo( atividade ) );
        atividadeDto.setCodigo( atividade.getCodigo() );
        atividadeDto.setDescricao( atividade.getDescricao() );

        return atividadeDto;
    }

    @Override
    public Atividade toEntity(AtividadeDto atividadeDTO) {
        if ( atividadeDTO == null ) {
            return null;
        }

        Atividade atividade = new Atividade();

        atividade.setMapa( map( atividadeDTO.getMapaCodigo() ) );
        atividade.setCodigo( atividadeDTO.getCodigo() );
        atividade.setDescricao( atividadeDTO.getDescricao() );

        return atividade;
    }

    private Long atividadeMapaCodigo(Atividade atividade) {
        Mapa mapa = atividade.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }
}
