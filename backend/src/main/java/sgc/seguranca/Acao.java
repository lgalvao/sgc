package sgc.seguranca;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeração de todas as ações possíveis no sistema SGC.
 * Cada ação representa uma operação que pode ser controlada por permissões.
 */
@Getter
@RequiredArgsConstructor
public enum Acao {
    // ========== PROCESSO ==========
    CRIAR_PROCESSO("Criar processo"),
    VISUALIZAR_PROCESSO("Visualizar processo"),
    EDITAR_PROCESSO("Editar processo"),
    EXCLUIR_PROCESSO("Excluir processo"),
    INICIAR_PROCESSO("Iniciar processo"),
    FINALIZAR_PROCESSO("Finalizar processo"),
    ENVIAR_LEMBRETE_PROCESSO("Enviar lembrete de processo"),

    // ========== SUBPROCESSO - CRUD ==========
    LISTAR_SUBPROCESSOS("Listar subprocessos"),
    VISUALIZAR_SUBPROCESSO("Visualizar subprocesso"),
    CRIAR_SUBPROCESSO("Criar subprocesso"),
    EDITAR_SUBPROCESSO("Editar subprocesso"),
    EXCLUIR_SUBPROCESSO("Excluir subprocesso"),
    ALTERAR_DATA_LIMITE("Alterar data limite"),
    REABRIR_CADASTRO("Reabrir cadastro"),
    REABRIR_REVISAO("Reabrir revisão"),

    // ========== SUBPROCESSO - CADASTRO ==========
    EDITAR_CADASTRO("Editar cadastro de atividades"),
    DISPONIBILIZAR_CADASTRO("Disponibilizar cadastro"),
    DEVOLVER_CADASTRO("Devolver cadastro"),
    ACEITAR_CADASTRO("Aceitar cadastro"),
    HOMOLOGAR_CADASTRO("Homologar cadastro"),

    // ========== SUBPROCESSO - REVISÃO CADASTRO ==========
    EDITAR_REVISAO_CADASTRO("Editar revisão de cadastro"),
    DISPONIBILIZAR_REVISAO_CADASTRO("Disponibilizar revisão de cadastro"),
    DEVOLVER_REVISAO_CADASTRO("Devolver revisão de cadastro"),
    ACEITAR_REVISAO_CADASTRO("Aceitar revisão de cadastro"),
    HOMOLOGAR_REVISAO_CADASTRO("Homologar revisão de cadastro"),

    // ========== SUBPROCESSO - MAPA ==========
    VISUALIZAR_MAPA("Visualizar mapa"),
    EDITAR_MAPA("Editar mapa"),
    DISPONIBILIZAR_MAPA("Disponibilizar mapa"),
    VERIFICAR_IMPACTOS("Verificar impactos no mapa"),
    APRESENTAR_SUGESTOES("Apresentar sugestões ao mapa"),
    VALIDAR_MAPA("Validar mapa"),
    DEVOLVER_MAPA("Devolver mapa"),
    ACEITAR_MAPA("Aceitar mapa"),
    HOMOLOGAR_MAPA("Homologar mapa"),
    AJUSTAR_MAPA("Ajustar mapa"),

    // ========== ATIVIDADES ==========
    CRIAR_ATIVIDADE("Criar atividade"),
    EDITAR_ATIVIDADE("Editar atividade"),
    EXCLUIR_ATIVIDADE("Excluir atividade"),
    ASSOCIAR_CONHECIMENTOS("Associar conhecimentos à atividade"),

    // ========== MAPA DE COMPETÊNCIAS ==========
    LISTAR_MAPAS("Listar mapas"),
    VISUALIZAR_MAPA_DETALHES("Visualizar detalhes do mapa"),
    CRIAR_MAPA("Criar mapa"),
    EDITAR_MAPA_DIRETO("Editar mapa diretamente"),
    EXCLUIR_MAPA("Excluir mapa"),

    // ========== AÇÕES EM BLOCO ==========
    ACEITAR_CADASTRO_EM_BLOCO("Aceitar cadastros em bloco"),
    DISPONIBILIZAR_MAPA_EM_BLOCO("Disponibilizar mapas em bloco"),
    HOMOLOGAR_CADASTRO_EM_BLOCO("Homologar cadastros em bloco"),
    HOMOLOGAR_MAPA_EM_BLOCO("Homologar mapas em bloco"),

    // ========== DIAGNÓSTICO ==========
    VISUALIZAR_DIAGNOSTICO("Visualizar diagnóstico"),
    REALIZAR_AUTOAVALIACAO("Realizar autoavaliação");

    private final String descricao;

    @Override
    public String toString() {
        return descricao;
    }
}
