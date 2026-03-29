# Plano de Simplificação do SGC

Este plano consolida apenas recomendações concretas sustentadas pela leitura do código atual. O objetivo é reduzir
complexidade acidental sem perder regras de negócio, segurança, transações, contratos de API ou padronizações úteis já
existentes.

## 0. Guardrails obrigatórios

Antes de qualquer simplificação, aplicar as seguintes restrições:

* Não remover DTOs de forma mecânica.
* DTOs devem continuar sendo a fronteira padrão para respostas e cargas úteis externas.
* Qualquer proposta de remoção ou fusão de DTO precisa verificar antes:
  * contrato consumido pelo frontend;
  * proteção contra lazy loading fora da transação;
  * prevenção de serialização acidental de grafo JPA;
  * isolamento entre modelo de domínio e API.
* Em caso de dúvida, preferir manter o DTO e simplificar apenas mapeamento, nome, escopo ou duplicação.
* Simplificação arquitetural não pode aumentar acoplamento entre controller e entidade JPA.

## 1. Backend

### 1.1 Revisar fronteiras dos serviços de `subprocesso`

Há fragmentação real na área de `Subprocesso`, com responsabilidades distribuídas entre:

* `SubprocessoService`
* `SubprocessoValidacaoService`
* `SubprocessoSituacaoService`
* `SubprocessoTransicaoService`
* `SubprocessoNotificacaoService`

Essa separação não é automaticamente ruim, mas hoje o conjunto já exige navegação frequente entre classes para seguir
fluxos simples e o `SubprocessoService` continua concentrando muitas dependências.

**Recomendação concreta:**

* Mapear fluxos de negócio ponta a ponta de `Subprocesso` e identificar duplicações de busca, validação e cálculo de
  localização/situação.
* Extrair apenas um plano de consolidação pontual onde houver sobreposição clara de responsabilidade.
* Priorizar eliminação de duplicações antes de qualquer fusão ampla de classes.

**Candidatos concretos para revisão:**

* Busca repetida de subprocesso com mapa/atividades.
* Cálculo de localização atual duplicado entre serviços.
* Validações de situação espalhadas entre operações de edição e transição.

### 1.2 Evitar novas facades sem regra própria

As facades existentes não são meros pass-throughs em todos os casos. `PainelFacade`, `AlertaFacade` e `UsuarioFacade`
já concentram regras de visibilidade, autenticação, leitura, montagem de resposta e orquestração.

**Recomendação concreta:**

* Não remover facades existentes de forma mecânica.
* Adotar como regra que novas facades só sejam criadas quando centralizarem regra transversal clara.
* Onde houver método realmente pass-through no futuro, preferir movê-lo para service existente em vez de ampliar a
  fachada.

### 1.3 Manter controllers fora de acesso direto a repositórios com regra de negócio

O código atual usa services e facades para aplicar permissão, transação, validação e efeitos colaterais. Isso aparece
com força nas rotas de processo, subprocesso e alertas.

**Recomendação concreta:**

* Não adotar acesso direto de controllers a repositórios como diretriz geral.
* Restringir acesso direto apenas a leituras triviais e isoladas, desde que não exista regra de negócio, segurança
  contextual ou montagem de resposta já encapsulada em service/facade.
* Antes de criar um novo service para leitura simples, verificar se a lógica já cabe em uma camada existente.

## 2. Frontend

### 2.1 Reduzir stores e composables que funcionam como cache singleton fino

Há casos em que o estado compartilhado é pequeno e a abstração está próxima de um wrapper de service com tratamento de
erro e loading.

**Casos concretos identificados:**

* `useConfiguracoesStore`
* `useMapas`

**Recomendação concreta:**

* Revisar se esses módulos realmente precisam ser singletons globais.
* Onde o consumo for local a poucas telas, considerar migração para composables por tela ou por fluxo.
* Preservar abstrações que já encapsulam transformação de DTO, sincronização entre telas ou coordenação entre módulos.

### 2.2 Evitar componentes utilitários que só repassam props sem padronização real

O problema existe como risco, mas não se aplica indistintamente a todos os componentes comuns. Alguns componentes
aparentemente simples ainda agregam valor por acessibilidade, layout responsivo, slots padronizados e comportamento.

**Recomendação concreta:**

* Antes de criar novo wrapper de componente visual, exigir pelo menos um destes ganhos:
  * padronização visual de uso recorrente;
  * comportamento adicional;
  * contrato de eventos estável;
  * acessibilidade;
  * responsividade;
  * redução real de duplicação.
* Revisar componentes muito finos existentes e remover apenas os que forem comprovadamente redundantes.

**Candidato óbvio para revisão:**

* `LoadingButton.vue`

## 3. Backlog executável

### Prioridade 1

#### Item 1. Mapear duplicações em `subprocesso`

**Objetivo:**
Criar inventário curto de pontos onde a mesma regra ou o mesmo carregamento aparece em mais de um serviço.

**Entregável:**
Documento técnico curto ou checklist no repositório com:

* método atual;
* duplicação identificada;
* classe candidata a concentrar a regra;
* risco de mudança.

**Critério de pronto:**

* duplicações de busca, localização atual e validação de situação estiverem listadas;
* cada duplicação tiver uma proposta explícita de destino;
* nenhum código for alterado ainda.

**Status atual:**

* Mapeamento concluído para os principais pontos de duplicação.
* Primeira consolidação de baixo risco aplicada na rodada atual.
* Segunda consolidação de baixo risco aplicada dentro do próprio `SubprocessoService`.

**Achados desta rodada:**

* Há duplicação real de carregamento de `Subprocesso` com mapa/atividades.
  * `SubprocessoService.buscarSubprocesso(...)`
  * `SubprocessoTransicaoService.buscarSubprocesso(...)`
  * usos diretos repetidos de `subprocessoRepo.buscarPorCodigoComMapaEAtividades(...)` dentro do próprio `SubprocessoService`
* Há duplicação real do cálculo de localização atual.
  * `SubprocessoService.obterUnidadeLocalizacao(...)`
  * `SubprocessoTransicaoService.obterUnidadeLocalizacao(...)`
  * lógica semelhante reaparece em `SubprocessoService.obterDetalhes(...)`
* Há duplicação parcial de validações ligadas a atividades/conhecimentos.
  * `SubprocessoValidacaoService.validarExistenciaAtividades(...)`
  * `SubprocessoValidacaoService.validarCadastro(...)`
  * `SubprocessoTransicaoService.disponibilizar(...)` refaz parte da coleta para montar `atividadesSemConhecimento`
* Há um ponto de sobreposição entre `MapaManutencaoService.reconciliarSituacaoSubprocesso(...)` e regras locais de mudança de situação em `SubprocessoService`.
  * Isso exige cuidado para não unificar fluxos que tratam casos distintos de mapa vazio.
* O `README` do módulo `subprocesso` está desalinhado com a arquitetura atual.
  * Ele cita classes e divisões que não correspondem às implementações existentes.

**Classificação inicial de risco:**

* Baixo risco:
  * centralizar carregamento de subprocesso com fetch;
  * centralizar cálculo de localização atual;
  * alinhar documentação do módulo.
* Médio risco:
  * reduzir duplicação entre validações de cadastro/disponibilização sem mudar contrato de erro.
* Alto risco:
  * fundir serviços de workflow;
  * mexer em transições e efeitos colaterais no mesmo passo;
  * substituir DTOs por entidades.

**Próxima ação sugerida a partir dos achados:**

* Extrair um único ponto de verdade para localização atual do subprocesso e reaproveitá-lo em leitura e workflow.

**Executado nesta rodada:**

* `SubprocessoTransicaoService` passou a reutilizar `SubprocessoService.buscarSubprocesso(...)`.
* `SubprocessoTransicaoService` passou a reutilizar `SubprocessoService.obterUnidadeLocalizacao(...)`.
* Duplicatas locais equivalentes foram removidas de `SubprocessoTransicaoService`.
* Métodos de leitura em `SubprocessoService` passaram a reutilizar `buscarSubprocesso(...)` em vez de repetir acesso direto ao repositório com o mesmo fetch.
* `SubprocessoService.obterDetalhes(...)` passou a reutilizar `obterUnidadeLocalizacao(...)` em vez de recalcular a localização atual inline.
* O `README` do módulo `subprocesso` foi reescrito para refletir a arquitetura real atual.
* A coleta de atividades e a detecção de atividades sem conhecimento foram centralizadas em `SubprocessoValidacaoService`.
* `SubprocessoTransicaoService` deixou de repetir a leitura do mapa para disponibilização e passou a delegar a validação completa ao serviço de validação.
* O bloco de histórico de análises em `SubprocessoService` passou a reutilizar um helper privado comum por tipo, eliminando filtragem repetida.

**Novo cuidado identificado após a consolidação:**

* O reaproveitamento reduziu duplicação, mas aumentou o acoplamento de `SubprocessoTransicaoService` com `SubprocessoService`.
* Em rodada futura, avaliar se esse ponto de verdade deve permanecer em `SubprocessoService` ou migrar para um componente mais neutro de consulta/domínio.
* Documentação de módulo também precisa entrar no ciclo de simplificação; README desatualizado mascara responsabilidades reais e atrapalha decisões de refatoração.

#### Item 2. Consolidar uma duplicação de baixo risco por vez

**Objetivo:**
Eliminar duplicações reais sem reestruturar toda a área de `subprocesso` de uma vez.

**Estratégia:**

* começar por helpers privados ou métodos internos;
* evitar mover regras de transição, notificação e permissão no mesmo PR;
* limitar cada mudança a uma duplicação clara.

**Critério de pronto:**

* uma duplicação concreta removida;
* testes existentes ajustados;
* comportamento externo preservado.

**Observação nova:**

* Preferir iniciar por duplicações internas que não alteram contrato HTTP nem mensagens de validação.
* Reutilizar serviço já existente pode ser aceitável como etapa intermediária, desde que o acoplamento gerado fique registrado e seja reavaliado.
* Quando já existe um método com o fetch correto e tratamento uniforme de erro, o reuso tende a ser melhor que repetir query inline.
* Consolidação de risco médio é viável quando o ponto comum é só coleta/checagem interna e as mensagens externas permanecem intactas.
* Ao simplificar serviços usados por testes de cobertura, preservar assinaturas públicas ou introduzir sobrecargas compatíveis evita retrabalho desnecessário.
* Refinamento: isso vale apenas para refatorações internas. Quando a simplificação remove de fato uma API redundante, os testes devem ser atualizados para o contrato novo.
* Reuso de overload já existente com helper privado tende a ser uma boa estratégia para reduzir duplicação sem criar novas abstrações.
* Quando uma simplificação troca o collaborator principal de um fluxo, os testes precisam sair do ponto antigo de mock e passar a validar o collaborator novo e o comportamento real do branch.

#### Item 3. Registrar diretriz de DTOs

**Objetivo:**
Evitar novas simplificações perigosas envolvendo remoção de DTOs.

**Entregável:**
Adicionar orientação em documentação interna relevante do backend.

**Conteúdo mínimo:**

* DTO não é boilerplate descartável por padrão;
* DTO protege contrato e carregamento;
* entidade JPA não deve vazar para API por conveniência.

**Critério de pronto:**

* regra documentada em local visível do módulo;
* futuras simplificações passam a citar esse critério.

**Observação nova:**

* Esta diretriz continua obrigatória. A rodada anterior mostrou que remoção de DTO pode reabrir problemas de segurança e lazy loading.

### Prioridade 2

#### Item 4. Revisar `useConfiguracoesStore`

**Objetivo:**
Decidir se o estado de configurações precisa mesmo ser global.

**Passos:**

* mapear telas consumidoras;
* verificar se há consumo concorrente real entre telas;
* verificar se os helpers atuais justificam store dedicada.

**Saídas possíveis:**

* manter como store, com justificativa documentada;
* migrar para composable local de tela;
* reduzir a API pública mantendo compatibilidade.

**Critério de pronto:**

* decisão tomada com base nos usos reais;
* sem regressão na tela de parâmetros.

#### Item 5. Revisar `useMapas`

**Objetivo:**
Avaliar se o singleton atual simplifica o fluxo ou apenas espalha estado compartilhado implícito.

**Passos:**

* mapear telas que dependem do mesmo estado;
* identificar onde o compartilhamento é intencional;
* separar consultas independentes de estado global reutilizado.

**Critério de pronto:**

* decisão documentada;
* nenhum fluxo de mapa quebra por perda de sincronização.

#### Item 6. Auditar wrappers visuais finos

**Objetivo:**
Remover ou manter wrappers com critério uniforme, não por impressão.

**Escopo inicial:**

* `LoadingButton.vue`
* demais componentes comuns com baixo volume de lógica própria

**Perguntas de triagem:**

* padroniza algo recorrente?
* reduz duplicação material?
* adiciona acessibilidade ou comportamento?
* evita divergência visual entre telas?

**Critério de pronto:**

* cada componente auditado fica classificado como manter, ajustar ou remover;
* remoções acontecem apenas quando a substituição direta for simples e segura.

### Prioridade 3

#### Item 7. Formalizar regra para facades e abstrações pass-through

**Objetivo:**
Evitar reintrodução gradual de camadas sem responsabilidade própria.

**Regra proposta:**

* nova facade só entra se concentrar regra transversal, orquestração real ou política de acesso/leitura;
* método que só repassa chamada deve ficar na camada já existente, salvo justificativa explícita.

**Critério de pronto:**

* diretriz registrada em documentação do projeto;
* novos PRs passam a usar esse critério.

#### Item 8. Definir critério para acesso direto a repositório

**Objetivo:**
Evitar tanto excesso de service vazio quanto exposição indevida de persistência.

**Regra proposta:**

* acesso direto só em leitura trivial, sem regra de negócio, sem montagem complexa e sem risco de violar segurança;
* fora disso, manter service/facade.

**Critério de pronto:**

* critério documentado;
* exemplos positivos e negativos registrados.

## 4. Ordem sugerida

1. Mapear duplicações em `subprocesso`.
2. Documentar a regra de preservação de DTOs.
3. Executar pequenas consolidações de baixo risco no backend.
4. Revisar `useConfiguracoesStore` e `useMapas`.
5. Auditar wrappers visuais finos.
6. Formalizar diretrizes para facades e acesso direto a repositórios.

## 5. Critério de sucesso

O plano terá sido bem executado se:

* houver menos pontos de navegação para seguir um fluxo simples;
* não houver perda de regras de segurança, transação ou notificação;
* o frontend reduzir singletons desnecessários sem espalhar tratamento de erro;
* componentes comuns restantes tiverem justificativa clara de existência.

## 6. Log de aprendizados

### Rodada atual

* O melhor candidato inicial não é fusão de serviço, e sim remoção de duplicação utilitária.
* `SubprocessoService` mistura CRUD, montagem de resposta, permissões de UI, importação e operações de mapa; portanto, simplificação deve ser incremental.
* O `README` de `backend/src/main/java/sgc/subprocesso` precisa ser tratado como débito técnico, porque hoje ele descreve uma arquitetura diferente da real.
* A primeira consolidação de baixo risco foi viável sem mexer em DTOs, contratos HTTP ou mensagens de negócio.
* Repetição de query com o mesmo fetch era mais espalhada do que parecia inicialmente; houve ganho imediato só com reuso de `buscarSubprocesso(...)`.
* O próximo candidato natural continua sendo documentação do módulo ou centralização mais neutra da lógica de localização atual.
* Ambos os candidatos naturais foram executados com baixo risco nesta rodada.
* A primeira consolidação de risco médio também foi executada sem alterar DTOs, mensagens públicas ou endpoints.
* Foi necessário restaurar compatibilidade de assinatura em validação para manter `compileTestJava` verde sem reintroduzir a duplicação removida.
* Na sequência, a compatibilidade temporária foi removida e os testes foram alinhados à API simplificada, que era a direção correta para este caso.
* O bloco de histórico/análises também aceitou simplificação incremental sem impacto externo.
* A estabilização de testes em `SubprocessoTransicaoService` exigiu remover stubs obsoletos, alinhar mocks ao uso de `SubprocessoService` e ajustar expectativas para o fluxo real de revisão sem impactos.
* No frontend, `configuracoes` foi um bom candidato para sair do Pinia: havia um único consumo real e nenhum benefício claro de estado global.
* `useMapas` aceitou redução de repetição interna com helper genérico e remoção de fallback duplicado.
* A remoção de `mapaService` foi segura porque ele só duplicava uma consulta já exposta por `subprocessoService`; a consolidação reduziu uma fronteira artificial sem mexer em contrato HTTP.
* `useProcessos` tinha repetição interna relevante de carregamento e recarga de detalhe; helpers privados foram suficientes para reduzir ruído sem mudar a API pública do composable.
* `UnidadesView` era um caso claro de `loading`/`erro` manual já coberto por `useAsyncAction`; a migração simplificou a tela sem alterar mensagens nem comportamento visual.
* `AdministradoresView` também aceitou migração parcial para `useAsyncAction`; o carregamento da listagem foi simplificado e o tratamento de erro local deixou de se repetir nas mutações.
* `MapaVisualizacaoView` tinha quatro ações com o mesmo ciclo de loading, fechamento de modal, toast pendente e redirecionamento; helpers privados locais reduziram esse ruído sem alterar permissões nem fluxo.
* No backend, `SubprocessoTransicaoService` ainda carregava muita ramificação binária entre cadastro e revisão; um contexto privado de fluxo reduziu condicionais repetidas em devolução, aceite, homologação e ações em bloco.
* Em permissões, a fonte de verdade continua sendo `etc/docs/regras-acesso.md`; simplificação nessa área só pode acontecer quando o comportamento continuar exatamente alinhado à regra documental.
* `UnidadeView` ainda parece depender de estado global implícito de mapa; esse acoplamento só deve ser removido com segurança quando houver API suficiente para reconstruir o destino da navegação sem reaproveitar estado de outra tela.
* `LoadingButton.vue` continua sendo um wrapper fino, mas o volume de uso torna a remoção imediata cara demais; o melhor caminho agora é impedir novos wrappers fracos e só reavaliar esse componente junto de refatorações de telas maiores.
* A validação de backend com Gradle pode produzir falso negativo por problema de cache de build; quando `:backend:test` passa e `:backend:compileTestJava` falha só ao armazenar cache, vale repetir com `--no-configuration-cache` antes de tratar como regressão de código.
