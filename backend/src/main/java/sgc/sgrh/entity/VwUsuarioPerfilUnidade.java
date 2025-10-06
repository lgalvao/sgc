package sgc.sgrh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * Entidade JPA para view VW_USUARIO_PERFIL_UNIDADE do SGRH.
 * Representa perfis dos usuários por unidade.
 * <p>
 * Esta é uma view READ-ONLY do Oracle SGRH.
 */
@Entity
@Table(name = "VW_USUARIO_PERFIL_UNIDADE", schema = "sgc")
@Immutable  // View read-only
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VwUsuarioPerfilUnidade {
    
    @EmbeddedId
    private VwUsuarioPerfilUnidadeId id;
    
    @Column(name = "PERFIL", nullable = false, length = 50)
    private String perfil;  // ADMIN, GESTOR, CHEFE, SERVIDOR
    
    @Column(name = "ATIVO")
    private Boolean ativo;
    
    /**
     * Classe interna para chave composta
     */
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class VwUsuarioPerfilUnidadeId implements Serializable {
        
        @Column(name = "USUARIO_TITULO", nullable = false, length = 20)
        private String usuarioTitulo;
        
        @Column(name = "UNIDADE_CODIGO", nullable = false)
        private Long unidadeCodigo;
    }
}