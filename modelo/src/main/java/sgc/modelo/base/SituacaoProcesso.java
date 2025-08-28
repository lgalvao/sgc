package sgc.modelo.base;

import lombok.Getter;

@Getter
public enum SituacaoProcesso {
    CRIADO("Criado"),
    EM_ANDAMENTO("Em andamento"),
    FINALIZADO("Finalizado");

    final String descricao;

    SituacaoProcesso(String descricao) {
        this.descricao = descricao;
    }
}