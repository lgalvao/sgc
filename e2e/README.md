# Testes End-to-End (E2E)

## Papel da suíte

`e2e/` contém a suíte Playwright do SGC. Ela valida fluxos completos do produto sobre backend e frontend reais, com apoio de fixtures de banco, autenticação e endpoints auxiliares do perfil `e2e`.

A suíte é orientada por **casos de uso** e não por telas isoladas: por isso a maioria dos arquivos segue a convenção `cdu-XX.spec.ts`.

## Arquitetura da execução

```mermaid
graph LR
    Playwright --> Lifecycle[e2e/lifecycle.js]
    Lifecycle --> Backend[Spring Boot perfil e2e ou hom]
    Lifecycle --> Frontend[Vite]
    Lifecycle --> SMTP[SMTP local]
    Playwright --> Fixtures[fixtures/]
    Playwright --> Helpers[helpers/]
    Backend --> E2EApi[/e2e/*]
```

## Estrutura do diretório

| Caminho | Papel |
|---|---|
| `cdu-*.spec.ts` | cenários alinhados aos casos de uso do sistema |
| `jornada*.spec.ts` | jornadas transversais e cenários mais amplos |
| `feedback-control.spec.ts` | fluxo do widget/controle de feedback |
| `captura.spec.ts` | suporte a capturas específicas |
| `helpers/` | operações semânticas reutilizáveis |
| `fixtures/` | extensões do Playwright para autenticação, banco e preparação de estado |
| `hooks/` | limpeza/preparação complementar |
| `setup/` | `seed.sql` e artefatos de inicialização |
| `lifecycle.js` | sobe backend, frontend e SMTP locais |
| `README.md` | visão do módulo |

## Organização dos helpers

Helpers especializados em `helpers/`:

- `helpers-auth.ts`: login, usuários e escolha de perfil
- `helpers-navegacao.ts`: navegação e limpeza de notificações
- `helpers-processos.ts`: criação e inspeção de processos/subprocessos
- `helpers-atividades.ts`: cadastro e manutenção de atividades/conhecimentos
- `helpers-mapas.ts`: navegação e mutações no mapa
- `helpers-analise.ts`: aceite, devolução, homologação e histórico

A regra prática é: **o teste descreve o cenário; o helper executa os detalhes da UI**.

## Fixtures e preparação de estado

`fixtures/` oferece suporte para reduzir repetição e custo estrutural:

- `auth-fixtures.ts`: autenticação pronta por perfil/unidade
- `database-fixtures.ts`: preparo/limpeza de base
- `fixtures-processos.ts` e `processo-fixtures.ts`: criação de processos/estado inicial
- `base.ts` e `complete-fixtures.ts`: composição do ambiente de teste

O backend ainda expõe `/e2e/fixtures/*` para **state-jumping** quando um cenário precisa começar em estado profundo sem percorrer toda a UI.

## Lifecycle local

`lifecycle.js` é a peça central da suíte.

Ele:

- sobe backend via `gradlew bootRun -PENV=e2e|hom`;
- sobe frontend via `npm exec -- vite`;
- cria um SMTP local para testes de e-mail;
- verifica portas e, em `e2e`, pode reutilizar backend existente quando configurado;
- permite ligar monitoramento de lentidão por variável de ambiente.

Variáveis relevantes:

- `SGC_PERFIL=e2e|hom`
- `SGC_MONITORAMENTO=sim|nao`
- `SGC_LIFECYCLE_REUTILIZAR_EXISTENTE=on|off`
- `E2E_BACKEND_BASE_PORT`
- `E2E_FRONTEND_PORT`
- `E2E_SMTP_PORT`

## Modos de execução

### Suíte padrão

```bash
npm run test:e2e
```

Por padrão, `playwright.config.ts`:

- usa `workers: 1`;
- sobe a infra via `node e2e/lifecycle.js`;
- roda em `chromium-headless-shell`;
- considera `http://localhost:5173` como baseURL.

### Execuções úteis

```bash
npx playwright test --ui
npx playwright test e2e/cdu-01.spec.ts
node e2e/lifecycle.js
```

## Perfil `hom`

O lifecycle também aceita `SGC_PERFIL=hom` para subir a aplicação apontando para homologação/local hom, mas sem usar a suíte funcional padrão.

Nesse modo:

- não se deve depender de `/e2e/*`;
- não se deve confiar em `seed.sql`;
- o objetivo é observação/manualidade controlada, não a execução da suíte oficial.

## Como os testes se conectam ao backend

O backend fornece suporte dedicado em `backend/src/main/java/sgc/e2e`:

- `E2eController`
- `E2eSecurityConfig`
- endpoints de reset/limpeza
- endpoints de fixtures para estados complexos

Veja também: [Suporte E2E no backend](../backend/src/main/java/sgc/e2e/README.md).

## Princípios de manutenção da suíte

- preferir `data-testid` a seletores frágeis;
- evitar `waitForTimeout()` em testes funcionais;
- usar fixtures para preparar estado repetitivo, não para esconder comportamento;
- respeitar cenários seriais quando o arquivo assim exigir;
- investigar causa raiz de falhas em vez de introduzir resiliência artificial.

As regras detalhadas estão em [../etc/docs/regras-e2e.md](../etc/docs/regras-e2e.md).

## Referências

- [README raiz](../README.md)
- [Backend do SGC](../backend/README.md)
- [Suporte E2E no backend](../backend/src/main/java/sgc/e2e/README.md)
- [Regras E2E](../etc/docs/regras-e2e.md)
