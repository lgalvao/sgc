package sgc.processo.dto;

import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ProcessoDetalheDto(
    Long codigo,
    String descricao,
    String tipo,
    SituacaoProcesso situacao,
    LocalDate dataLimite,
    LocalDateTime dataCriacao,
    LocalDateTime dataFinalizacao,
    List<UnidadeParticipanteDTO> unidades,
    List<ProcessoResumoDto> resumoSubprocessos
) {
    public ProcessoDetalheDto(
        Long codigo,
        String descricao,
        String tipo,
        SituacaoProcesso situacao,
        LocalDate dataLimite,
        LocalDateTime dataCriacao,
        LocalDateTime dataFinalizacao,
        List<UnidadeParticipanteDTO> unidades,
        List<ProcessoResumoDto> resumoSubprocessos
    ) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.tipo = tipo;
        this.situacao = situacao;
        this.dataLimite = dataLimite;
        this.dataCriacao = dataCriacao;
        this.dataFinalizacao = dataFinalizacao;
        this.unidades = unidades != null ? unidades : new ArrayList<>();
        this.resumoSubprocessos = resumoSubprocessos != null ? resumoSubprocessos : new ArrayList<>();
    }

    public record UnidadeParticipanteDTO(
        Long unidadeCodigo,
        String nome,
        String sigla,
        Long unidadeSuperiorCodigo,
        SituacaoSubprocesso situacaoSubprocesso,
        LocalDate dataLimite,
        List<UnidadeParticipanteDTO> filhos
    ) {
        public UnidadeParticipanteDTO(
            Long unidadeCodigo,
            String nome,
            String sigla,
            Long unidadeSuperiorCodigo,
            SituacaoSubprocesso situacaoSubprocesso,
            LocalDate dataLimite,
            List<UnidadeParticipanteDTO> filhos
        ) {
            this.unidadeCodigo = unidadeCodigo;
            this.nome = nome;
            this.sigla = sigla;
            this.unidadeSuperiorCodigo = unidadeSuperiorCodigo;
            this.situacaoSubprocesso = situacaoSubprocesso;
            this.dataLimite = dataLimite;
            this.filhos = filhos != null ? filhos : new ArrayList<>();
        }
    }
}
