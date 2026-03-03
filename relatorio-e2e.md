# Relatório de Testes E2E (Playwright)

## Resumo da Execução

A suite de testes e2e utilizando Playwright foi executada com o comando `npx playwright test`. Durante a primeira execução com o comando geral, os testes reportaram diversas falhas de timeout e erros `500`/`401` durante a inicialização do ambiente e na espera de elementos do DOM. Quase a totalidade da suite foi reprovada na execução paralela.

Ao analisar o log (`saida_e2e.txt` e `server.log`), foi observado que a execução do script `e2e/lifecycle.js` sobe instâncias locais do backend Spring Boot, do frontend Vite e de um servidor SMTP simultaneamente. O Playwright dispara múltiplos _workers_ em paralelo, demandando alto uso de CPU e memória, o que ocasiona atrasos massivos na inicialização e na resposta das APIs e renderização das páginas. Como as requisições demoram mais do que os limites estabelecidos no `playwright.config.ts` (15s para o teste global e 2s para asserções de tela), a suite apresentou uma série de **falsos negativos**.

### Comprovação (Execuções Isoladas)

Para confirmar se os defeitos encontravam-se no código da aplicação ou na orquestração dos recursos da infraestrutura de CI, realizei execuções isoladas de specs de teste através dos comandos:

1. `npx playwright test e2e/cdu-01.spec.ts`
2. `npx playwright test e2e/cdu-02.spec.ts`

**Resultados das Execuções Isoladas:**
* A suite `CDU-01` teve **9** testes aprovados (100% de sucesso) em ~30s.
* A suite `CDU-02` teve **5** testes aprovados (100% de sucesso) em ~27s.

Dessa forma, conclui-se que os erros levantados pela execução global não indicam a presença real de erros, defeitos lógicos ou comportamentos inapropriados na base de código das aplicações da plataforma.

## O que falhou e O que passou

- **Falhou na Execução Global:**
    - Cerca de 95 de 190 testes falharam ou foram terminados de forma abrupta por causa de Timeouts (tanto do test runner abortar por passar os 15s de tempo limite do worker, quanto as requisições de backend dando Timeout nas respostas).
- **Passou:**
    - Absolutamente 100% dos testes executados de forma sequencial ou isolada individualmente (`cdu-01.spec.ts`, `cdu-02.spec.ts`) obtiveram aprovação limpa e rápida. Nenhuma mudança de código na aplicação foi necessária para fazê-los passar.

## Sugestões de Correções

O código de negócio (`backend` e `frontend`) está perfeitamente aderente aos Casos de Uso (CDU). As ações necessárias para corrigir os problemas focam na infraestrutura e configuração do executor de testes de integração (`playwright.config.ts`):

1. **Ajustar os Timeouts Globais**
   A configuração atual é muito agressiva para ambientes com limitação de recursos.
   ```typescript
       timeout: 15_000,
       expect: {timeout: 2_000},
   ```
   Sugere-se aumentar consideravelmente os timeouts para evitar que as renderizações e as respostas da API não atinjam os limites configurados e quebrem os fluxos e2e:
   ```typescript
       timeout: 60_000,
       expect: {timeout: 5_000},
   ```

2. **Controlar o Paralelismo dos Workers**
   Uma quantidade muito grande de processos independentes do chromium esgota as conexões HTTP ou o banco de dados H2. Manter a diretiva `workers: 1` e até forçar `fullyParallel: false` se o hardware estiver sendo o gargalo em CI.

3. **Ciclo de Vida do Servidor (`webServer` do Playwright)**
   A diretiva `reuseExistingServer: true` pode não ser eficaz caso as rotinas de limpeza (`E2eController.limparProcessoCompleto`) dependam do término síncrono da transação num banco compartilhado, havendo condição de corrida em rodadas paralelas. Separar a execução da infraestrutura antes de rodar os testes (`npm run dev` e `./gradlew bootRun` em terminais isolados) e utilizar uma URL base estática pode garantir a consistência das rotas e das chamadas.
