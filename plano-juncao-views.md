# Plano: unificar views de Cadastro e Mapa

## Objetivo

Unificar as experiências de `Cadastro` e `Mapa` para que cada domínio tenha uma única view de referência:

- `CadastroView.vue` para atividades e conhecimentos;
- `MapaView.vue` para mapa de competências.

As rotas atuais podem continuar existindo inicialmente, mas não devem mais apontar para implementações visuais separadas. O modo de uso da tela deve ser definido por permissões, situação do subprocesso e dados carregados, não pelo nome da rota.

## Problema atual

Quando o projeto ainda era um protótipo sofisticado, a separação entre edição e visualização parecia uma forma simples de isolar fluxos. Hoje ela cria custo real:

- `CadastroView.vue` e `CadastroVisualizacaoView.vue` representam a mesma tela de negócio, mas têm templates, botões e carregamento duplicados.
- `MapaView.vue` e `MapaVisualizacaoView.vue` seguem a mesma duplicação para o mapa.
- Os visuais foram divergindo entre edição e somente leitura, gerando uma experiência inconsistente.
- Alterações recentes acabam sendo aplicadas em apenas uma das views, principalmente botões e ações compartilhadas.
- A separação confunde agentes e humanos: a pergunta "esse botão vai em qual view?" deveria não existir.
- A decisão real já está em `useAcesso` e no estado do subprocesso; a rota só deveria levar ao domínio correto.

## Fora de escopo inicial

Não fazer no primeiro corte:

- fundir endpoints backend de edição e visualização;
- criar um BFF amplo para retornar todos os dados de `contexto-edicao`, `contexto-cadastro-atividades` e `mapa-visualizacao`;
- remover imediatamente as rotas públicas `/vis-cadastro` e `/vis-mapa`;
- reescrever regras de acesso;
- alterar textos de negócio sem necessidade;
- trocar o desenho dos componentes de edição por uma nova biblioteca ou novo padrão visual sem relação direta com a clareza da tela mesclada.

Está dentro do escopo fazer ajustes na forma de editar atividades, conhecimentos, competências e associações se isso deixar as operações mais claras na view unificada. Por exemplo, é aceitável mover certas edições inline para modais, desde que a mudança reduza ambiguidade, preserve o fluxo de negócio e seja validada com testes focados.

Os contratos HTTP atuais ainda têm valor:

- cadastro usa contexto enxuto de atividades;
- mapa de edição usa contexto/mapa completo;
- mapa de visualização usa DTO formatado para leitura.

## Princípios

1. Uma tela por domínio.
   `Cadastro` e `Mapa` devem ter uma view de entrada cada, com componentes internos para partes editáveis e somente leitura.

2. Permissão decide ação; rota decide destino.
   A view deve renderizar ações conforme `useAcesso`, situação e disponibilidade dos dados.

3. Rotas antigas viram compatibilidade temporária.
   `/cadastro` e `/vis-cadastro` devem carregar a mesma view. O mesmo vale para `/mapa` e `/vis-mapa`.

4. Botões compartilhados têm um único ponto de manutenção.
   Histórico, impacto, sugestões, aceite, homologação, devolução e ações equivalentes não devem ficar duplicados em templates paralelos.

5. Separar comandos de leitura.
   A tela unificada pode mostrar os mesmos dados para leitura e edição, mas operações destrutivas ou estruturais devem ficar visualmente claras. Quando edição inline competir com ações de análise, preferir comandos explícitos, modais ou painéis dedicados.

6. Não criar monólitos maiores.
   A fusão não deve virar um arquivo de 1.200 linhas. O corte correto é extrair componentes e composables pequenos antes ou durante a junção.

7. Validar por caso de uso.
   Como os testes E2E dependem muito das rotas e cards atuais, cada etapa deve ser acompanhada por testes focados.

## Estado atual confirmado

### Rotas

As rotas de processo apontam para quatro componentes separados:

- `SubprocessoCadastro` -> `CadastroView.vue`;
- `SubprocessoVisCadastro` -> `CadastroVisualizacaoView.vue`;
- `SubprocessoMapa` -> `MapaView.vue`;
- `SubprocessoVisMapa` -> `MapaVisualizacaoView.vue`.

O componente `SubprocessoCards.vue` escolhe entre rotas de edição e visualização com base em permissões (`podeEditarCadastro`, `podeEditarMapa`, `habilitarAcessoCadastro`, `habilitarAcessoMapa`).

### Cadastro

`CadastroView.vue` concentra:

- edição de atividades e conhecimentos;
- importação;
- disponibilização;
- impacto no mapa;
- histórico;
- início/cancelamento de revisão sem mudanças;
- validação local antes de disponibilizar.

`CadastroVisualizacaoView.vue` concentra:

- listagem somente leitura de atividades e conhecimentos;
- impacto no mapa;
- histórico;
- devolução;
- aceite/homologação de cadastro.

Há sobreposição clara em header, carregamento de contexto, lista de atividades, impacto e histórico. A diferença principal é o conjunto de ações liberadas para cada permissão/situação.

### Mapa

`MapaView.vue` concentra:

- edição de competências;
- associação e remoção de atividades;
- disponibilização do mapa;
- impacto no mapa;
- visualização de sugestões;
- devolução e aceite/homologação em alguns cenários.

`MapaVisualizacaoView.vue` concentra:

- renderização somente leitura do mapa;
- apresentação e visualização de sugestões;
- histórico;
- validação;
- devolução;
- aceite/homologação.

Há sobreposição em header, ações de análise, sugestões, devolução, aceite/homologação e resolução do subprocesso. A diferença relevante é o corpo principal: edição usa `MapaCompleto`; visualização usa `MapaVisualizacao`.

## Decisão proposta

Fazer a junção em duas frentes, começando por Cadastro.

### Decisão 1: Cadastro primeiro

Cadastro é o menor risco porque os dois modos usam a mesma estrutura base: atividades e conhecimentos. O corpo somente leitura pode ser um modo do mesmo conjunto de dados carregado pelo contexto de cadastro.

Resultado esperado:

- `CadastroView.vue` vira a única view de rota para cadastro;
- `CadastroVisualizacaoView.vue` deixa de ser importada por rotas;
- `/processo/:codProcesso/:siglaUnidade/cadastro` e `/processo/:codProcesso/:siglaUnidade/vis-cadastro` apontam para `CadastroView.vue`;
- ações de edição e análise ficam no mesmo header, condicionadas por `useAcesso`;
- corpo editável usa `AtividadeItem`;
- corpo somente leitura usa um componente extraído, por exemplo `AtividadesSomenteLeitura.vue`.

Durante essa etapa, avaliar se a edição atual inline continua adequada na tela unificada. Se a junção deixar a tela carregada ou ambígua, considerar:

- criar/editar atividade por modal;
- criar/editar conhecimento por modal;
- manter remoções sempre por confirmação modal;
- deixar a lista principal mais próxima de leitura, com ações explícitas por item.

Essa mudança não é obrigatória para a primeira implementação, mas está autorizada se simplificar o modelo mental e reduzir divergência futura.

### Decisão 2: Mapa depois

Mapa tem maior risco porque os DTOs de edição e visualização são diferentes. A junção deve manter carregadores separados dentro da mesma view.

Resultado esperado:

- `MapaView.vue` vira a única view de rota para mapa;
- `MapaVisualizacaoView.vue` deixa de ser importada por rotas;
- `/processo/:codProcesso/:siglaUnidade/mapa` e `/processo/:codProcesso/:siglaUnidade/vis-mapa` apontam para `MapaView.vue`;
- corpo editável continua usando `CompetenciaCard` e `MapaCompleto`;
- corpo somente leitura usa um componente extraído, por exemplo `MapaSomenteLeitura.vue`, alimentado por `MapaVisualizacao`;
- ações de sugestões, histórico, validação, devolução e aceite/homologação ficam em um bloco único.

Importante: `MapaVisualizacaoView.vue` não deve ser referência visual para a tela final. O corpo somente leitura deve seguir o padrão visual do restante do projeto, especialmente o layout de mapa já usado em `CompetenciaCard`, evitando a estética divergente da view antiga.

Durante essa etapa, avaliar se criação/edição de competências, associação de atividades e remoções devem ficar mais modalizadas. O objetivo é evitar que ações de manutenção do mapa disputem espaço com ações de validação/análise quando a view for única.

## Plano de execução

### Etapa 1: preparar Cadastro

1. Extrair o corpo somente leitura de `CadastroVisualizacaoView.vue` para um componente:
   - caminho sugerido: `frontend/src/components/atividades/AtividadesSomenteLeitura.vue`;
   - props: `atividades`;
   - preservar `data-testid` existentes (`txt-atividade-descricao`, `txt-conhecimento-descricao`).

2. Extrair ou concentrar ações de análise de cadastro:
   - aceite/homologação;
   - devolução;
   - histórico;
   - impacto.

   Opção pragmática inicial: mover a lógica diretamente para `CadastroView.vue` antes de criar composable, desde que o arquivo não fique difícil de revisar. Se ficar grande demais, criar `useCadastroAcoesAnalise`.

3. Atualizar `CadastroView.vue` para renderizar:
   - ações de edição quando `podeEditarCadastro` / `podeDisponibilizarCadastro`;
   - ações de análise quando `acaoPrincipalCadastro`, `podeDevolverCadastro` ou histórico/impacto forem aplicáveis;
   - formulário de nova atividade apenas quando a edição for permitida;
   - lista editável ou somente leitura conforme permissão de edição efetiva.

4. Avaliar o modelo de edição de atividades e conhecimentos:
   - manter inline se continuar claro;
   - usar modais para criação/edição se a view unificada ficar visualmente confusa;
   - preservar feedback de erro inline próximo ao item afetado;
   - manter foco e acessibilidade dos formulários.

5. Garantir que botões comuns não existam em dois lugares:
   - histórico;
   - impacto;
   - devolução;
   - ação principal de análise.

6. Alterar `processo.routes.ts`:
   - manter `SubprocessoCadastro` e `SubprocessoVisCadastro`;
   - apontar ambos para `CadastroView.vue`;
   - manter títulos por enquanto para reduzir impacto em breadcrumbs/testes.

7. Remover import/mock/testes diretos de `CadastroVisualizacaoView.vue` ou convertê-los para a nova view/componente extraído.

8. Se não houver mais referência, remover `CadastroVisualizacaoView.vue`.

### Etapa 2: validar Cadastro

Executar validação focada:

```bash
npx vitest run frontend/src/views/__tests__/AtividadesCadastroView.spec.ts frontend/src/views/__tests__/CadastroViewPermissoes.spec.ts frontend/src/views/__tests__/CadastroViewCoverage.spec.ts frontend/src/views/__tests__/CadastroVisualizacaoView.spec.ts --reporter=dot --no-color
npm run typecheck
```

Depois, se a mudança passar localmente, rodar pelo menos os E2E que cobrem edição e visualização de cadastro:

```bash
npx playwright test e2e/cdu-05.spec.ts e2e/cdu-09.spec.ts e2e/cdu-13.spec.ts e2e/cdu-14.spec.ts
```

Se o custo for alto, priorizar `cdu-05` e um fluxo de análise visual.

### Etapa 3: preparar Mapa

1. Extrair o corpo somente leitura de `MapaVisualizacaoView.vue` para um componente:
   - caminho sugerido: `frontend/src/components/mapa/MapaSomenteLeitura.vue`;
   - props: `mapa`;
   - preservar `data-testid` existentes (`vis-mapa__txt-competencia-descricao`, `txt-conhecimento-item`).

2. Concentrar ações compartilhadas de mapa:
   - sugestões;
   - ver sugestões;
   - histórico;
   - validar;
   - devolução;
   - aceite/homologação.

   Como já existe `useMapaAcoesAnalise`, preferir reutilizá-lo e mover apenas o que ainda está duplicado na view.

3. Atualizar `MapaView.vue` para trabalhar com dois corpos possíveis:
   - modo edição: `MapaCompleto` + `CompetenciaCard`;
   - modo visualização: `MapaVisualizacao` + `MapaSomenteLeitura`.

4. Avaliar o modelo de edição do mapa:
   - manter modais existentes para competência quando estiverem claros;
   - considerar modal ou painel dedicado para associação de atividades se a lista editável ficar pesada;
   - manter remoções com confirmação;
   - separar visualmente ações de manutenção das ações de análise/validação.

5. Definir regra explícita para carregamento:
   - se a permissão/situação permitir edição de mapa, carregar contexto de edição;
   - se for apenas visualização/análise, carregar `mapa-visualizacao`;
   - se a rota for `/mapa` mas o usuário não puder editar, ainda exibir visualização se `habilitarAcessoMapa` permitir;
   - se a rota for `/vis-mapa` mas o usuário puder editar, abrir a mesma view com ações disponíveis, sem esconder botões necessários.

6. Alterar `processo.routes.ts`:
   - manter `SubprocessoMapa` e `SubprocessoVisMapa`;
   - apontar ambos para `MapaView.vue`.

7. Atualizar/remover testes diretos de `MapaVisualizacaoView.vue`.

8. Se não houver mais referência, remover `MapaVisualizacaoView.vue`.

### Etapa 4: validar Mapa

Executar validação focada:

```bash
npx vitest run frontend/src/views/__tests__/MapaViewCoverage.spec.ts frontend/src/views/__tests__/MapaViewUncovered.spec.ts frontend/src/views/__tests__/MapaVisualizacaoView.spec.ts frontend/src/views/__tests__/VisMapa.spec.ts --reporter=dot --no-color
npm run typecheck
```

Depois rodar E2E de mapa:

```bash
npx playwright test e2e/cdu-17.spec.ts e2e/cdu-19.spec.ts e2e/cdu-20.spec.ts
```

Se necessário, incluir `e2e/jornada.spec.ts` como teste serial de regressão ampla.

### Etapa 5: limpar navegação e docs

Depois que Cadastro e Mapa estiverem unificados:

1. Revisar `SubprocessoCards.vue`.
   - Decidir se os cards ainda precisam navegar para sufixos diferentes.
   - Possível estado intermediário: manter sufixos para compatibilidade.
   - Estado final desejado: card navega para `/cadastro` e `/mapa`; a view decide o modo.

2. Revisar breadcrumbs.
   - `SubprocessoVisCadastro` e `SubprocessoCadastro` já têm o mesmo rótulo.
   - `SubprocessoVisMapa` hoje usa "Visualizar mapa"; decidir se continua enquanto a rota existir ou se vira "Mapa de competências".

3. Atualizar `etc/reqs/design/breadcrumbs.md` se as rotas finais mudarem.

4. Atualizar helpers E2E:
   - `e2e/helpers/helpers-atividades.ts`;
   - `e2e/helpers/helpers-mapas.ts`.

5. Só depois considerar redirecionamentos:
   - `/vis-cadastro` -> `/cadastro`;
   - `/vis-mapa` -> `/mapa`.

## Critérios de aceite

Cadastro:

- Um botão novo de cadastro/análise precisa ser adicionado em apenas um lugar.
- Usuário com permissão de edição vê as ações de edição esperadas.
- Usuário sem permissão de edição vê a mesma tela com corpo somente leitura e ações de análise quando aplicável.
- Histórico, impacto, devolução e aceite/homologação continuam acessíveis nos cenários atuais.
- Rotas antigas continuam abrindo a tela correta.

Mapa:

- Um botão novo de mapa/análise precisa ser adicionado em apenas um lugar.
- Usuário com permissão de edição vê competências editáveis e ações de manutenção.
- Usuário sem permissão de edição vê mapa somente leitura e ações de análise quando aplicável.
- Sugestões, histórico, validação, devolução e aceite/homologação continuam acessíveis nos cenários atuais.
- `mapa-visualizacao` continua sendo usado para leitura quando fizer sentido; não substituir por DTO pesado sem evidência.

Geral:

- `npm run typecheck` passa.
- Testes unitários focados passam.
- E2E principais de cadastro e mapa passam ou têm falha analisada com causa clara.
- `CadastroVisualizacaoView.vue` e `MapaVisualizacaoView.vue` não ficam como shells vazios permanentes.

## Achados recentes da implementação

### Cadastro

- `SubprocessoVisCadastro` já pode apontar para `CadastroView.vue` sem quebrar as specs unitárias focadas.
- `CadastroView.vue` passou a concentrar ações de análise que antes existiam apenas em `CadastroVisualizacaoView.vue`: devolução, aceite/homologação, observações e validação de justificativa.
- A rota antiga `/vis-cadastro` ainda deve existir por enquanto, mas deixou de significar "view separada".
- `CadastroVisualizacaoView.vue` ainda existe porque seus testes diretos precisam ser migrados ou removidos antes da exclusão.

### Mapa

- `MapaSomenteLeitura.vue` foi extraído, mas não deve copiar o estilo antigo de `MapaVisualizacaoView.vue`.
- A referência visual correta para o corpo somente leitura do mapa deve ser o padrão já usado por `CompetenciaCard`: cards simples, BootstrapVueNext, densidade parecida com a edição e sem bordas coloridas/estética isolada.
- `MapaVisualizacaoView.vue` ainda é a rota ativa de `SubprocessoVisMapa`; a fusão real de Mapa ainda não foi feita.

## Impacto nos E2E

A suíte E2E vai precisar de revisão forte. Ela hoje testa a separação antiga como se fosse regra de negócio, principalmente por três tipos de acoplamento:

1. Cards distintos como contrato primário.
   Muitos testes verificam `card-subprocesso-atividades` versus `card-subprocesso-atividades-vis`, e `card-subprocesso-mapa-edicao` versus `card-subprocesso-mapa-visualizacao`.

2. URLs `vis-*` como prova de modo.
   Helpers e specs usam `waitForURL(/\/vis-cadastro$/)` ou esperam explicitamente `/vis-mapa`.

3. Comentários/cenários descrevendo "view de visualização" como destino separado.
   Isso ficará desatualizado quando a tela for única e o modo vier de permissão/situação.

### Arquivos E2E mais afetados

- `e2e/helpers/helpers-atividades.ts`
- `e2e/helpers/helpers-mapas.ts`
- `e2e/jornada.spec.ts`
- `e2e/cdu-05.spec.ts`
- `e2e/cdu-07.spec.ts`
- `e2e/cdu-09.spec.ts`
- `e2e/cdu-13.spec.ts`
- `e2e/cdu-19.spec.ts`
- `e2e/cdu-20.spec.ts`
- `e2e/cdu-21.spec.ts`
- `e2e/regressao-cache-sessao.spec.ts`

### Nova regra para E2E

Os E2E não devem mais afirmar "qual view" abriu. Devem afirmar:

- o usuário consegue acessar a tela do domínio correto;
- os dados aparecem;
- ações de edição aparecem ou ficam ausentes conforme permissão/situação;
- ações de análise aparecem ou ficam ausentes conforme permissão/situação;
- mutações esperadas funcionam;
- rotas antigas continuam aceitas enquanto existirem.

Ou seja, a distinção relevante passa a ser capacidade operacional, não sufixo de rota.

### Adaptação recomendada dos helpers

`helpers-atividades.ts`:

- trocar `navegarParaAtividadesVisualizacao` para aceitar `/cadastro` ou `/vis-cadastro` enquanto a compatibilidade existir;
- preferir assertar o heading e os botões esperados, não o sufixo da URL;
- criar helpers semânticos:
  - `esperarTelaAtividades`;
  - `esperarAtividadesEditaveis`;
  - `esperarAtividadesSomenteLeitura`;
  - `esperarAcoesAnaliseCadastro`;
  - `esperarSemAcoesEdicaoCadastro`.

`helpers-mapas.ts`:

- manter `waitForURL(/\/(mapa|vis-mapa)$/)` no estado intermediário;
- depois da fusão, evitar depender de card de edição versus visualização;
- criar helpers semânticos:
  - `esperarTelaMapa`;
  - `esperarMapaEditavel`;
  - `esperarMapaSomenteLeitura`;
  - `esperarAcoesAnaliseMapa`;
  - `esperarSemAcoesManutencaoMapa`.

### Adaptação recomendada das specs

1. Substituir asserts de presença/ausência dos cards por asserts de permissões efetivas.
   Exemplo: em vez de "card visualização visível e card edição oculto", testar "card do domínio acessível" e, após abrir a tela, "botão de edição ausente / botão de análise presente".

2. Manter poucos testes de compatibilidade de rota.
   Deve haver teste explícito para garantir que `/vis-cadastro` e `/vis-mapa` ainda abrem, mas isso não deve aparecer em todos os fluxos.

3. Atualizar `jornada.spec.ts` por fases.
   Esse arquivo é serial e muito acoplado aos estados intermediários. Deve ser revisado depois dos helpers, não antes.

4. Atualizar comentários dos testes.
   Comentários como "card vis-cadastro" e "view de visualização" devem virar "modo somente leitura" ou "ações de análise".

5. Evitar enfraquecer regra de acesso.
   A remoção da distinção de view não significa que edição fica disponível. E2E deve continuar verificando ausência de botões de mutação quando o perfil/situação não permite.

## Plano específico para E2E

### Fase E2E 1: compatibilidade durante a fusão

- Ajustar helpers para aceitar rotas antigas e novas.
- Trocar `waitForURL(/\/vis-cadastro$/)` por aceitação de `/cadastro` ou `/vis-cadastro` onde a rota exata não for o comportamento testado.
- Trocar asserts repetidos de cards separados por helpers semânticos.
- Manter `data-testid` antigos nos cards enquanto os testes forem migrados.

### Fase E2E 2: contrato novo

- Fazer os testes navegarem pelo card de domínio, não pelo card de modo.
- Centralizar expectativa de modo em botões e campos:
  - edição de atividades: `inp-nova-atividade`, `btn-adicionar-atividade`, botões de editar/remover;
  - análise de cadastro: `btn-acao-devolver`, `btn-acao-analisar-principal`;
  - edição de mapa: `btn-abrir-criar-competencia`, `btn-cad-mapa-disponibilizar`;
  - análise de mapa: `btn-mapa-validar`, `btn-mapa-devolver`, `btn-mapa-homologar-aceite`, `btn-mapa-sugestoes`.

### Fase E2E 3: limpeza final

- Remover dependência ampla de `card-subprocesso-atividades-vis` e `card-subprocesso-mapa-visualizacao`.
- Se as rotas `vis-*` forem redirecionadas no futuro, manter apenas testes pequenos de redirecionamento/compatibilidade.
- Atualizar `etc/reqs/design/breadcrumbs.md` para refletir o contrato final de navegação.

## Riscos

1. Arquivo grande demais.
   Mitigação: extrair corpos somente leitura e ações compartilhadas antes de remover as views antigas.

2. Regressão de permissão.
   Mitigação: preservar `useAcesso` como fonte de decisão e rodar testes de `SubprocessoCards`/permissões.

3. Quebra de rotas E2E.
   Mitigação: manter nomes e caminhos antigos inicialmente, só mudando o componente de destino.

4. Carregamento excessivo no mapa.
   Mitigação: não carregar simultaneamente `mapa-completo` e `mapa-visualizacao` sem necessidade.

5. Divergência continuar dentro da view unificada.
   Mitigação: ações compartilhadas em um único bloco/composable, corpos separados apenas quando a diferença visual for intencional.

6. A tela unificada ficar operacionalmente confusa.
   Mitigação: separar comandos de edição em modais/painéis e deixar a área principal legível como estado atual do cadastro/mapa.

## Ordem recomendada

1. Cadastro: extrair corpo somente leitura.
2. Cadastro: apontar as duas rotas para `CadastroView.vue`.
3. Cadastro: remover `CadastroVisualizacaoView.vue`.
4. Mapa: extrair corpo somente leitura.
5. Mapa: apontar as duas rotas para `MapaView.vue`.
6. Mapa: remover `MapaVisualizacaoView.vue`.
7. Navegação: simplificar cards e, se desejado, redirecionar rotas `vis-*`.
8. Docs/E2E: alinhar breadcrumbs e helpers ao contrato final.

## Observação sobre nomenclatura

Mesmo preservando rotas antigas no começo, o código novo deve evitar reforçar a separação "vis" versus "edição" como se fossem telas diferentes. A nomenclatura interna deve favorecer:

- `modoSomenteLeitura`;
- `podeEditar`;
- `acoesAnalise`;
- `corpoEdicao`;
- `corpoLeitura`.

Isso reduz a chance de novos botões serem adicionados no lugar errado.
