package sgc.processo.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.model.*;

import java.io.*;
import java.time.*;


/**
 * Entidade que representa a associação entre um Processo e uma Unidade participante,
 * armazenando um snapshot dos dados da unidade no momento em que o processo foi iniciado.
 */
@Entity
@Table(name = "UNIDADE_PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class UnidadeProcesso implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UnidadeProcessoId id = new UnidadeProcessoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("processoCodigo")
    @JoinColumn(name = "processo_codigo")
    @JsonIgnore
    private Processo processo;
    @Column(name = "nome")
    private String nome;
    @Column(name = "sigla", length = 20)
    private String sigla;
    @Column(name = "matricula_titular", length = 8)
    private String matriculaTitular;
    @Column(name = "titulo_titular", length = 12)
    private String tituloTitular;
    @Column(name = "data_inicio_titularidade")
    private LocalDateTime dataInicioTitularidade;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20)
    private TipoUnidade tipo;
    @Column(name = "situacao", length = 20)
    private String situacao;
    @Column(name = "unidade_superior_codigo")
    private @Nullable Long unidadeSuperiorCodigo;

    /**
     * Cria um snapshot de uma unidade para um processo.
     */
    public static UnidadeProcesso criarSnapshot(Processo processo, Unidade unidade) {
        UnidadeProcesso snapshot = new UnidadeProcesso();
        snapshot.setProcesso(processo);
        snapshot.setUnidadeCodigo(unidade.getCodigo());
        snapshot.setNome(unidade.getNome());
        snapshot.setSigla(unidade.getSigla());
        snapshot.setMatriculaTitular(unidade.getMatriculaTitular());
        snapshot.setTituloTitular(unidade.getTituloTitular());
        snapshot.setDataInicioTitularidade(unidade.getDataInicioTitularidade());
        snapshot.setTipo(unidade.getTipo());
        snapshot.setSituacao(unidade.getSituacao().name());
        snapshot.setUnidadeSuperiorCodigo(
                unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
        return snapshot;
    }

    public Long getUnidadeCodigo() {
        return id != null ? id.getUnidadeCodigo() : null;
    }

    public void setUnidadeCodigo(Long unidadeCodigo) {
        if (id == null) {
            id = new UnidadeProcessoId();
        }
        id.setUnidadeCodigo(unidadeCodigo);
    }
}
