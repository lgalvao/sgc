# Relatório de Execução de Testes E2E (Playwright)

## Resumo Geral

A execução dos testes E2E foi realizada parcialmente (aproximadamente 170 de 246 testes) devido ao timeout do ambiente de execução (400s). No entanto, foi possível identificar um erro crítico que impacta a maioria dos fluxos de teste subsequentes.

*   **Total de Testes Executados:** ~170
*   **Status:** Falha Crítica Detectada
*   **CDUs com Sucesso:**
    *   CDU-01 (Login)
    *   CDU-03 (Manter Processo)
    *   CDU-04 (Iniciar Mapeamento)
    *   CDU-06 (Detalhar Processo)
    *   CDU-07 (Detalhar Subprocesso)
    *   CDU-02 (Painel - Parcial)
*   **CDUs com Falha:**
    *   CDU-05, CDU-08, CDU-09, CDU-10, CDU-11, CDU-12, CDU-13, CDU-14, CDU-15, CDU-16, CDU-17, CDU-18, CDU-19, CDU-20, CDU-21, CDU-22, CDU-23 (e provavelmente os restantes).

## Erros Detalhados

### 1. Erro Crítico no Endpoint de Mapa Completo (500 Internal Server Error)

A falha mais recorrente, que impede a execução de diversos cenários de teste, é um erro 500 ao tentar carregar o mapa completo de um subprocesso.

*   **Endpoint:** `GET /api/subprocessos/{id}/mapa-completo`
*   **Erro:** `500 Internal Server Error`
*   **Corpo da Resposta:**
    ```json
    {
      "detail": "Failed to write request",
      "instance": "/api/subprocessos/201/mapa-completo",
      "status": 500,
      "title": "Internal Server Error"
    }
    ```
*   **Impacto:** O frontend falha ao carregar a visualização do mapa, causando falhas em cascata em todos os testes que dependem dessa funcionalidade (validação, homologação, visualização de competências).

### 2. Timeout e Lentidão
O ambiente de teste apresentou lentidão significativa, levando ao timeout da execução completa. Alguns testes individuais também falharam por timeout (ex: `CDU-02 - Alertas no painel`).

## Passos Necessários para Correção

Para corrigir os erros identificados, recomenda-se seguir os seguintes passos:

1.  **Investigar o Endpoint `buscarMapaCompleto`:**
    *   Verificar a classe `SubprocessoController` e o método mapeado para `GET /api/subprocessos/{id}/mapa-completo`.
    *   Analisar o DTO retornado e o `SubprocessoMapper`. A mensagem "Failed to write request" geralmente indica um erro de serialização JSON (Jackson), como:
        *   **Referência Circular:** Um objeto A referencia B, que referencia A, criando um loop infinito na serialização.
        *   **LazyInitializationException:** Tentativa de serializar uma entidade JPA com relacionamentos Lazy fora de uma transação ativa.
        *   **Getter com Exceção:** Um método getter no DTO ou Entidade lançando uma exceção durante a serialização.

2.  **Verificar Logs do Backend:**
    *   Executar o backend localmente e reproduzir a chamada ao endpoint para visualizar a stack trace completa da exceção, que não foi capturada completamente nos logs do teste E2E.

3.  **Ajustes de Timeout:**
    *   Considerar aumentar o timeout global do Playwright ou otimizar a inicialização do ambiente para garantir que todos os testes consigam rodar no ambiente de CI/CD.

4.  **Correção do Código:**
    *   Caso seja confirmado o erro de serialização, utilizar `@JsonIgnore`, `@JsonManagedReference`/`@JsonBackReference` ou DTOs específicos que não exponham a estrutura cíclica ou Lazy.

## Próximos Passos
Após a correção do erro 500, executar novamente a suíte completa de testes para verificar se os demais CDUs passam com sucesso.
