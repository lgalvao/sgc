package sgc.subprocesso.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.Subprocesso;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class SubprocessoMapperImpl implements SubprocessoMapper {

    @Override
    public SubprocessoDto toDto(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        SubprocessoDto.SubprocessoDtoBuilder subprocessoDto = SubprocessoDto.builder();

        subprocessoDto.codProcesso( subprocessoProcessoCodigo( subprocesso ) );
        subprocessoDto.codUnidade( subprocessoUnidadeCodigo( subprocesso ) );
        subprocessoDto.codMapa( subprocessoMapaCodigo( subprocesso ) );
        subprocessoDto.codigo( subprocesso.getCodigo() );
        subprocessoDto.dataLimiteEtapa1( subprocesso.getDataLimiteEtapa1() );
        subprocessoDto.dataFimEtapa1( subprocesso.getDataFimEtapa1() );
        subprocessoDto.dataLimiteEtapa2( subprocesso.getDataLimiteEtapa2() );
        subprocessoDto.dataFimEtapa2( subprocesso.getDataFimEtapa2() );
        subprocessoDto.situacao( subprocesso.getSituacao() );

        return subprocessoDto.build();
    }

    private Long subprocessoProcessoCodigo(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getCodigo();
    }

    private Long subprocessoUnidadeCodigo(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        return unidade.getCodigo();
    }

    private Long subprocessoMapaCodigo(Subprocesso subprocesso) {
        Mapa mapa = subprocesso.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }
}
