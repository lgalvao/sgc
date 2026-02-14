# Plano E2E - Descoberta e Correção de Bugs

## Objetivo
Criar e executar testes E2E focados em fluxos críticos, descobrir falhas reais do sistema (sem mocks/atalhos) e aplicar correções com validação.

## Escopo Crítico
- CDU-02 (Painel): seção de alertas, ordenação e marcação de leitura na primeira visualização.
- Fluxo fim a fim de alerta: criação de processo, envio de lembrete, exibição para unidade destino.
- Regressão nos fluxos existentes impactados por alertas no painel.

## Estratégia
1. Implementar testes E2E focados no comportamento crítico de alertas.
2. Executar testes isolados para evidenciar falhas reproduzíveis.
3. Corrigir a causa raiz no backend/frontend, mantendo o fluxo real do usuário.
4. Reexecutar os testes novos e registrar evidências.
5. Atualizar documentação de trabalho em `e2e-work.md` com descobertas e correções.

## Casos de Teste Prioritários
- Exibir alerta novo em negrito para usuário destino.
- Marcar alerta como visualizado após primeira visualização do painel.
- Validar histórico/movimentação após envio de lembrete.

## Entregáveis
- Novos specs E2E focados em alertas críticos.
- Correções de bugs encontradas durante execução E2E.
- Registro consolidado em `e2e-work.md`.
