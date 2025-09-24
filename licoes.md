# Lições Aprendidas - Correção de Testes E2E

## Introdução
Este documento registra as lições aprendidas durante a correção de testes end-to-end (E2E) com Playwright, especialmente ao lidar com elementos dinâmicos e assíncronos em aplicações Vue.js.

## Lições Técnicas

### 1. Seletor de Modal Genérico vs Específico

**Problema:** O teste estava usando `.modal` como seletor, que é muito genérico e pode encontrar outros modais na página.

**Solução:** Usar seletor mais específico como `.modal.show` ou `.modal.fade.show`.

```typescript
// ❌ Genérico - pode causar conflitos
await expect(page.locator('.modal')).toBeVisible();

// ✅ Específico - identifica apenas o modal visível
await expect(page.locator('.modal.show')).toBeVisible();
```

**Lição:** Sempre use seletores específicos para evitar conflitos com outros elementos similares na página.

### 2. Elementos que Aparecem Assincronamente

**Problema:** Testes tentavam interagir com elementos que só aparecem após ativação de modo de edição, sem aguardar a renderização.

**Solução:** Implementar múltiplas estratégias de espera com fallbacks.

```typescript
// ❌ Tenta encontrar imediatamente
await page.getByTestId('input-editar-conhecimento').fill(novoNome);

// ✅ Aguarda com múltiplas estratégias
await page.waitForSelector('.conhecimento-edicao-input', { state: 'visible', timeout: 5000 });
await page.waitForFunction(() => {
  const elements = document.querySelectorAll('[data-testid="input-editar-conhecimento"]');
  return Array.from(elements).some(el => el.isConnected && el.offsetParent !== null);
}, { timeout: 5000 });
```

**Lição:** Para elementos que aparecem dinamicamente, implemente múltiplas estratégias de espera e use timeouts adequados.

### 3. Tempos de Espera Insuficientes

**Problema:** Tempos de espera muito curtos não permitiam que o Vue.js processasse as mudanças de estado.

**Solução:** Aumentar tempos de espera e adicionar verificações intermediárias.

```typescript
// ❌ Espera muito curta
await page.waitForTimeout(100);

// ✅ Espera adequada com verificações
await page.waitForTimeout(500);
await expect(btnEditar).toBeVisible();
await btnEditar.click({ force: true });
await page.waitForTimeout(1000);
```

**Lição:** Vue.js precisa de tempo para processar mudanças de estado reativo. Use tempos de espera adequados e verifique estados intermediários.

### 4. Estratégias de Fallback

**Problema:** Quando uma estratégia de localização falha, o teste para completamente.

**Solução:** Implementar múltiplas estratégias com fallbacks.

```typescript
let inputEncontrado = false;
try {
  // Estratégia 1: classe específica
  await page.waitForSelector('.conhecimento-edicao-input', { state: 'visible', timeout: 5000 });
  inputEncontrado = true;
} catch (error) {
  // Estratégia 2: data-testid
  await page.waitForFunction(() => {
    const elements = document.querySelectorAll('[data-testid="input-editar-conhecimento"]');
    return Array.from(elements).some(el => el.isConnected && el.offsetParent !== null);
  }, { timeout: 5000 });
  inputEncontrado = true;
}

if (!inputEncontrado) {
  // Estratégia 3: fallback genérico
  const inputVisivel = linhaConhecimento.locator('input').first();
  await expect(inputVisivel).toBeVisible({ timeout: 5000 });
  await inputVisivel.fill(novoNome);
}
```

**Lição:** Sempre tenha estratégias de fallback para quando a estratégia principal falhar.

### 5. Verificação de Estados Intermediários

**Problema:** Testes assumiam que elementos estavam em certo estado sem verificar.

**Solução:** Verificar estados antes de interagir.

```typescript
// ❌ Assume que o botão está visível
await btnEditar.click({ force: true });

// ✅ Verifica antes de interagir
const btnEditar = linhaConhecimento.getByTestId(SELETORES.BTN_EDITAR_CONHECIMENTO);
await expect(btnEditar).toBeVisible();
await btnEditar.click({ force: true });
```

**Lição:** Sempre verifique o estado dos elementos antes de interagir com eles.

## Lições sobre Ferramentas

### 1. Estrutura do apply_diff

**Problema:** Erros na estrutura XML do apply_diff causavam falhas na aplicação das mudanças.

**Solução:** Seguir rigorosamente a estrutura correta.

```xml
<!-- ✅ Estrutura correta -->
<apply_diff>
<args>
  <file>
    <path>caminho/relativo/arquivo.ext</path>
    <diff>
      <content>
        <![CDATA[
          Conteúdo exato a ser substituído
          incluindo contexto suficiente
        ]]>
      </content>
      <start_line>número_da_linha</start_line>
    </diff>
  </file>
</args>
</apply_diff>
```

**Lição:** Sempre inclua contexto suficiente no diff e use a estrutura XML correta.

### 2. Leitura de Arquivos Relacionados

**Problema:** Modificações sem entender o contexto completo causavam problemas.

**Solução:** Ler todos os arquivos relacionados antes de fazer mudanças.

```typescript
// ❌ Modificar sem contexto
await expect(page.locator('.modal')).toBeVisible();

// ✅ Entender o contexto primeiro
// Ler componente Vue para entender como o modal funciona
// Verificar outros testes que usam o mesmo padrão
```

**Lição:** Sempre leia o contexto completo antes de fazer modificações.

### 3. Testes Incrementais

**Problema:** Modificar múltiplas partes de uma vez dificultava a identificação do problema.

**Solução:** Testar mudanças incrementalmente.

```typescript
// ❌ Múltiplas mudanças de uma vez
// Modificar seletor E aumentar timeout E adicionar verificações

// ✅ Mudanças incrementais
// 1. Primeiro corrigir apenas o seletor
// 2. Testar
// 3. Depois adicionar verificações
// 4. Testar novamente
```

**Lição:** Faça mudanças incrementais e teste cada uma separadamente.

## Boas Práticas para Testes E2E

### 1. Nomenclatura de Seletores
- Use `data-testid` consistentes e descritivos
- Evite seletores baseados em classes CSS que podem mudar
- Prefira seletores semânticos como `getByRole`, `getByLabel`, etc.

### 2. Estrutura de Testes
- Separe lógica de teste em funções auxiliares
- Use constantes para seletores e textos
- Implemente funções de espera reutilizáveis

### 3. Debugging
- Adicione logs para entender o fluxo de execução
- Use `page.pause()` para debug interativo
- Verifique o DOM gerado para entender a estrutura real

### 4. Manutenção
- Mantenha funções auxiliares organizadas
- Documente estratégias complexas
- Revise testes regularmente para remover código obsoleto

## Lição Fundamental: Seguir o Guia de Testes

### O Problema Real
**Problema:** Tentamos resolver problemas técnicos (timeouts, seletores complexos) quando o problema real era não seguir as melhores práticas estabelecidas no guia.

**Solução:** Aplicar rigorosamente as regras do `guia-testes-playwright.md`.

### Aplicação das Regras do Guia

#### ❌ Abordagem Inicial (Problemática)
```typescript
// Múltiplas estratégias complexas com timeouts longos
await page.waitForTimeout(2000);
await page.waitForSelector('.conhecimento-edicao-input', { state: 'visible', timeout: 5000 });
await page.waitForFunction(() => { /* lógica complexa */ }, { timeout: 5000 });
```

#### ✅ Abordagem Corrigida (Seguindo o Guia)
```typescript
// Simples, direto e seguindo as melhores práticas
await expect(btnEditar).toBeVisible();
await btnEditar.click();
await expect(inputEdicao).toBeVisible();
await inputEdicao.fill(novoNome);
await expect(btnSalvar).toBeVisible();
await btnSalvar.click();
```

### Regras Aplicadas com Sucesso

#### 1. **Timeouts Curtos (Regra 17)**
- ❌ Máximo 5000ms, mas usávamos timeouts de até 12000ms
- ✅ Usar apenas timeouts necessários para operações específicas
- ✅ Remover `waitForTimeout` desnecessários

#### 2. **Espera Ativa vs Tempos Fixos (Regra 28)**
- ❌ `await page.waitForTimeout(2000)` (tempo fixo)
- ✅ `await expect(inputEdicao).toBeVisible()` (espera ativa)

#### 3. **Seletores Precisos (Regra 23)**
- ❌ Seletores ambíguos e múltiplas estratégias
- ✅ Seletores diretos e específicos: `getByTestId()`

#### 4. **Testes Atômicos (Regra 27)**
- ❌ Funções complexas com múltiplas responsabilidades
- ✅ Funções simples e focadas em uma única ação

### Resultado da Aplicação Correta

**Antes:** Testes falhando com timeouts e erros de localização
**Depois:** Testes passando consistentemente

### Principais Aprendizados

#### 1. **Simplificação é a Chave**
- Remover complexidade desnecessária
- Usar a abordagem mais direta possível
- Deixar o Playwright fazer o que ele faz melhor

#### 2. **O Guia Está Certo**
- As regras não são sugestões, são requisitos
- Seguir rigorosamente as melhores práticas resolve 90% dos problemas
- Não tente "otimizar" o que já está otimizado

#### 3. **Debugging Sistemático**
- Identificar a regra violada
- Corrigir seguindo a regra específica
- Testar incrementalmente

#### 4. **Manutenibilidade**
- Código simples é mais fácil de manter
- Menos estratégias = menos pontos de falha
- Seguindo padrões estabelecidos

#### 5. **Pré-condições dos Testes**
- **Verifique as pré-condições**: Alguns testes requerem dados específicos ou estados particulares
- **Use dados mockados corretos**: Certifique-se de que os dados de teste atendem aos requisitos do teste
- **Documente pré-requisitos**: Anote quando um teste depende de outros testes ou dados específicos

**Exemplo:**
```typescript
// ❌ Teste usando processo genérico
await page.goto('/processo/1');

// ✅ Teste usando processo com pré-condições específicas
await page.goto('/processo/99'); // Processo configurado para finalização
```

## Conclusão

A correção bem-sucedida dos testes demonstrou que:

1. **O problema não era técnico, era de processo**: Estávamos tentando resolver sintomas em vez de seguir as melhores práticas estabelecidas.

2. **Simplificação resolve complexidade**: A solução mais simples geralmente é a correta.

3. **O guia é prescritivo por uma razão**: As regras foram criadas com base em experiência e resolvem problemas comuns.

4. **Consistência é fundamental**: Seguir as mesmas regras em todos os testes garante confiabilidade.

**Lição Final:** Quando os testes falham, primeiro verifique se você está seguindo todas as regras do guia. A maioria dos problemas se resolve aplicando corretamente as melhores práticas estabelecidas, não criando soluções complexas.

Essas lições devem ser aplicadas em futuros desenvolvimentos de testes E2E para evitar problemas similares e garantir testes robustos e manuteníveis.