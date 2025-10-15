package sgc.atividade.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.mapa.modelo.Mapa;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ATIVIDADE", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Atividade extends EntidadeBase {
    @ManyToOne
    @JoinColumn(name = "mapa_codigo")
    private Mapa mapa;

    @Column(name = "descricao")
    private String descricao;

    @OneToMany(mappedBy = "atividade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Conhecimento> conhecimentos = new ArrayList<>();

    public Atividade(Mapa mapa, String descricao) {
        this.mapa = mapa;
        this.descricao = descricao;
    }

    public Atividade(Atividade atividade) {
        if (atividade != null) {
            super.setCodigo(atividade.getCodigo());
            this.mapa = atividade.getMapa();
            this.descricao = atividade.getDescricao();
            this.conhecimentos = new ArrayList<>(atividade.getConhecimentos());
        }
    }

    public void setConhecimentos(List<Conhecimento> conhecimentos) {
        this.conhecimentos = new ArrayList<>(conhecimentos);
    }

    public List<Conhecimento> getConhecimentos() {
        return new ArrayList<>(this.conhecimentos);
    }
}
