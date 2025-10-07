package sgc.alerta;

import sgc.processo.Processo;
import sgc.subprocesso.Subprocesso;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço para gerenciar alertas do sistema.
 * <p>
 * Responsável por criar alertas diferenciados para unidades participantes
 * de processos, considerando os tipos de unidade (OPERACIONAL, INTERMEDIÁRIA, INTEROPERACIONAL).
 */
public interface ServicoAlerta {
    
    /**
     * Cria um alerta para uma unidade específica.
     * Identifica automaticamente os destinatários (responsável + superiores).
     * 
     * @param processo Processo relacionado ao alerta
     * @param tipoAlerta Tipo do alerta (PROCESSO_INICIADO_OPERACIONAL, etc)
     * @param unidadeDestinoCodigo Código da unidade destino
     * @param descricao Descrição do alerta
     * @param dataLimite Data limite relacionada ao alerta (opcional)
     * @return Alerta criado
     */
    Alerta criarAlerta(
        Processo processo,
        String tipoAlerta,
        Long unidadeDestinoCodigo,
        String descricao,
        LocalDate dataLimite
    );
    
    /**
     * Cria alertas diferenciados para processo iniciado.
     * Considera o tipo de cada unidade para criar mensagens apropriadas:
     * - OPERACIONAL: "Preencha atividades e conhecimentos"
     * - INTERMEDIÁRIA: "Aguarde mapas das unidades subordinadas"
     * - INTEROPERACIONAL: Cria 2 alertas (operacional + intermediária)
     * 
     * @param processo Processo iniciado
     * @param subprocessos Lista de subprocessos criados
     * @return Lista de alertas criados
     */
    List<Alerta> criarAlertasProcessoIniciado(
        Processo processo,
        List<Subprocesso> subprocessos
    );
    
    /**
     * Cria alerta para cadastro disponibilizado.
     * 
     * @param processo Processo relacionado
     * @param unidadeOrigemCodigo Código da unidade que disponibilizou
     * @param unidadeDestinoCodigo Código da unidade destino (SEDOC)
     * @return Alerta criado
     */
    Alerta criarAlertaCadastroDisponibilizado(
        Processo processo,
        Long unidadeOrigemCodigo,
        Long unidadeDestinoCodigo
    );
    
    /**
     * Cria alerta para cadastro devolvido.
     * 
     * @param processo Processo relacionado
     * @param unidadeDestinoCodigo Código da unidade que receberá o alerta
     * @param motivo Motivo da devolução
     * @return Alerta criado
     */
    Alerta criarAlertaCadastroDevolvido(
        Processo processo,
        Long unidadeDestinoCodigo,
        String motivo
    );
}