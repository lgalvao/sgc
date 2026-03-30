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
* Remoção de wrappers redundantes (ex: `podeVerImpacto` centralizada para utilizar nativamente `podeVisualizarImpacto`).
* Validações de UI (como `isSituacaoMapaValidadoOuAjustado`) não bloqueiam o workflow, funcionam puramente como filtro reativo.

#### Backend — DTOs, Desempenho e Diretrizes

* Criação das `Diretrizes Arquiteturais` (`/etc/docs/diretrizes-arquiteturais.md`), amarrando formalmente a regra de injeção de DTOs e uso explícito de Facades em oposição a chamadas de cascata excessivas.
* Correção do ciclo E2E via supressões de *LazyInitializationException*, encapsulando a população completa da coleção de `participantes` com `LEFT JOIN FETCH` diretamente nos métodos do `ProcessoRepo.java`.
* A carga centralizada unificada por `ProcessoDetalheView` e `buscas` foi mantida intacta pois atende integralmente a redução de round-trips e performance de backend exigidos na Frente D.

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

### Frente E. Redução de Classes Hipertrofiadas no Backend

**Problema:**
Serviços como `SubprocessoService` (42.7 KB) e `SubprocessoTransicaoService` (33 KB) concentram responsabilidades demais e estão difíceis de manter.
Embora o reaproveitamento inicial tenha removido duplicações locais, o acoplamento entre esses componentes ainda é alto e o volume de linhas penaliza a manutenibilidade.

**Objetivo:**
* Extirpar domínios paralelos (como orquestração de leitura x transições).
* Separar fluxos de workflow num componente coeso (ex: `SubprocessoWorkflowFacade` ou segregando a orquestração).
* Distribuir consultas (leitura) isoladas para um `SubprocessoConsultaService` puro.

**Critério de execução:**
* Antes de cortar classes, mapear interdependências atuais.
* Reduzir o acoplamento entre essas duas superclasses transferindo a interface de dados puramente a componentes menores quando viável.
* Preservar o 100% de cobertura nos testes de unidade (1401 testes green).

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
