package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-09T14:38:41-0300",
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
        String situacao = null;
        LocalDate dataLimite = null;
        LocalDateTime dataCriacao = null;
        LocalDateTime dataFinalizacao = null;

        codigo = processo.getCodigo();
        descricao = processo.getDescricao();
        tipo = processo.getTipo();
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
        String situacaoSubprocesso = null;
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

        codigo = subprocesso.getCodigo();

        String descricao = null;
        String situacao = null;
        String tipo = null;
        LocalDate dataLimite = null;
        LocalDateTime dataCriacao = null;
        Long unidadeCodigo = null;
        String unidadeNome = null;

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
        String situacaoSubprocesso = null;
        LocalDate dataLimite = null;

        unidadeCodigo = subprocessoUnidadeCodigo( subprocesso );
        nome = subprocessoUnidadeNome( subprocesso );
        sigla = subprocessoUnidadeSigla( subprocesso );
        unidadeSuperiorCodigo = subprocessoUnidadeUnidadeSuperiorCodigo( subprocesso );
        situacaoSubprocesso = subprocesso.getSituacaoId();
        dataLimite = subprocesso.getDataLimiteEtapa1();

        List<ProcessoDetalheDto.UnidadeParticipanteDTO> filhos = null;

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO( unidadeCodigo, nome, sigla, unidadeSuperiorCodigo, situacaoSubprocesso, dataLimite, filhos );

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
