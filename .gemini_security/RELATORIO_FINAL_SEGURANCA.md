# Relatório de Auditoria de Segurança e Privacidade - SGC

**Data:** 22 de Março de 2026
**Status:** Concluído (Deep Dive Finalizado)
**Resultado:** Sistema em conformidade com as melhores práticas de segurança, com arquitetura robusta contra ataques comuns.

## Resumo Executivo

Foi realizada uma auditoria de segurança e privacidade profunda no código de produção do sistema SGC. Além da análise inicial, foram conduzidas investigações detalhadas (Deep Dives) em áreas de alto risco, como controle de acesso granular, geração de documentos dinâmicos e lógica de autenticação. O sistema demonstra uma maturidade elevada em segurança, utilizando padrões modernos de proteção.

## Áreas Analisadas em Profundidade

1.  **Proteção contra IDOR (Insecure Direct Object Reference):**
    - **Análise:** Investigação da `AtividadeFacade` e `SubprocessoService`.
    - **Conclusão:** O sistema protege contra IDOR ao carregar a entidade do banco de dados e validar a permissão (`EDITAR_CADASTRO`) do usuário autenticado sobre o subprocesso proprietário *antes* de qualquer operação de escrita. IDs manipulados em requisições são ineficazes se o usuário não tiver permissão explícita.

2.  **Segurança na Geração de PDF:**
    - **Análise:** Revisão do `RelatorioFacade` e `PdfFactory`.
    - **Conclusão:** O sistema utiliza a biblioteca OpenPDF. Embora o conteúdo dinâmico (descrições de atividades, nomes de unidades) seja inserido sem escape explícito na biblioteca, o risco é mitigado pois a geração de relatórios é restrita ao perfil `ADMIN`. Recomenda-se, como boa prática, a sanitização de entradas antes da inserção no documento PDF.

3.  **Integridade de Autenticação e JWT:**
    - **Análise:** Investigação do `LoginFacade` e `GerenciadorJwt`.
    - **Conclusão:** O fluxo de login em dois passos é seguro. O sistema valida rigorosamente se o perfil e a unidade solicitados para a geração do token final estão na lista de autorizações pré-carregadas do usuário no Active Directory/Banco de Dados. O uso de tokens de pré-autenticação de 5 minutos minimiza janelas de ataque.

4.  **Sanitização de HTML:**
    - **Análise:** Uso do `UtilSanitizacao` (OWASP HTML Sanitizer).
    - **Conclusão:** Campos de texto livre (justificativas, observações) são sistematicamente sanitizados antes de serem processados pela lógica de negócio ou persistidos, mitigando riscos de XSS.

## Conclusões Gerais

- **Robustez de Autorização:** A "Regra de Ouro" (Hierarquia para Leitura, Localização para Escrita) está consistentemente implementada através do `SgcPermissionEvaluator`.
- **Defesa em Profundidade:** O sistema utiliza múltiplas camadas de proteção (Filtros JWT, `@PreAuthorize` nos controllers, validações em Facades e Services).
- **Tratamento de Dados Sensíveis:** PII (Título Eleitoral) é mascarado em logs e protegido por cookies seguros.

## Recomendações

1.  **PDF Sanitization:** Implementar uma camada de escape ou sanitização para textos inseridos em parágrafos de PDF para prevenir injeção de caracteres de controle ou formatação maliciosa por administradores.
2.  **Monitoramento de Claims:** Embora seguro, recomenda-se auditoria periódica dos perfis atribuídos a usuários para garantir o princípio do privilégio mínimo.

**Nenhuma vulnerabilidade crítica ou explorável por usuários comuns foi identificada.**
