package sesel.sgc.seguranca;

import lombok.Getter;

/**
 * Perfis de acesso no sistema.
 */
@Getter
public enum Perfil {
    SEDOC("SEDOC",
            "Administrador do sistema, responsável por criar, configurar, monitorar processo de mapeamento e criar o Mapa de Competências"),
    GESTOR("GESTOR",
            "Responsável por uma unidade com subordinadas hierárquicas. Pode visualizar e validar mapeamentos das unidade sob sua gestão"),
    CHEFE("CHEFE",
            "Responsável por uma unidade operacional. Pode cadastrar, editar e finalizar atividades e conhecimentos de sua unidade"),
    SERVIDOR("SERVIDOR", "Avaliação e autoavaliação");

    private final String codigo;
    private final String descricao;

    Perfil(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }
}
