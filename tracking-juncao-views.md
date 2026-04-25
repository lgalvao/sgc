# Tracking: junção das views de Cadastro e Mapa

## Status geral

Status: não iniciado

Objetivo rastreado por este arquivo: acompanhar a execução do `plano-juncao-views.md`, mantendo progresso, validações e pendências em um lugar separado do plano.

## Decisões já tomadas

- [x] Unificar primeiro o par `CadastroView.vue` / `CadastroVisualizacaoView.vue`.
- [x] Unificar depois o par `MapaView.vue` / `MapaVisualizacaoView.vue`.
- [x] Preservar rotas antigas no primeiro corte.
- [x] Não fundir endpoints backend de edição e visualização no primeiro corte.
- [x] Tratar divergência visual e confusão de botões como motivação central da mudança.
- [x] Permitir ajustes no modelo de edição se isso deixar as telas mescladas mais claras.

## Cadastro

### Preparação

- [x] Extrair corpo somente leitura de atividades para componente dedicado.
- [x] Preservar `data-testid` da visualização de atividades.
- [ ] Mapear botões existentes em `CadastroView.vue`.
- [ ] Mapear botões existentes em `CadastroVisualizacaoView.vue`.
- [ ] Definir bloco único de ações do header de cadastro.
- [ ] Avaliar se edição inline de atividades continua clara na view mesclada.
- [ ] Avaliar se edição inline de conhecimentos continua clara na view mesclada.
- [ ] Decidir se criação/edição de atividade deve usar modal.
- [ ] Decidir se criação/edição de conhecimento deve usar modal.

### Implementação

- [x] Mover ações de análise de cadastro para `CadastroView.vue`.
- [ ] Renderizar formulário de nova atividade apenas quando edição estiver permitida.
- [ ] Renderizar lista editável quando edição estiver permitida.
- [ ] Renderizar lista somente leitura quando edição não estiver permitida.
- [ ] Separar visualmente comandos de edição e ações de análise.
- [x] Garantir histórico em um único ponto.
- [ ] Garantir impacto no mapa em um único ponto.
- [x] Garantir devolução em um único ponto.
- [x] Garantir aceite/homologação em um único ponto.
- [x] Apontar `SubprocessoCadastro` e `SubprocessoVisCadastro` para `CadastroView.vue`.
- [x] Atualizar mocks/testes de rota.
- [x] Atualizar/remover testes diretos de `CadastroVisualizacaoView.vue`.
- [x] Remover `CadastroVisualizacaoView.vue` se ficar sem uso.

### Validação

- [ ] Rodar Vitest focado de cadastro.
- [ ] Rodar `npm run typecheck`.
- [ ] Rodar E2E principal de cadastro editável.
- [ ] Rodar E2E principal de cadastro somente leitura/análise.
- [ ] Registrar falhas encontradas.
- [ ] Corrigir regressões.

### Resultado

- [x] Cadastro tem uma única view de rota.
- [ ] Botões de cadastro não estão duplicados entre duas views.
- [ ] Visual de edição e leitura está coerente.
- [ ] Rotas antigas continuam funcionais.

## Mapa

### Preparação

- [x] Extrair corpo somente leitura de mapa para componente dedicado.
- [x] Preservar `data-testid` da visualização de mapa.
- [ ] Mapear botões existentes em `MapaView.vue`.
- [ ] Mapear botões existentes em `MapaVisualizacaoView.vue`.
- [ ] Definir bloco único de ações do header de mapa.
- [ ] Definir regra explícita de carregamento entre `MapaCompleto` e `MapaVisualizacao`.
- [x] Registrar que `MapaVisualizacaoView.vue` não deve ser referência de estilo para a tela final.
- [ ] Avaliar se edição de competências continua clara na view mesclada.
- [ ] Avaliar se associação de atividades continua clara na view mesclada.
- [ ] Decidir se associação/edição deve usar modal ou painel dedicado.

### Implementação

- [x] Mover ações faltantes de análise de mapa para `MapaView.vue`.
- [ ] Renderizar corpo editável quando edição estiver permitida.
- [ ] Renderizar corpo somente leitura quando edição não estiver permitida.
- [ ] Separar visualmente comandos de manutenção e ações de análise.
- [x] Garantir sugestões em um único ponto.
- [x] Garantir ver sugestões em um único ponto.
- [x] Garantir histórico em um único ponto.
- [x] Garantir validação em um único ponto.
- [x] Garantir devolução em um único ponto.
- [x] Garantir aceite/homologação em um único ponto.
- [x] Apontar `SubprocessoMapa` e `SubprocessoVisMapa` para `MapaView.vue`.
- [x] Atualizar mocks/testes de rota.
- [ ] Atualizar/remover testes diretos de `MapaVisualizacaoView.vue`.
- [ ] Remover `MapaVisualizacaoView.vue` se ficar sem uso.

### Validação

- [ ] Rodar Vitest focado de mapa.
- [ ] Rodar `npm run typecheck`.
- [ ] Rodar E2E principal de mapa editável.
- [ ] Rodar E2E principal de mapa somente leitura/análise.
- [ ] Registrar falhas encontradas.
- [ ] Corrigir regressões.

### Resultado

- [ ] Mapa tem uma única view de rota.
- [ ] Botões de mapa não estão duplicados entre duas views.
- [ ] Visual de edição e leitura está coerente.
- [ ] Rotas antigas continuam funcionais.
- [ ] Leitura não passou a carregar DTO pesado sem necessidade.

## Navegação e documentação

- [ ] Revisar `SubprocessoCards.vue`.
- [ ] Decidir se os cards continuam usando sufixos `vis-*` temporariamente.
- [ ] Revisar breadcrumbs.
- [ ] Atualizar `etc/reqs/design/breadcrumbs.md` se as rotas finais mudarem.
- [ ] Atualizar `e2e/helpers/helpers-atividades.ts`.
- [ ] Atualizar `e2e/helpers/helpers-mapas.ts`.
- [ ] Decidir se `/vis-cadastro` redireciona para `/cadastro`.
- [ ] Decidir se `/vis-mapa` redireciona para `/mapa`.

## E2E

### Achados

- [x] Confirmado que os E2E estão fortemente acoplados aos cards separados de edição/visualização.
- [x] Confirmado uso amplo de `card-subprocesso-atividades-vis`.
- [x] Confirmado uso amplo de `card-subprocesso-mapa-visualizacao`.
- [x] Confirmado uso de `waitForURL(/\/vis-cadastro$/)` em helper.
- [x] Confirmado uso de `/vis-cadastro` e `/vis-mapa` como prova de modo em specs.

### Helpers a revisar

- [ ] `e2e/helpers/helpers-atividades.ts`: aceitar `/cadastro` e `/vis-cadastro` no estado intermediário.
- [ ] `e2e/helpers/helpers-atividades.ts`: criar helper semântico para tela de atividades.
- [ ] `e2e/helpers/helpers-atividades.ts`: criar helper para modo editável.
- [ ] `e2e/helpers/helpers-atividades.ts`: criar helper para modo somente leitura/análise.
- [ ] `e2e/helpers/helpers-mapas.ts`: manter aceitação de `/mapa` e `/vis-mapa` durante compatibilidade.
- [ ] `e2e/helpers/helpers-mapas.ts`: criar helper semântico para tela de mapa.
- [ ] `e2e/helpers/helpers-mapas.ts`: criar helper para modo editável.
- [ ] `e2e/helpers/helpers-mapas.ts`: criar helper para modo somente leitura/análise.

### Specs a revisar

- [ ] `e2e/cdu-05.spec.ts`
- [ ] `e2e/cdu-07.spec.ts`
- [ ] `e2e/cdu-09.spec.ts`
- [ ] `e2e/cdu-13.spec.ts`
- [ ] `e2e/cdu-19.spec.ts`
- [ ] `e2e/cdu-20.spec.ts`
- [ ] `e2e/cdu-21.spec.ts`
- [ ] `e2e/jornada.spec.ts`
- [ ] `e2e/regressao-cache-sessao.spec.ts`

### Novo contrato de teste

- [ ] Trocar asserts de "view editável vs view visualização" por "ações disponíveis vs ausentes".
- [ ] Manter poucos testes de compatibilidade das rotas `vis-*`.
- [ ] Atualizar comentários que falam em `vis-cadastro` como tela separada.
- [ ] Evitar remover cobertura de regra de acesso: botões de mutação devem continuar ausentes quando não permitidos.

## Validações executadas

```text
Data: 2026-04-25
Comando: npm run test:unit --prefix frontend -- src/views/__tests__/CadastroVisualizacaoView.spec.ts
Resultado: passou, 7 testes
Observações: valida o primeiro corte de extração de AtividadesSomenteLeitura.
```

```text
Data: 2026-04-25
Comando: npm run test:unit --prefix frontend -- src/views/__tests__/VisMapa.spec.ts
Resultado: passou, 25 testes
Observações: valida a extração de MapaSomenteLeitura sem usar o estilo divergente de MapaVisualizacaoView como referência.
```

```text
Data: 2026-04-25
Comando: npm run test:unit --prefix frontend -- src/views/__tests__/AtividadesCadastroView.spec.ts src/views/__tests__/CadastroViewPermissoes.spec.ts src/views/__tests__/CadastroViewCoverage.spec.ts src/router/__tests__/processo.routes.spec.ts
Resultado: passou, 41 testes
Observações: valida a primeira fusão de rota de cadastro, com vis-cadastro apontando para CadastroView.
```

```text
Data: 2026-04-25
Comando: npm run typecheck --prefix frontend
Resultado: passou
Observações: executado após adicionar ações de análise em CadastroView.
```

```text
Data: 2026-04-25
Comando: npm run test:unit --prefix frontend -- src/views/__tests__/AtividadesCadastroView.spec.ts src/views/__tests__/CadastroViewPermissoes.spec.ts src/views/__tests__/CadastroViewCoverage.spec.ts src/router/__tests__/processo.routes.spec.ts src/router/__tests__/router.spec.ts
Resultado: passou, 47 testes
Observações: executado após remover CadastroVisualizacaoView e seu teste legado.
```

```text
Data: 2026-04-25
Comando: npm run typecheck --prefix frontend
Resultado: passou
Observações: executado após remover CadastroVisualizacaoView.
```

```text
Data: 2026-04-25
Comando: npx vitest run frontend/src/views/__tests__/CadastroVisualizacaoView.spec.ts --reporter=dot --no-color
Resultado: falhou antes dos testes por resolução do alias @ fora do cwd/config do frontend
Observações: usar o script com --prefix frontend e caminho relativo a frontend.
```
Use este formato ao registrar novas validações:

```text
Data:
Comando:
Resultado:
Observações:
```

## Falhas e achados

```text
Achado: E2E usa cards e URLs vis-* como contrato de modo.
Impacto: a junção das views vai exigir revisão forte dos helpers e specs, mesmo que as rotas antigas sejam preservadas.
Decisão: migrar E2E para asserts semânticos de ações disponíveis/ausentes e manter poucos testes de compatibilidade de rota.
```

Use este formato ao registrar:

```text
Achado:
Impacto:
Decisão:
```

## Pendências abertas

- [ ] Implementar etapa de Cadastro.
- [ ] Implementar etapa de Mapa.
- [ ] Revisar navegação final.
- [ ] Revisar documentação final.
