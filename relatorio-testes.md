# Relatório de Testes Automatizados

**Data:** 20/01/2026, 19:35:08
**Sistema:** Linux 6.8.0

## Resumo Executivo

| Teste | Status | Duração (s) |
| :--- | :---: | :---: |
| Backend - Testes Unitários | ✅ Sucesso | 9.79s |
| Backend - Testes de Integração | ✅ Sucesso | 1.53s |
| Frontend - Testes Unitários | ✅ Sucesso | 75.69s |
| E2E - Playwright | ❌ Falha | 112.21s |

**Status Geral:** 🔴 REPROVADO

## Detalhes da Execução

### Backend - Testes Unitários

- **Comando:** `./gradlew unitTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso

<details>
<summary>Ver Logs de Saída</summary>

```text
Starting a Gradle Daemon, 1 busy Daemon could not be reused, use --status for details
Reusing configuration cache.
> Task :backend:processTestResources UP-TO-DATE
> Task :backend:processResources UP-TO-DATE
> Task :backend:compileJava UP-TO-DATE
> Task :backend:classes UP-TO-DATE
> Task :backend:compileTestJava UP-TO-DATE
> Task :backend:testClasses UP-TO-DATE
> Task :backend:unitTest UP-TO-DATE

BUILD SUCCESSFUL in 9s
5 actionable tasks: 5 up-to-date
Configuration cache entry reused.

```

</details>

---

### Backend - Testes de Integração

- **Comando:** `./gradlew integrationTest`
- **Diretório:** `backend`
- **Status:** ✅ Sucesso

<details>
<summary>Ver Logs de Saída</summary>

```text
Reusing configuration cache.
> Task :backend:processResources UP-TO-DATE
> Task :backend:processTestResources UP-TO-DATE
> Task :backend:compileJava UP-TO-DATE
> Task :backend:classes UP-TO-DATE
> Task :backend:compileTestJava UP-TO-DATE
> Task :backend:testClasses UP-TO-DATE
> Task :backend:integrationTest UP-TO-DATE

BUILD SUCCESSFUL in 1s
5 actionable tasks: 5 up-to-date
Configuration cache entry reused.

```

</details>

---

### Frontend - Testes Unitários

- **Comando:** `npm run test:unit`
- **Diretório:** `frontend`
- **Status:** ✅ Sucesso

<details>
<summary>Ver Logs de Saída</summary>

```text

> sgc@1.0.0 test:unit
> vitest --run --reporter=dot --no-color


 RUN  v4.0.17 /app/frontend

·····························Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
···········Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
········································Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
···········································Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
·······························Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
···········Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
········Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
····Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
·····Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
···············Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
················································Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
··············Not implemented: HTMLCanvasElement's getContext() method: without installing the canvas npm package
··········································································································································································································································································································································································································································································································································································································································································································································································································································································

 Test Files  98 passed (98)
      Tests  1101 passed (1101)
   Start at  19:32:02
   Duration  73.80s (transform 7.05s, setup 41.13s, import 35.54s, tests 26.08s, environment 93.24s)


```

</details>

---

### E2E - Playwright

- **Comando:** `npx playwright test`
- **Diretório:** `.`
- **Status:** ❌ Falha

<details>
<summary>Ver Logs de Saída</summary>

```text
... (Log truncado - mostrando últimos 20k caracteres) ...
impar(request);
         |                       ^
      42 |     });
      43 |
      44 |     // ========================================================================
        at /app/e2e/cdu-27.spec.ts:41:23


ium] › e2e/cdu-27.spec.ts:76:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 1: ADMIN navega para detalhes do subprocesso
ium] › e2e/cdu-27.spec.ts:88:9 › CDU-27 - Alterar data limite de subprocesso › Cenario 2: ADMIN visualiza botão Alterar data limite
ium] › e2e/cdu-28.spec.ts:32:9 › CDU-28 - Manter atribuição temporária › Cenario 1: ADMIN acessa menu de Unidades
ium] › e2e/cdu-28.spec.ts:32:9 › CDU-28 - Manter atribuição temporária › Cenario 1: ADMIN acessa menu de Unidades

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-28.spec.ts:25:28


ium] › e2e/cdu-28.spec.ts:50:9 › CDU-28 - Manter atribuição temporária › Cenario 2: ADMIN seleciona unidade na árvore
ium] › e2e/cdu-28.spec.ts:80:9 › CDU-28 - Manter atribuição temporária › Cenario 3: Verificar botão de criar atribuição
ium] › e2e/cdu-29.spec.ts:32:9 › CDU-29 - Consultar histórico de processos › Cenario 1: ADMIN navega para página de histórico
ium] › e2e/cdu-29.spec.ts:32:9 › CDU-29 - Consultar histórico de processos › Cenario 1: ADMIN navega para página de histórico

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-29.spec.ts:25:28


ium] › e2e/cdu-29.spec.ts:45:9 › CDU-29 - Consultar histórico de processos › Cenario 2: GESTOR pode acessar histórico
ium] › e2e/cdu-29.spec.ts:56:9 › CDU-29 - Consultar histórico de processos › Cenario 3: CHEFE pode acessar histórico
ium] › e2e/cdu-29.spec.ts:71:9 › CDU-29 - Consultar histórico de processos › Cenario 4: Tabela apresenta colunas corretas
ium] › e2e/cdu-30.spec.ts:31:9 › CDU-30 - Manter Administradores › Cenario 1: ADMIN acessa página de configurações
ium] › e2e/cdu-30.spec.ts:31:9 › CDU-30 - Manter Administradores › Cenario 1: ADMIN acessa página de configurações

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-30.spec.ts:24:28


ium] › e2e/cdu-30.spec.ts:44:9 › CDU-30 - Manter Administradores › Cenario 2: Página de configurações contém seção de administradores
ium] › e2e/cdu-30.spec.ts:73:9 › CDU-30 - Manter Administradores › Cenario 3: Lista de administradores é exibida
ium] › e2e/cdu-31.spec.ts:28:9 › CDU-31 - Configurar sistema › Cenario 1: ADMIN navega para configurações
ium] › e2e/cdu-31.spec.ts:28:9 › CDU-31 - Configurar sistema › Cenario 1: ADMIN navega para configurações

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-31.spec.ts:21:28


ium] › e2e/cdu-31.spec.ts:45:9 › CDU-31 - Configurar sistema › Cenario 2: Tela exibe configurações editáveis
ium] › e2e/cdu-31.spec.ts:65:9 › CDU-31 - Configurar sistema › Cenario 3: ADMIN salva configurações com sucesso
ium] › e2e/cdu-32.spec.ts:50:9 › CDU-32 - Reabrir cadastro › Preparacao 1: Admin cria e inicia processo
ium] › e2e/cdu-32.spec.ts:50:9 › CDU-32 - Reabrir cadastro › Preparacao 1: Admin cria e inicia processo

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-32.spec.ts:38:28

    TypeError: Cannot read properties of undefined (reading 'limpar')

      41 |
      42 |     test.afterAll(async ({request}) => {
    > 43 |         await cleanup.limpar(request);
         |                       ^
      44 |     });
      45 |
      46 |     // ========================================================================
        at /app/e2e/cdu-32.spec.ts:43:23


ium] › e2e/cdu-32.spec.ts:74:9 › CDU-32 - Reabrir cadastro › Preparacao 2: Chefe disponibiliza cadastro
ium] › e2e/cdu-32.spec.ts:94:9 › CDU-32 - Reabrir cadastro › Cenario 1: ADMIN navega para subprocesso disponibilizado
ium] › e2e/cdu-32.spec.ts:105:9 › CDU-32 - Reabrir cadastro › Cenario 2: ADMIN visualiza botão Reabrir cadastro
ium] › e2e/cdu-32.spec.ts:120:9 › CDU-32 - Reabrir cadastro › Cenario 3: ADMIN abre modal de reabertura de cadastro
ium] › e2e/cdu-32.spec.ts:140:9 › CDU-32 - Reabrir cadastro › Cenario 4: Botão confirmar desabilitado sem justificativa
ium] › e2e/cdu-33.spec.ts:50:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 1: Admin cria e inicia processo
ium] › e2e/cdu-33.spec.ts:50:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 1: Admin cria e inicia processo

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-33.spec.ts:38:28

    TypeError: Cannot read properties of undefined (reading 'limpar')

      41 |
      42 |     test.afterAll(async ({request}) => {
    > 43 |         await cleanup.limpar(request);
         |                       ^
      44 |     });
      45 |
      46 |     // ========================================================================
        at /app/e2e/cdu-33.spec.ts:43:23


ium] › e2e/cdu-33.spec.ts:74:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 2: Chefe disponibiliza revisão de cadastro
ium] › e2e/cdu-33.spec.ts:94:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 1: ADMIN navega para subprocesso de revisão
ium] › e2e/cdu-33.spec.ts:104:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 2: ADMIN visualiza botão Reabrir Revisão
ium] › e2e/cdu-33.spec.ts:119:9 › CDU-33 - Reabrir revisão de cadastro › Cenario 3: ADMIN abre modal de reabertura de revisão
ium] › e2e/cdu-34.spec.ts:44:9 › CDU-34 - Enviar lembrete de prazo › Preparacao: Admin cria e inicia processo
ium] › e2e/cdu-34.spec.ts:44:9 › CDU-34 - Enviar lembrete de prazo › Preparacao: Admin cria e inicia processo

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-34.spec.ts:32:28

    TypeError: Cannot read properties of undefined (reading 'limpar')

      35 |
      36 |     test.afterAll(async ({request}) => {
    > 37 |         await cleanup.limpar(request);
         |                       ^
      38 |     });
      39 |
      40 |     // ========================================================================
        at /app/e2e/cdu-34.spec.ts:37:23


ium] › e2e/cdu-34.spec.ts:72:9 › CDU-34 - Enviar lembrete de prazo › Cenario 1: ADMIN navega para detalhes do processo
ium] › e2e/cdu-34.spec.ts:81:9 › CDU-34 - Enviar lembrete de prazo › Cenario 2: Verificar indicadores de prazo
ium] › e2e/cdu-34.spec.ts:98:9 › CDU-34 - Enviar lembrete de prazo › Cenario 3: Verificar opção de enviar lembrete
ium] › e2e/cdu-35.spec.ts:28:9 › CDU-35 - Gerar relatório de andamento › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e/cdu-35.spec.ts:28:9 › CDU-35 - Gerar relatório de andamento › Cenario 1: ADMIN navega para página de relatórios

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-35.spec.ts:21:28


ium] › e2e/cdu-35.spec.ts:38:9 › CDU-35 - Gerar relatório de andamento › Cenario 2: Página exibe card de relatório de andamento
ium] › e2e/cdu-35.spec.ts:48:9 › CDU-35 - Gerar relatório de andamento › Cenario 3: Abrir modal de Andamento Geral
ium] › e2e/cdu-35.spec.ts:62:9 › CDU-35 - Gerar relatório de andamento › Cenario 4: Modal contém tabela de dados
ium] › e2e/cdu-35.spec.ts:77:9 › CDU-35 - Gerar relatório de andamento › Cenario 5: Botão de exportação está disponível
ium] › e2e/cdu-35.spec.ts:88:9 › CDU-35 - Gerar relatório de andamento › Cenario 6: Filtros estão disponíveis
ium] › e2e/cdu-36.spec.ts:28:9 › CDU-36 - Gerar relatório de mapas › Cenario 1: ADMIN navega para página de relatórios
ium] › e2e/cdu-36.spec.ts:28:9 › CDU-36 - Gerar relatório de mapas › Cenario 1: ADMIN navega para página de relatórios

    Error: apiRequestContext.post: connect ECONNREFUSED ::1:5173
    Call log:
      - → POST http://localhost:5173/e2e/reset-database
        - user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.7499.4 Safari/537.36
        - accept: */*
        - accept-encoding: gzip,deflate,br

        at apiRequestContext.post: connect ECONNREFUSED ::1:5173
        at resetDatabase (/app/e2e/hooks/hooks-limpeza.ts:70:36)
        at /app/e2e/cdu-36.spec.ts:21:28


ium] › e2e/cdu-36.spec.ts:38:9 › CDU-36 - Gerar relatório de mapas › Cenario 2: Página exibe card de relatório de mapas
ium] › e2e/cdu-36.spec.ts:48:9 › CDU-36 - Gerar relatório de mapas › Cenario 3: Abrir modal de Mapas Vigentes
ium] › e2e/cdu-36.spec.ts:62:9 › CDU-36 - Gerar relatório de mapas › Cenario 4: Botão de exportação está disponível
[1A[2K  58 failed
    [chromium] › e2e/captura-telas.spec.ts:145:13 › Captura de Telas - Sistema SGC › 02 - Painel Principal › Captura painel GESTOR
    [chromium] › e2e/captura-telas.spec.ts:166:13 › Captura de Telas - Sistema SGC › 02 - Painel Principal › Captura painel CHEFE
    [chromium] › e2e/captura-telas.spec.ts:189:13 › Captura de Telas - Sistema SGC › 03 - Fluxo de Processo › Captura criação e detalhamento de processo
    [chromium] › e2e/captura-telas.spec.ts:229:13 › Captura de Telas - Sistema SGC › 03 - Fluxo de Processo › Captura validações de formulário
    [chromium] › e2e/captura-telas.spec.ts:263:13 › Captura de Telas - Sistema SGC › 04 - Subprocesso e Atividades › Captura fluxo completo de atividades
    [chromium] › e2e/captura-telas.spec.ts:331:13 › Captura de Telas - Sistema SGC › 04 - Subprocesso e Atividades › Captura estados de validação inline de atividades
    [chromium] › e2e/captura-telas.spec.ts:432:13 › Captura de Telas - Sistema SGC › 05 - Mapa de Competências › Captura fluxo de mapa de competências
    [chromium] › e2e/captura-telas.spec.ts:545:13 › Captura de Telas - Sistema SGC › 06 - Navegação e Menus › Captura elementos de navegação
    [chromium] › e2e/captura-telas.spec.ts:581:13 › Captura de Telas - Sistema SGC › 07 - Estados e Situações › Captura diferentes estados de processo
    [chromium] › e2e/captura-telas.spec.ts:621:13 › Captura de Telas - Sistema SGC › 08 - Responsividade (Tamanhos de Tela) › Captura em diferentes resoluções
    [chromium] › e2e/captura-telas.spec.ts:647:13 › Captura de Telas - Sistema SGC › 09 - Operações em Bloco › Captura fluxo de aceitar cadastros em bloco
    [chromium] › e2e/captura-telas.spec.ts:746:13 › Captura de Telas - Sistema SGC › 10 - Gestão de Subprocessos › Captura modais de gestão de subprocesso
    [chromium] › e2e/captura-telas.spec.ts:804:13 › Captura de Telas - Sistema SGC › 11 - Gestão de Unidades › Captura página de unidades e atribuição temporária
    [chromium] › e2e/captura-telas.spec.ts:847:13 › Captura de Telas - Sistema SGC › 12 - Histórico › Captura seção de histórico
    [chromium] › e2e/captura-telas.spec.ts:871:13 › Captura de Telas - Sistema SGC › 13 - Configurações › Captura página de configurações e administradores
    [chromium] › e2e/captura-telas.spec.ts:909:13 › Captura de Telas - Sistema SGC › 14 - Relatórios › Captura página e modais de relatórios
    [chromium] › e2e/cdu-01.spec.ts:9:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir erro com credenciais inválidas
    [chromium] › e2e/cdu-01.spec.ts:14:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve realizar login com sucesso (Perfil Único)
    [chromium] › e2e/cdu-01.spec.ts:22:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir seleção de perfil se houver múltiplos
    [chromium] › e2e/cdu-01.spec.ts:34:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir barra de navegação após login
    [chromium] › e2e/cdu-01.spec.ts:46:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir informações do usuário e controles
    [chromium] › e2e/cdu-01.spec.ts:60:9 › CDU-01 - Realizar login e exibir estrutura das telas › Deve exibir rodapé
    [chromium] › e2e/cdu-02.spec.ts:23:13 › CDU-02 - Visualizar Painel › Como ADMIN › Deve exibir seções de Processos e Alertas
    [chromium] › e2e/cdu-03.spec.ts:21:9 › CDU-03 - Manter Processo › Deve validar campos obrigatórios
    [chromium] › e2e/cdu-04.spec.ts:19:9 › CDU-04 - Iniciar processo de mapeamento › Deve iniciar um processo com sucesso
    [chromium] › e2e/cdu-05.spec.ts:100:9 › CDU-05 - Iniciar processo de revisao › Fase 1.1: ADMIN cria e inicia processo de Mapeamento
    [chromium] › e2e/cdu-06.spec.ts:16:9 › CDU-06 - Detalhar processo › Deve exibir detalhes do processo para ADMIN
    [chromium] › e2e/cdu-07.spec.ts:18:9 › CDU-07 - Detalhar subprocesso › Deve exibir detalhes do subprocesso para CHEFE
    [chromium] › e2e/cdu-08.spec.ts:18:9 › CDU-08 - Manter cadastro de atividades e conhecimentos › Cenário 1: Processo de Mapeamento (Fluxo Completo + Importação)
    [chromium] › e2e/cdu-08.spec.ts:84:9 › CDU-08 - Manter cadastro de atividades e conhecimentos › Cenário 2: Processo de Revisão (Botão Impacto)
    [chromium] › e2e/cdu-09.spec.ts:31:9 › CDU-09 - Disponibilizar cadastro de atividades e conhecimentos › Preparacao: Admin cria e inicia processo
    [chromium] › e2e/cdu-10.spec.ts:47:9 › CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-11.spec.ts:45:9 › CDU-11 - Visualizar cadastro de atividades e conhecimentos › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-12.spec.ts:47:9 › CDU-12 - Verificar impactos no mapa de competências › Preparacao 1: Setup Mapeamento (Atividades, Competências, Homologação)
    [chromium] › e2e/cdu-13.spec.ts:53:9 › CDU-13 - Analisar cadastro de atividades e conhecimentos › Preparacao 1: ADMIN cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-14.spec.ts:59:9 › CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos › Preparacao 0.1: ADMIN cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-15.spec.ts:56:9 › CDU-15 - Manter mapa de competências › Preparacao: Criar processo e homologar cadastro de atividades
    [chromium] › e2e/cdu-16.spec.ts:57:9 › CDU-16 - Ajustar mapa de competências › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-17.spec.ts:41:9 › CDU-17 - Disponibilizar mapa de competências › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-18.spec.ts:23:9 › CDU-18: Visualizar mapa de competências › Cenário 1: ADMIN visualiza mapa via detalhes do processo
    [chromium] › e2e/cdu-19.spec.ts:40:9 › CDU-19 - Validar mapa de competências › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-20.spec.ts:48:9 › CDU-20 - Analisar validação de mapa de competências › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-21.spec.ts:48:9 › CDU-21 - Finalizar processo de mapeamento ou de revisão › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-22.spec.ts:55:9 › CDU-22 - Aceitar cadastros em bloco › Preparacao 1: Admin cria e inicia processo de mapeamento
    [chromium] › e2e/cdu-23.spec.ts:53:9 › CDU-23 - Homologar cadastros em bloco › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-24.spec.ts:54:9 › CDU-24 - Disponibilizar mapas em bloco › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-25.spec.ts:63:9 › CDU-25 - Aceitar validação de mapas em bloco › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-26.spec.ts:60:9 › CDU-26 - Homologar validação de mapas em bloco › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-27.spec.ts:48:9 › CDU-27 - Alterar data limite de subprocesso › Preparacao: Admin cria e inicia processo
    [chromium] › e2e/cdu-28.spec.ts:32:9 › CDU-28 - Manter atribuição temporária › Cenario 1: ADMIN acessa menu de Unidades
    [chromium] › e2e/cdu-29.spec.ts:32:9 › CDU-29 - Consultar histórico de processos › Cenario 1: ADMIN navega para página de histórico
    [chromium] › e2e/cdu-30.spec.ts:31:9 › CDU-30 - Manter Administradores › Cenario 1: ADMIN acessa página de configurações
    [chromium] › e2e/cdu-31.spec.ts:28:9 › CDU-31 - Configurar sistema › Cenario 1: ADMIN navega para configurações
    [chromium] › e2e/cdu-32.spec.ts:50:9 › CDU-32 - Reabrir cadastro › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-33.spec.ts:50:9 › CDU-33 - Reabrir revisão de cadastro › Preparacao 1: Admin cria e inicia processo
    [chromium] › e2e/cdu-34.spec.ts:44:9 › CDU-34 - Enviar lembrete de prazo › Preparacao: Admin cria e inicia processo
    [chromium] › e2e/cdu-35.spec.ts:28:9 › CDU-35 - Gerar relatório de andamento › Cenario 1: ADMIN navega para página de relatórios
    [chromium] › e2e/cdu-36.spec.ts:28:9 › CDU-36 - Gerar relatório de mapas › Cenario 1: ADMIN navega para página de relatórios
  180 did not run
  2 passed (1.8m)

```

</details>

---

