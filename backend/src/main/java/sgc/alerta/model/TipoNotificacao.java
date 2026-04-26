package sgc.alerta.model;

import lombok.*;

/**
 * Tipos de notificações de e-mail enviadas pelo sistema.
 */
@Getter
@RequiredArgsConstructor
public enum TipoNotificacao {
    PROCESSO_INICIADO("Início de processo"),
    PROCESSO_FINALIZADO("Finalização de processo"),
    DATA_LIMITE_ALTERADA("Alteração de data limite"),
    LEMBRETE_PRAZO("Lembrete de prazo"),
    ATRIBUICAO_TEMPORARIA("Atribuição temporária de responsável"),
    
    // Transições de Subprocesso (sincronizado com TipoTransicao)
    CADASTRO_DISPONIBILIZADO("Cadastro disponibilizado"),
    CADASTRO_DEVOLVIDO("Cadastro devolvido"),
    CADASTRO_ACEITO("Cadastro aceito"),
    CADASTRO_HOMOLOGADO("Cadastro homologado"),
    CADASTRO_REABERTO("Cadastro reaberto"),
    
    REVISAO_CADASTRO_DISPONIBILIZADA("Revisão de cadastro disponibilizada"),
    REVISAO_CADASTRO_DEVOLVIDA("Revisão de cadastro devolvida"),
    REVISAO_CADASTRO_ACEITA("Revisão de cadastro aceita"),
    REVISAO_CADASTRO_HOMOLOGADA("Revisão de cadastro homologada"),
    REVISAO_CADASTRO_REABERTA("Revisão de cadastro reaberta"),
    
    MAPA_DISPONIBILIZADO("Mapa disponibilizado"),
    MAPA_SUGESTOES_APRESENTADAS("Sugestões ao mapa apresentadas"),
    MAPA_VALIDADO("Mapa validado"),
    MAPA_VALIDACAO_DEVOLVIDA("Validação de mapa devolvida"),
    MAPA_VALIDACAO_ACEITA("Validação de mapa aceita"),
    MAPA_HOMOLOGADO("Mapa homologado");

    private final String descricao;
}
