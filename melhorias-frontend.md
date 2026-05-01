# Melhorias do frontend

## Escopo da análise

Análise feita sobre o checkout atual em `frontend/src`, com foco em consistência, caching, defensividade excessiva, verbosidade, chamadas duplicadas ou sobrepostas ao backend, carregamento de dados e organização. Não foram executadas alterações de código nesta rodada; este arquivo é um mapa de achados e próximos passos.

Medições rápidas do código de produção, sem testes e sem stories:

- 140 arquivos de produção em `frontend/src` contra 143 arquivos de teste.
- Maiores telas: `CadastroView.vue` com 886 linhas, `MapaView.vue` com 859, `SubprocessoView.vue` com 579, `NotificacoesAdminView.vue` com 490 e `ProcessoCadastroView.vue` com 470.
- Maiores focos de estado local: `CadastroView.vue` com 16 `ref(...)`, `MapaView.vue` com 14 e `SubprocessoView.vue` com 9.
- Maiores focos de regras derivadas: `useAcesso.ts` com 50 `computed(...)`, `MapaView.vue` com 22 e `CadastroView.vue` com 12.
- Maiores services HTTP: `subprocessoService.ts` com 29 chamadas `apiClient`, `processoService.ts` com 25, `cadastroService.ts` com 10 e `atividadeService.ts` com 9.

## Achados

### 1. Documentação local descreve uma arquitetura que não existe mais

`frontend/README.md` ainda afirma o fluxo `Views -> Stores -> Services -> Backend`, mas o código atual mistura stores, composables de domínio, services diretos nas views e orquestrações intermediárias. Isso aparece, por exemplo, em `PainelView.vue`, que chama `painelService` diretamente e escreve no `usePainelStore`, e em `CadastroView.vue`/`MapaView.vue`, que combinam store, composables, services e estado local.

`frontend/src/stores/README.md` cita stores inexistentes (`usuarios.ts`, `feedback.ts`, `alertas.ts`, `atividades.ts`, `diagnosticos.ts`, `atribuicoes.ts`, `analises.ts`, `unidades.ts`) e também diz que processo/subprocesso/mapa ficam prioritariamente em composables, enquanto o checkout atual tem `processo.ts`, `subprocesso.ts` e `mapas.ts`.

`frontend/src/composables/README.md` também cita composables que não existem (`useRelatorios.ts`, `useProcessoView.ts`, `useUnidadeView.ts`, `useCadAtividades.ts`, `useVisAtividades.ts`, `useVisMapa.ts`). Isso atrapalha novos cortes porque agentes tendem a seguir um contrato arquitetural falso.

Passos:

- Atualizar os READMEs do frontend para descrever o padrão real desejado, não o histórico.
- Definir uma regra simples: estado global só para sessão, cache compartilhado de fato e feedback pendente entre rotas; tela de uso único fica local.
- Registrar explicitamente que services não precisam passar por store quando a store não agrega cache, dedupe ou contrato de sessão.

### 2. A fronteira entre `processoService` e `subprocessoService` está misturada

`processoService.ts` contém endpoints de processo, mas também operações de subprocesso e mapa: alterar data limite, apresentar sugestões, validar mapa, homologar/aceitar/devolver validação, reabrir cadastro e reabrir revisão. Ao mesmo tempo, `subprocessoService.ts` centraliza contexto, mapa, competências, histórico e ações em bloco de subprocesso.

Exemplos concretos:

- `processoService.ts` chama `/subprocessos/{codSubprocesso}/data-limite`, `/subprocessos/{codSubprocesso}/apresentar-sugestoes`, `/subprocessos/{codSubprocesso}/validar-mapa`, `/subprocessos/{codSubprocesso}/homologar-validacao`, `/subprocessos/{codSubprocesso}/aceitar-validacao`, `/subprocessos/{codSubprocesso}/devolver-validacao`, `/subprocessos/{codSubprocesso}/reabrir-cadastro` e `/subprocessos/{codSubprocesso}/reabrir-revisao-cadastro`.
- `useFluxoMapa.ts` importa ações de mapa dos dois services.
- `useMapaAcoesAnalise.ts` chama diretamente `processoService` para ações de validação do mapa, duplicando parte da intenção já exposta por `useFluxoMapa.ts`.
- `SubprocessoView.vue` importa `enviarLembrete` de `processoService`, embora a tela esteja no contexto de subprocesso.

Passos:

- Mover ações com rota `/subprocessos/...` para `subprocessoService.ts`, preservando os nomes públicos por domínio.
- Manter em `processoService.ts` apenas endpoints realmente de `/processos/...` ou agregados de processo.
- Consolidar `useMapaAcoesAnalise.ts` e `useFluxoMapa.ts`: hoje ambos sabem validar, aceitar, homologar e devolver mapa.
- Ajustar testes de services depois da movimentação para garantir que nenhum endpoint mudou.

### 3. Caching existe, mas a política não é uniforme

Há pelo menos quatro modelos de cache em paralelo:

- `usePainelStore` guarda processos/alertas com TTL de 5 minutos.
- `useHistoricoStore` guarda histórico indefinidamente até invalidação manual.
- `useUnidadeStore` cacheia árvores, unidades e mapas vigentes por `Map`, com dedupe por promessa apenas para árvore de elegibilidade.
- `useSubprocessoStore` faz dedupe por chave e guarda apenas um `contextoEdicao` e um `contextoCadastro` correntes, além de mapas auxiliares processo/unidade.

Além disso, a invalidação está espalhada:

- `usePerfilStore` invalida painel, processo, subprocesso, unidade e mapas ao trocar perfil/logout.
- `useInvalidacaoNavegacao` invalida parte dos mesmos caches após workflow.
- `useCacheSync` invalida unidade, organização, painel e processo via SSE, mas não invalida `subprocesso` nem `mapas`.
- Algumas telas ainda fazem recarga direta após mutação, por exemplo `SubprocessoView.vue` invalida caches e logo em seguida chama `carregarSubprocesso()`.

Esse desenho funciona em casos felizes, mas fica difícil saber se a fonte válida é a store, a view, a rota reativada por `KeepAlive` ou o backend.

Passos:

- Criar uma tabela de política por recurso: `painel`, `historico`, `processo`, `subprocesso`, `mapa`, `organizacao`, `unidade`.
- Para cada recurso, definir: escopo do cache, chave, TTL ou invalidação explícita, eventos que invalidam, e se a mutação retorna payload suficiente para atualizar localmente.
- Unificar `useInvalidacaoNavegacao` e `useCacheSync` em torno da mesma política, evitando listas divergentes de stores.
- Em `useProcessoStore.invalidar()`, decidir se deve limpar também `contextoCompleto`/`codProcessoCarregado`; hoje o comentário diz "invalida o cache", mas só limpa promessas em andamento.
- Em `useCacheSync`, tratar erro/reconexão do SSE antes de depender dele como mecanismo de invalidação confiável.

### 4. O carregamento de mapa ainda faz chamada sobreposta em modo somente leitura

`useMapaOrquestracao.ts` primeiro carrega `contexto-edicao`, sincroniza `data.mapa` em `mapasStore.mapaCompleto`, e depois, quando `podeEditarMapa` é falso, chama `obterMapaVisualizacao(codigo)`. O backend já expõe `mapa-visualizacao`, `mapa-completo` e `contexto-edicao`; a tela hoje pode trazer contexto com mapa e ainda buscar outra representação para leitura.

Isso pode ser correto se os payloads forem propositalmente diferentes, mas o contrato não está explícito na camada de frontend. O resultado é uma decisão de performance escondida dentro de um composable de orquestração.

Passos:

- Comparar os DTOs reais de `contexto-edicao`, `mapa-completo` e `mapa-visualizacao`.
- Se `contexto-edicao` já contiver o necessário para leitura, remover a chamada extra.
- Se `mapa-visualizacao` for a fonte correta para leitura, evitar carregar mapa completo no caminho de leitura ou criar endpoint agregado específico para `MapaView` por modo.
- Medir antes/depois com `SGC_MONITORAMENTO=sim` em um fluxo de abertura de mapa.

### 5. Telas grandes concentram estado, regra, workflow, validação e DOM

`CadastroView.vue` e `MapaView.vue` são os exemplos mais claros. Elas acumulam:

- estado de modais;
- flags de loading por ação;
- validação de formulário;
- decisões de permissão;
- chamadas de workflow;
- sincronização manual de store;
- manipulação de foco/scroll;
- mensagens de domínio;
- adaptação de payload de backend.

Em `CadastroView.vue`, o watcher de `disponibilizacaoSemMudancas` chega a iniciar/cancelar revisão com efeito colateral no backend. Em `MapaView.vue`, a tela sincroniza manualmente `mapasStore.mapaCompleto` e também muta `subprocessoStore.contextoEdicao.mapa` após salvar competências.

Passos:

- Quebrar por fluxo de usuário, não por camada genérica. Bons candidatos:
  - `useCadastroRevisaoSemMudancas` para iniciar/cancelar revisão e controlar checkbox.
  - `useCadastroAtividadesMutacoes` para adicionar/remover/editar atividade e conhecimento.
  - `useMapaCompetenciasMutacoes` para criar/editar/excluir competência e atualizar mapa.
  - `useMapaSugestoes` para abrir, carregar, editar e submeter sugestões.
- Cada composable deve retornar dados ou resultado explícito; evitar escrever em outra store por dentro quando a view pode aplicar o retorno.
- Depois de cada extração, reduzir `defineExpose` dos testes para cobrir comportamento público, não detalhes internos acumulados.

### 6. Há defensividade excessiva e sinais de contratos frouxos

Foram encontrados casts como `as unknown` em services e stores (`unidadeService.ts`, `usuarioService.ts`, `perfil.ts`, `ModalConfirmacao.vue`) e muitos `catch` locais que convertem falhas em `null`, `false`, lista vazia ou toast genérico.

Exemplos:

- `useSubprocessoStore` retorna `null` em falhas de integração, enquanto também mantém `erroIntegracaoContexto`. A view precisa interpretar dois canais de erro.
- `useMapaOrquestracao` captura erro, loga e retorna `false`, e a view precisa decidir se mostra erro da store ou mensagem genérica.
- `useFluxoSubprocesso.executarAcaoWorkflow` captura qualquer erro e retorna `false`; algumas telas perdem o motivo normalizado e exibem mensagem genérica.
- `unidadeStore.garantirArvoreElegibilidade` captura erro e retorna `[]`, tornando indistinguível "sem unidades" de "falha ao carregar".

Passos:

- Padronizar retornos de operações assíncronas em uma de duas formas: lançar erro normalizado para a view tratar, ou retornar `Resultado<T>` discriminado. Evitar misturar `null`, `false`, erro em store e toast.
- Remover casts `as unknown` conforme os DTOs forem estreitados. O alvo não é "zero cast" imediato; é eliminar casts em fronteiras de API primeiro.
- Trocar retornos silenciosos de lista vazia por erro exibível quando a falha muda o significado da tela.

### 7. `useAcesso.ts` virou espelho grande do backend

`useAcesso.ts` expõe cerca de 50 `computed(...)` para permissões, habilitações e visibilidade. Parte disso é importante porque `regras-acesso.md` diferencia ocultar e desabilitar, mas a concentração atual cria risco de divergência: qualquer nova ação precisa ser modelada no backend, no DTO e em múltiplos computed do frontend.

Passos:

- Separar `useAcesso` em grupos por domínio: cadastro, mapa, subprocesso/admin e sessão.
- Preferir consumir ações prontas vindas do backend (`acaoPrincipalCadastro`, `acaoPrincipalMapa`, listas de ações visíveis/habilitadas) quando já existirem no DTO.
- Manter no frontend apenas composição visual simples, sem rederivar regra de negócio que já chegou no payload.

### 8. Há duplicação de carregamento e transformação em relatórios/importação

`RelatorioMapasView.vue` busca árvore de unidades e códigos com mapa em paralelo, enquanto `unidadeService`/backend já possuem conceitos de árvore, mapa vigente e elegibilidade. `ImportarAtividadesModal.vue` faz uma coreografia em três etapas: processos para importação, unidades do processo selecionado e atividades da unidade selecionada.

Esses fluxos podem estar aceitáveis em volume baixo, mas são bons candidatos a payloads agregados porque o backend já conhece as relações.

Passos:

- Medir `ImportarAtividadesModal` e `RelatorioMapasView` com monitoramento antes de refatorar.
- Se houver latência ou repetição real, criar endpoints agregados por tela:
  - opções de importação por processo, com unidades elegíveis e metadados necessários;
  - árvore de relatório de mapas já anotada com `temMapaVigente`.
- Remover chamadas intermediárias no frontend só depois do contrato agregado existir e estar testado.

### 9. Organização de testes e arquivos reflete acúmulo histórico

Há testes duplicados por localização e nome: por exemplo, existem arquivos em `frontend/src/components/__tests__` e também em `frontend/src/components/<dominio>/__tests__` para componentes como `TabelaProcessos`, `SubprocessoCards`, `ModalAcaoBloco`, `LoadingButton` e outros. Também há sufixos de cobertura (`Coverage`, `Uncovered`) espalhados em views.

Isso não é erro funcional, mas aumenta custo de manutenção e favorece correções parciais em um teste enquanto outro fica preso ao contrato antigo.

Passos:

- Escolher um padrão de localização por tipo de teste: co-localizado por domínio ou diretório central, não ambos para o mesmo componente.
- Consolidar testes `Coverage`/`Uncovered` quando eles deixarem de representar lacuna temporária.
- Antes de mover testes, rodar a suíte afetada para evitar perder cobertura de casos reais.

## Plano recomendado

### Fase 1: estabilizar o mapa arquitetural

1. Atualizar os READMEs de `frontend`, `stores`, `composables` e `services`.
2. Criar uma pequena tabela de política de cache em `frontend/README.md` ou documento dedicado.
3. Definir a regra de fronteira: `processoService` não deve exportar ações de `/subprocessos/...`.

Validação: `npm run typecheck` e testes unitários dos arquivos alterados.

### Fase 2: reduzir overlap de services e workflows

1. Migrar ações de subprocesso de `processoService.ts` para `subprocessoService.ts`.
2. Consolidar `useMapaAcoesAnalise.ts` com `useFluxoMapa.ts`.
3. Ajustar imports nas views sem mudar endpoints.

Validação: testes de `processoService`, `subprocessoService`, `useFluxoMapa`, `MapaView` e `SubprocessoView`.

### Fase 3: tornar cache e invalidação previsíveis

1. Corrigir `useProcessoStore.invalidar()` para combinar comportamento e comentário.
2. Centralizar a lista de recursos invalidados por evento de workflow.
3. Revisar `useCacheSync` para reconexão/erro e alinhar invalidação de `mapas`/`subprocesso`.

Validação: testes de stores e um E2E representativo de workflow com navegação de volta por `KeepAlive`.

### Fase 4: atacar telas grandes por fluxo

1. Extrair revisão sem mudanças de `CadastroView.vue`.
2. Extrair mutações de atividades de `CadastroView.vue`.
3. Extrair mutações de competências de `MapaView.vue`.
4. Extrair sugestões de mapa de `MapaView.vue`.

Critério de aceite: cada extração reduz estado local da view e mantém o retorno explícito do fluxo.

### Fase 5: medir chamadas sobrepostas antes de criar endpoints agregados

1. Rodar monitoramento em abertura de painel, processo, subprocesso, cadastro, mapa, relatórios e importação.
2. Priorizar apenas fluxos com round-trips sobrepostos comprovados.
3. Criar contratos agregados no backend quando ele já possuir a informação completa.

Validação: comparação antes/depois com `SGC_MONITORAMENTO=sim` e testes de contrato no backend/frontend.

## Ordem prática dos primeiros cortes

1. Corrigir documentação local desatualizada.
2. Mover ações `/subprocessos/...` para `subprocessoService.ts`.
3. Unificar `useMapaAcoesAnalise` e `useFluxoMapa`.
4. Ajustar `useProcessoStore.invalidar`.
5. Medir `MapaView` em modo somente leitura para decidir se a chamada extra de `mapa-visualizacao` deve ficar.
6. Extrair `useCadastroRevisaoSemMudancas`.

Essa sequência reduz confusão estrutural antes de mexer nas telas maiores, mantém diffs pequenos e evita criar endpoint novo sem evidência de round-trip real.
