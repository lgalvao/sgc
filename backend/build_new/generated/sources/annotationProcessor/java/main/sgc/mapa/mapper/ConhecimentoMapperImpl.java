package sgc.mapa.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:24-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class ConhecimentoMapperImpl implements ConhecimentoMapper {

    @Override
    public ConhecimentoResponse toResponse(Conhecimento conhecimento) {
        if ( conhecimento == null ) {
            return null;
        }

        ConhecimentoResponse.ConhecimentoResponseBuilder conhecimentoResponse = ConhecimentoResponse.builder();

        conhecimentoResponse.atividadeCodigo( conhecimentoAtividadeCodigo( conhecimento ) );
        conhecimentoResponse.codigo( conhecimento.getCodigo() );
        conhecimentoResponse.descricao( conhecimento.getDescricao() );

        return conhecimentoResponse.build();
    }

    @Override
    public Conhecimento toEntity(CriarConhecimentoRequest request) {
        if ( request == null ) {
            return null;
        }

        Conhecimento.ConhecimentoBuilder<?, ?> conhecimento = Conhecimento.builder();

        conhecimento.descricao( request.descricao() );

        return conhecimento.build();
    }

    @Override
    public Conhecimento toEntity(AtualizarConhecimentoRequest request) {
        if ( request == null ) {
            return null;
        }

        Conhecimento.ConhecimentoBuilder<?, ?> conhecimento = Conhecimento.builder();

        conhecimento.descricao( request.descricao() );

        return conhecimento.build();
    }

    private Long conhecimentoAtividadeCodigo(Conhecimento conhecimento) {
        Atividade atividade = conhecimento.getAtividade();
        if ( atividade == null ) {
            return null;
        }
        return atividade.getCodigo();
    }
}
