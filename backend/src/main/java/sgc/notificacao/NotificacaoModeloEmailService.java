package sgc.notificacao;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Serviço responsável por criar templates HTML para diferentes tipos de e-mail.
 * Cada método cria um template específico para um caso de uso do sistema.
 */
@Service
public class NotificacaoModeloEmailService {
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
                        <p>Um novo processo de <strong>%s</strong> foi iniciado para sua unidade.</p>%n
                        %n
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>%n
                        </div>%n
                        %n
                        <p><strong>Próximos passos:</strong></p>%n
                        <ol>%n
                            <li>Acesse o sistema SGC</li>%n
                            <li>Complete o cadastro de atividades da unidade</li>%n
                            <li>Disponibilize o cadastro para análise antes da data limite</li>%n
                        </ol>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #0066cc; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Acessar Sistema%n
                            </a>%n
                        </p>%n
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
                        <p>Um cadastro de atividades está disponível para sua análise.</p>%n
                        %n
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Atividades cadastradas:</strong> %d</p>%n
                        </div>%n
                        %n
                        <p><strong>Ações necessárias:</strong></p>%n
                        <ul>%n
                            <li>Analisar o cadastro de atividades</li>%n
                            <li>Verificar conformidade com as diretrizes</li>%n
                            <li>Aceitar o cadastro ou devolver para ajustes</li>%n
                        </ul>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #00aa00; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Analisar Cadastro%n
                            </a>%n
                        </p>%n
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
                        <p>O cadastro de atividades da sua unidade foi devolvido para ajustes.</p>%n
                        %n
                        <div style="background-color: #fff8dc; padding: 15px; margin: 15px 0; border-left: 4px solid #ff9900;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                        </div>%n
                        %n
                        <div style="background-color: #ffe0e0; padding: 15px; margin: 15px 0; border-left: 4px solid #cc0000;">%n
                            <p style="margin: 5px 0;"><strong>Motivo da devolução:</strong></p>%n
                            <p style="margin: 10px 0;">%s</p>%n
                        %n
                            <p style="margin: 15px 0 5px 0;"><strong>Observações do analista:</strong></p>%n
                            <p style="margin: 10px 0;">%s</p>%n
                        </div>%n
                        %n
                        <p><strong>Próximos passos:</strong></p>%n
                        <ul>%n
                            <li>Realize os ajustes solicitados</li>%n
                            <li>Disponibilize novamente o cadastro para análise</li>%n
                        </ul>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="httphttps://tre-pe.jus.br"%n
                               style="background-color: #ff9900; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Ajustar Cadastro%n
                            </a>%n
                        </p>%n
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
                        <p>O Mapa de Competências da sua unidade foi disponibilizado para validação.</p>%n
                        %n
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Data limite para validação:</strong> %s</p>%n
                        </div>%n
                        %n
                        <p><strong>Ações necessárias:</strong></p>%n
                        <ul>%n
                            <li>Revisar o mapa de competências gerado</li>%n
                            <li>Validar ou solicitar ajustes</li>%n
                            <li>Concluir a validação antes da data limite</li>%n
                        </ul>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #0066cc; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Validar Mapa%n
                            </a>%n
                        </p>%n
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
                        <p>O Mapa de Competências foi validado pela unidade.</p>%n
                        %n
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Status:</strong> Validado ✓</p>%n
                        </div>%n
                        %n
                        <p>O mapa foi aceito e está pronto para ser publicado.</p>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #00aa00; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Visualizar Mapa%n
                            </a>%n
                        </p>%n
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
                        <p>O processo de mapeamento de competências foi finalizado com sucesso!</p>%n
                        %n
                        <div style="background-color: #f0fff0; padding: 15px; margin: 15px 0; border-left: 4px solid #00aa00;">%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Data de finalização:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Mapas vigentes:</strong> %d</p>%n
                        </div>%n
                        %n
                        <p>Os Mapas de Competências estão agora oficialmente vigentes e disponíveis para consulta.</p>%n
                        %n
                        <p><strong>Benefícios dos mapas:</strong></p>%n
                        <ul>%n
                            <li>Identificação clara das competências necessárias</li>%n
                            <li>Base para planejamento de capacitação</li>%n
                            <li>Apoio à gestão de pessoas</li>%n
                            <li>Diagnóstico de lacunas de competências</li>%n
                        </ul>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #00aa00; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Acessar Mapas%n
                            </a>%n
                        </p>%n
                        """,
            nomeProcesso, dataFinalizacao.format(FORMATADOR), quantidadeMapas);
        
        return criarTemplateBase("Processo Finalizado - Mapas Vigentes", conteudo);
    }
    
    /**
     * Template para notificar finalização de processo por unidade (CDU-21).
     */
    public String criarEmailDeProcessoFinalizadoPorUnidade(
            String siglaUnidade,
            String nomeProcesso) {

        String conteudo = String.format("""
                        <p>Prezado(a) responsável pela %s,</p>
                        <p>Comunicamos a conclusão do processo <strong>%s</strong> para a sua unidade.</p>
                        <p>Já é possível visualizar o seu mapa de competências atualizado através do menu Minha Unidade do Sistema de Gestão de Competências.</p>
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Sistema
                            </a>
                        </p>
                        """,
            siglaUnidade, nomeProcesso);

        return criarTemplateBase("SGC: Conclusão do processo " + nomeProcesso, conteudo);
    }

    /**
     * Template para notificar finalização de processo para unidades intermediárias (CDU-21).
     */
    public String criarEmailDeProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade,
            String nomeProcesso,
            List<String> siglasUnidadesSubordinadas) {

        String listaHtmlUnidades = "<ul>" +
            siglasUnidadesSubordinadas.stream()
                .map(sigla -> "<li>" + sigla + "</li>")
                .reduce("", String::concat) + "</ul>";

        String conteudo = String.format("""
                        <p>Prezado(a) responsável pela %s,</p>
                        <p>Comunicamos a conclusão do processo <strong>%s</strong> para as unidades:</p>
                        %s
                        <p>Já é possível visualizar os mapas de competências atualizados destas unidades através do menu Minha Unidade do Sistema de Gestão de Competências.</p>
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #00aa00; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Sistema
                            </a>
                        </p>
                        """,
            siglaUnidade, nomeProcesso, listaHtmlUnidades);

        return criarTemplateBase("SGC: Conclusão do processo " + nomeProcesso + " em unidades subordinadas", conteudo);
    }
    
    public String criarTemplateBase(String titulo, String conteudo) {
        return String.format("""
            <!DOCTYPE html>%n
            <html lang="pt-BR">%n
            <head>%n
                <meta charset="UTF-8">%n
                <meta name="viewport" content="width=device-width, initial-scale=1.0">%n
                <style>%n
                    body {%n
                        font-family: Arial, sans-serif;%n
                        line-height: 1.6;%n
                        color: #333;%n
                        margin: 0;%n
                        padding: 0;%n
                        background-color: #f4f4f4;%n
                    }%n
                    .container {%n
                        max-width: 600px;%n
                        margin: 20px auto;%n
                        background-color: #ffffff;%n
                        border-radius: 8px;%n
                        overflow: hidden;%n
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);%n
                    }%n
                    .header {%n
                        background-color: #0066cc;%n
                        color: white;%n
                        padding: 30px 20px;%n
                        text-align: center;%n
                    }%n
                    .header h1 {%n
                        margin: 0;%n
                        font-size: 24px;%n
                        font-weight: normal;%n
                    }%n
                    .content {%n
                        padding: 30px 20px;%n
                        background-color: #ffffff;%n
                    }%n
                    .content p {%n
                        margin: 10px 0;%n
                    }%n
                    .content ul, .content ol {%n
                        margin: 10px 0;%n
                        padding-left: 25px;%n
                    }%n
                    .content li {%n
                        margin: 5px 0;%n
                    }%n
                    .footer {%n
                        padding: 20px;%n
                        font-size: 12px;%n
                        color: #666;%n
                        background-color: #f9f9f9;%n
                        border-top: 1px solid #e0e0e0;%n
                        text-align: center;%n
                    }%n
                    .footer p {%n
                        margin: 5px 0;%n
                    }%n
                    a {%n
                        color: #0066cc;%n
                    }%n
                </style>%n
            </head>%n
            <body>%n
                <div class="container">%n
                    <div class="header">%n
                        <h1>%s</h1>%n
                    </div>%n
                    <div class="content">%n
                        %s%n
                    </div>%n
                    <div class="footer">%n
                        <p><strong>Sistema de Gestão de Competências - SGC</strong></p>%n
                        <p>TRE-PE - Tribunal Regional Eleitoral de Pernambuco</p>%n
                        <p style="margin-top: 10px; font-style: italic;">%n
                            Este é um e-mail automático. Por favor, não responda.%n
                        </p>%n
                    </div>%n
                </div>%n
            </body>%n
            </html>%n
            """, titulo, conteudo);
    }
}