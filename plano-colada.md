# Plano de Simplificação Estrutural do Frontend (SGC)

## Estado atual

Score arquitetural: **47** (medido em 2026-05-29, após adição de sinais de fragmentação).
Todos os problemas estruturais graves foram resolvidos. Restam três hotspots de coesão e sinais de fragmentação de composables.

Medir com: `node etc/scripts/sgc.js frontend arquitetura auditar`

---

## Métricas atuais

| Métrica                         | Atual | Meta     |
| ------------------------------- | ----- | -------- |
| Score total                     | 47    | < 25     |
| `superficieAmpla` (arquivos)    | 9     | < 6      |
| `palavraForcar` (ocorrências)   | 20    | 0        |
| `fachadasPuras`                 | 1     | 0        |
| `composablesMinusculos`         | 9     | < 4      |
| `familiasPulverizadas`          | 4     | < 2      |
| demais sinais                   | 0     | manter 0 |

---

## O que falta fazer

### 1. `stores/subprocesso/index.ts` — `forcar` exposto + superfície ampla

**Problema**: A store expõe o parâmetro `{forcar}` em `obterContextoEdicao` e
`obterContextoCadastroAtividades`. Consumidores em views (`subprocessoCarregamento.ts`,
`subprocessoAcoesAdministrativas.ts`) passam `{forcar: true}` diretamente, violando
o princípio de que views não devem conhecer estratégia de cache.

**Ação**:
- Remover `{forcar}` da API pública da store — a decisão de limpar cache deve ficar
  interna à store (baseada em evento/sinal, não em parâmetro passado por view).
- Consumidores em composables (`useFluxoSubprocessoExecucao`, `useCadastroOrquestracao`)
  que precisam de recarga forçada devem usar um método semântico (ex: `invalidarContexto()`).

---

### 2. `composables/useMapaSugestoes.ts` — superfície ampla

**Problema**: Sinal `superficieAmpla` — o composable exporta mais símbolos do que
o contrato de tela exige.

**Ação**: Auditar o que é consumido externamente e tornar interno o que for detalhe
de implementação.

---

### 3. `composables/useBuscadorUsuarios.ts` — superfície ampla

**Problema**: Mesmo padrão do item anterior.

**Ação**: Idem — estreitar o contrato público ao mínimo necessário pelos consumidores.

### 4. Fachada pura — `composables/useFluxoSubprocesso.ts`

**Problema**: Composable de 17 linhas que não tem lógica própria — apenas importa três outros
composables e re-exporta seus resultados (`useFluxoMapa`, `useFluxoSubprocessoExecucao`,
`useFluxoAdministrativoSubprocesso`). Isso indica que o ponto de entrada do módulo é o próprio
consumidor, não uma abstração real.

**Ação**: Verificar quem consome `useFluxoSubprocesso`. Se for apenas uma view, inlinar as três
chamadas direto nela. Se houver múltiplos consumidores, avaliar se o papel de fachada é justificado
e, em caso positivo, adicionar à lista `HUBS_CENTRAIS`.

---

### 5. Fragmentação de composables (9 minúsculos, 4 famílias)

**Problema**: Há 9 composables com menos de 30 linhas que dificilmente justificam arquivo próprio,
e 4 famílias com 4–5 membros (Fluxo, Mapa, Processo, Cadastro) que apontam para pulverização de
domínio.

**Ação sugerida por família**:
- **Fluxo** (5 arquivos, 423L): `useFluxoSubprocesso` é fachada pura (ver item 4); os demais
  cobrem fluxos distintos — avaliar fusão de `useFluxoAdministrativoSubprocesso` com
  `useFluxoSubprocessoExecucao` se tiverem consumidores sobrepostos.
- **Mapa** (5 arquivos, 952L): família densa, avaliar consolidar `useMapaQuery` + `useMapaSugestoes`
  em um único composable de domínio (isso também resolveria o sinal `superficieAmpla` de sugestões).
- **Processo** (5 arquivos, 581L): avaliar se `useProcessoQuery` (arquivo minúsculo) pode ser
  absorvido por `useProcessoCadastroCarga`.
- **Cadastro** (4 arquivos, 909L): família justificada pela separação de responsabilidades
  (mutações, orquestração, revisão, tela) — inspecionar antes de consolidar.

--- no Trabalho de Limpeza e Refatoração

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
