# E2E Test Investigation - Learnings and Fixes

## Data da Investigação
2026-02-12

## Contexto
Testes E2E (cdu-xx) estavam falhando após grandes refatorações no projeto. Esta investigação foi conduzida para identificar e corrigir os problemas de isolamento, estado do banco de dados e estabilidade dos seletores.

## Descobertas Principais

### 1. Isolamento de Estado e describe.serial()

**Problema Identificado:**
Acreditava-se que `test.describe.serial()` manteria o estado do banco de dados entre os blocos `test()`. No entanto, descobriu-se que o Playwright re-executa ou re-importa o contexto das fixtures para cada teste individual. 

- A variável `lastResetFile` no helper de limpeza era resetada.
- O banco de dados sofria reset no início de cada `test()` dentro do bloco serial.
- Isso fazia com que processos criados em passos de "Preparação" sumissem nos passos seguintes.

**Solução:**
Consolidar fluxos de trabalho dependentes (ex: Preparação 1 a 8) em um **único bloco `test()`**. Isso garante que a transação e o estado do banco permaneçam consistentes durante todo o workflow do caso de uso.

### 2. Inconsistência nas Fixtures Utilizadas

**Problema Identificado:**
Os testes CDU estavam usando dois padrões diferentes de fixtures, causando inconsistência no reset do banco de dados.

#### Fixtures Disponíveis:
1. **`complete-fixtures.ts`** - Completa com auto-reset de DB
   - Herda de `processo-fixtures.js` (que herda de `auth-fixtures.js`)
   - Inclui reset automático de database via `beforeEach`
   - Inclui fixture `cleanupAutomatico` para limpar processos criados
   - **Padrão recomendado** para testes E2E

2. **`auth-fixtures.ts`** - Apenas autenticação
   - Fornece apenas fixtures de login (sem reset de DB)
   - Requer `resetDatabase()` manual em `beforeAll`/`beforeEach`

#### Distribuição Atual:
- ✅ **35/36 testes CDU** migrados para `complete-fixtures.ts`.
- ✅ **CDU-01** mantém `auth-fixtures.ts` (teste de login puro, correto).

### 3. Estabilidade da UI e Transições de Estado

**Aprendizado:**
Verificar apenas a mensagem de sucesso (toast) não garante que a transação de fundo no backend ou a renderização de elementos dependentes na tela seguinte foi concluída.

**Melhores Práticas:**
- Sempre aguardar por um indicador de estado na UI antes de prosseguir (ex: esperar que a badge de situação mude para "Disponibilizado" antes de tentar homologar).
- Usar `expect(locator).toContainText()` em linhas de tabela para confirmar transições de estado.
- Utilizar helpers centralizados (`criarCompetencia`, `navegarParaMapa`) que já possuem asserções de estabilidade embutidas.

## Problemas Resolvidos

### 1. Visibilidade de Processos no Painel
**Problema:** Processos sumiam do painel do CHEFE após serem iniciados pelo ADMIN.
**Causa Real:** Reset do banco de dados entre os testes de preparação no bloco serial.
**Correção:** Consolidação dos testes de preparação em um único bloco funcional. O "Bug JPA" anteriormente suspeitado foi mitigado por esta mudança de abordagem.

### 2. Redução de Boilerplate
**Ação:** Remoção de blocos `beforeAll`/`afterAll` e chamadas manuais a `resetDatabase()` em favor do uso padronizado de `complete-fixtures.ts`.
**Resultado:** Redução líquida de 170 linhas de código na suíte de testes.

## Comandos Úteis

### Executar Testes
```bash
# Teste único (arquivo completo)
npx playwright test e2e/cdu-XX.spec.ts --reporter=list

# Com saída capturada para análise profunda
npx playwright test e2e/cdu-XX.spec.ts --reporter=list > test_output.txt 2>&1
```

### Analisar Resultados
```bash
# Ver apenas falhas e timeouts
grep -i "error\|failed\|timeout" test_output.txt

# Ver logs do backend durante o teste
grep "BACKEND" test_output.txt
```

## Observações Finais

1. **Nunca aumentar timeouts sem motivo**: Se um elemento não aparece, é problema de dados ou estado, não de lentidão.
2. **Serial ≠ Stateful**: `describe.serial` garante ordem, mas não preserva variáveis de memória ou estado de DB entre `test()` se as fixtures resetarem.
3. **Use o error-context.md**: Ele contém o snapshot exato da página (YAML) e ajuda a identificar se o usuário está na página errada ou se uma validação barrou a navegação.

---
**Responsável:** Jules (Agente Copilot)  
**Última Atualização:** 2026-02-12
