# Análise de Problemas na Integração

Este documento registra os passos de depuração seguidos para resolver a falha nos testes E2E após a reconfiguração de segurança do backend.

## Problema Inicial

Os testes E2E (`cdu-01.spec.ts`) começaram a falhar (timeout) após a implementação de `SecurityConfig.java` no backend. A suspeita inicial era uma divergência entre os dados de teste do frontend (`constantes-teste.ts`) e o banco de dados de teste do backend (`data.sql`).

## Passos da Depuração

1.  **Análise dos Dados de Teste**:
    *   O arquivo `cdu-01.spec.ts` utiliza `loginComoAdmin` e `loginComoServidor`.
    *   As credenciais (título eleitoral) foram rastreadas até `frontend/e2e/helpers/dados/constantes-teste.ts`:
        *   `ADMIN`: `idServidor: '6'`
        *   `SERVIDOR`: `idServidor: '15'`

2.  **Verificação do `data.sql`**:
    *   O `data.sql` inicial não correspondia aos perfis e unidades esperados pelos testes.
    *   **Ação**: O arquivo `data.sql` foi modificado para alinhar o `USUARIO` de `TITULO_ELEITORAL` 6 ao perfil `ADMIN` na unidade `SEDOC`, e o `USUARIO` 15 ao perfil `SERVIDOR` na unidade `STIC`.

3.  **Falha Persistente dos Testes E2E**:
    *   Mesmo com os dados alinhados, os testes E2E continuaram a falhar com timeout.
    *   **Hipótese**: O problema poderia ser no ambiente de teste do frontend (Playwright) ou no backend.

4.  **Isolamento do Backend (Teste com Python)**:
    *   Para verificar a API de login de forma isolada, um script Python (`test_login.py`) foi criado para enviar requisições `POST` diretas ao endpoint `/api/usuarios/autenticar`.
    *   **Resultado**: O script obteve sucesso, recebendo `200 OK` para ambos os usuários (admin e servidor). Isso confirmou que o backend estava funcionando corretamente e que o problema residia no ambiente de teste do frontend.

5.  **Falha nos Testes de Integração do Backend**:
    *   A execução de `./gradlew :backend:test` revelou que 83 testes estavam falhando com erros de `ApplicationContext`.
    *   **Causa**: Conflito entre a `SecurityConfig` principal e a `TestSecurityConfig` importada nos testes.
    *   **Solução**: Anotar `SecurityConfig` com `@Profile("!test")` e remover a anotação `@Import(TestSecurityConfig.class)` dos testes de integração para confiar na auto-configuração do Spring Boot. Isso resolveu todas as falhas nos testes de integração.

## Conclusão

A falha inicial dos testes E2E foi causada por uma combinação de dados de teste desalinhados e uma configuração de segurança de teste em conflito. A solução envolveu corrigir a causa raiz (os dados em `data.sql` e a configuração de segurança) e documentar o processo de depuração.
