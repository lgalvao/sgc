# Pacote E2E (End-to-End Testing Support)

## Visão Geral

O pacote `e2e` (backend) fornece **endpoints auxiliares e configurações específicas para testes end-to-end** (E2E).

> **⚠️ AVISO IMPORTANTE:** Este código **só é ativado no perfil Spring `e2e`**. Ele contém endpoints que executam operações destrutivas (como limpar o banco de dados) e **NUNCA** deve estar ativo em produção.

## Funcionalidades

O objetivo deste pacote é facilitar a automação de testes, permitindo:

1.  **Reset de Estado:** Endpoint para limpar e reseear o banco de dados para um estado conhecido (`/e2e/reset-database`).
2.  **Criação de Fixtures:** Endpoints para criar cenários complexos (ex: um processo em andamento) rapidamente, sem a necessidade de navegar por toda a UI (`/e2e/fixtures/*`).
3.  **Limpeza Específica:** Endpoint para remover dados de um teste específico (`/e2e/processo/{id}/limpar`).

## Componentes

*   **`E2eController`**: Expõe a API REST auxiliar.
*   **`E2eSecurityConfig`**: Configuração de segurança relaxada para permitir que os testes acessem os endpoints de controle sem autenticação complexa, ao mesmo tempo que mantém a simulação de segurança para a aplicação principal.

## Como Usar

Este pacote é utilizado exclusivamente pela suite de testes Playwright localizada no diretório `/e2e` na raiz do projeto.
