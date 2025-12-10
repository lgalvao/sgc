package sgc.mapa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapa extends EntidadeBase {
    @Column(name = "data_hora_disponibilizado")
    private LocalDateTime dataHoraDisponibilizado;
    @Column(name = "observacoes_disponibilizacao", length = 1000)
    private String observacoesDisponibilizacao;
    @Column(name = "sugestoes", columnDefinition = "TEXT")
    private String sugestoes;
    @Column(name = "sugestoes_apresentadas")
    private Boolean sugestoesApresentadas = false;
    @Column(name = "data_hora_homologado")
    private LocalDateTime dataHoraHomologado;
    @ManyToOne
    private Unidade unidade;

    public Mapa(Long codigo) {
        super(codigo);
    }
}
