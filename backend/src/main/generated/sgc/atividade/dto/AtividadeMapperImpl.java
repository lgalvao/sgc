package sgc.atividade.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-13T10:00:59-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class AtividadeMapperImpl implements AtividadeMapper {

    @Override
    public AtividadeDto toDTO(Atividade atividade) {
        if ( atividade == null ) {
            return null;
        }

        Long mapaCodigo = null;
        Long codigo = null;
        String descricao = null;

        mapaCodigo = atividadeMapaCodigo( atividade );
        codigo = atividade.getCodigo();
        descricao = atividade.getDescricao();

        AtividadeDto atividadeDto = new AtividadeDto( codigo, mapaCodigo, descricao );

        return atividadeDto;
    }

    @Override
    public Atividade toEntity(AtividadeDto atividadeDTO) {
        if ( atividadeDTO == null ) {
            return null;
        }

        Atividade atividade = new Atividade();

        atividade.setMapa( map( atividadeDTO.mapaCodigo() ) );
        atividade.setCodigo( atividadeDTO.codigo() );
        atividade.setDescricao( atividadeDTO.descricao() );

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
