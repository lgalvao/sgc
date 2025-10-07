package sgc.atividade;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class AtividadeMapperImpl implements AtividadeMapper {

    @Override
    public AtividadeDTO toDTO(Atividade atividade) {
        if ( atividade == null ) {
            return null;
        }

        AtividadeDTO atividadeDTO = new AtividadeDTO();

        atividadeDTO.setMapaCodigo( atividadeMapaCodigo( atividade ) );
        atividadeDTO.setCodigo( atividade.getCodigo() );
        atividadeDTO.setDescricao( atividade.getDescricao() );

        return atividadeDTO;
    }

    @Override
    public Atividade toEntity(AtividadeDTO atividadeDTO) {
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
        if ( atividade == null ) {
            return null;
        }
        Mapa mapa = atividade.getMapa();
        if ( mapa == null ) {
            return null;
        }
        Long codigo = mapa.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }
}
