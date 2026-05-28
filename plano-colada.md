# Plano de Simplificação Estrutural do Frontend (SGC)

## Contexto

As fases de migração para `Pinia Colada` nos domínios simples estão concluídas.
Os ganhos fáceis foram capturados. O que resta é trabalho arquitetural:
apertar contratos, reduzir hubs centrais e eliminar server state caseiro nos nós que resistiram.

---

## Princípios

- **`Pinia`** → estado de aplicação e sessão (perfil, toast, UI local, formulários).
- **`Pinia Colada`** → apenas onde o problema principal é cache de dados remotos e a chave de query é clara e estável.
- Quando uma abstração mistura cache remoto com sessão, permissão, navegação e UI local, o primeiro passo é **separar responsabilidades**, não refatorar a implementação interna.
- View não deve conhecer estratégia de cache, `stale`, `snapshot`, `invalidar`, `forcar` ou equivalentes.
- Contratos consumidos por view devem ser orientados a **caso de uso da tela**, não a mecanismo de armazenamento.
- Não migrar domínios para Colada por simetria — só quando o problema principal for realmente server state.
- Mudar contratos internos obsoletos quando a compatibilidade já não paga o custo cognitivo.
- Cada mudança deve reduzir código ou simplificar testes de verdade.

---

## Diretrizes para contratos de tela (views)

### O que a view deve receber

- `carregarDadosDaTela()`
- `recarregarDadosDaTela()` — apenas quando a semântica de atualização explícita for relevante
- `executarAcaoDeTela(...)`
- Estado pronto para renderização: `carregando`, `erro`, `dados`

### O que a view **não** deve ver

- `temXEmCache()`, `obterXEmCache()`
- `forcar`, `stale`, `invalidar`, `reaplicarSnapshot`
- Distinção entre "obter do cache" vs "recarregar" — essa decisão deve ficar interna à abstração

Se uma view precisa decidir entre "usar cache ou não", a abstração ainda está vazando detalhe estrutural.

---

## Diretrizes para desfazer nós complexos

Diante de emaranhamento, na ordem:

1. Remover compatibilidade interna obsoleta
2. Apertar o contrato para refletir a dependência real
3. Separar estado remoto de estado local
4. Quebrar a abstração central em unidades menores
5. Só então simplificar a implementação restante

Evita refatoração cosmética onde o código muda de forma mas preserva o mesmo acoplamento.

---

## Régua de lint estrutural

As regras de lint estrutural (`complexity`, `max-params`, `max-depth`, `max-nested-callbacks`,
`max-lines`, `max-lines-per-function`, `max-statements`) estão ativas e **não devem ser desligadas** para "passar o gate".

Quando a régua acusar excesso, a ação esperada é:

- simplificar o código; ou
- quebrar a função/arquivo em contratos menores; ou
- recalibrar explicitamente o limite com justificativa técnica.

---

## O que falta fazer

### 1. `LimpezaProcessosView.vue` — service direto em view ✅ CONCLUÍDO

**Ação**: Lógica extraída para `useLimpezaProcessosTela.ts`. View reduzida para ~12 linhas. `viewsComServiceDireto` → 0.

---

### 2. `stores/organizacao.ts` — server state caseiro

**Problema**: A store ainda implementa cache manual completo:
`carregado`, `carregando`, `carregamentoEmAndamento`, `dadosValidos()`, `garantirDiagnostico()`, `invalidar()`.
É o único arquivo ainda marcado como `arquivosComServerStateCaseiro: 1`.

**Opções**:

- Migrar para `useQuery` do Pinia Colada (chave: `["diagnostico-organizacional"]`), já que o diagnóstico é leitura simples de sessão.
- Ou, se o contexto de instanciação fora de `setup()` ainda for um impeditivo real, ao menos encapsular o cache interno e remover `invalidar()` e `dadosValidos()` da superfície pública.

**Ação prioritária**: avaliar se os consumidores atuais permitem migração para query. Se sim, migrar. Se não, encapsular e reduzir superfície.

---

### 3. `stores/subprocesso/index.ts` — hub de cache manual complexo

**Problema**: A store gerencia dois contextos (`edicao` e `cadastro`) com deduplicação manual via `Map<string, Promise>`, flags `invalido`, e 8+ métodos `obter/recarregar` variantes. Score 9 (`superficieAmpla`, 18 símbolos exportados).

**Parcialmente resolvido**: `marcarContextoEdicaoParaAtualizacao` (código morto) removido.

**Pendente**: Consolidar variantes por código vs. por processo/unidade — ex: `obterContextoEdicao(chave)` aceitando ambos os padrões reduziria o surface a ~12 símbolos.

---

### 4. `composables/useSubprocessoTela.ts` — estratégia de cache exposta ✅ CONCLUÍDO

**Ação**: `useSubprocessoCarregamento` e `useSubprocessoAcoesAdministrativas` passaram a chamar `useSubprocessoStore()` diretamente, eliminando os 7 métodos de store passados como DI. Sinal `estrategiaCache` removido. Score total: 152 → 144.

---

### 5. `stores/perfilAutenticacao.ts` e `composables/usePerfil.ts` ✅ CONCLUÍDO

**Situação**: Falsos positivos eliminados via correção do script de auditoria (`arquitetura-lib.js`). Ambos saíram dos hotspots.

---

## Metas de progresso arquitetural

| Métrica                             | Baseline | Meta     | Atual   |
| ----------------------------------- | -------- | -------- | ------- |
| `viewsComServiceDireto`             | 1        | 0        | **0** ✅ |
| `arquivosComServerStateCaseiro`     | 1        | 0        | 1       |
| `arquivosComSuperficieAmpla`        | 16       | < 10     | 16      |
| `hubsCentraisComSinais`             | 2        | ≤ 1      | 2       |
| `arquivosComBolsaDependenciasLarga` | 1        | 0        | 1       |
| `viewsComVazamentoCache`            | 0        | manter 0 | **0** ✅ |
| `viewsComFanoutAlto`                | 0        | manter 0 | **0** ✅ |
| `viewsComServerStateCaseiro`        | 0        | manter 0 | **0** ✅ |
| Score arquitetural total            | 175      | < 100    | **144** |

Medir com: `node etc/scripts/sgc.js frontend arquitetura auditar`

---

## Lições Aprendidas no Trabalho de Limpeza e Refatoração

1. **Evitar Acoplamento de Testes de View com Queries do Pinia Colada**:
   
   - Componentes que utilizam `useQuery` dependem da inicialização de plugins adicionais do Pinia no contexto de testes. Em testes de visualização (specs de view), é preferível isolar a lógica usando `vi.mock()` para os composables que englobam a query (ex: `useDiagnosticoOrganizacionalQuery`), em vez de tentar configurar o ecossistema completo da query no `createTestingPinia`.
   - Isso evita o erro `TypeError: Cannot read properties of undefined (reading 'ext')` e mantém o teste focado apenas nas reações da interface aos estados de dados.

2. **Uso de Mocks Reativos em Testes**:
   
   - Ao mockar composables de queries/stores, retorne referências reativas (`ref`). Isso possibilita alterar dinamicamente o estado de resposta (ex: alterando `mockQuery.data.value`) dentro de testes individuais ou no `beforeEach`, sem precisar reinstanciar ou remontar o componente globalmente.
   - Sempre certifique-se de resetar os valores dos mocks em `beforeEach` para que mutações efetuadas por um teste não contaminem a execução dos testes subsequentes.

3. **Remoção Absoluta de Código e Configurações Mortas**:
   
   - Ao refatorar uma store ou composable (ex: removendo a dependência da `organizacaoStore`), faça a varredura completa nos arquivos `.spec.ts` correspondentes para eliminar inicializações de dados que não existem mais (ex: propriedades sob `initialState` no Pinia mockado).
   - Manter configurações obsoletas de Pinia gera falsos positivos de sucesso ou pode causar erros silenciosos difíceis de rastrear.

4. **Correção de Testes Pré-existentes de Forma Proativa**:
   
   - Ao deparar-se com falhas de testes pré-existentes durante a refatoração, resolva-as imediatamente. Muitas vezes as falhas ocorrem por dependências cruzadas ocultas ou estados de inicialização inválidos que vieram à tona com o isolamento adequado dos componentes.

---

## Validação por mudança

```bash
npm run quality:lint
npm run quality:typecheck
npm run test:unit
```

Quando a mudança tocar navegação relevante, validar também o fluxo real da tela afetada.
