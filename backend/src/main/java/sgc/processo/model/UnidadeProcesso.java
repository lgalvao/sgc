package sgc.processo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;

import java.time.LocalDateTime;

/**
 * Entidade que representa a associação entre um Processo e uma Unidade participante,
 * armazenando um snapshot dos dados da unidade no momento em que o processo foi iniciado.
 * 
 * <p>Conforme os requisitos CDU-04 e CDU-05, ao iniciar um processo, o sistema deve
 * armazenar uma cópia da árvore de unidades participantes para preservar a representação
 * hierárquica vigente no momento do início do processo.
 */
@Entity
@Table(name = "UNIDADE_PROCESSO", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
public class UnidadeProcesso {
    
    @EmbeddedId
    private UnidadeProcessoId id = new UnidadeProcessoId();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("processoCodigo")
    @JoinColumn(name = "processo_codigo")
    private Processo processo;
    
    /**
     * Getter de compatibilidade que retorna o código do processo.
     */
    public Long getProcessoCodigo() {
        return id != null ? id.getProcessoCodigo() : null;
    }

    /**
     * Getter de conveniência para unidadeCodigo.
     */
    public Long getUnidadeCodigo() {
        return id != null ? id.getUnidadeCodigo() : null;
    }

    /**
     * Setter de conveniência para unidadeCodigo.
     */
    public void setUnidadeCodigo(Long unidadeCodigo) {
        if (id == null) {
            id = new UnidadeProcessoId();
        }
        id.setUnidadeCodigo(unidadeCodigo);
    }
    
    // ========== Colunas de Snapshot ==========
    
    @Column(name = "nome", length = 255)
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
    
    // ========== Factory Method ==========
    
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
        snapshot.setSituacao(unidade.getSituacao() != null ? unidade.getSituacao().name() : null);
        snapshot.setUnidadeSuperiorCodigo(
            unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
        return snapshot;
    }
}
