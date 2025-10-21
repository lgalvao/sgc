# Análise Detalhada da Implementação

Este documento aprofunda a análise da implementação do backend em relação aos requisitos, focando em divergências e pontos críticos.

## CDU-15: Manter Mapa de Competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-15.md` | O requisito descreve um fluxo de **CRUD de competências individuais**. |
| `frontend/e2e/cdu/cdu-15.spec.ts` | O teste E2E **não foi encontrado**. |
| `backend/.../CDU15IntegrationTest.java` | O teste de integração valida uma abordagem de **salvar o mapa inteiro** em uma única operação. |
| `backend/.../SubprocessoMapaControle.java` | O controlador expõe os endpoints `GET /api/subprocessos/{id}/mapa-completo` e `POST /api/subprocessos/{codSubprocesso}/mapa-completo/atualizar`, que operam no mapa como um todo, confirmando a implementação de "salvar o mapa inteiro". |
| `backend/.../SubprocessoMapaWorkflowService.java` | O serviço `salvarMapaSubprocesso` implementa a lógica de negócio para salvar o mapa completo e avançar o estado do subprocesso. |

### Detalhamento da Divergência

A implementação do backend para o CDU-15 se distancia do fluxo de trabalho descrito nos requisitos. Enquanto o requisito detalha um processo interativo onde o usuário pode criar, editar e excluir competências de forma individual, a implementação da API e dos serviços correspondentes foi projetada para receber e persistir o estado completo do mapa de uma só vez.

**Pontos de atenção:**

1.  **Estratégia de Persistência:** A abordagem de "salvar o mapa inteiro" é funcional, mas não corresponde à experiência de usuário descrita no requisito.
2.  **Ausência de Teste E2E:** A falta de um teste E2E para este caso de uso significa que o fluxo, mesmo que divergente, não está sendo validado de ponta a ponta.

**Recomendações:**

*   **Verificar o Frontend:** É crucial analisar a implementação do frontend para entender como a tela de "Edição de mapa" foi construída. Ela pode ter sido adaptada para a abordagem de "salvar o mapa inteiro", enviando o estado completo do mapa ao backend em uma única requisição.
*   **Criar Teste E2E:** Independentemente da abordagem, um teste E2E deve ser criado para garantir a funcionalidade de ponta a ponta.
*   **Alinhar Documentação:** A documentação (requisito e `plano-alinhamento.md`) deve ser atualizada para refletir a implementação real e evitar futuras confusões.
