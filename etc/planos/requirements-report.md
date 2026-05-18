# Relatório de Revisão de Requisitos — SGC

**Data de geração:** 2026-05-16  
**Base analisada:** `etc/reqs/` (36 CDUs + 3 intros + 6 documentos de design) e código-fonte (`backend/` e `frontend/`)

---

## Sumário executivo

A revisão identificou **5 inconsistências entre requisitos**, **11 aspectos que precisam de melhor especificação**, **6 oportunidades de melhoria nos documentos de design** e **9 desalinhamentos com a implementação**. Adicionalmente, foram mapeadas **8 funcionalidades implementadas sem cobertura de requisitos**. O maior gap estrutural é o módulo de Diagnóstico, que tem situações definidas no código mas cujo fluxo completo ainda não possui CDUs.

---

## 1. Inconsistências entre os Requisitos

### 1.1 CDU-01 × CDU-29 — Visibilidade do link "Histórico" para perfil SERVIDOR

**CDU-01** lista "Link `Histórico`" entre os "Itens principais de navegação" sem nenhuma restrição de perfil — sugerindo que SERVIDOR também vê o link.  
**CDU-29** especifica explicitamente: _"Pré-condições: Usuário logado com qualquer perfil, **exceto SERVIDOR**"_.

Existe portanto uma contradição direta entre os dois documentos. A implementação (`MainNavbar.vue`) segue CDU-01 (mostra o link para todos), mas o fluxo de CDU-29 não garante dados úteis para SERVIDOR (que não participa de mapeamento/revisão finalizado). É necessário definir canonicamente se SERVIDOR deve ou não enxergar a tela de Histórico.

---

### 1.2 CDU-05, passo 12.2 — Assunto de e-mail errado para processo de Revisão

O modelo de e-mail de notificação para unidades **intermediárias** no início de um processo de **Revisão** traz o assunto:

> `SGC: Início de processo de **mapeamento** de competências em unidades subordinadas`

O assunto menciona "mapeamento" sendo que o contexto é de revisão. O equivalente correto em CDU-04 (mapeamento) usa o mesmo assunto. CDU-05 deveria usar algo como:

> `SGC: Início de processo de **revisão** do mapa de competências em unidades subordinadas`

---

### 1.3 CDU-10 — Dupla numeração no passo 8

O CDU-10 possui dois passos rotulados como "**8.**" em sequência (linhas 39 e 40 do documento), o que ambiguiza o fluxo:

- Linha 39 — "8. Se o usuário decidir disponibilizar sem mudanças…"
- Linha 40 — "8. Se houver mudanças, ou se o usuário clicar no checkbox…"

O segundo item 8 deveria ser numerado **9**, e os subsequentes renumerados.

---

### 1.4 CDU-32 — Numeração de passos reiniciada no meio do fluxo

Após o passo 6, o CDU-32 reinicia a numeração:

```
...5 → 6 → 1 (deveria ser 7) → 2 (deveria ser 8) → 3 (deveria ser 9)
```

Os passos 7, 8 e 9 estão equivocadamente rotulados como 1, 2 e 3. Isso prejudica referências cruzadas e leitura do fluxo.

---

### 1.5 CDU-20 — Botões de modal inconsistentes com o padrão do sistema

**Passo 9.2** descreve o modal de aceite do GESTOR com os botões `Confirmar` **ou** `Aceitar` — dois botões de confirmação positiva. O padrão de todos os outros modais no sistema é `Confirmar` / `Cancelar` ou ação-primária / `Cancelar`. O `Cancelar` está ausente.

**Passo 10.2** descreve o modal de homologação do ADMIN com `Confirmar` **ou** `Homologar` — igualmente, dois botões de ação positiva sem `Cancelar`. Por consistência, deveria ser `Homologar` / `Cancelar` (ou `Confirmar` / `Cancelar`).

---

## 2. Aspectos que Precisam de Melhor Especificação

### 2.1 Processo de Diagnóstico — Fluxo completamente não especificado

O documento `_intro_geral.md` reconhece explicitamente que _"o módulo de diagnóstico **ainda está em especificação**"_, mas esta lacuna tem impacto prático imediato:

- O `SituacaoSubprocesso` já tem três estados de diagnóstico implementados no código: `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, `DIAGNOSTICO_MONITORAMENTO` e `DIAGNOSTICO_CONCLUIDO`.
- CDU-07 menciona dois cards para processos de diagnóstico: **"Diagnóstico da equipe"** e **"Ocupações críticas"** — sem CDU próprio para nenhum deles.
- CDU-03 menciona que processos de diagnóstico podem ser criados, mas não há CDU equivalente ao CDU-04/CDU-05 para iniciar um diagnóstico.
- CDU-21 (Finalizar processo) trata apenas mapeamento e revisão, deixando aberto como um processo de diagnóstico é concluído.
- CDU-01 afirma que SERVIDOR "só atua nos processos de diagnóstico", mas não há nenhuma especificação de como essa atuação ocorre.

**Faltam CDUs para:** Iniciar processo de diagnóstico, Autoavaliação pelo SERVIDOR, Diagnóstico da equipe, Ocupações críticas, Monitoramento e Finalizar processo de diagnóstico.

---

### 2.2 CDU-02 × CDU-31 — Dois mecanismos conflitantes para alertas "novos"

**CDU-02** especifica que alertas ainda não visualizados são exibidos em **negrito**, sendo marcados como visualizados _"na primeira visualização pelo usuário logado"_. Este é um mecanismo baseado em **ação do usuário**.

**CDU-31** especifica a configuração `DIAS_ALERTA_NOVO` — _"Dias depois de um alerta ser enviado para que deixe de ser marcado como novo"_ — o que implica um mecanismo de **TTL baseado em tempo**.

Os dois comportamentos não são claramente relacionados entre si. O requisito não responde: são conceitos distintos ("lido" vs. "novo")? O TTL sobrepõe a marcação manual? O alerta volta a ser "novo" após o prazo se nunca for lido? Isso precisa ser especificado sem ambiguidade.

---

### 2.3 CDU-32 — Pré-condições insuficientes para reabertura de cadastro

CDU-32 diz que é possível reabrir cadastros de processos "Mapeamento ou Revisão" quando o subprocesso estiver com situação "Mapa homologado ou posterior", mas não especifica:

- Se a reabertura em contexto de **Revisão** altera a situação para `REVISAO_CADASTRO_EM_ANDAMENTO` ou `MAPEAMENTO_CADASTRO_EM_ANDAMENTO` (o código usa o primeiro).
- Quais são as "situações posteriores a Mapa homologado" — para o fluxo de mapeamento, `MAPEAMENTO_MAPA_HOMOLOGADO` é o estado final antes do processo ser finalizado, o que aparentemente não tem "posterior". Para revisão, igualmente.
- O impacto da reabertura nas etapas de data-limite (as datas de conclusão das etapas são apagadas?).

---

### 2.4 CDU-33 — Formato incompleto (sem Pré-condições)

CDU-33 (Reabrir revisão de cadastro) não possui seção de Pré-condições, ao contrário do CDU-32 que tem essas informações. Não está especificado:

- A situação mínima do subprocesso para permitir reabertura da revisão.
- Se o campo `Observação` da movimentação é diferente de `Descrição` (o CDU menciona `Descrição: 'Reabertura de revisão de cadastro'` **e** `Observação: [JUSTIFICATIVA]` — campo `Observação` não existe no modelo de Movimentação dos outros CDUs).

---

### 2.5 CDU-31 — Efeito de `DIAS_INATIVACAO_PROCESSO` não especificado na UI

O glossário define claramente que processos inativos ficam "disponíveis apenas para consulta a partir da tela Histórico de processos". No entanto, nenhum CDU especifica:

- Se processos inativados são removidos automaticamente do Painel.
- Se há indicação visual de que um processo "recém-finalizado" ainda está visível no Painel mas logo será movido para o Histórico.
- Se o processo de inativação é automático (tarefa agendada) ou disparado pela consulta do Painel.

---

### 2.6 CDU-03 — Desativação de unidades na árvore durante edição

CDU-03 diz que unidades "já participantes de processo ativo do tipo selecionado" devem ficar desativadas. Porém, não especifica:

- Se, ao **editar** um processo já existente, as unidades já incluídas naquele mesmo processo devem ficar ativas ou desativadas (atualmente a implementação as mantém selecionáveis, mas a regra poderia excluí-las por serem "participantes de processo ativo").
- O que acontece se uma unidade pertence a dois processos de tipos diferentes — pode ser selecionada para um terceiro processo de tipo diferente?

---

### 2.7 CDU-06 — Pré-requisitos para o botão "Finalizar processo"

CDU-06 afirma apenas: _"Se for perfil ADMIN, exibe o botão Finalizar processo."_  
CDU-21 especifica que a finalização só é permitida quando **todos** os subprocessos estão em 'Mapa homologado'.

A tela CDU-06 não descreve se o botão é exibido sempre (e a validação ocorre ao clicar) ou se é condicional à existência de todos os mapas homologados. A inconsistência entre "exibe sempre" (CDU-06) e a validação posterior (CDU-21) pode gerar experiência ruim de UX (botão visível mas sempre bloqueado).

---

### 2.8 CDU-28 — Regras de validação de data de início da atribuição

CDU-28 especifica: _"A data de término deve ser posterior ou igual à data de início"_, mas não define:

- Se a data de início pode estar no passado (para corrigir atribuições retroativas).
- Se, ao editar uma atribuição vigente, a data de início pode ser alterada.
- O que acontece se a atribuição vencer durante um processo em andamento onde o servidor substituto está logado.

---

### 2.9 CDU-13, passo numerado como "1" incorretamente

O passo que inicia o fluxo de homologação do ADMIN está numerado como `1.` (linha 139) em vez de `11.`, quebrando a sequência após o passo 10.9. Isso pode causar confusão ao interpretar se se trata de um fluxo alternativo independente ou da continuação do fluxo principal.

---

### 2.10 CDU-07 — Regras de habilitação dos cards de Diagnóstico não especificadas

Para processos de **Diagnóstico**, CDU-07 especifica dois cards: `Diagnóstico da equipe` e `Ocupações críticas`. Porém não define:

- Quais perfis podem acessar cada card.
- Em quais situações do subprocesso cada card está habilitado ou desabilitado.
- Se o SERVIDOR pode acessar diretamente a partir do Painel (sem passar pela tela Detalhes do subprocesso).

---

### 2.11 Atribuição temporária — Perfil CHEFE vs. múltiplos perfis simultâneos

O CDU-28, passo 14, diz que após atribuição temporária _"o novo par CHEFE-[UNIDADE] será mostrado entre as opções de login"_. Porém os CDUs não especificam o comportamento quando:

- O servidor já é CHEFE de sua própria unidade e recebe atribuição em outra — ele aparece com dois pares CHEFE?
- O titular original ainda está no sistema. Ele perde acesso ao seu perfil CHEFE durante a atribuição ou ambos têm acesso simultâneo?

---

## 3. Sugestões de Melhorias nos Documentos de Design

### 3.1 `design/feedback.md` — Profile "uat" diverge da implementação "hom"

O documento especifica o Spring profile `@Profile("uat")`, o arquivo `.env.uat` e o modo Vite `--mode uat`. A implementação usa `@Profile({"hom", "e2e", "test"})` e o arquivo `.env.hom` (convenção estabelecida no projeto).

**Sugestão:** Atualizar o documento para usar `hom` em vez de `uat` em todas as ocorrências, ou definir explicitamente se o projeto mudará a convenção de nomenclatura de ambientes.

---

### 3.2 `design/feedback.md` — Campo `activeYear` não existe no domínio do SGC

O `FeedbackMetadata` define o campo `activeYear: string | null — Active year/period — from app store`. O SGC não opera com o conceito de "ano ativo" ou "período". O próprio documento reconhece a incerteza ao dizer _"The exact store and field names must be verified against the actual codebase before implementation"_.

**Sugestão:** Substituir `activeYear` por um campo relevante ao contexto (ex: `processoAtivoCodigo: number | null` ou `processoAtivoDescricao: string | null`), ou remover o campo se não há informação contextual de processo na sessão.

---

### 3.3 `design/feedback.md` — "Out of Scope" contradiz a implementação

A seção 9 diz explicitamente: _"A UI for reviewing or managing submitted feedback (a simple database query or export is sufficient for UAT)"_ está fora do escopo.

Porém a implementação já inclui `FeedbacksAdminView.vue` e o endpoint `GET /api/feedback/listar` no `FeedbackController`. Esta funcionalidade existe e precisa de especificação (critérios de busca, paginação, permissões).

**Sugestão:** Remover o "Out of Scope" ou documentar formalmente a tela de administração de feedbacks.

---

### 3.4 `design/acesso.md` — Seção 8.3 (Diagnóstico) insuficiente

A seção descreve apenas as transições de estado do diagnóstico (`NAO_INICIADO → AUTOAVALIACAO_EM_ANDAMENTO → MONITORAMENTO → CONCLUIDO`), mas não documenta:

- Quem executa cada transição (atores/perfis).
- Se o fluxo de acesso segue as mesmas regras de localização/hierarquia dos outros processos.
- Se as ações em bloco se aplicam ao diagnóstico.

**Sugestão:** Adicionar uma tabela de ações de diagnóstico análoga às tabelas 4.1 e 5.1 do documento, assim que o módulo for especificado.

---

### 3.5 `design/acesso.md` — Endpoints administrativos sem CDU (tabela 4.2)

A tabela 4.2 lista endpoints que não correspondem a nenhum CDU:

| Endpoint | Observação |
|----------|------------|
| `POST /api/subprocessos` (criar) | Sem CDU |
| `POST /api/subprocessos/{codigo}/atualizar` | Sem CDU |
| `POST /api/subprocessos/{codigo}/excluir` | Sem CDU |
| `GET /api/subprocessos` (listar) | Sem CDU |
| `POST /api/mapas` (criar mapa) | Já coberto por CDU-15, mas listado separado |

**Sugestão:** Identificar a quais casos de uso esses endpoints pertencem ou criá-los como CDUs administrativos de apoio (ex: CDU-Adm-01: Manter subprocesso individualmente).

---

### 3.6 `design/ux.md` e `design/acesso.md` — Regra de esconder controles documentada em dois lugares

A regra de esconder (em vez de apenas desabilitar) controles de edição nas telas de Cadastro de Atividades (CHEFE) e Edição de Mapa (ADMIN) aparece em **ambos** os documentos com formulações ligeiramente diferentes:

- `design/acesso.md` §6.1: define a exceção de UX.
- `design/ux.md` (presumidamente): documenta regras de apresentação visual.

**Sugestão:** Centralizar a regra em `design/acesso.md` e apenas referenciar em `design/ux.md`, evitando divergência futura.

---

## 4. Desalinhamentos com a Implementação

### 4.1 CDU-29 — SERVIDOR tem acesso ao link "Histórico" na navbar

`MainNavbar.vue` exibe o link `Histórico` para **todos os perfis** sem condição. CDU-29 restringe explicitamente a ADMIN, GESTOR e CHEFE.

O `podeVerRelatorios` está implementado (exibe Relatórios condicionalmente), mas não há `podeVerHistorico` equivalente. O comportamento atual viola CDU-29, salvo se a decisão seja atualizar CDU-01 para restringir Histórico para não-SERVIDOR.

---

### 4.2 CDU-01 — Barra de navegação do ADMIN tem mais itens do que o especificado

CDU-01 afirma: _"Se perfil ADMIN: Mostrar adicionalmente dois ícones para acesso às telas Configurações e Administradores."_

A implementação exibe cinco elementos adicionais para ADMIN:
1. Ícone Notificações (`/administracao/notificacoes`)
2. Ícone Configurações (`/configuracoes`) ✓
3. Ícone Administradores (`/administradores`) ✓
4. Dropdown "Ações especiais" com:
   - Feedbacks (`/administracao/feedbacks`)
   - Limpeza de processos (`/administracao/limpeza-processos`)

Os itens 1, 4a e 4b não possuem CDU correspondente.

---

### 4.3 CDU-31 — Configuração `DIAS_ALERTA_NOVO` não tem efeito observável no backend

A configuração `DIAS_ALERTA_NOVO` existe no frontend (`useConfiguracoes.ts`, `textos.ts`) e é salva no banco via `ConfiguracaoController`, mas não há evidência de que seja consumida pelo backend para qualquer lógica de marcação de alertas.

A entidade `Alerta` possui o campo transitório `dataHoraLeitura` e existe o endpoint `POST /api/alertas/marcar-como-lidos`, sugerindo que a "novidade" é determinada por leitura, não por TTL. A configuração parece ser um "campo morto" no backend.

---

### 4.4 CDU-07 — Cards de Diagnóstico (`Diagnóstico da equipe` e `Ocupações críticas`) não implementados

CDU-07 especifica que para processos de Diagnóstico a seção `Elementos do processo` deve exibir os cards `Diagnóstico da equipe` e `Ocupações críticas`. Não há nenhuma view, componente Vue ou endpoint backend correspondente a esses cards.

A `SubprocessoView.vue` e os componentes da pasta `frontend/src/components/` não contêm referências a diagnóstico de competências no sentido de fluxo de trabalho (apenas ao diagnóstico organizacional de dados do SGRH).

---

### 4.5 CDU-05, passo 12.2 — Template de e-mail usa "mapeamento" (ver também §1.2)

Além da inconsistência documental já apontada, o `EmailModelosService.java` pode ter herdado o mesmo problema. É necessário verificar se o texto incorreto ("mapeamento") foi copiado para o código ou se a implementação já usa o texto correto para revisão.

---

### 4.6 CDU-20, passo 8.10 — Texto do alerta de devolução de mapa usa "Cadastro"

O alerta gerado na devolução de validação de mapa (`CDU-20 §8.10`) tem a descrição:

> `"Cadastro da unidade [SIGLA] devolvido para ajustes"`

O termo **"Cadastro"** está equivocado no contexto de CDU-20 (que trata do mapa de competências, não do cadastro de atividades). O texto correto deveria ser algo como:

> `"Validação do mapa da unidade [SIGLA] devolvida para ajustes"`

---

### 4.7 `design/feedback.md` — Profile Spring diverge (`uat` no doc vs `hom` no código)

Já detalhado em §3.1. O `FeedbackController` usa `@Profile({"hom", "e2e", "test"})` enquanto o documento especifica `@Profile("uat")`. A divergência é real e pode causar confusão ao configurar novos ambientes.

---

### 4.8 CDU-06 — Botão "Aceitar/Homologar mapa em bloco" aparece condicionalmente na tela de processo

CDU-06 menciona o botão `Aceitar/Homologar mapas em bloco` para situações `Mapa validado` ou `Mapa com sugestões`. A implementação também inclui esse botão para ADMIN via CDU-26 (`Homologar validação de mapas em bloco`), mas CDU-06 só descreve os botões para processos onde a unidade do usuário **contém subprocessos localizados nela**. A condição de exibição para GESTOR vs. ADMIN é descrita em CDU-22/25 e CDU-23/26 respectivamente, mas CDU-06 não deixa claro como os dois tipos de botão se distinguem na tela.

---

### 4.9 CDU-35 — Relatório de andamento: campo `Data limite do processo` não existe no modelo

CDU-35 especifica no cabeçalho do relatório: _"Data limite do processo"_. O modelo de `Processo` armazena `dataLimiteEtapa1`, mas não uma data limite única do processo inteiro. O processo pode ter datas limites por etapa e por subprocesso. É necessário definir qual data é exibida como "Data limite do processo" no cabeçalho do relatório.

---

## 5. Funcionalidades sem Especificação de Requisitos

### 5.1 Tela de Administração de Notificações (`NotificacoesAdminView`)

Existe a rota `/administracao/notificacoes`, o `NotificacaoAdminController` e a view `NotificacoesAdminView.vue`, acessíveis para ADMIN. Não há CDU para essa funcionalidade.

---

### 5.2 Tela de Limpeza de Processos (`LimpezaProcessosView`)

Existe a rota `/administracao/limpeza-processos` e o `ProcessoExclusaoController`. Esta funcionalidade permite exclusão completa de processos (diferente do CDU-03 que trata apenas de processos na situação 'Criado'). Não há CDU.

---

### 5.3 Tela de Feedbacks do Admin (`FeedbacksAdminView`)

A rota `/administracao/feedbacks` com listagem de feedbacks enviados é explicitamente excluída do escopo em `design/feedback.md §9`, mas está implementada. Precisa de formalização.

---

### 5.4 Modo Escuro (Dark Mode)

O botão de alternância de tema escuro/claro está implementado na barra de navegação (`MainNavbar.vue`). Não há requisito funcional ou de design documentando esta funcionalidade.

---

### 5.5 SSE — Server-Sent Events para invalidação de cache

O `EventosController` implementa um endpoint SSE para notificação de mudanças em cache organizacional. `design/cache.md` menciona o mecanismo tecnicamente, mas não há CDU descrevendo o comportamento do ponto de vista do usuário (como e quando o cache é atualizado na sessão ativa).

---

### 5.6 `E2eController`

Controller Spring exclusivo para testes end-to-end, sem documentação de requisito técnico. Embora seja correto existir para testes, recomenda-se documentar formalmente as operações que ele suporta (reset de estado, seed de dados) em algum artefato de engenharia.

---

### 5.7 `CacheAdminController`

Endpoint para administração manual de cache (`/api/cache/...`). Não há CDU correspondente e não aparece na documentação de design.

---

### 5.8 Tela de detalhes de Unidade fora de contexto de processo (CDU-28 parcial)

CDU-28 menciona a tela `Detalhes da unidade` no contexto de Atribuição Temporária. Porém a `UnidadeView.vue` pode ser acessada por GESTOR/CHEFE (via link "Minha unidade") exibindo informações da unidade sem processo ativo. Não há CDU especificando o que deve ser exibido nessa tela quando acessada por CHEFE/GESTOR sem processo em andamento, nem quais informações são visíveis a cada perfil nesse contexto.

---

## Apêndice: Referência Rápida das Lacunas por CDU

| CDU | Tipo de problema | Descrição resumida |
|-----|------------------|--------------------|
| CDU-01 | Inconsistência | Link Histórico não restringe SERVIDOR |
| CDU-02 | Especificação ambígua | "Novo" por leitura vs. por TTL (`DIAS_ALERTA_NOVO`) |
| CDU-03 | Especificação ambígua | Regra de desativação de unidades na edição |
| CDU-04 | Desalinhamento | Cards de Diagnóstico sem implementação |
| CDU-05 | Inconsistência | Assunto de e-mail usa "mapeamento" em vez de "revisão" |
| CDU-06 | Especificação ambígua | Condição de exibição do botão Finalizar |
| CDU-07 | Lacuna de requisito | Fluxo de Diagnóstico completamente ausente |
| CDU-10 | Erro de documento | Passo 8 duplicado |
| CDU-13 | Erro de documento | Passo de homologação numerado como "1" |
| CDU-20 | Inconsistência | Botões de modal sem `Cancelar`; alerta com texto errado |
| CDU-21 | Lacuna de requisito | Finalização de processo de diagnóstico não coberta |
| CDU-28 | Especificação ambígua | Data de início no passado; perfis simultâneos |
| CDU-29 | Inconsistência × CDU-01 | SERVIDOR exclui/não exclui Histórico |
| CDU-31 | Desalinhamento | `DIAS_ALERTA_NOVO` não implementado no backend |
| CDU-32 | Erro de documento | Numeração reiniciada |
| CDU-33 | Especificação ambígua | Pré-condições ausentes |
| CDU-35 | Desalinhamento | "Data limite do processo" não definida no modelo |
| — | Lacuna de requisito | NotificacoesAdmin, LimpezaProcessos, FeedbacksAdmin, DarkMode, SSE, E2eController, CacheAdminController, UnidadeView sem processo |
