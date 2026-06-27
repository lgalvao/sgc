# Plano de organização de feedbacks e redução de `BAlert`

Este documento registra o diagnóstico do estado atual do frontend e propõe uma sequência prática para padronizar
feedbacks de interface no SGC.

O objetivo não é apenas "arrumar toasts". O objetivo é tornar a UX de feedback mais clara, consistente e previsível,
reduzindo o uso direto de `BAlert` nas views e concentrando decisões de comportamento em primitives do app.

## Estado atual

### O que já existe de bom

- O sistema já possui um padrão reutilizável para validação inline com `useValidacaoFormulario`, `state` e
  `BFormInvalidFeedback`.
- Esse padrão já cobre um requisito importante: após a primeira tentativa inválida, a mensagem reaparece em novas
  tentativas enquanto a condição inválida continuar verdadeira.
- Em alguns fluxos, mensagens derivadas do estado já são limpas automaticamente quando a condição deixa de valer.
- O app já possui `AppAlert` como primitive base para feedback persistente e `BOrchestrator` para toast transitório.

### O principal problema

- O frontend usa `BAlert` diretamente em muitos pontos diferentes, com responsabilidades distintas:
  - erro global de tela
  - erro global de formulário ou modal
  - aviso ou erro contextual de ação
  - sucesso persistente
  - banner informativo de estado
- Hoje essas responsabilidades estão misturadas entre `BAlert`, `AppAlert`, strings locais como `erroGlobal`,
  `retornoFluxo`, `alertaSucesso` e `useToastStore`.
- O resultado é que casos parecidos são implementados de formas diferentes, com regras diferentes de:
  - exibição
  - limpeza
  - dismiss
  - reapresentação
  - localização da mensagem

### Números do checkout atual

- `21` arquivos de produção importam `BAlert` diretamente de `bootstrap-vue-next`.
- `17` arquivos de produção usam `AppAlert`.
- `19` arquivos de produção usam `BFormInvalidFeedback`.
- `13` arquivos de produção usam `useToastStore`.
- Não foi encontrado uso atual de `Vuelidate` neste checkout.

Conclusão prática:

- A base mais madura hoje está em validação inline.
- A superfície mais fragmentada hoje está em alertas persistentes.
- O plano deve partir da infraestrutura existente do checkout e não assumir uma adoção corrente de `Vuelidate`.

## Diagnóstico por famílias de uso

### 1. Validação inline próxima ao campo ou grupo

Esse é o padrão mais consistente do sistema hoje.

Exemplos:

- `frontend/src/views/LoginView.vue`
- `frontend/src/views/ConfiguracaoView.vue`
- `frontend/src/components/processo/ModalAcaoBloco.vue`
- `frontend/src/components/mapa/modais/MapaDisponibilizacaoModal.vue`
- `frontend/src/components/atividades/ImportarAtividadesModal.vue`

Direção:

- Preservar esse modelo como baseline.
- Reforçar o uso de `state` + `BFormInvalidFeedback`.
- Evitar que erro de preenchimento simples seja promovido para `BAlert`.

### 2. Erro global de formulário ou modal

Hoje esse caso aparece tanto com `BAlert` quanto com `AppAlert`, sem uma regra única.

Exemplos:

- `frontend/src/views/CadastroView.vue`
- `frontend/src/views/AtribuicaoTemporariaView.vue`
- `frontend/src/components/administracao/AdministradoresFluxoModais.vue`
- `frontend/src/components/atividades/ImportarAtividadesModal.vue`
- `frontend/src/components/processo/ModalAcaoBloco.vue`

Direção:

- Padronizar esse caso em um primitive único do app.
- O primitive deve encapsular `BAlert`, sem exigir que cada view decida markup e comportamento.

### 3. Erro persistente de tela

Algumas telas exibem erro de carregamento ou falha sistêmica com `BAlert` direto na própria view.

Exemplos:

- `frontend/src/views/UnidadeView.vue`
- `frontend/src/views/ConfiguracaoView.vue`
- `frontend/src/views/NotificacoesAdminView.vue`
- `frontend/src/views/FeedbacksAdminView.vue`
- `frontend/src/views/AdministradoresView.vue`

Direção:

- Criar um padrão explícito para erro de tela.
- A view não deve decidir isoladamente se usa `BAlert` bruto, `AppAlert` ou string local.

### 4. Sucesso na mesma tela

Esse caso hoje aparece sob nomes diferentes e precisa de uma política mais restritiva.

Exemplos:

- `frontend/src/components/diagnostico/SubprocessoDiagnosticoPainel.vue` com `alertaSucesso`
- `frontend/src/views/useAutoavaliacaoDiagnosticoView.ts` com `retornoFluxo`
- usos de `notify(..., "success")` que viram `AppAlert`

Direção:

- Não usar alerta persistente para sucesso local por padrão.
- Se a mudança já ficar óbvia pela própria interface, não mostrar mensagem de sucesso.
- Se ainda houver valor em confirmar a operação, preferir toast breve.
- Não misturar sucesso local com erro global.

### 5. Sucesso transitório após navegação

Esse é o uso mais coerente do toast hoje.

Exemplos:

- `frontend/src/composables/usePainelTela.ts`
- `frontend/src/views/useAutoavaliacaoDiagnosticoView.ts`
- `frontend/src/views/SubprocessoView.vue`
- `frontend/src/views/useFinalizacaoProcesso.ts`
- `frontend/src/composables/useProcessoMutacoes.ts`

Direção:

- Preservar toast para sucesso breve, principalmente quando a interface de origem deixou de existir por navegação ou
  redirecionamento.

## Direção arquitetural

### Regra central

`BAlert` não deve ser o primitive de domínio do app.

`BAlert` continua sendo a base visual oferecida por `BootstrapVueNext`, mas deve ficar encapsulado em components do
SGC.

### Superfície alvo

O frontend deve convergir para uma pequena família de primitives:

- `AppAlert`
  - feedback persistente de ação ou de tela
- `AppAlertaFormulario`
  - erro global de formulário ou modal
- `AppAlertaStatus`
  - banner informativo de estado, sem semântica de erro de ação
- `useToast`
  - fachada única para sucesso transitório

Observação:

- `AppAlert` atual pode ser evoluído para cobrir parte desse espaço.
- Não é necessário criar quatro components no primeiro commit de migração.
- O importante é fechar a superfície permitida e remover importações diretas de `BAlert` das views ao longo do tempo.

### Papel de `Vuelidate`

Como `Vuelidate` não aparece em uso no checkout atual, ele não deve ser tratado como dependência imediata do plano.

Direção:

- curto prazo: consolidar o que já existe com `useValidacaoFormulario`, `computed`, `state` e
  `BFormInvalidFeedback`
- médio prazo: reavaliar se vale introduzir `Vuelidate` em formulários mais complexos, depois que a superfície de
  feedback estiver mais estável

## Regras funcionais que o padrão final precisa respeitar

### 1. Reapresentação

- Se a condição inválida continua verdadeira, a mensagem deve reaparecer em novas tentativas.
- Dismiss manual não pode impedir a exibição em uma nova tentativa inválida.

### 2. Limpeza por mudança real de estado

- Se a condição de erro ou aviso deixou de valer, a mensagem deve sair automaticamente.
- Erro derivado do estado não deve ficar preso como string órfã.

### 3. Proximidade

- Erro corrigível deve ficar o mais perto possível do local de correção.
- Erro de campo permanece inline.
- Erro de grupo permanece no grupo.
- Erro de modal permanece dentro do modal.
- Erro de tela permanece no contexto principal da tela.

### 4. Toast restrito

- Toast não deve ser usado para validação.
- Toast não deve carregar erro que a pessoa precise reler com calma.
- Toast deve ser preferido para sucesso breve, especialmente após navegação.

## Sequência de execução proposta

### Fase 1. Congelar a superfície permitida

Objetivo:

- Parar de expandir o uso direto de `BAlert` em views novas ou alteradas.

Ações:

- Atualizar `specs/design/ux.md` com uma regra curta:
  - evitar `BAlert` direto em view
  - preferir primitive do app para feedback persistente
- Registrar no review checklist do time:
  - novo uso de `BAlert` em view deve ser excepcional e justificado

Critério de aceite:

- novas alterações deixam de introduzir `BAlert` direto sem motivo claro

### Fase 2. Consolidar erro global de formulário e modal

Objetivo:

- Atacar a família mais repetida e de menor risco visual.

Escopo inicial sugerido:

- `frontend/src/components/administracao/AdministradoresFluxoModais.vue`
- `frontend/src/components/atividades/ImportarAtividadesModal.vue`
- `frontend/src/components/processo/ModalAcaoBloco.vue`
- `frontend/src/components/mapa/modais/MapaDisponibilizacaoModal.vue`
- `frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue`

Ações:

- introduzir `AppAlertaFormulario` ou ampliar `AppAlert` para suportar o caso
- trocar `BAlert` direto desses modais pelo primitive
- alinhar props mínimas:
  - `mensagem`
  - `variante`
  - `dispensavel`
  - `data-testid`

Critério de aceite:

- modais com erro global deixam de importar `BAlert` diretamente
- comportamento de dismiss permanece estável
- feedback inline de campo continua separado do alerta global

### Fase 3. Consolidar erro persistente de tela

Objetivo:

- Remover divergência entre telas administrativas e telas de detalhe.

Escopo inicial sugerido:

- `frontend/src/views/UnidadeView.vue`
- `frontend/src/views/ConfiguracaoView.vue`
- `frontend/src/views/NotificacoesAdminView.vue`
- `frontend/src/views/FeedbacksAdminView.vue`
- `frontend/src/views/AdministradoresView.vue`

Ações:

- definir uma variante de `AppAlert` para erro de tela
- migrar `ultimoErro`, `erro`, `erroAdmins` e equivalentes para esse primitive
- revisar se o dismiss é apenas visual ou se precisa marcar erro dispensado no estado

Critério de aceite:

- telas equivalentes deixam de alternar entre `BAlert` bruto e `AppAlert`
- o ciclo de vida do erro fica explícito

### Fase 4. Remover sucesso persistente sem navegação

Objetivo:

- Eliminar alertas persistentes de sucesso local e substituir por:
  - nenhuma mensagem, quando a UI já comunicar o resultado;
  - toast breve, quando a confirmação ainda agregar valor.

Escopo inicial sugerido:

- `frontend/src/components/diagnostico/SubprocessoDiagnosticoPainel.vue`
- `frontend/src/views/useAutoavaliacaoDiagnosticoView.ts`
- `frontend/src/views/useDiagnosticoUnidadeView.ts`
- fluxos equivalentes em cadastro ou mapa que permaneçam na mesma tela

Ações:

- remover estados ad hoc como `alertaSucesso` quando eles servirem apenas para sucesso local
- revisar `retornoFluxo` e equivalentes para que carreguem erro local, não sucesso persistente
- separar claramente:
  - erro local persistente
  - sucesso local sem mensagem
  - sucesso transitório pós-navegação

Critério de aceite:

- ações equivalentes na mesma tela não alternam arbitrariamente entre alerta persistente, toast e ausência de mensagem

### Fase 5. Reduzir `BAlert` informativo ou de estado

Objetivo:

- Distinguir banner de estado de erro de ação.

Escopo inicial sugerido:

- `frontend/src/views/AutoavaliacaoDiagnosticoView.vue`
- `frontend/src/views/ConsensoDiagnosticoView.vue`
- `frontend/src/views/LimpezaProcessosView.vue`
- `frontend/src/components/processo/ProcessoDiagnosticoAlert.vue`

Ações:

- criar ou formalizar `AppAlertaStatus`
- migrar mensagens de estado persistente para esse primitive

Critério de aceite:

- mensagem de estado não reaproveita o mesmo primitive destinado a erro de ação por acidente

### Fase 6. Reavaliar `useToastStore`

Objetivo:

- Manter toast para o que realmente é toast e reduzir duplicação de comportamento.

Ações:

- mapear os fluxos que usam `useToastStore`
- padronizar payload mínimo do toast
- avaliar se vale manter apenas mensagem simples ou evoluir para payload estruturado
- evitar `useToast()` direto fora da fachada comum do app

Critério de aceite:

- toasts seguem uma política única de duração, posição e variante
- sucesso pós-navegação deixa de depender de implementações locais duplicadas

## Ordem recomendada de implementação

Para maximizar retorno e minimizar risco:

1. Fase 2
2. Fase 3
3. Fase 4
4. Fase 5
5. Fase 6

Motivo:

- Modais e formulários já têm fronteira de contexto mais clara.
- Têm menor impacto visual global.
- Permitem estabilizar primitives antes de mexer em telas mais amplas.

## Critérios de revisão para PRs futuros

Perguntas que devem ser respondidas em qualquer alteração de feedback:

- A mensagem é corrigível no mesmo contexto?
- Ela está no nível certo: campo, grupo, modal, tela ou navegação?
- Ela reaparece em nova tentativa inválida?
- Ela some quando a condição deixa de valer?
- O uso direto de `BAlert` é realmente necessário?
- Esse caso deveria ser `AppAlert`, `AppAlertaFormulario`, `AppAlertaStatus` ou toast?

## Próximo passo recomendado

Executar primeiro uma rodada focada apenas em primitives e migração dos modais com erro global.

Esse recorte é o melhor ponto de entrada porque:

- remove vários `BAlert` diretos
- não depende de `Vuelidate`
- preserva o padrão forte já existente de validação inline
- reduz a fragmentação sem exigir uma refatoração global de uma vez
