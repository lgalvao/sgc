package sgc.mapa.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class AtividadeMapperImpl extends AtividadeMapper {

    @Override
    public AtividadeResponse toResponse(Atividade atividade) {
        if ( atividade == null ) {
            return null;
        }

        AtividadeResponse.AtividadeResponseBuilder atividadeResponse = AtividadeResponse.builder();

        atividadeResponse.mapaCodigo( atividadeMapaCodigo( atividade ) );
        atividadeResponse.codigo( atividade.getCodigo() );
        atividadeResponse.descricao( atividade.getDescricao() );

        return atividadeResponse.build();
    }

    @Override
    public Atividade toEntity(CriarAtividadeRequest request) {
        if ( request == null ) {
            return null;
        }

        Atividade.AtividadeBuilder<?, ?> atividade = Atividade.builder();

        atividade.descricao( request.descricao() );

        return atividade.build();
    }

    @Override
    public Atividade toEntity(AtualizarAtividadeRequest request) {
        if ( request == null ) {
            return null;
        }

        Atividade.AtividadeBuilder<?, ?> atividade = Atividade.builder();

        atividade.descricao( request.descricao() );

        return atividade.build();
    }

    private Long atividadeMapaCodigo(Atividade atividade) {
        Mapa mapa = atividade.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }
}
