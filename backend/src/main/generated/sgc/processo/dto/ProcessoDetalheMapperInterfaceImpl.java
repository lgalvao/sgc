package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-13T10:53:07-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class ProcessoDetalheMapperInterfaceImpl implements ProcessoDetalheMapperInterface {

    @Override
    public ProcessoDetalheDto toDetailDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        ProcessoDetalheDto.ProcessoDetalheDtoBuilder processoDetalheDto = ProcessoDetalheDto.builder();

        processoDetalheDto.codigo( processo.getCodigo() );
        processoDetalheDto.descricao( processo.getDescricao() );
        if ( processo.getTipo() != null ) {
            processoDetalheDto.tipo( processo.getTipo().name() );
        }
        processoDetalheDto.situacao( processo.getSituacao() );
        processoDetalheDto.dataLimite( processo.getDataLimite() );
        processoDetalheDto.dataCriacao( processo.getDataCriacao() );
        processoDetalheDto.dataFinalizacao( processo.getDataFinalizacao() );

        return processoDetalheDto.build();
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO unidadeProcessoToUnidadeParticipanteDTO(UnidadeProcesso unidadeProcesso) {
        if ( unidadeProcesso == null ) {
            return null;
        }

        ProcessoDetalheDto.UnidadeParticipanteDTO.UnidadeParticipanteDTOBuilder unidadeParticipanteDTO = ProcessoDetalheDto.UnidadeParticipanteDTO.builder();

        unidadeParticipanteDTO.unidadeCodigo( unidadeProcesso.getUnidadeCodigo() );
        unidadeParticipanteDTO.nome( unidadeProcesso.getNome() );
        unidadeParticipanteDTO.sigla( unidadeProcesso.getSigla() );
        unidadeParticipanteDTO.unidadeSuperiorCodigo( unidadeProcesso.getUnidadeSuperiorCodigo() );

        return unidadeParticipanteDTO.build();
    }

    @Override
    public ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        ProcessoResumoDto.ProcessoResumoDtoBuilder processoResumoDto = ProcessoResumoDto.builder();

        processoResumoDto.codigo( subprocessoProcessoCodigo( subprocesso ) );
        processoResumoDto.descricao( subprocessoProcessoDescricao( subprocesso ) );
        processoResumoDto.situacao( subprocessoProcessoSituacao( subprocesso ) );
        TipoProcesso tipo = subprocessoProcessoTipo( subprocesso );
        if ( tipo != null ) {
            processoResumoDto.tipo( tipo.name() );
        }
        processoResumoDto.dataLimite( subprocessoProcessoDataLimite( subprocesso ) );
        processoResumoDto.dataCriacao( subprocessoProcessoDataCriacao( subprocesso ) );
        processoResumoDto.unidadeCodigo( subprocessoUnidadeCodigo( subprocesso ) );
        processoResumoDto.unidadeNome( subprocessoUnidadeNome( subprocesso ) );

        return processoResumoDto.build();
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        ProcessoDetalheDto.UnidadeParticipanteDTO.UnidadeParticipanteDTOBuilder unidadeParticipanteDTO = ProcessoDetalheDto.UnidadeParticipanteDTO.builder();

        unidadeParticipanteDTO.unidadeCodigo( subprocessoUnidadeCodigo( subprocesso ) );
        unidadeParticipanteDTO.nome( subprocessoUnidadeNome( subprocesso ) );
        unidadeParticipanteDTO.sigla( subprocessoUnidadeSigla( subprocesso ) );
        unidadeParticipanteDTO.unidadeSuperiorCodigo( subprocessoUnidadeUnidadeSuperiorCodigo( subprocesso ) );
        unidadeParticipanteDTO.situacaoSubprocesso( subprocesso.getSituacao() );
        unidadeParticipanteDTO.dataLimite( subprocesso.getDataLimiteEtapa1() );

        return unidadeParticipanteDTO.build();
    }

    private Long subprocessoProcessoCodigo(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getCodigo();
    }

    private String subprocessoProcessoDescricao(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getDescricao();
    }

    private SituacaoProcesso subprocessoProcessoSituacao(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getSituacao();
    }

    private TipoProcesso subprocessoProcessoTipo(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getTipo();
    }

    private LocalDate subprocessoProcessoDataLimite(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getDataLimite();
    }

    private LocalDateTime subprocessoProcessoDataCriacao(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        if ( processo == null ) {
            return null;
        }
        return processo.getDataCriacao();
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
