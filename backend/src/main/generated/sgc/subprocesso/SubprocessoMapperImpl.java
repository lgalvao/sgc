package sgc.subprocesso;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.mapa.Mapa;
import sgc.processo.Processo;
import sgc.unidade.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-07T13:39:14-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class SubprocessoMapperImpl implements SubprocessoMapper {

    @Override
    public SubprocessoDTO toDTO(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        SubprocessoDTO subprocessoDTO = new SubprocessoDTO();

        subprocessoDTO.setProcessoCodigo( subprocessoProcessoCodigo( subprocesso ) );
        subprocessoDTO.setUnidadeCodigo( subprocessoUnidadeCodigo( subprocesso ) );
        subprocessoDTO.setMapaCodigo( subprocessoMapaCodigo( subprocesso ) );
        subprocessoDTO.setCodigo( subprocesso.getCodigo() );
        subprocessoDTO.setDataLimiteEtapa1( subprocesso.getDataLimiteEtapa1() );
        subprocessoDTO.setDataFimEtapa1( subprocesso.getDataFimEtapa1() );
        subprocessoDTO.setDataLimiteEtapa2( subprocesso.getDataLimiteEtapa2() );
        subprocessoDTO.setDataFimEtapa2( subprocesso.getDataFimEtapa2() );
        subprocessoDTO.setSituacaoId( subprocesso.getSituacaoId() );

        return subprocessoDTO;
    }

    @Override
    public Subprocesso toEntity(SubprocessoDTO dto) {
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

        subprocesso.setProcesso( dto.getProcessoCodigo() != null ? new sgc.processo.Processo() {{ setCodigo(dto.getProcessoCodigo()); }} : null );
        subprocesso.setUnidade( dto.getUnidadeCodigo() != null ? new sgc.unidade.Unidade() {{ setCodigo(dto.getUnidadeCodigo()); }} : null );
        subprocesso.setMapa( dto.getMapaCodigo() != null ? new sgc.mapa.Mapa() {{ setCodigo(dto.getMapaCodigo()); }} : null );

        return subprocesso;
    }

    private Long subprocessoProcessoCodigo(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        Long codigo = processo.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }

    private Long subprocessoUnidadeCodigo(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        Long codigo = unidade.getCodigo();
        if ( codigo == null ) {
            return null;
        }
        return codigo;
    }

    private Long subprocessoMapaCodigo(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }
        Mapa mapa = subprocesso.getMapa();
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
