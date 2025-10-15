package sgc.mapa.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.modelo.EntidadeBase;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDateTime;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapa extends EntidadeBase {

    public Mapa(Mapa mapa) {
        super(mapa.getCodigo());
        this.dataHoraDisponibilizado = mapa.getDataHoraDisponibilizado();
        this.observacoesDisponibilizacao = mapa.getObservacoesDisponibilizacao();
        this.sugestoes = mapa.getSugestoes();
        this.sugestoesApresentadas = mapa.getSugestoesApresentadas();
        this.dataHoraHomologado = mapa.getDataHoraHomologado();
        this.unidade = mapa.getUnidade();
    }
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
}