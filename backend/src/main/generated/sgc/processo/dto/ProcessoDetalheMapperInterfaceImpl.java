package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.processo.enums.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-10T13:02:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25 (Amazon.com Inc.)"
)
@Component
public class ProcessoDetalheMapperInterfaceImpl implements ProcessoDetalheMapperInterface {

    @Override
    public ProcessoDetalheDto toDetailDTO(Processo processo) {
        if ( processo == null ) {
            return null;
        }

        Long codigo = null;
        String descricao = null;
        String tipo = null;
        SituacaoProcesso situacao = null;
        LocalDate dataLimite = null;
        LocalDateTime dataCriacao = null;
        LocalDateTime dataFinalizacao = null;

        codigo = processo.getCodigo();
        descricao = processo.getDescricao();
        if ( processo.getTipo() != null ) {
            tipo = processo.getTipo().name();
        }
        situacao = processo.getSituacao();
        dataLimite = processo.getDataLimite();
        dataCriacao = processo.getDataCriacao();
        dataFinalizacao = processo.getDataFinalizacao();

        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = null;
        List<ProcessoResumoDto> resumoSubprocessos = null;

        ProcessoDetalheDto processoDetalheDto = new ProcessoDetalheDto( codigo, descricao, tipo, situacao, dataLimite, dataCriacao, dataFinalizacao, unidades, resumoSubprocessos );

        return processoDetalheDto;
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO unidadeProcessoToUnidadeParticipanteDTO(UnidadeProcesso unidadeProcesso) {
        if ( unidadeProcesso == null ) {
            return null;
        }

        Long unidadeCodigo = null;
        String nome = null;
        String sigla = null;
        Long unidadeSuperiorCodigo = null;

        unidadeCodigo = unidadeProcesso.getUnidadeCodigo();
        nome = unidadeProcesso.getNome();
        sigla = unidadeProcesso.getSigla();
        unidadeSuperiorCodigo = unidadeProcesso.getUnidadeSuperiorCodigo();

        List<ProcessoDetalheDto.UnidadeParticipanteDTO> filhos = null;
        SituacaoSubprocesso situacaoSubprocesso = null;
        LocalDate dataLimite = null;

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO( unidadeCodigo, nome, sigla, unidadeSuperiorCodigo, situacaoSubprocesso, dataLimite, filhos );

        return unidadeParticipanteDTO;
    }

    @Override
    public ProcessoResumoDto subprocessoToProcessoResumoDto(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        Long codigo = null;
        String descricao = null;
        SituacaoProcesso situacao = null;
        String tipo = null;
        LocalDate dataLimite = null;
        LocalDateTime dataCriacao = null;
        Long unidadeCodigo = null;
        String unidadeNome = null;

        codigo = subprocessoProcessoCodigo( subprocesso );
        descricao = subprocessoProcessoDescricao( subprocesso );
        situacao = subprocessoProcessoSituacao( subprocesso );
        TipoProcesso tipo1 = subprocessoProcessoTipo( subprocesso );
        if ( tipo1 != null ) {
            tipo = tipo1.name();
        }
        dataLimite = subprocessoProcessoDataLimite( subprocesso );
        dataCriacao = subprocessoProcessoDataCriacao( subprocesso );
        unidadeCodigo = subprocessoUnidadeCodigo( subprocesso );
        unidadeNome = subprocessoUnidadeNome( subprocesso );

        ProcessoResumoDto processoResumoDto = new ProcessoResumoDto( codigo, descricao, situacao, tipo, dataLimite, dataCriacao, unidadeCodigo, unidadeNome );

        return processoResumoDto;
    }

    @Override
    public ProcessoDetalheDto.UnidadeParticipanteDTO subprocessoToUnidadeParticipanteDTO(Subprocesso subprocesso) {
        if ( subprocesso == null ) {
            return null;
        }

        Long unidadeCodigo = null;
        String nome = null;
        String sigla = null;
        Long unidadeSuperiorCodigo = null;
        SituacaoSubprocesso situacaoSubprocesso = null;
        LocalDate dataLimite = null;

        unidadeCodigo = subprocessoUnidadeCodigo( subprocesso );
        nome = subprocessoUnidadeNome( subprocesso );
        sigla = subprocessoUnidadeSigla( subprocesso );
        unidadeSuperiorCodigo = subprocessoUnidadeUnidadeSuperiorCodigo( subprocesso );
        situacaoSubprocesso = subprocesso.getSituacao();
        dataLimite = subprocesso.getDataLimiteEtapa1();

        List<ProcessoDetalheDto.UnidadeParticipanteDTO> filhos = null;

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO( unidadeCodigo, nome, sigla, unidadeSuperiorCodigo, situacaoSubprocesso, dataLimite, filhos );

        return unidadeParticipanteDTO;
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
