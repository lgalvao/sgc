# Análise Detalhada dos Testes E2E

**Data:** 17 de dezembro de 2025
**Escopo:** Arquivos `cdu-01.spec.ts` a `cdu-21.spec.ts`

---

## 🧩 Padrões de Problemas Identificados

Antes de detalhar por arquivo, é crucial notar que os mesmos problemas se repetem em quase toda a suíte de testes, indicando uma dívida técnica sistêmica.

1.  **Testes Seriais e Monolíticos (`test.describe.serial`):** É o problema mais grave e presente na maioria dos arquivos. Testes dependem do estado deixado pelo teste anterior, tornando-os impossíveis de executar isoladamente, difíceis de depurar e lentos, pois impedem a paralelização.
2.  **Setup via UI:** A maioria dos testes gasta uma quantidade enorme de tempo e código preparando o ambiente através da interface gráfica (criando processos, atividades, etc.). Isso é extremamente ineficiente e frágil. A abordagem correta seria usar **fixtures** ou **seeding de dados via API** para colocar o sistema no estado desejado instantaneamente.
3.  **Duplicação de Helpers:** Funções básicas como `fazerLogout`, `verificarPaginaPainel`, e `acessarSubprocesso` são redefinidas localmente em múltiplos arquivos, em vez de serem centralizadas em módulos `helpers`.
4.  **Extração de ID Frágil:** A extração de IDs de processo da URL é feita com múltiplas variações de regex, sem tratamento de erro, uma falha silenciosa esperando para acontecer.

---

## 📋 Resumo por Arquivo

### `cdu-01.spec.ts` (Login)
- **Status:** ✅ Razoável
- **Análise:** Testes atômicos e independentes. Não apresenta os problemas críticos dos demais.
- **Oportunidade:** Mover validações de UI (navbar, rodapé) para um teste de layout dedicado.

### `cdu-02.spec.ts` (Painel)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Já introduz a extração de ID frágil e a duplicação de lógica de criação de processo. `localStorage.clear()` é um hack para contornar a falta de isolamento.

### `cdu-03.spec.ts` (Manter Processo)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Segue o padrão de extração de ID inconsistente e repete a lógica de criação de processo.

### `cdu-04.spec.ts` (Iniciar Processo)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Teste linear que mistura várias ações. Uma falha no meio invalida o resto.

### `cdu-05.spec.ts` (Iniciar Revisão)
- **Status:** 🔴 Crítico
- **Análise:** Primeiro exemplo claro de `test.describe.serial` com um teste monolítico ("Fase 1") de mais de 200 linhas e duplicação de helpers.

### `cdu-06.spec.ts` (Detalhar Processo)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Repete o padrão de setup via UI e extração de ID frágil.

### `cdu-07.spec.ts` (Detalhar Subprocesso)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Fluxo de teste pesado (login admin, cria, logout, login chefe) para uma simples verificação de detalhes.

### `cdu-08.spec.ts` (Manter Atividades)
- **Status:** ⚠️ Atenção Necessária
- **Análise:** Usa `test.step`, mas o teste continua sendo um monólito sequencial com dependência de estado entre os passos.

### `cdu-09.spec.ts` (Disponibilizar Atividades)
- **Status:** 🔴 Crítico
- **Análise:** Outro caso grave de `test.describe.serial`, com helpers duplicados e estado (`processoId`) compartilhado entre os testes.

### `cdu-10.spec.ts` (Disponibilizar Revisão)
- **Status:** 🔴 Crítico
- **Análise:** Um dos piores casos. **8 testes de "Preparação"** antes dos 5 testes principais. Extremamente lento, frágil e complexo.

### `cdu-11.spec.ts` (Visualizar Cadastro)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial`, helpers duplicados e uma fase de preparação massiva que executa um fluxo completo de outro CDU.

### `cdu-12.spec.ts` (Verificar Impactos)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial`. Outro teste gigante com uma preparação que executa um processo de mapeamento inteiro apenas para poder iniciar uma revisão.

### `cdu-13.spec.ts` (Analisar Cadastro)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial`. Embora use helpers de análise (bom!), a estrutura serial persiste, com cada cenário dependendo do anterior.

### `cdu-14.spec.ts` (Analisar Revisão)
- **Status:** 🔴 Crítico
- **Análise:** Talvez o caso mais extremo. Um "Preparacao 0" gigantesco executa um ciclo de vida inteiro. Os cenários são todos encadeados. É o oposto de um teste robusto.

### `cdu-15.spec.ts` (Manter Mapa)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial`. Os testes de criar, editar e excluir competências dependem uns dos outros, em vez de serem atômicos.

### `cdu-16.spec.ts` (Ajustar Mapa)
- **Status:** 🔴 Crítico
- **Análise:** Um monstro com **9 testes de preparação** antes do início dos testes principais. Exemplo perfeito de anti-padrão em testes E2E.

### `cdu-17.spec.ts` (Disponibilizar Mapa)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial`, longa fase de preparação e helpers duplicados.

### `cdu-18.spec.ts` (Visualizar Mapa)
- **Status:** ✅ **Excelente**
- **Análise:** Este teste é um modelo a ser seguido. **Não usa `serial`**. Ele depende de um estado pré-configurado no banco de dados (`resetDatabase` provavelmente com um seed). Os testes são curtos, focados, independentes e rápidos.

### `cdu-19.spec.ts` (Validar Mapa)
- **Status:** 🔴 Crítico
- **Análise:** Retorna ao padrão de `test.describe.serial` com setup via UI e testes dependentes.

### `cdu-20.spec.ts` (Analisar Validação do Mapa)
- **Status:** 🔴 Crítico
- **Análise:** `test.describe.serial` com 7 passos de preparação e cenários encadeados.

### `cdu-21.spec.ts` (Finalizar Processo)
- **Status:** 🔴 Crítico
- **Análise:** Inacreditavelmente ineficiente. Executa um ciclo de vida de processo inteiro (7 testes de preparação) apenas para testar o clique no botão "Finalizar".

---

## 🚀 Estratégia de Solução: Dynamic Seeding com Isolamento

O desafio de remover o `serial` não é trivial devido às regras de negócio restritivas (ex: uma unidade só pode ter um processo ativo). Simplesmente encher o `seed.sql` causaria conflitos entre testes paralelos.

A solução proposta é o **Dynamic Seeding via API com Alocação de Recursos**.

### 1. O Conceito
Em vez de depender de dados estáticos ou criar dados via UI (lento), cada teste deve criar seu próprio cenário via API no momento da execução (`beforeEach` ou Fixture), garantindo o uso de recursos (Unidades) exclusivos.

### 2. Alocação de Unidades por Worker
Para evitar que dois testes paralelos tentem criar processos na mesma unidade (o que violaria a regra de negócio), usamos o `workerIndex` do Playwright para distribuir as unidades disponíveis.

**Exemplo de Distribuição:**
- Worker 0: Usa `SECAO_111`, `SECAO_112`
- Worker 1: Usa `SECAO_121`, `SECAO_211`
- Worker 2: Usa `SECAO_212`, `SECAO_221`

### 3. Implementação Técnica (Exemplo)

**A. Helper de API (`api-helpers.ts`)**
Funções que chamam o backend diretamente para criar cenários complexos em milissegundos.
```typescript
export async function criarCenarioProcessoPronto(request, unidade) {
    const proc = await request.post('/api/processos', { ... });
    await request.post(`/api/processos/${proc.id}/iniciar`);
    // ... chamadas para criar atividades, homologar, etc.
    return proc;
}
```

**B. Fixture Inteligente (`fixtures.ts`)**
```typescript
export const test = base.extend({
    cenarioFinalizacao: async ({ request }, use, testInfo) => {
        // Seleciona unidade baseada no worker para garantir isolamento
        const unidades = ['SECAO_111', 'SECAO_121', 'SECAO_221', ...];
        const unidadeAlvo = unidades[testInfo.workerIndex % unidades.length];
        
        // Cria dados via API
        const dados = await criarCenarioProcessoPronto(request, unidadeAlvo);
        
        await use(dados); // Teste roda aqui
        
        // Cleanup via API
        await request.delete(`/api/processos/${dados.id}`);
    }
});
```

### 4. Benefícios
- **Velocidade:** Setup cai de minutos (UI) para milissegundos (API).
- **Robustez:** Testes não dependem da UI para preparação.
- **Paralelismo:** Testes podem rodar simultaneamente sem conflito de dados.
- **Manutenibilidade:** Fim dos testes gigantes e interdependentes.

---

## 📋 Plano de Ação Imediato

1.  **Criar `helpers/api-helpers.ts`:** Implementar funções para criar processos, atividades e competências via API.
2.  **Criar Fixtures de Isolamento:** Implementar a lógica de seleção de unidade baseada em `workerIndex`.
3.  **Refatorar CDU-21 (Piloto):** Converter o teste mais ineficiente para o novo modelo como prova de conceito.
4.  **Padronizar Extração de ID:** Implementar `extrairProcessoId` robusto.
5.  **Consolidar Helpers de UI:** Eliminar duplicação de código nos arquivos `.spec.ts`.
