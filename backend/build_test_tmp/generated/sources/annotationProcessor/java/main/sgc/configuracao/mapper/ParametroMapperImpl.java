package sgc.configuracao.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.dto.ParametroResponse;
import sgc.configuracao.model.Parametro;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class ParametroMapperImpl implements ParametroMapper {

    @Override
    public ParametroResponse toResponse(Parametro parametro) {
        if ( parametro == null ) {
            return null;
        }

        ParametroResponse.ParametroResponseBuilder parametroResponse = ParametroResponse.builder();

        parametroResponse.codigo( parametro.getCodigo() );
        parametroResponse.chave( parametro.getChave() );
        parametroResponse.descricao( parametro.getDescricao() );
        parametroResponse.valor( parametro.getValor() );

        return parametroResponse.build();
    }

    @Override
    public void atualizarEntidade(ParametroRequest request, Parametro parametro) {
        if ( request == null ) {
            return;
        }

        parametro.setChave( request.chave() );
        parametro.setDescricao( request.descricao() );
        parametro.setValor( request.valor() );
    }
}
