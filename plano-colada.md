# Plano Prático de Adoção do Pinia Colada no Frontend

## Objetivo

Reduzir a infraestrutura caseira de **server state** no frontend do SGC sem substituir o uso de `Pinia` para estado de aplicação, sessão e UI local.

O foco é usar `Pinia Colada` para:

- queries com cache;
- deduplicação de requisições;
- invalidação por chave;
- estados de loading/erro de leitura remota;
- simplificação de fluxos hoje baseados em `dadosValidos()`, `garantirDados()`, TTL manual e `invalidar()`.

## Princípios

- `Pinia` continua responsável por estado de aplicação e sessão.
- `Pinia Colada` entra apenas onde o problema principal é **cache de dados remotos**.
- Migrar por fatias pequenas, começando pelos casos mais simples.
- Não reescrever `subprocesso` e `mapas` antes de provar ganho real em casos menores.
- Cada fase precisa terminar com validação real e redução perceptível de código.

## Escopo

### Fica em Pinia

- sessão e autorização (`perfil`);
- preferências locais e tema;
- toast pendente;
- estado efêmero de modal, formulário e workflow;
- coordenação de logout e reset de sessão.

### Candidatos imediatos ao Colada

- `historico`;
- `painel`;
- `processo`;
- leituras administrativas simples feitas direto nas views.

### Casos para depois

- `subprocesso`;
- `mapas`;
- `unidade`;
- `configuracoes`;
- `relatorios`.

## Ordem de execução

## Fase 0 - Preparação

### Meta

Instalar e preparar a infraestrutura mínima do `Pinia Colada` sem alterar comportamento funcional.

### Passos

1. Adicionar a dependência do `Pinia Colada` no frontend.
2. Registrar o plugin no bootstrap da aplicação.
3. Definir uma convenção local para chaves de query.
4. Definir uma convenção de invalidação por domínio.

### Convenção inicial de chaves

- `["historico"]`
- `["painel"]`
- `["processo", codProcesso]`
- `["notificacoes-admin"]`
- `["feedbacks-admin"]`

### Critério de sucesso

- aplicação sobe normalmente;
- nenhum comportamento existente muda;
- typecheck e lint seguem verdes.

## Fase 1 - Piloto no Histórico

### Meta

Substituir o cache manual do histórico por uma query simples.

### Alvo

- `frontend/src/stores/historico.ts`
- `frontend/src/views/HistoricoView.vue`

### Estratégia

1. Criar query `historico` com `useQuery`.
2. Remover `carregado`, `carregando`, `garantirDados()` e dedupe manual do store.
3. Fazer a view consumir a query em vez do store de cache.
4. Manter ordenação local da tabela fora da query.

### Resultado esperado

- eliminação do store como cache remoto;
- remoção de `dadosValidos()` e parte da lógica de `onActivated()`;
- loading e erro vindos da query.

### Critério de sucesso

- histórico continua carregando no primeiro acesso;
- navegação em `keepAlive` não força gambiarras de recarga;
- testes da view e fluxo de histórico continuam passando.

## Fase 2 - Migração do Painel

### Meta

Trocar o cache manual do painel por query com invalidação explícita.

### Alvo

- `frontend/src/stores/painel.ts`
- `frontend/src/views/PainelView.vue`
- `frontend/src/composables/useInvalidacaoNavegacao.ts`
- `frontend/src/composables/useCacheSync.ts`

### Estratégia

1. Criar query `painel` baseada no bootstrap já existente.
2. Remover TTL manual, `dadosValidos()` e `definirDados()` do store.
3. Preservar separadamente apenas o que for estado local útil, como marcação otimista de alertas lidos, se ainda fizer sentido.
4. Adaptar invalidação para `invalidateQueries({ key: ["painel"] })`.
5. Adaptar o SSE organizacional para invalidar queries do domínio afetado, em vez de invalidar stores inteiras.

### Resultado esperado

- menos acoplamento entre `PainelView`, `painelStore`, `useCacheSync` e `useInvalidacaoNavegacao`;
- fim do TTL manual no store;
- query cache mais previsível.

### Critério de sucesso

- painel carrega normalmente;
- mutações que afetam o painel continuam refletindo após invalidação;
- evento SSE continua forçando recarga na próxima leitura.

## Fase 3 - Contexto de Processo

### Meta

Migrar o contexto completo de processo, hoje tratado como cache/dedupe manual.

### Alvo

- `frontend/src/stores/processo.ts`
- `frontend/src/views/ProcessoDetalheView.vue`
- pontos de invalidação que hoje chamam `processoStore.invalidar()`

### Estratégia

1. Criar query `["processo", codProcesso]`.
2. Remover `carregamentosEmAndamento`, `dadosValidos()` e `garantirContextoCompleto()` do store.
3. Ajustar a tela para consumir o estado reativo da query.
4. Preservar no `Pinia` apenas o que ainda for estado local de navegação/sessão, caso reste algo.

### Critério de sucesso

- processo abre e reabre corretamente;
- keepAlive não exige recarga manual baseada em store stale;
- mutações continuam invalidando a query certa.

## Fase 4 - Leituras Administrativas Simples

### Meta

Usar Colada diretamente em views que hoje fazem fetch manual com `ref + try/finally`.

### Alvos prioritários

- `NotificacoesAdminView.vue`
- `FeedbacksAdminView.vue`

### Estratégia

1. Criar queries dedicadas por tela.
2. Substituir `carregando`, `erro` e `carregar()` manuais por query e refresh.
3. Manter no componente apenas estado local de modal, item selecionado e ação em andamento.

### Critério de sucesso

- menos boilerplate por tela;
- refresh explícito continua funcionando;
- modais e ações seguem intactos.

## Fase 5 - Reavaliação dos Casos Híbridos

### Meta

Decidir com evidência se vale migrar parcialmente `subprocesso`, `mapas`, `unidade` e `configuracoes`.

### Perguntas para responder

- o ganho é real ou só muda o lugar da complexidade?
- a chave de query é clara e estável?
- há atualização local rica demais para uma query simples?
- parte do estado deve permanecer em `Pinia`?

### Regra

Não migrar esses casos por simetria. Só migrar se os pilotos anteriores tiverem reduzido código e simplificado testes de verdade.

## Mudanças esperadas na arquitetura

### Depois das fases 1 a 4

- menos stores usadas como cache remoto;
- menos `invalidar()` genérico espalhado;
- menos `onActivated()` com checagem manual de stale;
- menos `loading/error` replicado por view;
- invalidação mais semântica por chave de query.

### O que continua existindo

- `Pinia` para sessão e UI local;
- orquestração de mutações;
- invalidação orientada a domínio;
- estados locais de fluxo e formulários.

## Validação por fase

Executar no `frontend/`:

```bash
npm run quality:lint
npm run quality:typecheck
npm run test:unit
```

Quando a fase tocar navegação relevante, também validar o fluxo real da tela afetada.

## Critério de aprovação do plano

O plano só está funcionando se, ao final das primeiras fases:

- houver menos código de cache manual;
- houver menos estados duplicados de loading/erro;
- a invalidação ficar mais localizada;
- os testes ficarem mais simples, não mais frágeis.

Se isso não acontecer já no `historico` e no `painel`, interromper a adoção e reavaliar.
