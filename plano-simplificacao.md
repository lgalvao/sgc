# Plano de Simplificação do SGC

Este plano consolida apenas recomendações concretas sustentadas pela leitura do código atual. O objetivo é reduzir
complexidade acidental sem perder regras de negócio, segurança, transações, contratos de API ou padronizações úteis já
existentes.

## Princípios gerais de simplificação

Estes princípios valem para backend e frontend e devem orientar cada rodada:

* Simplificar primeiro a menor fronteira segura.
* Tornar dependências e fluxos explícitos.
* Reduzir superfície pública antes de criar abstração nova.
* Desconfiar de abstração genérica com um único consumidor real.
* Preservar contratos externos, DTOs e regras de acesso.
* Remover código morto logo após a simplificação.
* Validar em passos pequenos e registrar aprendizado no próprio plano.

## Decisão atual

Depois dos achados sobre protótipo inicial, round-trips excessivos e desempenho em Oracle, a decisão desta fase é:

* não abrir uma frente paralela grande de instrumentação;
* continuar simplificando por fluxos completos de tela;
* usar medição como guarda da simplificação, não como objetivo separado.

Na prática, isso significa:

* frontend:
  * reduzir round-trips e regra duplicada de workflow;
  * paralelizar chamadas independentes quando a agregação não for viável ainda;
  * manter testes de orçamento de chamadas para evitar regressão estrutural;
* backend:
  * manter o `MonitoramentoAspect` como apoio para observar o efeito real;
  * permitir modo de trace completo quando for preciso investigar fluxo específico;
  * evitar instrumentação invasiva enquanto o desenho ainda está sendo simplificado.

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

**Status atual:**

* O singleton `useMapas` foi estreitado em rodadas anteriores com remoção de estado morto.
* `UnidadeView` já deixou de depender de contexto herdado de `mapasStore`.
* Nesta rodada, `useSubprocessos.buscarContextoEdicao(...)` deixou de sincronizar `useMapas` por efeito colateral.
* Nesta rodada, `useFluxoMapa` também deixou de escrever em `useMapas` por dentro e passou a retornar o mapa atualizado para a view decidir como sincronizar.
* `MapaView` assumiu explicitamente a sincronização de `mapaCompleto` e a recarga do contexto de edição, com helpers locais para reduzir repetição.

**Aprendizado novo:**

* O melhor uso de `useMapas` no SGC parece ser como estado compartilhado explícito do fluxo de mapa, não como destino implícito de escrita por outros composables.
* Quando o composable de fluxo retorna o dado atualizado, a view fica mais previsível e os testes deixam de depender de efeito colateral escondido.
* O frontend atual ainda carrega resquícios do protótipo sem backend; por isso, próximos cortes devem priorizar remoção de regra duplicada de workflow, validação local redundante e cálculo de permissão fora da fonte de verdade.

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

## 4.1 Próxima fase coesa

Os próximos passos devem ser executados como uma frente única, e não como otimizações isoladas:

### Frente A. Recentrar a fonte de verdade no backend

**Problema consolidado:**

* O frontend ainda carrega resquícios do protótipo inicial sem backend.
* Há regras de workflow, validações e guardas locais que duplicam comportamento já protegido no backend.
* Isso aumenta manutenção, abre risco de divergência com `etc/reqs` e `etc/docs/regras-acesso.md` e tende a produzir chamadas extras.

**Objetivo:**

* deixar o frontend responsável por estado de tela, navegação e feedback visual;
* deixar o backend como fonte de verdade de permissão, situação e validação de negócio.

**Critério de execução:**

* remover primeiro guardas locais redundantes de situação e workflow;
* manter apenas validações locais de formulário e consistência puramente visual;
* evitar reimplementar regra de permissão fora de `useAcesso` e do contrato vindo do backend.

### Frente B. Reduzir round-trips por tela

**Problema consolidado:**

* No Oracle real, o custo do vai-e-vem ficou visível.
* O frontend ainda faz sequências de chamadas finas demais para montar uma única tela.

**Objetivo:**

* preferir cargas de contexto por tela ou por fluxo;
* evitar sequências como “buscar id -> buscar detalhe -> buscar mapa -> buscar permissões” quando um endpoint de contexto já pode entregar isso junto.

**Candidatos prioritários:**

* `SubprocessoView`
* `CadastroView`
* `CadastroVisualizacaoView`
* `MapaVisualizacaoView`

**Critério de execução:**

* cada tela deve ter um inventário explícito de chamadas no `onMounted`;
* se duas ou mais chamadas sempre andam juntas, avaliar consolidação;
* preferir reutilizar endpoint de contexto existente antes de criar endpoint novo;
* criar endpoint novo apenas quando a composição atual continuar cara demais ou incompleta.
* sempre que possível, transformar a redução de round-trips em teste com orçamento explícito de chamadas por tela.

**Validação de desempenho:**

* No frontend, usar testes de orçamento de chamadas para evitar regressão de round-trips lógicos.
* No backend, confrontar os fluxos críticos com o aspecto existente em `backend/src/main/java/sgc/comum/util/MonitoramentoAspect.java`.
* A leitura correta é combinada:
  * menos chamadas por tela;
  * menos ocorrências de `EXECUCAO LENTA` nos serviços e repositórios envolvidos;
  * redução de latência percebida no fluxo completo.

### Frente C. Estreitar composables para refletir fluxos reais

**Problema consolidado:**

* Alguns composables ainda foram desenhados como infraestrutura genérica do protótipo, não como representação do fluxo real do SGC.
* Isso gera escrita implícita em stores paralelos, API pública larga demais e acoplamento desnecessário.

**Objetivo:**

* manter cada composable responsável por um fluxo claro;
* eliminar sincronizações implícitas;
* devolver dados para a view quando isso tornar o fluxo mais legível.

**Critério de execução:**

* se o composable altera outro store por dentro, reavaliar;
* se a view é o único consumidor, preferir helper local ou composable de escopo curto;
* se uma abstração genérica tiver só um consumidor real, preferir estado e helpers explícitos na própria tela ou componente;
* se o estado é realmente compartilhado, explicitar isso no plano e manter o singleton.

### Frente D. Consolidar backend para servir melhor o frontend real

**Problema consolidado:**

* Parte da complexidade percebida no frontend nasce de endpoints finos ou fragmentados demais para o uso real das telas.
* Simplificar só no frontend não resolve o custo de orquestração quando a tela depende de múltiplos pedaços sempre carregados juntos.

**Objetivo:**

* identificar onde vale expor contexto agregado útil para a tela;
* manter DTOs e fronteiras seguras;
* evitar que a simplificação de frontend empurre regra para a UI.

**Critério de execução:**

* primeiro reutilizar `contexto-edicao` e endpoints já existentes;
* depois, se necessário, criar DTO/endpoint agregado orientado à tela;
* nunca substituir isso por exposição direta de entidade ou acoplamento controller-repo.

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
* Em telas de cadastro e visualização, o frontend ainda usava `buscarProcessoDetalhe(...)` só para descobrir `codSubprocesso` e `tipoProcesso`; isso era herança do protótipo inicial e gerava round-trip desnecessário.
* `CadastroView`, `CadastroVisualizacaoView` e `MapaVisualizacaoView` aceitaram simplificação na mesma direção: usar lookup direto do subprocesso e o próprio contexto do subprocesso como fonte de verdade, em vez de depender do processo inteiro para navegação indireta.
* Quando a tela só precisa do subprocesso e do seu contexto, `buscarProcessoDetalhe(...)` tende a ser custo excessivo e acoplamento acidental.
* Em `CadastroView`, o pós-sucesso de disponibilização não precisava recarregar detalhe antes de redirecionar; remover essa chamada economiza round-trip sem perda funcional.
* Em `MapaVisualizacaoView`, depois de remover a dependência do processo inteiro, a paralelização útil ficou concentrada apenas nas chamadas realmente necessárias para detalhe e mapa.
* Testes de orçamento de chamadas funcionam bem como guarda estrutural, mas exigem alinhar o helper de montagem ao novo ponto de verdade da tela; quando a identificação do subprocesso muda, os cenários de ação precisam inicializar esse estado de forma explícita.
* No backend, `SubprocessoTransicaoService` ainda carregava muita ramificação binária entre cadastro e revisão; um contexto privado de fluxo reduziu condicionais repetidas em devolução, aceite, homologação e ações em bloco.
* Em permissões, a fonte de verdade continua sendo `etc/docs/regras-acesso.md`; simplificação nessa área só pode acontecer quando o comportamento continuar exatamente alinhado à regra documental.
* A frente ambiciosa de `UnidadeView` foi concluída com segurança: a tela deixou de depender de `mapasStore.mapaCompleto` e passou a usar uma referência explícita de mapa vigente vinda do backend.
* Para viabilizar isso sem inferência frágil no frontend, foi criado um endpoint específico de referência do mapa vigente por unidade, retornando `codProcesso` e `codSubprocesso`.
* Depois dessa mudança, o caminho `verificarMapaVigente -> useMapas.temMapaVigente` ficou morto no frontend e pôde ser removido sem impacto em produção.
* `useMapas` ainda era mais largo do que o consumo real exigia; `mapaVisualizacao` e `mapaAjuste` não tinham mais usos de produção e foram eliminados para reduzir estado singleton ocioso.
* `useSubprocessos.buscarContextoEdicao(...)` e `useFluxoMapa` ficaram mais estreitos quando pararam de sincronizar `useMapas` por dentro; isso reduziu o acoplamento implícito entre composables.
* Em `MapaView`, helpers locais de sincronização e recarga reduziram repetição sem exigir nova abstração compartilhada; quando a duplicação está concentrada numa única view, helper privado costuma bastar.
* O modal de impacto repetia o mesmo ciclo de `mostrar`, `loading` e `buscarImpactoMapa` em três views; um composable curto foi suficiente para consolidar esse comportamento sem deslocar regra de negócio.
* Em `CadastroView`, a checagem local de situação antes da validação era redundante com a fonte de verdade do backend; remover esse pré-check deixou a view mais enxuta e reduziu duplicação de workflow.
* O próximo ciclo não deve reagir a sintomas isolados. O critério correto agora é consolidar as mudanças em torno de quatro frentes: fonte de verdade no backend, redução de round-trips por tela, estreitamento de composables e agregação backend orientada ao uso real da UI.
* Desempenho passou a ser parte explícita da simplificação. Em ambiente real com Oracle, chamadas pequenas demais e em sequência têm custo perceptível e devem entrar no critério de desenho.
* Para não tratar desempenho só por percepção, vale manter testes de orçamento de chamadas em telas críticas. Eles não substituem medição real, mas evitam regressão de round-trips durante refatorações futuras.
* Há dois tipos diferentes de ganho de desempenho que precisam ser tratados separadamente:
  * reduzir round-trips, reaproveitando contexto agregado e evitando chamadas em cascata;
  * reduzir latência por paralelização, quando duas chamadas ainda são necessárias mas independentes.
* `SubprocessoView` passou a usar `buscarContextoEdicao(...)` como carga principal, eliminando uma sequência de detalhe + mapa quando o contexto já traz o mapa.
* `SubprocessoView` ainda carregava um acoplamento implícito com `useProcessos`: lia `processoDetalhe` global para bloquear ações e calcular prazo, embora o backend já entregasse permissões suficientes e o próprio subprocesso já trouxesse datas relevantes.
* Remover esse acoplamento simplificou a tela e evitou depender de estado herdado de outra navegação; para envio de lembrete, a view também deixou de usar `useProcessos` e passou a chamar o service direto.
* `useFluxoSubprocesso` ficou mais previsível quando parou de recarregar `processoDetalhe` por efeito colateral; os consumidores atuais só dependiam do recarregamento explícito do subprocesso.
* Quando uma action de composable já busca um detalhe e a tela precisa consumir esse resultado imediatamente, é melhor ela devolver o objeto carregado. Em `ProcessoCadastroView`, isso eliminou a leitura indireta de `processoDetalhe.value` como ponte entre chamada e uso.
* `CadastroView` deixou de depender de `buscarProcessoDetalhe(...)` só para descobrir `codSubprocesso` e `tipoProcesso`; agora usa busca direta do subprocesso e o próprio contexto como fonte de verdade.
* `CadastroVisualizacaoView` também deixou de depender de `processos.processoDetalhe` para descobrir subprocesso e tipo; isso reduziu round-trip e removeu lógica herdada de árvore de unidades na própria tela.
* `MapaVisualizacaoView` aceitou paralelização segura no `onMounted`: busca de unidade e processo em paralelo, depois detalhe e mapa em paralelo. Isso preserva contratos e reduz tempo de espera total sem aumentar acoplamento.
* `MapaVisualizacaoView` ainda carregava `useProcessos` só como fachada para ações de mapa. Trocar essa dependência por chamadas explícitas de service reduziu uma fronteira larga herdada sem perda de contrato, porque a tela não consumia estado de processo no fluxo dessas ações.
* Em `ProcessoDetalheView`, textos, títulos, rótulos e mensagens de sucesso das ações em bloco estavam espalhados em vários `switch` paralelos. Consolidar isso em uma configuração local por ação e contexto reduziu duplicação sem alterar nenhum texto visível.
* `useProcessos` ainda retinha actions e helpers sem consumidor de produção. Remover essa API órfã deixou o composable mais próximo do uso real e liberou código morto interno, como `executarAcaoComRecarga`.
* `LoadingButton.vue` continua sendo um wrapper fino, mas o volume de uso torna a remoção imediata cara demais; o melhor caminho agora é impedir novos wrappers fracos e só reavaliar esse componente junto de refatorações de telas maiores.
* A validação de backend com Gradle pode produzir falso negativo por problema de cache de build; quando `:backend:test` passa e `:backend:compileTestJava` falha só ao armazenar cache, vale repetir com `--no-configuration-cache` antes de tratar como regressão de código.
* Endpoints com `@RequestBody(required = false)` não podem usar `Optional.of(...)` no controller; isso reintroduz `500` em fluxos válidos sem payload, como aceite e homologação da validação do mapa no CDU-20.
* A nova varredura confirmou que o plano original já não cobria todos os alvos bons. Depois de reduzir os grandes acoplamentos do frontend, passou a fazer mais sentido procurar abstrações genéricas herdadas do protótipo com consumo real muito pequeno.
* `useLoadingManager`, `useModalManager` e `useApi` eram exemplos claros desse padrão: abstrações genéricas para um único fluxo real. Removê-las reduziu superfície pública, arquivos para navegar e testes artificiais sem alterar contratos de negócio.
* Em `SubprocessoView`, dois modais e dois estados de loading ficaram mais simples como `ref` local do que como gerenciadores genéricos parametrizados.
* Em `ImportarAtividadesModal`, um wrapper genérico de API não agregava contrato além de `loading + erro`; mover esse estado para o próprio componente deixou o fluxo mais direto.
* `useSubprocessos` ainda tinha duplicação interna de pré-condição de perfil/unidade. Extrair um helper único para esse contexto melhorou a leitura sem ampliar a API pública do composable.
* `SubprocessoCards` ainda consultava `useProcessos` só para descobrir se o processo estava finalizado. Esse bloqueio já existia na fonte de verdade do backend, então a checagem local era redundante e o componente pôde voltar a depender apenas do próprio subprocesso.
* A nova varredura confirmou que parte da largura de `useProcessos` vinha de leituras locais empacotadas por conveniência histórica. `HistoricoView` e `ImportarAtividadesModal` não precisavam compartilhar estado de lista com o resto do app.
* Mover histórico e importação para leitura direta de service reduziu a API pública de `useProcessos` sem piorar testes nem fluxo de UI. O composable ficou mais próximo de casos realmente centrados em processo: painel, detalhe, cadastro e ações em bloco.
* Quando uma simplificação troca a fonte de dados de store singleton para service local, os testes precisam parar de assumir estado pré-carregado. No modal de importação, o ajuste correto foi alinhar os testes ao carregamento real na abertura do componente.
* Storybook também entra no escopo da simplificação: stories que continuam simulando o desenho antigo deixam documentação executável desalinhada com o código real.
* O painel também não precisava compartilhar lista de processos via `useProcessos`: havia um único consumidor real, nenhum reaproveitamento entre telas e nenhum ganho claro de manter paginação/lista como singleton.
* Depois desse corte, `useProcessos` ficou concentrado em detalhe, contexto completo e ações de processo. Isso é um bom sinal de fronteira útil: menos leitura local disfarçada de estado global e mais API alinhada ao domínio.
* Nem todo helper precisa continuar como composable. Quando a abstração encolhe para uma ou duas funções puramente locais, com um único consumidor real, voltar para o componente reduz navegação e custo cognitivo.
* `useValidacao` entrou nesse caso: manter um arquivo separado só para descobrir o primeiro campo com erro já não compensava, e o melhor destino passou a ser o próprio `ProcessoFormFields`.
