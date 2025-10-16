package sgc.sgrh.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Entidade JPA para view VW_RESPONSABILIDADE do SGRH.
 * Representa titulares e substitutos de unidades.
 * <p>
 * Esta é uma view READ-ONLY do Oracle SGRH.
 */
@Entity
@Table(name = "VW_RESPONSABILIDADE", schema = "sgc")
@Immutable  // View read-only
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VwResponsabilidade {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "UNIDADE_CODIGO", nullable = false)
    private Long unidadeCodigo;

    @Column(name = "TITULAR_TITULO", length = 20)
    private String titularTitulo;

    @Column(name = "SUBSTITUTO_TITULO", length = 20)
    private String substitutoTitulo;

    @Column(name = "DATA_INICIO")
    private LocalDateTime dataInicio;

    @Column(name = "DATA_FIM")
    private LocalDateTime dataFim;

    @Column(name = "ATIVA")
    private Boolean ativa;
}