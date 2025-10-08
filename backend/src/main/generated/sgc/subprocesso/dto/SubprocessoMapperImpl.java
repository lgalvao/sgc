package sgc.subprocesso.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T14:30:13-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class SubprocessoMapperImpl implements SubprocessoMapper {

    @Override
    public SubprocessoDto toDTO(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        SubprocessoDto subprocessoDto = new SubprocessoDto();

        subprocessoDto.setProcessoCodigo( subprocessoProcessoCodigo( subprocesso ) );
        subprocessoDto.setUnidadeCodigo( subprocessoUnidadeCodigo( subprocesso ) );
        subprocessoDto.setMapaCodigo( subprocessoMapaCodigo( subprocesso ) );
        subprocessoDto.setCodigo( subprocesso.getCodigo() );
        subprocessoDto.setDataLimiteEtapa1( subprocesso.getDataLimiteEtapa1() );
        subprocessoDto.setDataFimEtapa1( subprocesso.getDataFimEtapa1() );
        subprocessoDto.setDataLimiteEtapa2( subprocesso.getDataLimiteEtapa2() );
        subprocessoDto.setDataFimEtapa2( subprocesso.getDataFimEtapa2() );
        subprocessoDto.setSituacaoId( subprocesso.getSituacaoId() );

        return subprocessoDto;
    }

    @Override
    public Subprocesso toEntity(SubprocessoDto dto) {
        if ( dto == null ) {
            return null;
        }

        Subprocesso subprocesso = new Subprocesso();

        subprocesso.setCodigo( dto.getCodigo() );
        subprocesso.setDataLimiteEtapa1( dto.getDataLimiteEtapa1() );
        subprocesso.setDataFimEtapa1( dto.getDataFimEtapa1() );
        subprocesso.setDataLimiteEtapa2( dto.getDataLimiteEtapa2() );
        subprocesso.setDataFimEtapa2( dto.getDataFimEtapa2() );
        subprocesso.setSituacaoId( dto.getSituacaoId() );

        subprocesso.setProcesso( dto.getProcessoCodigo() != null ? new sgc.processo.modelo.Processo() {{ setCodigo(dto.getProcessoCodigo()); }} : null );
        subprocesso.setUnidade( dto.getUnidadeCodigo() != null ? new sgc.unidade.modelo.Unidade() {{ setCodigo(dto.getUnidadeCodigo()); }} : null );
        subprocesso.setMapa( dto.getMapaCodigo() != null ? new sgc.mapa.modelo.Mapa() {{ setCodigo(dto.getMapaCodigo()); }} : null );

        return subprocesso;
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
