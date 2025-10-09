package sgc.processo.dto;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-09T08:37:38-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ProcessoDetalheMapperInterfaceImpl implements ProcessoDetalheMapperInterface {

    @Override
    public ProcessoDetalheDto toDetailDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        ProcessoDetalheDto processoDetalheDto = new ProcessoDetalheDto();

        processoDetalheDto.setCodigo( processo.getCodigo() );
        processoDetalheDto.setDescricao( processo.getDescricao() );
        processoDetalheDto.setTipo( processo.getTipo() );
        processoDetalheDto.setSituacao( processo.getSituacao() );
        processoDetalheDto.setDataLimite( processo.getDataLimite() );
        processoDetalheDto.setDataCriacao( processo.getDataCriacao() );
        processoDetalheDto.setDataFinalizacao( processo.getDataFinalizacao() );

        return processoDetalheDto;
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO unidadeProcessoToUnidadeParticipanteDTO(UnidadeProcesso unidadeProcesso) {
        if ( unidadeProcesso == null ) {
            return null;
        }

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO();

        unidadeParticipanteDTO.setUnidadeCodigo( unidadeProcesso.getUnidadeCodigo() );
        unidadeParticipanteDTO.setNome( unidadeProcesso.getNome() );
        unidadeParticipanteDTO.setSigla( unidadeProcesso.getSigla() );
        unidadeParticipanteDTO.setUnidadeSuperiorCodigo( unidadeProcesso.getUnidadeSuperiorCodigo() );

        return unidadeParticipanteDTO;
    }

    @Override
    public ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        ProcessoResumoDto processoResumoDto = new ProcessoResumoDto();

        processoResumoDto.setCodigo( subprocesso.getCodigo() );

        return processoResumoDto;
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO();

        unidadeParticipanteDTO.setUnidadeCodigo( subprocessoUnidadeCodigo( subprocesso ) );
        unidadeParticipanteDTO.setNome( subprocessoUnidadeNome( subprocesso ) );
        unidadeParticipanteDTO.setSigla( subprocessoUnidadeSigla( subprocesso ) );
        unidadeParticipanteDTO.setUnidadeSuperiorCodigo( subprocessoUnidadeUnidadeSuperiorCodigo( subprocesso ) );
        unidadeParticipanteDTO.setSituacaoSubprocesso( subprocesso.getSituacaoId() );
        unidadeParticipanteDTO.setDataLimite( subprocesso.getDataLimiteEtapa1() );

        return unidadeParticipanteDTO;
    }

    private Long subprocessoUnidadeCodigo(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        return unidade.getCodigo();
    }

    private String subprocessoUnidadeNome(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        return unidade.getNome();
    }

    private String subprocessoUnidadeSigla(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        return unidade.getSigla();
    }

    private Long subprocessoUnidadeUnidadeSuperiorCodigo(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        Unidade unidadeSuperior = unidade.getUnidadeSuperior();
        if ( unidadeSuperior == null ) {
            return null;
        }
        return unidadeSuperior.getCodigo();
    }
}
