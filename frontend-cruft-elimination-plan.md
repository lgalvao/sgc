# Plano definitivo de eliminacao de cruft do frontend

## Objetivo

Eliminar complexidade acidental do frontend de forma **sistematica, mensuravel e replicavel**, para que o repositorio deixe de depender de rodadas heroicas de limpeza.

O alvo nao e apenas "refatorar arquivos grandes". O alvo e criar um circuito permanente que:

1. detecta cruft cedo;
2. prioriza o que mais importa;
3. valida que a simplificacao nao quebrou comportamento;
4. impede regressao estrutural em novos PRs.

## Principios de trabalho

1. **Sem compatibilidade artificial.** Nao manter aliases, mapeadores publicos falsos, APIs "so para teste" ou fallbacks herdados sem contrato real no backend.
2. **Fail-fast no frontend.** Erros irrecuperaveis de backend nao devem gerar branches locais excessivos.
3. **Menor fronteira segura primeiro.** Cortar duplicacao interna, exports mortos, setters artificiais, estado espelho e remapeamentos redundantes antes de mexer em contratos maiores.
4. **Budget por camada.** Services, stores, composables e views precisam ter limites claros de tamanho e superficie.
5. **Ratchet, nao big bang.** O gate inicial deve impedir regressao e reduzir o baseline aos poucos, com metas progressivas.

## Baseline atual

Levantamento desta rodada:

### Auditorias existentes

- `node etc/scripts/sgc.js codigo smells auditar --json`
  - pontuacao total do repo: **1660** (`critico`)
  - pontuacao frontend producao: **74**
  - `frontend_any_producao`: **1**
  - `frontend_null_checks`: **25**
  - `frontend_fallback_or`: **20**
- `node etc/scripts/sgc.js frontend cobertura auditoria --json`
  - linhas: **92.04%**
  - branches: **84.35%**
  - functions: **89.48%**

### Sinais estruturais do frontend

- **2** services de producao acima de 150 linhas
- **3** stores de producao acima de 120 linhas
- **4** composables de producao acima de 140 linhas
- **9** views acima de 300 linhas
- **67** fallbacks defensivos (`||`/`??` com valores default) em producao
- **68** blocos `catch` no frontend

### Hotspots atuais por tamanho

#### Services

- `frontend/src/services/subprocessoService.ts` — **396**
- `frontend/src/services/processoService.ts` — **193**
- `frontend/src/services/diagnosticoService.ts` — **122**

#### Stores

- `frontend/src/stores/subprocesso.ts` — **275**
- `frontend/src/stores/mapas.ts` — **183**
- `frontend/src/stores/perfil.ts` — **154**

#### Views

- `frontend/src/views/MapaView.vue` — **813**
- `frontend/src/views/CadastroView.vue` — **706**
- `frontend/src/views/SubprocessoView.vue` — **595**
- `frontend/src/views/NotificacoesAdminView.vue` — **489**
- `frontend/src/views/ProcessoCadastroView.vue` — **469**
- `frontend/src/views/ProcessoDetalheView.vue` — **367**
- `frontend/src/views/LoginView.vue` — **339**
- `frontend/src/views/AtribuicaoTemporariaView.vue` — **311**
- `frontend/src/views/RelatorioAndamentoView.vue` — **309**

#### Composables

- `frontend/src/composables/useAcesso.ts` — **187**
- `frontend/src/composables/useCadastroAtividadesMutacoes.ts` — **176**
- `frontend/src/composables/useFluxoSubprocesso.ts` — **173**
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts` — **153**
- `frontend/src/composables/useBreadcrumbs.ts` — **140**

## O que passa a contar como cruft

O circuito deve marcar como cruft qualquer item abaixo em **codigo de producao**:

1. arquivos acima do budget da camada;
2. remapeamento redundante de DTO para o mesmo shape ja garantido pelo backend;
3. exports usados apenas por testes ou pelo proprio arquivo;
4. store que apenas espelha service ou storage;
5. setter publico sem consumidor real de producao;
6. estado global usado como ponte implicita entre telas;
7. `catch` que apenas engole erro, loga e segue, ou devolve shape de sucesso falso;
8. fallbacks defensivos (`|| []`, `?? ""`, `?? 0`, `?? {}`) sem contrato justificavel;
9. `any`, casts em cascata e tipos frouxos para contratos estaveis;
10. view que mistura orquestracao de fluxo, regras de acesso, formatacao e markup grande;
11. testes que sustentam APIs legadas, mocks falsos ou comportamento que ja nao existe em producao.

## Solucao definitiva

A solucao deve combinar **auditoria automatica + budgets + waivers temporarios + loop de execucao + gate de regressao**.

### 1. Criar uma auditoria dedicada de cruft do frontend

Adicionar ao toolkit:

- `node etc/scripts/sgc.js frontend cruft auditar`

Script proposto:

- arquivo: `etc/scripts/frontend/cruft-auditar.js`
- saidas:
  - `etc/qualidade/frontend-cruft/latest/ultimo-snapshot.json`
  - `etc/qualidade/frontend-cruft/latest/ultimo-resumo.md`
- modo `--json`
- modo `--sem-gravar`

Esse script deve calcular, por arquivo e por camada:

1. linhas por arquivo;
2. quantidade de `catch`;
3. quantidade de fallbacks defensivos;
4. quantidade de checks explicitos de `null`;
5. quantidade de `any`/casts problemáticos;
6. numero de exports publicos sem consumidor de producao;
7. numero de stores/composables/services acima do budget;
8. numero de acessos diretos a storage fora dos wrappers permitidos;
9. numero de logs em fluxos que deveriam falhar explicitamente;
10. numero de mocks/testes que referenciam API inexistente em producao.

### 2. Criar um validador de budgets e regressao

Adicionar ao toolkit:

- `node etc/scripts/sgc.js frontend cruft validar`

Script proposto:

- arquivo: `etc/scripts/frontend/cruft-validar.js`
- entrada:
  - snapshot atual
  - arquivo de budget
  - arquivo de waivers
- saida:
  - exit `1` se houver regressao ou violacao nao autorizada

Arquivos de configuracao propostos:

- `etc/qualidade/frontend-cruft-budget.json`
- `etc/qualidade/frontend-cruft-waivers.json`

### 3. Introduzir budget por camada com ratchet

Os budgets precisam ter dois niveis:

1. **target**: tamanho desejado;
2. **hard limit**: acima disso, o arquivo so entra com waiver temporario.

Proposta inicial:

| Camada | Target | Hard limit |
|---|---:|---:|
| service | 120 | 180 |
| store | 100 | 150 |
| composable | 120 | 170 |
| view | 250 | 350 |
| component | 180 | 260 |

Como o baseline atual ja viola parte disso, o gate inicial deve seguir esta ordem:

1. nenhum arquivo novo pode nascer acima do `target`;
2. nenhum arquivo existente pode crescer se ja estiver acima do `target`;
3. nenhum arquivo pode cruzar o `hard limit` sem waiver;
4. a cada rodada de limpeza, o waiver precisa cair ou o arquivo precisa encolher.

### 4. Usar waivers com prazo e dono

O arquivo `frontend-cruft-waivers.json` deve registrar:

- arquivo;
- regra violada;
- justificativa curta;
- responsavel;
- criterio objetivo de remocao.

Waiver sem responsavel ou sem criterio de remocao nao vale.

### 5. Integrar o frontend cruft ao loop oficial de QA

Integracoes obrigatorias:

1. `etc/scripts/sgc.js`
   - adicionar grupo `frontend cruft`
2. `qa snapshot coletar`
   - incluir a auditoria de cruft no perfil `frontend` e no `completo`
3. `qa resumo`
   - mostrar score de cruft e top hotspots frontend
4. `frontendQualityCheck` / `qualityCheckFast`
   - adicionar validacao de cruft
5. `package.json`
   - adicionar atalhos legiveis, por exemplo:
     - `frontend:cruft`
     - `frontend:cruft:check`

## Heuristicas que a auditoria precisa implementar

### A. Superficie larga demais

- service/store/composable/view acima do budget;
- numero excessivo de funcoes exportadas;
- numero excessivo de dependencias importadas;
- arquivo que mistura leitura, mutacao, navegacao e renderizacao.

### B. Duplicacao de contrato

- mappers exportados usados so por testes;
- conversao de shape backend -> frontend sem mudar semantica;
- tipos locais que repetem DTO estavel sem agregar contrato.

### C. Defensividade acidental

- `?? []`, `?? ""`, `?? 0`, `|| []`, `|| false`, `|| {}`;
- `catch` com retorno "seguro" em vez de falha explicita;
- `if (!x) return` sem notificacao/log consistente em fluxos criticos.

### D. Estado implicito

- stores usadas como cache global para uma unica tela;
- composables que escrevem em multiplas stores;
- dependencias de `localStorage`/`sessionStorage` fora de wrappers centrais.

### E. Teste sustentando legado

- spec que depende de export morto;
- mock de API que a app nao usa mais;
- teste que prova fallback que o backend nao permite.

## Loop de execucao padrao

Toda rodada de limpeza deve seguir este fluxo:

1. `node etc/scripts/sgc.js projeto doctor`
2. `node etc/scripts/sgc.js frontend cruft auditar --json`
3. selecionar **um hotspot por fronteira**
   - service
   - store
   - composable
   - view
4. simplificar a menor fronteira segura
5. rodar testes focados dos consumidores afetados
6. rodar:
   - `npm run typecheck`
   - `npm run lint`
7. rodar novamente:
   - `node etc/scripts/sgc.js frontend cruft auditar --json`
   - `node etc/scripts/sgc.js frontend cruft validar`
8. registrar delta no snapshot/relatorio

## Politica de PR

Todo PR de frontend deve obedecer a pelo menos uma das regras abaixo:

1. nao aumentar score de cruft;
2. nao aumentar numero de arquivos acima do budget;
3. nao introduzir novo waiver;
4. se tocar em hotspot grande, reduzir ao menos um sinal estrutural mensuravel.

PR que mexe em arquivo com waiver deve:

1. reduzir a divida registrada; ou
2. atualizar o waiver com justificativa melhor e criterio de remocao mais estrito.

## Ordem de ataque recomendada

### Fase 0 — Instrumentacao

1. criar `frontend cruft auditar`
2. criar `frontend cruft validar`
3. criar budget e waivers
4. integrar em `qa snapshot coletar` e `frontendQualityCheck`

### Fase 1 — Reducao dos maiores hotspots

1. `frontend/src/views/MapaView.vue`
2. `frontend/src/views/CadastroView.vue`
3. `frontend/src/views/SubprocessoView.vue`
4. `frontend/src/stores/subprocesso.ts`
5. `frontend/src/services/subprocessoService.ts`
6. `frontend/src/stores/mapas.ts`
7. `frontend/src/views/ProcessoCadastroView.vue`
8. `frontend/src/views/ProcessoDetalheView.vue`
9. `frontend/src/views/LoginView.vue`

### Fase 2 — Defensividade e contratos

1. eliminar fallbacks defensivos restantes em producao
2. eliminar checks de `null` sem contrato
3. eliminar setters publicos artificiais em stores
4. remover exports usados apenas por testes
5. estreitar types frouxos e casts desnecessarios

### Fase 3 — Testes e mocks

1. apagar testes que so sustentam compatibilidade
2. reduzir `any` em testes de alta manutencao
3. substituir mocks de API legada por contratos reais

### Fase 4 — Ratchet permanente

1. reduzir budgets progressivamente
2. remover waivers vencidos
3. promover score de cruft a metrica oficial do dashboard

## Como priorizar hotspots

A priorizacao deve seguir esta formula:

`prioridade = (excesso de linhas) + (sinais de cruft) + (numero de consumidores) + (criticidade do fluxo)`

Interpretacao pratica:

1. primeiro arquivos grandes em fluxo central (`subprocesso`, `processo`, `mapa`, `login`);
2. depois arquivos medios com alta defensividade;
3. por ultimo arquivos pequenos com ruido localizado.

## Criterios de pronto

Consideraremos o frontend "sob controle" quando:

1. existir auditoria dedicada de cruft com snapshot versionado;
2. existir validador automatizado com exit code para CI;
3. `qa snapshot coletar --perfil frontend` incluir score de cruft;
4. nenhum arquivo novo ultrapassar budget;
5. todo hotspot acima do hard limit estiver em waiver formal;
6. o numero de waivers cair continuamente;
7. PR comum deixar de depender de revisao manual para detectar esse tipo de lixo.

## Primeira implementacao recomendada

Ordem concreta para a proxima rodada:

1. criar `etc/scripts/frontend/cruft-auditar.js`
2. criar `etc/scripts/frontend/cruft-validar.js`
3. criar `etc/qualidade/frontend-cruft-budget.json`
4. criar `etc/qualidade/frontend-cruft-waivers.json`
5. registrar comandos em `etc/scripts/sgc.js`
6. integrar no `qa snapshot coletar`
7. ligar o gate em `frontendQualityCheck`
8. atacar `MapaView.vue` e `subprocesso.ts` com o novo circuito ja ativo

## Regra final

Se a limpeza nao deixar um detector e um gate permanentes, ela nao resolveu o problema; apenas deslocou o cruft para a proxima rodada.
