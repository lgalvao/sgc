package sgc.mapa.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.model.EntidadeBase;

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
    
    @Column(name = "sugestoes", length = 1000)
    private String sugestoes;
    
    @Column(name = "data_hora_homologado")
    private LocalDateTime dataHoraHomologado;
    
    @OneToOne
    @JoinColumn(name = "subprocesso_codigo", nullable = false)
    private sgc.subprocesso.model.Subprocesso subprocesso;
}
