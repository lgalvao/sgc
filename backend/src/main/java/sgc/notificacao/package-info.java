/**
 * Módulo de Notificações e Orquestração de Eventos (Supporting).
 * 
 * <p>Responsável pelo envio de notificações por e-mail de forma robusta
 * e desacoplada, além de orquestrar a criação de alertas em resposta a
 * eventos de domínio da aplicação.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.notificacao.NotificacaoEmailService} - Interface para envio de e-mails</li>
 *   <li>{@link sgc.notificacao.api.EmailDto} - DTO para dados de e-mail</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>alerta - Criação de alertas internos</li>
 *   <li>processo - Eventos de processo e acesso a dados</li>
 *   <li>subprocesso - Acesso a dados de subprocessos</li>
 *   <li>sgrh - Informações de usuários</li>
 *   <li>unidade - Informações de unidades</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Notifica início de processo</li>
 *   <li>EventoProcessoFinalizado - Notifica finalização de processo</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Notificações",
    allowedDependencies = {"alerta", "processo", "subprocesso", "sgrh", "unidade", "comum"}
)
package sgc.notificacao;
