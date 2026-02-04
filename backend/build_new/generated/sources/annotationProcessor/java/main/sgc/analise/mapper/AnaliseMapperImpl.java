package sgc.analise.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.model.Analise;
import sgc.comum.util.FormatadorData;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:25-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class AnaliseMapperImpl extends AnaliseMapper {

    @Override
    public AnaliseHistoricoDto toAnaliseHistoricoDto(Analise analise) {
        if ( analise == null ) {
            return null;
        }

        AnaliseHistoricoDto.AnaliseHistoricoDtoBuilder analiseHistoricoDto = AnaliseHistoricoDto.builder();

        analiseHistoricoDto.analistaUsuarioTitulo( analise.getUsuarioTitulo() );
        analiseHistoricoDto.dataHora( analise.getDataHora() );
        analiseHistoricoDto.observacoes( analise.getObservacoes() );
        analiseHistoricoDto.acao( analise.getAcao() );
        analiseHistoricoDto.motivo( analise.getMotivo() );
        analiseHistoricoDto.tipo( analise.getTipo() );

        analiseHistoricoDto.unidadeSigla( getUnidadeSigla(analise.getUnidadeCodigo()) );
        analiseHistoricoDto.dataHoraFormatada( FormatadorData.formatarDataHora(analise.getDataHora()) );

        return analiseHistoricoDto.build();
    }

    @Override
    public AnaliseValidacaoHistoricoDto toAnaliseValidacaoHistoricoDto(Analise analise) {
        if ( analise == null ) {
            return null;
        }

        AnaliseValidacaoHistoricoDto.AnaliseValidacaoHistoricoDtoBuilder analiseValidacaoHistoricoDto = AnaliseValidacaoHistoricoDto.builder();

        analiseValidacaoHistoricoDto.analistaUsuarioTitulo( analise.getUsuarioTitulo() );
        analiseValidacaoHistoricoDto.dataHora( analise.getDataHora() );
        analiseValidacaoHistoricoDto.observacoes( analise.getObservacoes() );
        analiseValidacaoHistoricoDto.acao( analise.getAcao() );
        analiseValidacaoHistoricoDto.motivo( analise.getMotivo() );
        analiseValidacaoHistoricoDto.tipo( analise.getTipo() );

        analiseValidacaoHistoricoDto.unidadeSigla( getUnidadeSigla(analise.getUnidadeCodigo()) );

        return analiseValidacaoHistoricoDto.build();
    }
}
