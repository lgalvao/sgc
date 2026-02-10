## Current Status (2026-02-07 - 15:33)
- **Fixed**: CDU-10 (16/16), CDU-11 (7/7), CDU-19 (7/7), CDU-13 (6/12 progressing)  
- **Key Fixes Applied**:
  1. Removed auth fixtures from serial tests
  2. Added manual login() calls
  3. Removed cleanupAutomatico from intermediate steps
  4. Fixed toast timing with waitForResponse
  5. Added .first() to toast verifications

## ⚠️ CRITICAL: Auth Fixtures + cleanupAutomatico in Serial Tests

**TWO separate problems discovered:**

### Problem 1: Auth Fixtures Don't Logout
Auth fixtures (`autenticadoComoAdmin`) in serial tests cause session conflicts when switching users.

**Solution**: Use manual `login()` instead:
```typescript
test('Step with user change', async ({page}) => {
    await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
    // ...
})
```

### Problem 2: cleanupAutomatico Deletes Data Between Serial Steps  
`cleanupAutomatico.registrar(processoId)` in FIRST test of serial suite deletes the process AFTER that test, before the NEXT test runs!

**Error seen**:
```
ObjectRetrievalFailureException: No row exists for entity [Processo with id '1']
500 GET /api/painel/alertas
```

**Solution**: DON'T register cleanup in intermediate steps. Only at the END:
```typescript
test('Prep 1', async ({page}) => {  // NO cleanupAutomatico here!
    processoId = await extrairProcessoId(page);
    // Don't register yet!
})

test.afterAll(async ({request}) => {
    if (processoId > 0) {
        await request.delete(`http://localhost:10000/e2e/processos/${processoId}`);
    }
});
```

## Fixed Patterns

### 1. Toast Timing with waitForResponse
```typescript
// ❌ WRONG - Toast disappears too fast
await page.getByTestId('btn-confirmar').click();
await expect(page.getByText(/Success/i)).toBeVisible();

// ✅ CORRECT - Wait for API response
const responsePromise = page.waitForResponse(resp => 
    resp.url().includes('/disponibilizar-mapa') && resp.status() === 200
);
await page.getByTestId('btn-confirmar').click();
await responsePromise;
await verificarPaginaPainel(page);
```

### 2. Strict Mode Violations
```typescript
// ❌ WRONG
await expect(page.getByText(/Message/i)).toBeVisible();

// ✅ CORRECT
await expect(page.getByText(/Message/i).first()).toBeVisible();
```

## Debugging Steps That WORKED

1. ✅ **Check screenshots** - Shows actual UI state
2. ✅ **Read error-context.md** - Shows DOM structure
3. ✅ **Check backend logs** - Process created? User authenticated?
4. ✅ **Look for 500 errors** - Real backend failures vs test issues
5. ❌ **DON'T speculate** - Verify with screenshots/logs first!

## Fixed Files
- ✅ `cdu-10.spec.ts` - 16/16
- ✅ `cdu-11.spec.ts` - 7/7  
- ✅ `cdu-19.spec.ts` - 7/7
- 🔧 `cdu-13.spec.ts` - 6/12 (progressing)
- ✅ `helpers-auth.ts` - Removed clearCookies (causes double login)
- ✅ `helpers-analise.ts` - Fixed devolucao helper (toast vs heading)
- ✅ `SubprocessoAccessPolicy.java` - Removed DEBUG logs
