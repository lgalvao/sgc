# Plano de Simplificação do SGC

Documento operacional com foco no **que realmente falta fazer** no checkout validado em **10/04/2026**.

## Premissas

* simplificar sem alterar regra de negócio, contrato HTTP, DTO externo, transações e regras de acesso;
* evitar abstrações genéricas, camadas de compatibilidade e extrações sem ganho claro de leitura local;
* priorizar redução de leitura acidental, branches, null checks repetidos e acoplamento local;
* manter métodos com no máximo 3 parâmetros, usando command/DTO quando necessário;
* remover código `@Deprecated` assim que não houver dependências internas;
* considerar concluído apenas o que já foi confirmado no código atual, não apenas no snapshot anterior.

---

## Base usada nesta revisão

### Fontes conferidas diretamente no código

* `backend/src/main/java/sgc/processo/service/ProcessoService.java`
* `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java`
* `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`
* `backend/src/main/java/sgc/e2e/E2eController.java`
* `backend/src/main/java/sgc/organizacao/ValidadorDadosOrganizacionais.java`
* `frontend/src/views/CadastroView.vue`
* `frontend/src/views/MapaView.vue`
* `frontend/src/views/AtribuicaoTemporariaView.vue`
* `frontend/src/views/MapaVisualizacaoView.vue`
* `frontend/src/views/SubprocessoView.vue`
* `frontend/src/views/ProcessoCadastroView.vue`

### Instrumentos usados

* leitura direta dos hotspots citados no plano anterior;
* `wc -l` para revisão de volume por arquivo;
* inventário estrutural local para contagem de métodos, `if/switch`, checks explícitos de `null`, `computed`, `watch` e funções assíncronas;
* validação manual dos achados qualitativos contra o estado atual do repositório.

### Observação sobre o snapshot anterior

O plano anterior estava sustentado por medições de **09/04/2026**. Parte do diagnóstico continua válida, mas alguns números, caminhos de arquivo e prioridades ficaram desatualizados. Este documento substitui o foco anterior pelo backlog que ainda faz sentido agora.

---

## Estado atual validado

### Backend

Hotspots confirmados por volume e concentração de responsabilidade:

1. `ProcessoService.java`: **984 linhas**, **67 métodos**, **15 checks de null**, concentração de consulta, manutenção, workflow, validação e notificação.
2. `SubprocessoTransicaoService.java`: **926 linhas**, **63 métodos**, **11 checks de null**, fluxo transacional ainda extenso apesar de melhorias recentes.
3. `E2eController.java`: **806 linhas**, **40 métodos**, **15 checks de null**, mistura controller HTTP com utilidades de fixture, seed e manutenção de banco.
4. `SubprocessoConsultaService.java`: **613 linhas**, **69 métodos**, **6 checks de null**, ainda concentra leitura, contexto, permissões e montagem de resposta.
5. `ValidadorDadosOrganizacionais.java`: **534 linhas**, **26 métodos**, **14 checks de null**, hotspot real de regra de domínio.

Sinais adicionais ainda relevantes:

* `@Nullable` em DTOs backend: **57 ocorrências** no checkout atual;
* o problema principal segue sendo concentração de lógica em poucos pontos centrais, não falta de cobertura estrutural para refatorar;
* `ProcessoService` e `SubprocessoConsultaService` continuam sendo os pontos com maior risco de alteração cruzada.

### Frontend

Views de produção ainda grandes no checkout atual:

1. `CadastroView.vue`: **646 linhas**, **13 computed**, **2 watch**, **12 funções assíncronas**.
2. `MapaVisualizacaoView.vue`: **512 linhas**, **9 computed**, **0 watch**, **7 funções assíncronas**.
3. `SubprocessoView.vue`: **499 linhas**, **2 computed**, **0 watch**, **6 funções assíncronas**.
4. `AtribuicaoTemporariaView.vue`: **500 linhas**, **7 computed**, **1 watch**, **4 funções assíncronas**.
5. `ProcessoCadastroView.vue`: **476 linhas**, **4 computed**, **1 watch**, **6 funções assíncronas**.
6. `MapaView.vue`: **434 linhas**, **9 computed**, **0 watch**, **7 funções assíncronas**.

Leitura correta para o estado atual:

* `CadastroView.vue` continua sendo o principal concentrador de estado incidental no frontend;
* `AtribuicaoTemporariaView.vue` segue elegível para simplificação, mas por acoplamento entre pesquisa, validação e submissão, não por volume isolado;
* `MapaView.vue` ainda merece atenção, mas **não** por excesso de watchers; a complexidade remanescente está mais em cálculos derivados e handlers assíncronos;
* `MapaVisualizacaoView.vue` hoje aparece como candidato mais plausível do que no plano anterior, por combinar volume com múltiplas ações condicionadas por permissão e estado.

---

## O que já foi suficientemente atacado

Estas frentes não devem seguir como prioridade principal apenas por inércia do plano anterior:

1. `MapaView.vue` já recebeu simplificações úteis e hoje não sustenta mais prioridade P1 por problema de sincronização.
2. O backlog anterior subestimava o crescimento de `ProcessoService` e `AtribuicaoTemporariaView.vue`; portanto as prioridades antigas ficaram distorcidas.
3. O plano anterior tratava contagem de `@Nullable` em DTOs como um bloco maior do que o observado no checkout atual; continua sendo um tema válido, mas não é o melhor próximo passo isolado.

---

## Diagnóstico operacional do que falta fazer

## 1) Backend

### Ponto mais crítico

`ProcessoService` ainda é o maior concentrador de regras e infraestrutura do domínio de processo. O arquivo cresceu e permanece com responsabilidades demais para um único ponto de entrada.

### Alvos que ainda faltam

1. **`ProcessoService`**
   * separar melhor trechos de orquestração, elegibilidade, notificação e transições internas;
   * reduzir branches nos fluxos mais usados, especialmente onde há decisões condicionadas por perfil, situação e localização;
   * consolidar pré-condições e contexto no início dos fluxos públicos.

2. **`SubprocessoConsultaService`**
   * continuar a separação entre busca, construção de contexto, cálculo de permissão e montagem de resposta;
   * eliminar branches de fallback que hoje escondem contrato implícito;
   * reduzir mistura entre consulta simples e lógica derivada de interface.

3. **`SubprocessoTransicaoService`**
   * manter a estratégia incremental, focando em entradas públicas e blocos repetidos de “buscar + validar situação + registrar movimentação”;
   * padronizar normalizações textuais e commands internos;
   * evitar novos records/adaptadores que só renomeiem parâmetros sem simplificar leitura.

4. **`E2eController`**
   * recortar por grupos coesos de responsabilidade, mantendo o contrato HTTP existente;
   * mover implementação interna pesada para helpers ou serviços específicos de fixture, quando isso realmente reduzir o corpo do controller;
   * evitar continuar adicionando SQL utilitário e bootstrap de cenário no mesmo arquivo.

5. **`ValidadorDadosOrganizacionais`**
   * segmentar regras por cenário de validação em vez de manter um agregado grande de elegibilidade;
   * transformar null checks recorrentes em invariantes ou objetos de contexto locais;
   * melhorar nomeação de blocos de regra para reduzir leitura acidental.

## 2) Frontend

### Ponto mais crítico

`CadastroView.vue` continua sendo a view com maior custo de leitura e coordenação local. Ela mistura estado de edição, estado de revisão, permissões, validação, histórico, modais e integração com stores.

### Alvos que ainda faltam

1. **`CadastroView.vue`**
   * separar estado de edição de cadastro do estado derivado de permissão e revisão;
   * consolidar fluxos repetidos de sincronização local, validação e atualização de status;
   * reduzir o número de pontos que coordenam modal, erro, histórico e disponibilização no mesmo bloco.

2. **`MapaVisualizacaoView.vue`**
   * isolar o fluxo das ações de análise, aceite, devolução e validação;
   * separar capacidade de ação do estado de carregamento e histórico;
   * reduzir carga cognitiva do `<script setup>` sem extrair composables genéricos.

3. **`AtribuicaoTemporariaView.vue`**
   * desacoplar pesquisa incremental de usuário da validação de submissão;
   * centralizar estados transitórios de dropdown, destaque e timeouts;
   * manter a view como candidata de médio esforço, não mais como principal aposta do frontend.

4. **`MapaView.vue`**
   * tratar apenas simplificações locais restantes em cálculos derivados e handlers assíncronos;
   * não priorizar nova rodada ampla a menos que uma mudança funcional volte a inflar a tela.

---

## Backlog priorizado

## Prioridade P1

1. **`ProcessoService`**
   * maior retorno imediato no backend;
   * alvo principal para reduzir complexidade concentrada e risco de regressão em futuras mudanças.

2. **`CadastroView.vue`**
   * maior retorno imediato no frontend;
   * concentra o pior acoplamento entre estado local, permissões e fluxo assíncrono.

3. **`SubprocessoConsultaService`**
   * deve continuar logo após `ProcessoService`;
   * já tem sinais claros de ganho incremental possível sem mudar contrato.

## Prioridade P2

4. **`SubprocessoTransicaoService`**
   * segue relevante, mas não deve furar a fila de `ProcessoService` e `SubprocessoConsultaService`.

5. **`MapaVisualizacaoView.vue`**
   * hoje merece entrar antes de nova rodada grande em `MapaView.vue`.

6. **`E2eController`**
   * importante para manutenção e previsibilidade dos testes, mas com impacto mais indireto no produto.

## Prioridade P3

7. **`ValidadorDadosOrganizacionais`**
   * hotspot real, porém mais sensível a regra de domínio e, por isso, pede rodada própria e cuidadosa.

8. **DTOs backend com `@Nullable`**
   * revisar primeiro apenas DTOs mais usados nos fluxos alterados;
   * não executar como iniciativa isolada e ampla neste momento.

9. **`AtribuicaoTemporariaView.vue` e `MapaView.vue`**
   * manter como ajustes oportunistas ou segunda frente de frontend, não como foco principal da próxima onda.

---

## Plano recomendado em ondas

### Onda 1

* atacar `ProcessoService` + `CadastroView.vue`;
* objetivo: reduzir leitura local dos dois maiores concentradores atuais.

### Onda 2

* atacar `SubprocessoConsultaService` + `MapaVisualizacaoView.vue`;
* objetivo: clarear contexto/permissões no backend e ações condicionais no frontend.

### Onda 3

* atacar `SubprocessoTransicaoService` + `E2eController`;
* objetivo: consolidar simplificação de workflow e reduzir utilitário excessivo no apoio a E2E.

### Onda 4

* revisar `ValidadorDadosOrganizacionais` e lote pequeno de DTOs com `@Nullable` relacionados aos fluxos tocados;
* objetivo: transformar defensividade repetida em contratos locais mais explícitos.

---

## Critérios objetivos de sucesso

### Backend

* reduzir **10%+** do score ou de sinais estruturais relevantes no alvo principal da rodada;
* reduzir contagem local de checks explícitos de `null` sem escondê-los em helpers opacos;
* reduzir número de branches ou métodos excessivamente longos no hotspot atacado;
* manter ou elevar cobertura nos trechos alterados.

### Frontend

* reduzir **15%+** das linhas da view alvo ou justificar objetivamente por que a simplificação foi estrutural, não volumétrica;
* reduzir pontos de coordenação local concorrentes, especialmente carregamento, erro, modal e permissão;
* manter `frontend_any_producao = 0`;
* não introduzir novo acoplamento genérico por composables artificiais.

### Observabilidade da simplificação

Registrar no PR, para cada alvo:

* linhas antes/depois;
* condicionais aproximadas antes/depois;
* número de pontos de entrada assíncrona antes/depois;
* resumo curto do que deixou de ficar espalhado.

---

## Riscos e contenções

* **Risco:** extração arquitetural piorar legibilidade.
  * **Contenção:** extrair apenas quando o novo nome reduzir leitura local e não criar navegação excessiva.

* **Risco:** refatoração em serviços centrais alterar comportamento de autorização ou workflow.
  * **Contenção:** reforçar testes de integração nos casos alterados e preservar contratos externos.

* **Risco:** simplificação de view deslocar lógica para composables genéricos e pouco transparentes.
  * **Contenção:** preferir helpers locais e composables específicos de domínio quando houver duplicação real.

* **Risco:** usar métricas antigas para justificar prioridade errada.
  * **Contenção:** sempre reconferir volume e sinais estruturais do arquivo antes de iniciar a rodada.

---

## Validação mínima por rodada

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`
* `./gradlew :backend:jacocoTestReport`
* `node etc/scripts/sgc.js backend cobertura complexidade`

### Frontend

* `npm run typecheck`
* `npm run lint`
* `npm run test:unit`

### Auditoria complementar

* `node etc/scripts/sgc.js codigo smells auditar --json`
* `node etc/scripts/sgc.js projeto arvore-linhas`
* `npm run qa:dashboard` quando a rodada afetar qualidade percebida mais ampla
