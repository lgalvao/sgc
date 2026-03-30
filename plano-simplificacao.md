# Plano de Simplificação do SGC

Este plano consolida recomendações concretas sustentadas pela leitura do código atual. O objetivo é reduzir
complexidade acidental sem perder regras de negócio, segurança, transações, contratos de API ou padronizações úteis já
existentes.

## Princípios gerais

* Simplificar primeiro a menor fronteira segura.
* Tornar dependências e fluxos explícitos.
* Reduzir superfície pública antes de criar abstração nova.
* Desconfiar de abstração genérica com um único consumidor real.
* Preservar contratos externos, DTOs e regras de acesso.
* Remover código morto logo após a simplificação.
* Validar em passos pequenos e registrar aprendizado no próprio plano.
* Usar medição como guarda da simplificação, não como objetivo separado.

## Guardrails obrigatórios

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
* Novas facades só devem ser criadas quando centralizarem regra transversal clara.
* Acesso direto de controllers a repositórios: restrito a leituras triviais sem regra de negócio nem segurança
  contextual.

## Situação atual

### O que já foi concluído

#### Backend — módulo `subprocesso`

* Mapeamento de duplicações concluído.
* `SubprocessoTransicaoService` reutiliza `SubprocessoService.buscarSubprocesso(...)` e
  `SubprocessoService.obterUnidadeLocalizacao(...)` — duplicatas locais removidas.
* Métodos de leitura em `SubprocessoService` reutilizam `buscarSubprocesso(...)` em vez de repetir acesso direto ao
  repositório.
* `SubprocessoService.obterDetalhes(...)` reutiliza `obterUnidadeLocalizacao(...)`.
* Coleta de atividades e detecção de atividades sem conhecimento centralizadas em `SubprocessoValidacaoService`.
* `SubprocessoTransicaoService` delega validação completa ao serviço de validação em vez de refazer leitura do mapa.
* Bloco de histórico de análises reutiliza helper privado comum por tipo.
* Contexto privado de fluxo reduziu condicionais repetidas em devolução, aceite, homologação e ações em bloco.
* README do módulo `subprocesso` reescrito para refletir a arquitetura real.

#### Frontend — remoção de abstrações

* `useProcessos` — removido (consumidores migrados para chamadas diretas de service).
* `useLoadingManager` — removido (substituído por `ref` local).
* `useModalManager` — removido (substituído por `ref` local).
* `useApi` — removido (substituído por estado local no componente).
* `useValidacao` — removido (absorvido por `ProcessoFormFields`).
* `useConfiguracoesStore` (Pinia) — migrada para composable local `useConfiguracoes`.
* `mapaService` — removido (consulta já exposta por `subprocessoService`).

#### Frontend — estreitamento de composables e views

* `useMapas`: estado morto removido (`mapaVisualizacao`, `mapaAjuste`). API reduzida a `mapaCompleto`,
  `impactoMapa`, `buscarMapaCompleto`, `buscarImpactoMapa`.
* `useFluxoMapa`: não escreve mais em `useMapas` por dentro — retorna o mapa atualizado para a view decidir.
* `useFluxoSubprocesso`: não recarrega mais `processoDetalhe` por efeito colateral.
* `useSubprocessos.buscarContextoEdicao(...)`: não sincroniza mais `useMapas` por efeito colateral.
* `useSubprocessos`: helper único para pré-condição de perfil/unidade, eliminando duplicação interna.
* `MapaView`: helpers locais de sincronização e recarga.
* `SubprocessoView`: usa `buscarContextoEdicao(...)` como carga principal; removeu dependência de `useProcessos`.
* `CadastroView`: busca direta do subprocesso; removeu dependência de `buscarProcessoDetalhe(...)`.
* `CadastroVisualizacaoView`: removeu dependência de `processos.processoDetalhe`.
* `MapaVisualizacaoView`: paralelização segura no `onMounted`; removeu `useProcessos` como fachada.
* `ProcessoCadastroView`: chama `processoService` direto; trata erro estruturado de API separadamente.
* `ProcessoDetalheView`: carrega contexto via `processoService` com estado local.
* `SubprocessoCards`: removeu consulta a `useProcessos` — bloqueio de finalização já vem do backend.
* `HistoricoView` e `ImportarAtividadesModal`: migrados para leitura direta de service.
* `UnidadesView` e `AdministradoresView`: migrados para `useAsyncAction`.
* `UnidadeView`: referência explícita de mapa vigente vinda do backend, sem depender de `mapasStore`.
* Modal de impacto: composable curto (`useImpactoMapaModal`) consolidou ciclo de `mostrar/loading/buscar`.
* Stores reduzidas a 2: `perfil.ts` e `toast.ts` — ambas legitimamente globais.

### Pontos de atenção herdados

* O reaproveitamento em `SubprocessoTransicaoService` reduziu duplicação, mas aumentou acoplamento com
  `SubprocessoService`. Em rodada futura, avaliar se `buscarSubprocesso(...)` e `obterUnidadeLocalizacao(...)` devem
  migrar para um componente neutro de consulta.
* `SubprocessoService.java` (42.7 KB) e `SubprocessoTransicaoService.java` (33 KB) continuam grandes e aceitam mais
  rodadas incrementais.
* `useMapas` permanece como singleton intencional. Monitorar para não reintroduzir escrita implícita por outros
  composables.
* `LoadingButton.vue` continua como wrapper fino, mas com uso extenso (~10 arquivos). Remoção imediata é cara;
  impedir novos wrappers fracos e reavaliar junto de refatorações maiores.

---

## Próxima fase — Frentes A–D

Os próximos passos devem ser executados como frentes complementares, não como otimizações isoladas.

### Frente A. Recentrar a fonte de verdade no backend

**Problema:**
O frontend ainda carrega resquícios do protótipo inicial sem backend: regras de workflow, validações e guardas locais
que duplicam comportamento já protegido no backend. Isso aumenta manutenção e abre risco de divergência com
`etc/reqs` e `etc/docs/regras-acesso.md`.

**Objetivo:**

* Deixar o frontend responsável por estado de tela, navegação e feedback visual.
* Deixar o backend como fonte de verdade de permissão, situação e validação de negócio.

**Critério de execução:**

* Remover primeiro guardas locais redundantes de situação e workflow.
* Manter apenas validações locais de formulário e consistência puramente visual.
* Evitar reimplementar regra de permissão fora de `useAcesso` e do contrato vindo do backend.

### Frente B. Reduzir round-trips por tela

**Problema:**
No Oracle real, o custo do vai-e-vem ficou visível. O frontend ainda faz sequências de chamadas finas demais para
montar uma única tela.

**Objetivo:**

* Preferir cargas de contexto por tela ou por fluxo.
* Evitar sequências como "buscar id → buscar detalhe → buscar mapa → buscar permissões" quando um endpoint de
  contexto já pode entregar isso junto.

**Candidatos prioritários:**

* `SubprocessoView`
* `CadastroView`
* `CadastroVisualizacaoView`
* `MapaVisualizacaoView`

**Critério de execução:**

* Cada tela deve ter inventário explícito de chamadas no `onMounted`.
* Se duas ou mais chamadas sempre andam juntas, avaliar consolidação.
* Preferir reutilizar endpoint de contexto existente antes de criar endpoint novo.
* Criar endpoint novo apenas quando a composição atual continuar cara demais ou incompleta.
* Sempre que possível, transformar a redução de round-trips em teste com orçamento explícito de chamadas por tela.

**Validação de desempenho:**

* No frontend, usar testes de orçamento de chamadas para evitar regressão de round-trips lógicos.
* No backend, confrontar os fluxos críticos com o `MonitoramentoAspect`.
* Leitura combinada: menos chamadas por tela + menos `EXECUCAO LENTA` + redução de latência percebida.

### Frente C. Estreitar composables para refletir fluxos reais

**Problema:**
Alguns composables ainda foram desenhados como infraestrutura genérica do protótipo, não como representação do fluxo
real do SGC. Isso gera escrita implícita em stores paralelos, API pública larga demais e acoplamento desnecessário.

**Objetivo:**

* Manter cada composable responsável por um fluxo claro.
* Eliminar sincronizações implícitas.
* Devolver dados para a view quando isso tornar o fluxo mais legível.

**Critério de execução:**

* Se o composable altera outro store por dentro, reavaliar.
* Se a view é o único consumidor, preferir helper local ou composable de escopo curto.
* Se uma abstração genérica tiver só um consumidor real, preferir estado e helpers explícitos na própria tela.
* Se o estado é realmente compartilhado, explicitar isso e manter o singleton.

### Frente D. Consolidar backend para servir melhor o frontend real

**Problema:**
Parte da complexidade percebida no frontend nasce de endpoints finos ou fragmentados demais para o uso real das telas.
Simplificar só no frontend não resolve o custo de orquestração quando a tela depende de múltiplos pedaços sempre
carregados juntos.

**Objetivo:**

* Identificar onde vale expor contexto agregado útil para a tela.
* Manter DTOs e fronteiras seguras.
* Evitar que a simplificação de frontend empurre regra para a UI.

**Critério de execução:**

* Primeiro reutilizar `contexto-edicao` e endpoints já existentes.
* Depois, se necessário, criar DTO/endpoint agregado orientado à tela.
* Nunca substituir isso por exposição direta de entidade ou acoplamento controller-repo.

---

## Backlog pendente de prioridade menor

### Auditar wrappers visuais finos

**Escopo:** `LoadingButton.vue` e demais componentes comuns com baixo volume de lógica própria.

**Perguntas de triagem:**

* Padroniza algo recorrente?
* Reduz duplicação material?
* Adiciona acessibilidade ou comportamento?
* Evita divergência visual entre telas?

**Critério de pronto:** cada componente auditado classificado como manter, ajustar ou remover.

### Formalizar diretrizes para facades e acesso direto a repositórios

**Objetivo:** documentar as regras já adotadas na prática (seção Guardrails) em local visível do projeto.

**Critério de pronto:** diretriz registrada; novos PRs passam a citar esse critério.

### Registrar diretriz de DTOs

**Objetivo:** documentar formalmente que DTO não é boilerplate descartável. Conteúdo mínimo:

* DTO protege contrato e carregamento;
* entidade JPA não deve vazar para API por conveniência;
* remoção exige verificação de contrato, lazy loading e serialização.

**Critério de pronto:** regra documentada em local visível do módulo backend.

---

## Critério de sucesso

O plano terá sido bem executado se:

* houver menos pontos de navegação para seguir um fluxo simples;
* não houver perda de regras de segurança, transação ou notificação;
* o frontend reduzir singletons desnecessários sem espalhar tratamento de erro;
* componentes comuns restantes tiverem justificativa clara de existência.

---

## Aprendizados consolidados

Regras empíricas extraídas das rodadas anteriores, para orientar decisões futuras:

1. **Duplicação antes de fusão.** O melhor candidato inicial não é fusão de serviço, e sim remoção de duplicação
   utilitária.
2. **Reuso com registro.** Reutilizar serviço existente é aceitável como etapa intermediária, desde que o
   acoplamento gerado fique registrado e seja reavaliado.
3. **Assinaturas públicas.** Ao simplificar serviços usados por testes, preservar assinaturas públicas ou introduzir
   sobrecargas compatíveis. Porém, quando a simplificação remove API redundante, os testes devem ser atualizados.
4. **Testes seguem o fluxo real.** Quando uma simplificação troca o collaborator principal, os testes precisam
   validar o collaborator novo e o comportamento real do branch.
5. **Singleton só quando compartilhado.** Quando a abstração encolhe para uma ou duas funções com um único
   consumidor, voltar para o componente.
6. **View como dona da sincronização.** Quando o composable de fluxo retorna o dado atualizado, a view fica mais
   previsível e os testes deixam de depender de efeito colateral.
7. **Round-trips como critério de design.** Chamadas pequenas demais em sequência têm custo perceptível em Oracle.
   Testes de orçamento de chamadas evitam regressão durante refatorações.
8. **Erro estruturado vs. genérico.** Payload de validação de API deve ser tratado diferente de `Error` genérico:
   o primeiro mapeia campos, o segundo exibe mensagem padrão.
9. **Cache de build.** `compileTestJava` pode falhar por cache; `--no-configuration-cache` antes de tratar como
   regressão.
10. **`@RequestBody(required = false)`.** Não usar `Optional.of(...)` no controller — reintroduz `500` em fluxos
    sem payload.
11. **Storybook acompanha simplificação.** Stories que simulam o desenho antigo deixam documentação executável
    desalinhada.
12. **README como débito técnico.** Documentação de módulo também entra no ciclo de simplificação; README
    desatualizado mascara responsabilidades reais.
