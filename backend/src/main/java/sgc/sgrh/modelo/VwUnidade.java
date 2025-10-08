package sgc.sgrh.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Entidade JPA para view VW_UNIDADE do SGRH.
 * Representa a estrutura organizacional (unidades).
 * <p>
 * Esta é uma view READ-ONLY do Oracle SGRH.
 */
@Entity
@Table(name = "VW_UNIDADE", schema = "sgc")
@Immutable  // View read-only
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VwUnidade {
    
    @Id
    @Column(name = "CODIGO", nullable = false)
    private Long codigo;
    
    @Column(name = "NOME", nullable = false, length = 200)
    private String nome;
    
    @Column(name = "SIGLA", length = 50)
    private String sigla;
    
    @Column(name = "CODIGO_PAI")
    private Long codigoPai;  // Para hierarquia
    
    @Column(name = "TIPO", length = 50)
    private String tipo;  // OPERACIONAL, INTERMEDIARIA, etc
    
    @Column(name = "ATIVA")
    private Boolean ativa;
}