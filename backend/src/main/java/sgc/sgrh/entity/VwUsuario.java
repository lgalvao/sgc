package sgc.sgrh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Entidade JPA para view VW_USUARIO do SGRH.
 * Representa os dados dos servidores (usuários).
 * <p>
 * Esta é uma view READ-ONLY do Oracle SGRH.
 */
@Entity
@Table(name = "VW_USUARIO", schema = "sgc")
@Immutable  // View read-only
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VwUsuario {
    
    @Id
    @Column(name = "TITULO", nullable = false, length = 20)
    private String titulo;  // CPF/título do servidor
    
    @Column(name = "NOME", nullable = false, length = 200)
    private String nome;
    
    @Column(name = "EMAIL", length = 200)
    private String email;
    
    @Column(name = "MATRICULA", length = 20)
    private String matricula;
    
    @Column(name = "CARGO", length = 200)
    private String cargo;
    
    @Column(name = "ATIVO")
    private Boolean ativo;
}