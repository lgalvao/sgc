package sgc.alerta.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class AlertaMapperImpl extends AlertaMapper {

    @Override
    public AlertaDto toDto(Alerta alerta) {
        if ( alerta == null ) {
            return null;
        }

        AlertaDto.AlertaDtoBuilder alertaDto = AlertaDto.builder();

        alertaDto.codProcesso( alertaProcessoCodigo( alerta ) );
        alertaDto.unidadeOrigem( alertaUnidadeOrigemSigla( alerta ) );
        alertaDto.unidadeDestino( alertaUnidadeDestinoSigla( alerta ) );
        alertaDto.mensagem( alerta.getDescricao() );
        alertaDto.dataHoraFormatada( formatDataHora( alerta.getDataHora() ) );
        alertaDto.processo( alertaProcessoDescricao( alerta ) );
        alertaDto.origem( alertaUnidadeOrigemSigla( alerta ) );
        alertaDto.codigo( alerta.getCodigo() );
        alertaDto.descricao( alerta.getDescricao() );
        alertaDto.dataHora( alerta.getDataHora() );

        return alertaDto.build();
    }

    private Long alertaProcessoCodigo(Alerta alerta) {
        Processo processo = alerta.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getCodigo();
    }

    private String alertaUnidadeOrigemSigla(Alerta alerta) {
        Unidade unidadeOrigem = alerta.getUnidadeOrigem();
        if ( unidadeOrigem == null ) {
            return null;
        }
        return unidadeOrigem.getSigla();
    }

    private String alertaUnidadeDestinoSigla(Alerta alerta) {
        Unidade unidadeDestino = alerta.getUnidadeDestino();
        if ( unidadeDestino == null ) {
            return null;
        }
        return unidadeDestino.getSigla();
    }

    private String alertaProcessoDescricao(Alerta alerta) {
        Processo processo = alerta.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getDescricao();
    }
}
