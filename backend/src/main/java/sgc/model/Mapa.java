package sgc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "MAPA", schema = "sgc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mapa implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Long codigo;

    @Column(name = "data_hora_disponibilizado")
    private LocalDateTime dataHoraDisponibilizado;

    @Column(name = "observacoes_disponibilizacao", length = 1000)
    private String observacoesDisponibilizacao;

    @Column(name = "sugestoes_apresentadas", length = 1000)
    private String sugestoesApresentadas;

    @Column(name = "data_hora_homologado")
    private LocalDateTime dataHoraHomologado;
}