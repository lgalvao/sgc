# Plano de Melhorias - Suíte E2E SGC

Este documento detalha oportunidades de otimização identificadas na suíte de testes E2E do SGC após a consolidação da estratégia de `resetDatabase`. As análises são baseadas nos logs de execução e no comportamento do *test runner* do Playwright (188 testes).

---

## 1. Testes de "Preparação" Tratados como Testes Reais

### Contexto e Problema
A suíte utiliza blocos `test()` para realizar a preparação de estado do banco de dados (setup) em fluxos que exigem dados complexos (ex: um processo homologado ou com mapa validado). Isso é especialmente comum em blocos `test.describe.serial` (do `CDU-22` ao `CDU-33`).
Como resultado, o Playwright os contabiliza como testes independentes, inflando artificialmente o número de testes, aumentando o overhead de relatórios e, o mais grave, gerando dezenas de avisos no linter, pois esses blocos não possuem *assertions* (`expect`).

### Prova (Logs/Linter)
Trecho de log de execução:
```text
✓  137 [chromium] › e2e/cdu-23.spec.ts:42:5 › CDU-23 - Homologar cadastros em bloco › Preparacao 1: Admin cria e inicia processo (912ms)
✓  138 [chromium] › e2e/cdu-23.spec.ts:64:5 › CDU-23 - Homologar cadastros em bloco › Preparacao 2: Chefe disponibiliza cadastro (857ms)
```
Avisos do Linter (ESLint):
```text
/Users/leonardo/sgc/e2e/cdu-22.spec.ts
  33:5  warning  Test has no assertions  playwright/expect-expect
  58:5  warning  Test has no assertions  playwright/expect-expect
```

### Como Resolver
Passos de preparação não devem ser blocos `test()`. Se a preparação falhar, não é um "teste que falhou", é a suíte que não conseguiu ser montada.
1. **Migrar para `beforeAll`**: Mover a lógica desses blocos de preparação para dentro de um único gancho `test.beforeAll()` dentro do `describe.serial`.
2. **Consolidar em Fixtures de Setup**: Melhor ainda, agrupar essas chamadas de API em funções auxiliares robustas ou fixtures customizadas (ex: `prepararProcessoHomologadoFixture()`), chamando-as uma única vez antes de rodar os testes reais daquele arquivo.

---

## 2. Repetição Massiva de Autenticações (Overhead de Login)

### Contexto e Problema
A arquitetura atual realiza o login via API (`LoginFacade.entrar`) quase todas as vezes que um teste começa ou que a persona precisa mudar dentro de um fluxo. Embora a chamada de API seja rápida, acumular centenas de requisições de login e emissão de tokens aumenta significativamente o tempo total da suíte.

### Prova (Logs)
Durante a execução de fluxos multi-atores (como o `CDU-05` ou `CDU-26`), vemos trocas constantes no backend:
```text
[WebServer] [BACKEND-0] INFO  LoginFacade.entrar:107: Usuário 141414 autorizado: CHEFE-SECAO_221
[WebServer] [BACKEND-0] INFO  SubprocessoTransicaoService.disponibilizarCadastro:155...
[WebServer] [BACKEND-0] INFO  LoginFacade.entrar:107: Usuário 131313 autorizado: GESTOR-COORD_22
[WebServer] [BACKEND-0] INFO  SubprocessoTransicaoService.executarAceite:498...
```

### Como Resolver
Aproveitar o recurso nativo de **`storageState` do Playwright**.
1. Criar um projeto de `global-setup` que faz o login com os perfis chave (ADMIN, CHEFE, GESTOR) antes da suíte rodar e salva os *cookies/tokens* em arquivos `.json` na pasta temporária.
2. Criar fixtures customizadas no Playwright (ex: `pageAsAdmin`, `pageAsGestor`) que injetam esse `storageState` diretamente no contexto do navegador (`browser.newContext({ storageState: 'admin.json' })`).
3. O teste já começaria "logado", eliminando 90% das requisições ao `/api/auth`.

---

## 3. Variáveis e Imports Residuais "Órfãos"

### Contexto e Problema
Nas versões anteriores, a suíte exigia a exclusão manual dos dados no final de cada teste. Para isso, variáveis globais como `processoId` guardavam os identificadores para passá-los a funções de limpeza (ex: `cleanupAutomatico`). Como essa estratégia foi substituída pelo `resetDatabase` (TRUNCATE) global, essas variáveis e atribuições tornaram-se código morto.

### Prova (Linter)
36 ocorrências como esta no `npm run typecheck:e2e / eslint`:
```text
/Users/leonardo/sgc/e2e/cdu-26.spec.ts
  43:9  warning  'processoId' is assigned a value but never used. Allowed unused vars must match /^_|autenticado|cleanup/u  @typescript-eslint/no-unused-vars
```

### Como Resolver
1. Realizar uma varredura com expressões regulares ou via Linter Auto-fix para remover declarações (`let processoId: number;`) e atribuições (`processoId = res.id;`) inúteis.
2. Remover também eventuais imports de utilitários antigos de deleção que ficaram esquecidos no topo dos arquivos.

---

## 4. Transições de Estado "Manuais" em Testes Seriais (Flakiness)

### Contexto e Problema
Muitos CDUs (como `CDU-05` e `CDU-16`) executam a jornada completa do usuário através da UI passo a passo. Por exemplo, para testar a "Fase 3: Chefe verifica atividades", o teste obriga a passagem pela Fase 1 e 2. O teste se torna um roteiro enorme (`.serial`). Se a Fase 1 tiver um pequeno atraso de rede e falhar, o teste da Fase 3 nem executa.

### Prova (Contexto Arquitetural)
Isso é evidente na nomenclatura dos testes:
`Fase 1.1: ADMIN cria...` -> `Fase 1.2: CHEFE adiciona...` -> `Fase 1.7: ADMIN homologa...` -> `Fase 2: Iniciar Revisão`.

### Como Resolver
1. **State-Jumping via API**: Desacoplar os testes. Se queremos testar a "Visualização da Revisão" (Fase 2), devemos usar a API/Fixtures de backend para "injetar" diretamente no banco de dados um processo já nesse estado no bloco `beforeEach`.
2. O teste de UI (frontend) deve focar apenas em renderizar a tela de Revisão e clicar nos botões, reduzindo drasticamente o tempo de execução e o risco de falhas em cascata (Flakiness). A estratégia de `describe.serial` deve ser reservada apenas para fluxos estritamente transacionais onde o estado intermediário seja o real objeto do teste.
