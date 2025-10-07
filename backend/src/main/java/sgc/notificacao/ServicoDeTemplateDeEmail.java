package sgc.notificacao;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável por criar templates HTML para diferentes tipos de e-mail.
 * Cada método cria um template específico para um caso de uso do sistema.
 */
@Service
public class ServicoDeTemplateDeEmail {
    
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Template para notificar início de processo (CDU-04, CDU-05).
     */
    public String criarEmailDeProcessoIniciado(
            String nomeUnidade, 
            String nomeProcesso, 
            String tipoProcesso,
            LocalDate dataLimite) {
        
        String conteudo = String.format("""
                        <p>Um novo processo de <strong>%s</strong> foi iniciado para sua unidade.</p>
                        
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>
                        </div>
                        
                        <p><strong>Próximos passos:</strong></p>
                        <ol>
                            <li>Acesse o sistema SGC</li>
                            <li>Complete o cadastro de atividades da unidade</li>
                            <li>Disponibilize o cadastro para análise antes da data limite</li>
                        </ol>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #0066cc; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Sistema
                            </a>
                        </p>
                        """,
            tipoProcesso, nomeUnidade, nomeProcesso, tipoProcesso, 
            dataLimite.format(FORMATADOR));
        
        return criarTemplateBase("Processo Iniciado - " + tipoProcesso, conteudo);
    }
    
    /**
     * Template para notificar disponibilização de cadastro (CDU-09, CDU-10).
     */
    public String criarEmailDeCadastroDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            int quantidadeAtividades) {
        
        String conteudo = String.format("""
                        <p>Um cadastro de atividades está disponível para sua análise.</p>
                        
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Atividades cadastradas:</strong> %d</p>
                        </div>
                        
                        <p><strong>Ações necessárias:</strong></p>
                        <ul>
                            <li>Analisar o cadastro de atividades</li>
                            <li>Verificar conformidade com as diretrizes</li>
                            <li>Aceitar o cadastro ou devolver para ajustes</li>
                        </ul>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Analisar Cadastro
                            </a>
                        </p>
                        """,
            nomeUnidade, nomeProcesso, quantidadeAtividades);
        
        return criarTemplateBase("Cadastro Disponibilizado para Análise", conteudo);
    }
    
    /**
     * Template para notificar devolução de cadastro (CDU-13).
     */
    public String criarEmailDeCadastroDevolvido(
            String nomeUnidade,
            String nomeProcesso,
            String motivo,
            String observacoes) {
        
        String conteudo = String.format("""
                        <p>O cadastro de atividades da sua unidade foi devolvido para ajustes.</p>
                        
                        <div style="background-color: #fff8dc; padding: 15px; margin: 15px 0; border-left: 4px solid #ff9900;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                        </div>
                        
                        <div style="background-color: #ffe0e0; padding: 15px; margin: 15px 0; border-left: 4px solid #cc0000;">
                            <p style="margin: 5px 0;"><strong>Motivo da devolução:</strong></p>
                            <p style="margin: 10px 0;">%s</p>
                        
                            <p style="margin: 15px 0 5px 0;"><strong>Observações do analista:</strong></p>
                            <p style="margin: 10px 0;">%s</p>
                        </div>
                        
                        <p><strong>Próximos passos:</strong></p>
                        <ul>
                            <li>Realize os ajustes solicitados</li>
                            <li>Disponibilize novamente o cadastro para análise</li>
                        </ul>
                        
                        <p style="margin-top: 20px;">
                            <a href="httphttps://tre-pe.jus.br"
                               style="background-color: #ff9900; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Ajustar Cadastro
                            </a>
                        </p>
                        """,
            nomeUnidade, nomeProcesso, motivo, observacoes);
        
        return criarTemplateBase("Cadastro Devolvido para Ajustes", conteudo);
    }
    
    /**
     * Template para notificar disponibilização de mapa (CDU-17).
     */
    public String criarEmailDeMapaDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            LocalDate dataLimiteValidacao) {
        
        String conteudo = String.format("""
                        <p>O Mapa de Competências da sua unidade foi disponibilizado para validação.</p>
                        
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Data limite para validação:</strong> %s</p>
                        </div>
                        
                        <p><strong>Ações necessárias:</strong></p>
                        <ul>
                            <li>Revisar o mapa de competências gerado</li>
                            <li>Validar ou solicitar ajustes</li>
                            <li>Concluir a validação antes da data limite</li>
                        </ul>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #0066cc; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Validar Mapa
                            </a>
                        </p>
                        """,
            nomeUnidade, nomeProcesso, dataLimiteValidacao.format(FORMATADOR));
        
        return criarTemplateBase("Mapa de Competências Disponibilizado", conteudo);
    }
    
    /**
     * Template para notificar validação de mapa (CDU-18).
     */
    public String criarEmailDeMapaValidado(
            String nomeUnidade,
            String nomeProcesso) {
        
        String conteudo = String.format("""
                        <p>O Mapa de Competências foi validado pela unidade.</p>
                        
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Status:</strong> Validado ✓</p>
                        </div>
                        
                        <p>O mapa foi aceito e está pronto para ser publicado.</p>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Visualizar Mapa
                            </a>
                        </p>
                        """,
            nomeUnidade, nomeProcesso);
        
        return criarTemplateBase("Mapa de Competências Validado", conteudo);
    }
    
    /**
     * Template para notificar finalização de processo (CDU-21).
     */
    public String criarEmailDeProcessoFinalizado(
            String nomeProcesso,
            LocalDate dataFinalizacao,
            int quantidadeMapas) {
        
        String conteudo = String.format("""
                        <p>O processo de mapeamento de competências foi finalizado com sucesso!</p>
                        
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Data de finalização:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Mapas vigentes:</strong> %d</p>
                        </div>
                        
                        <p>Os Mapas de Competências estão agora oficialmente vigentes e disponíveis para consulta.</p>
                        
                        <p><strong>Benefícios dos mapas:</strong></p>
                        <ul>
                            <li>Identificação clara das competências necessárias</li>
                            <li>Base para planejamento de capacitação</li>
                            <li>Apoio à gestão de pessoas</li>
                            <li>Diagnóstico de lacunas de competências</li>
                        </ul>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Mapas
                            </a>
                        </p>
                        """,
            nomeProcesso, dataFinalizacao.format(FORMATADOR), quantidadeMapas);
        
        return criarTemplateBase("Processo Finalizado - Mapas Vigentes", conteudo);
    }
    
    /**
     * Template para notificar finalização de processo por unidade (CDU-21).
     */
    public String criarEmailDeProcessoFinalizadoPorUnidade(
            String siglaUnidade,
            String nomeProcesso,
            String mensagemPersonalizada) {
        
        String conteudo = String.format("""
                        <p>Comunicamos a conclusão do processo <strong>%s</strong> para a sua unidade.</p>
                        
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Status:</strong> Finalizado ✓</p>
                        </div>
                        
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">
                            <p style="margin: 5px 0;">%s</p>
                        </div>
                        
                        <p>Já é possível visualizar os mapas de competências atualizados através do menu
                           <strong>Minha Unidade</strong> do Sistema de Gestão de Competências.</p>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Sistema
                            </a>
                        </p>
                        """,
            nomeProcesso, siglaUnidade, nomeProcesso, mensagemPersonalizada);
        
        return criarTemplateBase("SGC: Conclusão do processo " + nomeProcesso, conteudo);
    }
    
    public String criarTemplateBase(String titulo, String conteudo) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #0066cc;
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: normal;
                    }
                    .content {
                        padding: 30px 20px;
                        background-color: #ffffff;
                    }
                    .content p {
                        margin: 10px 0;
                    }
                    .content ul, .content ol {
                        margin: 10px 0;
                        padding-left: 25px;
                    }
                    .content li {
                        margin: 5px 0;
                    }
                    .footer {
                        padding: 20px;
                        font-size: 12px;
                        color: #666;
                        background-color: #f9f9f9;
                        border-top: 1px solid #e0e0e0;
                        text-align: center;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                    a {
                        color: #0066cc;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        %s
                    </div>
                    <div class="footer">
                        <p><strong>Sistema de Gestão de Competências - SGC</strong></p>
                        <p>TRE-PE - Tribunal Regional Eleitoral de Pernambuco</p>
                        <p style="margin-top: 10px; font-style: italic;">
                            Este é um e-mail automático. Por favor, não responda.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, titulo, conteudo);
    }
}