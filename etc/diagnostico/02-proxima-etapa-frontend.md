# Relatório 02 — Próxima Etapa: Pré-requisitos para o Frontend de Diagnóstico

> **Objetivo:** Identificar o que precisa estar pronto no backend e na integração antes que o frontend de
> diagnóstico seja implementado com segurança.

---

## 1. Contexto

O backend tem o módulo `sgc.diagnostico` criado com controllers, services e repositórios. O frontend possui cards
de navegação no `SubprocessoCards.vue` que apontam para três rotas (`AutoavaliacaoDiagnostico`,
`OcupacoesCriticasDiagnostico`, `MonitoramentoDiagnostico`) que **ainda não existem**. Antes de construir as
views, é necessário consolidar a base.

---

## 2. Pré-requisitos Obrigatórios

### 2.1 Criação automática do `Diagnostico` na iniciação do processo

**Problema:** O `ProcessoService` já inicia subprocessos em `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, mas não há
evidência de que o registro `Diagnostico` e as `AvaliacaoServidor` iniciais sejam criados automaticamente. Se o
registro não existir, todos os endpoints `/api/diagnosticos/subprocessos/{id}/*` retornarão 404.

**O que fazer:**
- Verificar se existe um listener/event ou chamada no `SubprocessoService` que cria o `Diagnostico` ao criar o subprocesso de diagnóstico.
- Se não existir, adicionar em `SubprocessoService` (ou criar `DiagnosticoInicializacaoService`) a lógica de:
  1. Criar `Diagnostico` com `situacao = EM_ANDAMENTO` vinculado ao subprocesso.
  2. Para cada servidor da unidade (via `OrganizacaoService`), para cada competência do mapa copiado, criar um registro `AvaliacaoServidor` com `situacaoServidor = AUTOAVALIACAO_NAO_REALIZADA`.
  3. Para cada competência crítica (se definida), criar o registro `OcupacaoCritica` com `situacaoCapacitacao = null`.

**Impacto:** Sem isso, o frontend não consegue carregar nenhuma tela de diagnóstico.

---

### 2.2 Endpoint de monitoramento agregado (visão gestor/admin)

**Problema:** O DTO `DiagnosticoMonitoramentoDto` existe mas não tem endpoint correspondente. O `SubprocessoCards.vue`
já navega para a rota `MonitoramentoDiagnostico`, que precisará exibir **todas as unidades** do processo com sua
situação e localização atual.

**O que fazer:**
- Adicionar ao `DiagnosticoController`:
  ```
  GET /api/diagnosticos/processos/{codProcesso}/monitoramento
  ```
  Com `@PreAuthorize("hasPermission(#codProcesso, 'Processo', 'VISUALIZAR_DIAGNOSTICO')")` e retorno de
  `DiagnosticoMonitoramentoDto`.
- Implementar `DiagnosticoConsultaService.obterMonitoramento(Long codProcesso)` agregando todos os subprocessos
  do processo com tipo `DIAGNOSTICO`.

---

### 2.3 Endpoint de validação em bloco (gestor)

**Problema:** O DTO `ValidarDiagnosticosEmBlocoRequest` existe mas não tem endpoint. O plano previa ação de
"validar em bloco para o gestor".

**O que fazer:**
- Adicionar ao `DiagnosticoController`:
  ```
  POST /api/diagnosticos/processos/{codProcesso}/validar-em-bloco
  ```
- Implementar em `DiagnosticoFluxoService` iterando sobre os subprocessos selecionados e chamando
  `validarDiagnostico` para cada um.

---

### 2.4 Corrigir tipo da chave no `AvaliacaoServidorRepo`

**Problema:** `AvaliacaoServidorRepo extends JpaRepository<AvaliacaoServidor, Integer>` mas a `EntidadeBase`
usa `Long` como tipo da chave primária. Isso causa erro de tipo em tempo de execução ao usar `findById`.

**O que fazer:**
- Alterar para `JpaRepository<AvaliacaoServidor, Long>`.

---

### 2.5 Expor permissões estruturadas do diagnóstico para o frontend

**Problema:** O frontend precisa saber quais ações o usuário pode executar em determinado subprocesso sem
reconstruir perfis. Hoje o contexto do subprocesso expõe permissões via `SubprocessoContexto`, mas as ações
de diagnóstico (`PREENCHER_AUTOAVALIACAO`, `CRIAR_CONSENSO`, `CONCLUIR_DIAGNOSTICO`, etc.) ainda não estão
no contrato de resposta.

**O que fazer:**
- Verificar se `DiagnosticoContextoDto` já inclui flags de permissão ou se é necessário adicionar um campo
  `PermissoesDiagnosticoDto` com booleanos para cada ação relevante:
  ```json
  {
    "podePreencher": true,
    "podeCriarConsenso": false,
    "podeConcluir": false,
    "podeValidar": false,
    "podeDevolver": false,
    "podeHomologar": false
  }
  ```
- Alternativa: enriquecer o endpoint `GET /contexto` para já retornar essas flags consultando o
  `SgcPermissionEvaluator`.

---

### 2.6 Definir tipos TypeScript de diagnóstico de competências

**Problema:** O arquivo `frontend/src/types/diagnostico.ts` atual contém apenas tipos de
`DiagnosticoOrganizacional` (diagnóstico organizacional/estrutural), **não** os tipos do módulo de
diagnóstico de competências.

**O que fazer:**
- Criar ou enriquecer `diagnostico.ts` com as interfaces que espelham os DTOs do backend:
  ```typescript
  export interface DiagnosticoContexto { ... }
  export interface Autoavaliacao { ... }
  export interface Consenso { ... }
  export interface DiagnosticoEquipe { ... }
  export interface DiagnosticoUnidade { ... }
  export interface OcupacaoCritica { ... }
  export type SituacaoAvaliacaoServidor =
    | 'AUTOAVALIACAO_NAO_REALIZADA'
    | 'AUTOAVALIACAO_CONCLUIDA'
    | 'CONSENSO_CRIADO'
    | 'CONSENSO_APROVADO'
    | 'AVALIACAO_IMPOSSIBILITADA';
  export type SituacaoCapacitacao = 'NA' | 'AC' | 'EC' | 'C' | 'I';
  ```

---

### 2.7 Criar o service HTTP de diagnóstico no frontend

**Problema:** Não há `diagnosticoService.ts` em `frontend/src/services/`. O frontend não tem nenhuma função
para chamar os endpoints `/api/diagnosticos/*`.

**O que fazer:**
- Criar `frontend/src/services/diagnosticoService.ts` com funções para todos os endpoints:
  ```typescript
  export async function obterContextoDiagnostico(codSubprocesso: number): Promise<DiagnosticoContexto>
  export async function obterAutoavaliacao(codSubprocesso: number): Promise<Autoavaliacao>
  export async function salvarAutoavaliacao(codSubprocesso: number, request: AutoavaliacaoRequest): Promise<void>
  export async function concluirAutoavaliacao(codSubprocesso: number): Promise<void>
  export async function salvarConsenso(codSubprocesso: number, servidorTitulo: string, request: ConsensoRequest): Promise<void>
  export async function obterConsenso(codSubprocesso: number): Promise<Consenso>
  export async function aprovarConsenso(codSubprocesso: number): Promise<void>
  export async function impossibilitarAvaliacao(codSubprocesso: number, servidorTitulo: string, request: JustificativaRequest): Promise<void>
  export async function obterEquipe(codSubprocesso: number): Promise<DiagnosticoEquipe>
  export async function salvarOcupacoesCriticas(codSubprocesso: number, request: OcupacoesCriticasRequest): Promise<void>
  export async function obterDiagnosticoUnidade(codSubprocesso: number): Promise<DiagnosticoUnidade>
  export async function concluirDiagnostico(codSubprocesso: number): Promise<void>
  export async function validarDiagnostico(codSubprocesso: number, observacoes?: string): Promise<void>
  export async function devolverDiagnostico(codSubprocesso: number, justificativa: string): Promise<void>
  export async function homologarDiagnostico(codSubprocesso: number, observacoes?: string): Promise<void>
  ```

---

### 2.8 Registrar rotas de diagnóstico no router

**Problema:** `processo.routes.ts` não contém as rotas `AutoavaliacaoDiagnostico`, `OcupacoesCriticasDiagnostico`
e `MonitoramentoDiagnostico`. Os cards já navegam para essas rotas, mas o router não as conhece.

**O que fazer:**
- Criar `frontend/src/router/diagnostico.routes.ts` ou adicionar em `processo.routes.ts` as entradas:
  ```typescript
  { path: '/diagnostico/:codSubprocesso/:siglaUnidade/autoavaliacao', name: 'AutoavaliacaoDiagnostico', ... }
  { path: '/diagnostico/:codSubprocesso/:siglaUnidade/ocupacoes-criticas', name: 'OcupacoesCriticasDiagnostico', ... }
  { path: '/diagnostico/:codSubprocesso/:siglaUnidade/monitoramento', name: 'MonitoramentoDiagnostico', ... }
  ```
- Registrar no `router/index.ts`.

---

## 3. Verificação de Pré-requisitos

Antes de iniciar o frontend, execute os seguintes checks:

```bash
# 1. Backend compila sem erros
./gradlew :backend:compileJava

# 2. Testes de backend passam (incluindo diagnóstico)
./gradlew :backend:test

# 3. Typecheck do frontend passa
npm run typecheck

# 4. Lint do frontend passa
npm run lint
```

Se os checks passarem e os itens 2.1 a 2.8 estiverem resolvidos, o frontend pode ser implementado com segurança.

---

## 4. Resumo de Prioridades

| Prioridade | Item | Impacto |
|---|---|---|
| 🔴 Crítico | 2.1 — Criação do Diagnostico na iniciação | Sem isso nenhum endpoint funciona |
| 🔴 Crítico | 2.4 — Tipo da chave no AvaliacaoServidorRepo | Erro de runtime em produção |
| 🟠 Alto | 2.5 — Permissões estruturadas no contexto | Interface reativa e segura no frontend |
| 🟠 Alto | 2.6 — Tipos TypeScript de diagnóstico | Contrato tipado para os composables |
| 🟠 Alto | 2.7 — Service HTTP de diagnóstico | Camada de acesso aos dados |
| 🟠 Alto | 2.8 — Rotas de diagnóstico no router | Navegação funcional a partir dos cards |
| 🟡 Médio | 2.2 — Endpoint de monitoramento | View de acompanhamento do gestor |
| 🟡 Médio | 2.3 — Validação em bloco | Ação de produtividade do gestor |
