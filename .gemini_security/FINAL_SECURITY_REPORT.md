# Relatório de Auditoria de Segurança e Privacidade - Backend (Controllers)

Este relatório consolida as vulnerabilidades identificadas durante a auditoria manual dos controllers do sistema SGC.

## Sumário das Vulnerabilidades

| ID | Vulnerabilidade | Tipo | Severidade | Localização Principal |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Insecure Direct Object Reference (IDOR) - Acesso de Leitura **[CORRIGIDO]** | Security | High | `ProcessoController`, `AtividadeController`, `MapaController` |
| 2 | Broken Access Control e Risco de Privacidade no Painel | Security / Privacy | High | `PainelController` |
| 3 | Exposição de Dados Pessoais (PII) | Privacy | Medium | `UsuarioController` |
| 4 | Bypass de Limitador de Taxa (Rate Limit) via Header de Proxy | Security | Medium | `LoginController` |
| 5 | Risco Crítico em Ambiente de Testes (E2eController) | Security | Critical | `E2eController` |
| 6 | Segredo de JWT Fraco por Padrão | Security | Medium | `application.yml` |

---

## Detalhamento das Descobertas

### 1. Insecure Direct Object Reference (IDOR) - Acesso de Leitura não Autorizado
*   **Status:** ✅ **Corrigido**
*   **Vulnerabilidade:** Insecure Direct Object Reference (IDOR)
*   **Tipo:** Security
*   **Severidade:** High
*   **Localização:** 
    *   `backend/src/main/java/sgc/processo/ProcessoController.java`
    *   `backend/src/main/java/sgc/mapa/AtividadeController.java`
    *   `backend/src/main/java/sgc/mapa/MapaController.java`
*   **Descrição:** Os endpoints de consulta (GET) para Processos, Atividades e Mapas permitiam que qualquer usuário autenticado com os perfis `GESTOR` ou `CHEFE` visualizasse detalhes de qualquer registro no sistema, apenas fornecendo o ID (código). Não havia verificação se o registro pertencia à unidade do usuário ou à sua árvore hierárquica.
*   **Ação de Correção:** As anotações `@PreAuthorize` foram atualizadas para utilizar o `SgcPermissionEvaluator` e o método `processoService.checarAcesso`, que agora resolvem a hierarquia da unidade conforme definido em `regras-acesso.md`. Testes de integração foram corrigidos (ativação de segurança no `@WebMvcTest`) e novos testes para `CHEFE` e `GESTOR` foram adicionados para validar matematicamente os bloqueios (`403 Forbidden`) e os sucessos (`200 OK`).

### 2. Broken Access Control e Risco de Privacidade no Painel
*   **Vulnerabilidade:** IDOR / Privacy Violation
*   **Tipo:** Security / Privacy
*   **Severidade:** High
*   **Localização:** `backend/src/main/java/sgc/processo/painel/PainelController.java` (Linhas 29-35 e 46-52)
*   **Descrição:** Os endpoints do painel (`/processos` e `/alertas`) aceitam parâmetros de `unidade` e `usuarioTitulo` sem validar se o usuário autenticado tem permissão para visualizar dados daquela unidade ou daquele usuário específico. Isso permite que um usuário visualize o dashboard e alertas de qualquer outro usuário ou unidade do sistema.
*   **Recomendação:** Validar se a `unidade` solicitada pertence à hierarquia do usuário autenticado e se o `usuarioTitulo` coincide com o usuário logado (ou se o logado é um gestor da unidade do alvo).

### 3. Exposição de Dados Pessoais (PII)
*   **Vulnerabilidade:** PII Leakage
*   **Tipo:** Privacy
*   **Severidade:** Medium
*   **Localização:** `backend/src/main/java/sgc/organizacao/UsuarioController.java` (Linhas 28-30 e 36-39)
*   **Descrição:** Os endpoints de busca e pesquisa de usuários expõem o Título Eleitoral, Matrícula e E-mail de qualquer usuário para qualquer pessoa autenticada no sistema. Embora esses campos sejam usados internamente, sua exposição em massa via API de pesquisa permite a raspagem (scraping) da base de dados de usuários.
*   **Recomendação:** Revisar a `OrganizacaoViews.Publica` no modelo `Usuario` para ocultar campos sensíveis como Título Eleitoral e Matrícula, a menos que sejam estritamente necessários para a funcionalidade de busca.

### 4. Bypass de Limitador de Taxa (Rate Limit) via Header de Proxy
*   **Vulnerabilidade:** Rate Limit Bypass
*   **Tipo:** Security
*   **Severidade:** Medium
*   **Localização:** `backend/src/main/java/sgc/seguranca/login/LoginController.java` (Linhas 128-134)
*   **Descrição:** O método `extrairIp` confia cegamente no cabeçalho `X-Forwarded-For` para identificar o IP do cliente. Um atacante pode forjar este cabeçalho com IPs aleatórios para contornar o limitador de tentativas de login (`limitadorTentativasLogin`), facilitando ataques de força bruta.
*   **Recomendação:** Configurar o Spring Boot para confiar em cabeçalhos de proxy apenas de IPs conhecidos (trusted proxies) ou ignorar o cabeçalho se a aplicação não estiver atrás de um balanceador de carga configurado.

### 5. Risco Crítico em Ambiente de Testes (E2eController)
*   **Vulnerabilidade:** Authentication Bypass / Database Reset
*   **Tipo:** Security
*   **Severidade:** Critical (se ativo em produção)
*   **Localização:** `backend/src/main/java/sgc/e2e/E2eController.java` (Linhas 55-61 e 407-422)
*   **Descrição:** O `E2eController` permite o reset total do banco de dados e a escalada de privilégios para `ADMIN` sem senha. Embora esteja protegido pelo perfil `e2e`, a existência deste código no projeto representa um risco catastrófico caso o perfil seja ativado acidentalmente em um ambiente exposto.
*   **Recomendação:** Garantir que este controller e suas dependências não sejam compilados ou incluídos no artefato de produção (utilizando configurações específicas de build do Gradle).

### 6. Segredo de JWT Fraco por Padrão
*   **Vulnerabilidade:** Weak Default Secret
*   **Tipo:** Security
*   **Severidade:** Medium
*   **Localização:** `backend/src/main/resources/application.yml` (Linhas 55-57)
*   **Descrição:** A configuração de segurança define um valor padrão para `JWT_SECRET`. Se o administrador esquecer de definir essa variável de ambiente em produção, o sistema utilizará uma chave fraca e conhecida, permitindo que atacantes forjem tokens JWT.
*   **Recomendação:** Remover o valor padrão no `application.yml` ou lançar um erro na inicialização do sistema caso a chave não tenha sido definida em ambientes de produção.
