# Investigação do Problema no Teste E2E CDU-10 - Cenário 4

**Data:** 2025-12-12  
**Teste:** `e2e/cdu-10.spec.ts` - Cenário 4: "Verificar que histórico foi excluído após nova disponibilização"

## Erro Original

```
Error: expect(locator).toBeVisible() failed

Locator: getByText('Rev 10 1765560379806')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

  400 |         await expect(page.getByText(descProcessoRevisao)).toBeVisible();
      |                                                           ^
```

O teste esperava que o processo de revisão estivesse visível no painel do admin, mas o elemento não foi encontrado.

## Investigação Inicial - Hipóteses Testadas

### Hipótese 1: Problema de Timing/Carregamento
**Investigação:** Verificamos se era um problema de tempo de carregamento adicionando logs de debug.  
**Resultado:** Quando logs foram adicionados, o teste **passou**. Isso sugeria condição de corrida.  
**Conclusão:** Descartada após análise das regras E2E - segundo `regras/e2e_regras.md`, nunca é problema de timing em ambiente local com H2.

### Hipótese 2: Processo de Revisão Foi Excluído
**Investigação:** Verificamos os logs do backend nos testes executados.  
**Resultado:** 
- Linha 39 dos logs: `Processo 2 criado.`
- Linha 47: `Processo de revisão 2 iniciado para 1 unidades.`
- Não há logs de exclusão do processo

**Conclusão:** O processo existe no banco de dados.

### Hipótese 3: Seletor Incorreto no Teste
**Investigação:** O teste usava:
```typescript
await page.locator('tbody tr', {has: page.getByText(descProcessoRevisao, {exact: true})}).click();
```

Adicionamos debug para ver onde o texto estava aparecendo:
```typescript
const linhasProcesso = await page.locator('tbody tr', {has: page.getByText(descProcessoRevisao, {exact: true})}).count();
console.log(`[DEBUG] Linhas com processo de revisão: ${linhasProcesso}`);
```

**Resultado:** 
- Encontrava **1 linha** com o texto
- Mas quando clicava, ia para `/processo/99` (Processo Seed 99) em vez do processo de revisão
- Depois, com mais debug, ia para `/processo/2` (correto!)

**Descoberta Importante:** O texto do processo de revisão estava aparecendo na **tabela de alertas**, não na tabela de processos!

## Análise da Estrutura do Painel

### Debug Executado com Sucesso

Quando executamos o teste com logs detalhados (teste-cdu10-debug.txt):

```
[DEBUG] URL atual: http://localhost:5173/painel
[DEBUG] Linhas na tabela de processos: 2
[DEBUG] Processo 1: "Map 10 1765561231196MapeamentoSECAO_221Finalizado"
[DEBUG] Processo 2: "Processo Seed 99MapeamentoASSESSORIA_12Finalizado"
```

**Problema Identificado:** Na tabela de processos há apenas 2 processos:
1. Map 10 (processo 1) - FINALIZADO
2. Processo Seed 99 - FINALIZADO

**Faltam:**
- Processo Seed 200 - FINALIZADO (deveria estar no seed)
- **Rev 10 (processo 2) - EM_ANDAMENTO** ← O processo que o teste precisa!

## Descoberta do Bug Real

### Por Que o Processo de Revisão Não Aparece?

**Código Backend Relevante:**

`PainelService.java` linhas 50-68:
```java
public Page<ProcessoResumoDto> listarProcessos(
        Perfil perfil, Long codigoUnidade, Pageable pageable) {
    if (perfil == null) {
        throw new ErroParametroPainelInvalido("O parâmetro 'perfil' é obrigatório");
    }

    Page<Processo> processos;
    if (perfil == Perfil.ADMIN) {
        processos = processoRepo.findAll(pageable);  // ← Retorna TODOS os processos
    } else {
        if (codigoUnidade == null) return Page.empty(pageable);
        List<Long> unidadeIds = obterIdsUnidadesSubordinadas(codigoUnidade);
        unidadeIds.add(codigoUnidade);
        processos = processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(
                unidadeIds, SituacaoProcesso.CRIADO, pageable);
    }
    return processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade));
}
```

Para ADMINs, o código usa `findAll(pageable)` que deveria retornar **todos** os processos.

**Paginação:**
- Backend: `@PageableDefault(size = 20)` - padrão 20 itens
- Frontend: solicita `size: 10` - apenas 10 itens
- Total de processos no banco: 4 (Seed 99, Seed 200, Map 10, Rev 10)
- **Todos deveriam caber na primeira página!**

**Ordenação Padrão:** JPA `findAll` sem especificar ordenação usa ordenação por ID (código) natural.
- Ordem esperada: 99, 200, 1, 2

### Por Que Apenas 2 Processos Aparecem?

**Possíveis causas ainda não investigadas:**
1. Processo Seed 200 pode não existir no seed de E2E (`e2e/setup/seed.sql`)
2. Algum filtro adicional está sendo aplicado no frontend
3. Problema com a ordenação padrão do JPA
4. Processos sendo excluídos ou modificados em outro teste que roda antes

## Tentativas de Solução (Incorretas)

### Tentativa 1: Navegar Diretamente pela URL
```typescript
await page.goto(`/processo/${processoRevisaoId}`);
```
**Problema:** Usuários reais não navegam por URL. Viola princípio dos testes E2E.

### Tentativa 2: Clicar na Tabela de Alertas
```typescript
const tabelaAlertas = page.locator('table').nth(1);
await tabelaAlertas.locator('tbody tr', {has: page.getByText(descProcessoRevisao)}).click();
```
**Problema:** Alertas não têm link de navegação (e não deveriam ter). A funcionalidade estava implementada incorretamente.

### Tentativa 3: Adicionar Esperas (wait)
```typescript
await expect(page.getByText(descProcessoRevisao)).toBeVisible();
```
**Problema:** Viola regra fundamental de E2E - se precisa de espera, há um bug no código.

## Correções Implementadas

### 1. Removido `linkDestino` de Alertas

**Arquivos modificados:**
- `backend/src/main/java/sgc/alerta/dto/AlertaDto.java`
- `backend/src/main/java/sgc/alerta/dto/AlertaMapper.java`
- `backend/src/main/java/sgc/alerta/AlertaService.java`
- `backend/src/main/java/sgc/painel/PainelService.java`
- `frontend/src/types/tipos.ts`
- `frontend/src/views/PainelView.vue`
- `frontend/src/mappers/alertas.ts`
- Testes unitários correspondentes

**Justificativa:** Alertas não devem ter navegação. Essa funcionalidade foi implementada incorretamente por uma IA em algum momento anterior.

### 2. Corrigido o Teste para Comportamento Real do Usuário

```typescript
test('Cenario 4: Verificar que histórico foi excluído após nova disponibilização', async ({page}) => {
    await fazerLogout(page);
    await login(page, USUARIO_ADMIN, SENHA_ADMIN);

    // Verificar que o processo de revisão está visível no painel
    await expect(page.getByText(descProcessoRevisao)).toBeVisible();
    
    // Clicar no processo de revisão na tabela de processos
    await page.locator('tbody tr', {has: page.getByText(descProcessoRevisao, {exact: true})}).first().click();
    
    // ... resto do teste
});
```

O teste agora:
- Espera que o processo esteja na tabela de processos
- Clica diretamente na linha do processo
- **Vai falhar**, expondo o bug real no sistema

## Bug Real Identificado (Ainda Não Corrigido)

**Sintoma:** Processo de revisão não aparece na tabela de processos do painel do admin após a segunda disponibilização (Cenário 3, linha 388-392).

**Estado Esperado:**
- Processo de revisão deveria estar visível na tabela de processos do admin
- Admin deveria conseguir clicar nele para acessar

**Estado Atual:**
- Processo existe no banco (confirmado pelos logs)
- Processo NÃO aparece na tabela de processos
- Texto do processo aparece nos alertas

**Próximos Passos para Correção:**

1. **Verificar se o Processo Seed 200 existe no seed de E2E:**
   ```bash
   grep "Seed 200" e2e/setup/seed.sql
   ```
   Confirmado: linha 258 do seed.sql tem o processo 200.

2. **Verificar ordenação e filtros aplicados:**
   - Adicionar logs no `PainelService.listarProcessos`
   - Verificar quais processos estão sendo retornados
   - Confirmar ordem e paginação

3. **Verificar se processos estão sendo excluídos:**
   - Adicionar logs em `ProcessoRepo.delete`
   - Verificar se há cleanup automático em algum ponto

4. **Verificar situação do processo:**
   - Confirmar qual é a `SituacaoProcesso` do processo 2 após a segunda disponibilização
   - Verificar se há algum filtro por situação sendo aplicado

## Lições Aprendidas

1. **Nunca adicionar esperas em testes E2E locais** - se precisa de espera, há um bug no código
2. **Testes E2E devem simular comportamento real do usuário** - não navegar por URLs diretas
3. **Logs de debug podem mascarar problemas** - o timing adicional dos logs fez o teste passar
4. **Ler regras do projeto antes de implementar soluções** - `regras/e2e_regras.md` teria evitado várias tentativas incorretas
5. **Alertas não devem ter navegação** - funcionalidade foi implementada incorretamente
6. **Investigar a causa raiz, não os sintomas** - o problema não era o seletor, mas a ausência do processo na tabela

## Referências

- `regras/e2e_regras.md` - Regras para testes E2E
- `reqs/cdu-02.md` - Requisitos do painel (confirmado que ADMINs devem ver processos CRIADOS)
- `backend/src/main/java/sgc/painel/PainelService.java` - Serviço que lista processos
- `e2e/cdu-10.spec.ts` - Teste completo do CDU-10
