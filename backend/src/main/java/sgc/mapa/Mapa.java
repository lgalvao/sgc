package sgc.mapa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.comum.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapa extends BaseEntity {
    @Column(name = "data_hora_disponibilizado")
    private LocalDateTime dataHoraDisponibilizado;

    @Column(name = "observacoes_disponibilizacao", length = 1000)
    private String observacoesDisponibilizacao;

    @Column(name = "sugestoes_apresentadas", length = 1000)
    private String sugestoesApresentadas;

    @Column(name = "data_hora_homologado")
    private LocalDateTime dataHoraHomologado;
}