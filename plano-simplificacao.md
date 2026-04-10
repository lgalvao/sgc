# Plano de Simplificação do SGC

Documento operacional com foco no **que ainda falta fazer**, sustentado por medições objetivas coletadas em **09/04/2026**.

## Premissas (mantidas)

* simplificar sem alterar regra de negócio, contrato HTTP, DTO externo, transações e regras de acesso;
* evitar abstrações genéricas e camadas de compatibilidade;
* priorizar redução de leitura acidental, branches e acoplamento local;
* manter métodos com no máximo 3 parâmetros (usar command/DTO quando necessário);
* remover código `@Deprecated` quando não houver dependências internas.

---

## Metodologia e instrumentos usados nesta rodada

### Comandos executados

* `./gradlew :backend:test :backend:jacocoTestReport`
* `node etc/scripts/sgc.js backend cobertura complexidade`
* `node etc/scripts/sgc.js codigo smells auditar --json`
* `node etc/scripts/sgc.js projeto arvore-linhas`
* scripts locais em Python para inventário complementar:
  * ranking de tamanho de arquivos `.vue/.ts` do frontend;
  * contagem estrutural de `watch/computed/ref/async function` nas views;
  * contagem estrutural de `if/for/switch`, checks de `null` e métodos no backend.

### Métricas principais coletadas

#### Backend (JaCoCo + ranking de complexidade)

* classes analisadas: **106**;
* complexidade ciclomática total: **1787**;
* branches totais: **1490**;
* complexidade média por classe: **16,86**.

Top 5 por *complexity score*:

1. `ProcessoService` (**155,4**)
2. `SubprocessoConsultaService` (**127,0**)
3. `ValidadorDadosOrganizacionais` (**78,9**)
4. `SubprocessoTransicaoService` (**64,7**)
5. `SubprocessoService` (**59,2**)

#### Cheiros de código (snapshot consolidado)

* pontuação total: **1248** (*faixa crítica*);
* escopo backend: **857 pontos**;
* escopo frontend produção: **61 pontos**;
* escopo frontend testes: **330 pontos**.

Sinais que mais pesam:

* backend com `@Nullable` em DTOs: **89 ocorrências**;
* checks explícitos de `null` no backend: **201 ocorrências**;
* frontend produção com `null checks`: **19 ocorrências**;
* frontend produção com fallbacks defensivos `||`: **23 ocorrências**.

#### Tamanho/forma (inventário estrutural)

* backend: **70.530 linhas** no diretório `backend`; `src/test` (41.692) já supera `src/main` (22.148), sugerindo boa blindagem para refatoração incremental;
* frontend: **55.205 linhas** no diretório `frontend`.

Maiores views de produção no frontend:

1. `CadastroView.vue` (**649** linhas)
2. `MapaVisualizacaoView.vue` (**513**)
3. `SubprocessoView.vue` (**499**)
4. `ProcessoCadastroView.vue` (**476**)
5. `AtribuicaoTemporariaView.vue` (**445**)
6. `MapaView.vue` (**430**)

Hotspots de volume + sinais no backend (inventário complementar):

1. `SubprocessoTransicaoService.java` (**927 linhas**, 63 métodos, 11 checks de `null`)
2. `ProcessoService.java` (**895 linhas**, 58 métodos, 15 checks de `null`)
3. `E2eController.java` (**807 linhas**, 40 métodos, 15 checks de `null`)
4. `SubprocessoConsultaService.java` (**583 linhas**, 67 métodos)
5. `ValidadorDadosOrganizacionais.java` (**535 linhas**, 26 métodos, 14 checks de `null`)

---

## Diagnóstico integrado (backend + frontend)

## 1) Backend — concentração de regras em poucos serviços

Há forte concentração de complexidade em **5 classes**, com mistura de responsabilidades (orquestração de fluxo, validação, composição de resposta, notificação, permissões e infraestrutura).

### Achados prioritários

* `ProcessoService` e `SubprocessoConsultaService` lideram simultaneamente em score, branches e volume, sinalizando risco de alteração cruzada e efeitos colaterais;
* `SubprocessoTransicaoService` continua grande (927 linhas), com ganhos recentes, mas ainda com oportunidade de padronização de entradas e redução de duplicidade;
* `ValidadorDadosOrganizacionais` aparece como hotspot de domínio (não só infraestrutura), sugerindo regras de elegibilidade/árvore ainda pouco segmentadas por cenário;
* `E2eController` permanece grande e utilitário demais para o papel de controller de apoio a testes.

### Oportunidades de simplificação de maior retorno

1. **Fatiar por caso de uso explícito dentro dos serviços grandes** (sem camada genérica nova):
   * extrair blocos de decisão para métodos privados nomeados por intenção de regra;
   * reduzir caminhos de early return duplicados.

2. **Padronizar normalização e validação de entrada no início do fluxo público**:
   * manter o corpo dos métodos focado em regra de negócio;
   * diminuir checks defensivos repetidos no meio do fluxo.

3. **Converter `null checks` recorrentes para invariantes locais**:
   * usar construção de contexto/command logo no início;
   * falhar cedo com mensagens de negócio claras;
   * evitar `if (x == null)` espalhado em cadeia.

4. **Segmentar `E2eController` por grupos coesos de fixture/ação**:
   * separar endpoints de criação de cenário, transição de estado e utilitários;
   * manter fachada HTTP enxuta e roteamento sem regra interna extensa.

## 2) Frontend — views grandes como concentradores de estado incidental

O frontend de produção não está crítico por tipagem (`any` em produção zerado), mas ainda há **complexidade incidental em views grandes**.

### Achados prioritários

* `CadastroView.vue` combina alto volume (649 linhas) com alta densidade de `computed` e funções assíncronas;
* `AtribuicaoTemporariaView.vue` e `MapaView.vue` seguem como candidatos naturais por volume e multiplicidade de estado local;
* os maiores arquivos de teste (`ProcessoView.spec.ts`, `VisMapa.spec.ts`) indicam cenários ricos, e podem servir de guia para extrair fluxos coesos de UI sem perda de cobertura.

### Oportunidades de simplificação de maior retorno

1. **Separar estado de edição vs estado derivado de permissão/contexto**:
   * reduzir `computed` com regras mistas (UI + negócio + autorização);
   * manter um bloco de “estado da tela” e outro de “capacidade de ação”.

2. **Consolidar fluxos repetidos de carregamento/salvamento/erro**:
   * reaproveitar funções utilitárias locais por domínio (não genéricas demais);
   * manter `normalizeError` nos pontos de integração com service/store.

3. **Eliminar sincronizações redundantes entre watchers e efeitos de montagem**:
   * priorizar fonte única de verdade por trecho de estado;
   * reduzir risco de corridas em trocas de rota/contexto.

---

## Backlog priorizado (matriz impacto x esforço)

## Prioridade P1 — alto impacto / baixo-médio esforço

1. **`ProcessoService`**
   * Meta: reduzir branches e decisões por método nos fluxos mais usados.
   * Ações:
     * identificar top 5 métodos por densidade de `if/for/switch`;
     * aplicar extrações por intenção (sem abstração genérica);
     * padronizar pré-condições no início.

2. **`SubprocessoConsultaService`**
   * Meta: diminuir acoplamento entre permissão, busca e montagem de DTO.
   * Ações:
     * separar etapas em pipeline linear interno (`resolverContexto`, `validarPermissao`, `montarResposta`);
     * remover branches de fallback quando já cobertos por contrato.

3. **`MapaView.vue`**
   * Meta: reduzir lógica incidental no `<script setup>`.
   * Ações:
     * centralizar cálculos derivados repetidos;
     * colapsar watchers/sincronizações que tratam o mesmo gatilho.

## Prioridade P2 — alto impacto / médio esforço

4. **`SubprocessoTransicaoService`**
   * Meta: continuar a simplificação sem alterar regra funcional.
   * Ações:
     * consolidar normalização de texto na entrada pública;
     * remover adaptadores sem lógica líquida;
     * unificar blocos “buscar + validar situação”.

5. **`AtribuicaoTemporariaView.vue`**
   * Meta: separar estado transitório da tela e regras de habilitação.
   * Ações:
     * recortar funções longas em unidades de intenção;
     * reduzir dependência cruzada entre `ref`s de formulário e permissões.

## Prioridade P3 — médio impacto / baixo-médio esforço

6. **`E2eController`**
   * Meta: melhorar legibilidade e manutenção de testes E2E.
   * Ações:
     * dividir por grupos de endpoint;
     * manter contrato atual de rotas, mas com implementação interna mais coesa.

7. **DTOs backend com `@Nullable` (89 ocorrências)**
   * Meta: diminuir código defensivo cascata.
   * Ações:
     * revisar DTOs com maior incidência e promover campos obrigatórios quando o contrato permitir;
     * para opcionais reais, encapsular semântica em métodos auxiliares de leitura.

---

## Plano de execução em ondas (recomendado)

### Onda 1 (1 rodada)

* atacar `ProcessoService` + `MapaView.vue`;
* entregar redução mensurável em:
  * branches do `ProcessoService`;
  * linhas e blocos de sincronização em `MapaView.vue`.

### Onda 2 (1 rodada)

* atacar `SubprocessoConsultaService` + `AtribuicaoTemporariaView.vue`;
* priorizar remoção de duplicação de fluxo e clareza de pré-condições.

### Onda 3 (1 rodada)

* concluir frente de `SubprocessoTransicaoService` e recorte de `E2eController`;
* revisar lote inicial de DTOs com `@Nullable` mais frequente.

---

## Critérios objetivos de sucesso por rodada

## Backend

* reduzir **10%+** do *complexity score* no alvo principal da rodada;
* reduzir contagem local de checks explícitos de `null` no alvo (sem trocar por “defensivo escondido”);
* manter/elevar cobertura de branches nos hotspots alterados.

## Frontend

* reduzir linhas da view alvo em **15%+** (ou justificar impossibilidade);
* reduzir número de pontos de sincronização (`watch`, inicializações duplicadas, efeitos sobrepostos);
* manter `frontend_any_producao = 0`.

## Observabilidade de simplificação

* registrar antes/depois no próprio PR:
  * linhas do arquivo;
  * total de condicionais aproximado;
  * número de pontos de entrada assíncrona.

---

## Riscos e contenções

* **Risco:** extração “arquitetural” excessiva piorar legibilidade.
  * **Contenção:** só extrair quando reduzir leitura local e duplicação de decisão.

* **Risco:** alterações em serviços centrais gerarem regressão comportamental.
  * **Contenção:** reforçar testes de integração nos casos de uso alterados antes de refatorar.

* **Risco:** simplificação de view quebrar fluxo de permissão.
  * **Contenção:** validar estados de ação por perfil (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`) nos cenários já existentes.

---

## Validação mínima por rodada

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`
* `./gradlew :backend:jacocoTestReport`
* `node etc/scripts/sgc.js backend cobertura complexidade`

---

## Progresso executado em 10/04/2026

### Rodada A — `ProcessoService` (P1)

* aplicado recorte de contexto para início de subprocessos via `InicioSubprocessosContexto`, removendo assinatura com muitos parâmetros no miolo do fluxo e deixando o ponto de entrada do caso de uso mais linear;
* reduzida duplicação no processamento de ações em bloco (`ACEITAR`/`HOMOLOGAR`) com função única de aplicação condicional de transições;
* mantido contrato funcional (mesmas chamadas para `SubprocessoService` e `SubprocessoTransicaoService`, sem alteração de DTO externo ou regras de permissão).

**Aprendizados desta rodada**

* extrações locais com `record` privado funcionam bem para reduzir carga cognitiva sem introduzir camada nova;
* trechos de switch com ramos espelhados dão ganho rápido de legibilidade quando isolamos apenas a variação (qual transição chamar), preservando o fluxo principal.

### Rodada B — `MapaView.vue` (P1)

* consolidado padrão repetido de guarda de `codSubprocesso` em um executor único (`executarComSubprocesso`) para operações assíncronas da tela;
* extraído cálculo derivado de atividades associadas para `computed` dedicado, reduzindo reconstrução incidental no fluxo de filtragem;
* mantido comportamento de erro (`handleErrors`), atualização de mapa e navegação pós-disponibilização.

**Aprendizados desta rodada**

* a maior fonte de complexidade da view estava na repetição de pré-condições e não em regra de negócio;
* pequenos utilitários locais no `<script setup>` trazem simplificação mensurável sem mover lógica para composables genéricos.

### Próximos passos recomendados (mantendo backlog original)

1. avançar em `SubprocessoConsultaService` (P1), com pipeline interno explícito de contexto/permissão/resposta;
2. executar rodada dedicada em `AtribuicaoTemporariaView.vue` (P2) para separar estado transitório de regras de habilitação;
3. rodar nova coleta (`qa dashboard` + complexidade backend) ao fim da próxima onda para comparar tendência com o snapshot de 09/04/2026.

### Rodada C — `SubprocessoConsultaService` (P1)

* iniciado pipeline interno de contexto com `DadosContextoConsulta`, reduzindo acoplamento no método `montarContextoConsulta`;
* simplificadas verificações de acesso de cadastro/mapa com early return para estados indisponíveis, mantendo as mesmas regras de perfil;
* padronizada resolução de localização atual por expressão única, reduzindo branch incidental.

**Aprendizados desta rodada**

* o maior ganho de legibilidade veio da separação entre “coleta de dados do contexto” e “cálculo de permissões”;
* early return nas verificações de acesso elimina combinações redundantes sem alterar política de autorização.

### Rodada D — `AtribuicaoTemporariaView.vue` (P2)

* consolidada validação de submissão em `computed` único (`formularioValido`), removendo condição longa repetida no submit;
* padronizado fluxo de pesquisa incremental de usuários com funções de intenção (`devePesquisarUsuarios`, `agendarPesquisaUsuarios`, `cancelarPesquisaUsuariosPendente`);
* simplificada navegação por teclado da busca com `switch` e guarda única para cenário sem resultados.

**Aprendizados desta rodada**

* no front, a redução de complexidade veio mais de nomear intenções de fluxo do que de extrair composables;
* helpers pequenos para “estado de pesquisa” diminuem risco de drift entre eventos de input, blur e teclado.

### Frontend

* `npm run typecheck`
* `npm run lint`
* `npm run test:unit`

### Auditoria de simplificação

* `node etc/scripts/sgc.js codigo smells auditar --json`
* `node etc/scripts/sgc.js projeto arvore-linhas`

### QA dashboard (quando aplicável)

* `npm run qa:dashboard`

Fontes de verdade:

* `etc/qa-dashboard/latest/ultimo-snapshot.json`
* `etc/qa-dashboard/latest/ultimo-resumo.md`
