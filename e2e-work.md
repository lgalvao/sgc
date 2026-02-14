# E2E Worklog - Exploração de bugs e correções

## Contexto
- Base de requisitos: `etc/reqs` (foco em fluxos críticos do painel/alertas e detalhamento).
- Estratégia: testes E2E reais, sem mocks, sem atalhos de fluxo de usuário.

## Descobertas

### BUG-001 - Alertas não eram marcados como visualizados após primeira visualização
- **Requisito relacionado:** CDU-02 (alertas não lidos em negrito e marcação na primeira visualização).
- **Como foi reproduzido:** novo teste `e2e/cdu-02-alertas-leitura.spec.ts`.
- **Sintoma:** após abrir o painel com alerta novo, recarregar mantinha linha em `fw-bold`.
- **Causa raiz:** endpoint de listagem do painel retornava `dataHoraLeitura`, mas não persistia marcação de leitura no primeiro acesso.
- **Correção aplicada:** em `backend/src/main/java/sgc/painel/PainelFacade.java`, método `listarAlertas(...)`:
  - coleta alertas não lidos retornados para o usuário;
  - chama `alertaService.marcarComoLidos(usuarioTitulo, codigos)` após montar a página;
  - método passou a operar com transação de escrita (`@Transactional`).
- **Status:** corrigido e validado em E2E.

## Testes criados
- `e2e/cdu-02-alertas-leitura.spec.ts`
  - prepara processo e envio de lembrete;
  - valida alerta novo em negrito;
  - valida que na próxima visualização deixa de ficar em negrito.

## Execuções e resultados
- `npm run test:e2e -- e2e/cdu-02-alertas-leitura.spec.ts` ✅
- `npm run test:e2e -- e2e/cdu-02.spec.ts e2e/cdu-34.spec.ts e2e/cdu-07.spec.ts e2e/cdu-02-alertas-leitura.spec.ts` ✅
- `npm run test:e2e -- e2e/cdu-01.spec.ts e2e/cdu-06.spec.ts e2e/cdu-27.spec.ts e2e/cdu-32.spec.ts e2e/cdu-33.spec.ts e2e/cdu-34.spec.ts` ✅
- `npm run test:e2e` (suíte completa) ✅ `236 passed (6.4m)`

## Resultado da exploração ampla
- Após correção do BUG-001, não foram encontradas novas falhas na suíte completa.
- Fluxos críticos de login, painel, subprocessos, lembretes e relatórios permaneceram estáveis.

## Regressões mapeadas a partir de `Erros.docx`

Testes focados adicionados em `e2e/erros-reportados-regressao.spec.ts`:
- ADMIN em `Unidades` visualiza árvore ampla (SECRETARIA_1 e SECRETARIA_2), evitando cenário de exibir apenas "minha unidade".
- Fluxo de importação de atividades abre modal sem erro de "Acesso Negado".
- Botão `CRIAR` em atribuição temporária efetiva criação com mensagem de sucesso.

Testes focados adicionais em `e2e/micro-painel-funcionalidades.spec.ts`:
- Regras de menu por perfil (`Unidades` vs `Minha unidade`).
- Clique em processo `Criado` abre cadastro.
- Ordenação de alertas no painel (ordem padrão e alternância por coluna Processo).

Validação:
- `npm run test:e2e -- e2e/micro-painel-funcionalidades.spec.ts e2e/erros-reportados-regressao.spec.ts` ✅ (9 passed)

## Observações
- Durante execução de CDU-01 com credencial inválida, ocorreu `401` esperado (não é bug).
