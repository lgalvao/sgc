# E2E Improvements - PR Summary

## Overview

This PR consolidates E2E test helpers and adds secure test data setup endpoints that are only available when running with the `e2e` profile.

## Files Added/Modified

### Backend
- **`backend/src/main/java/sgc/sgrh/TestSetupController.java`** (NEW)
  - REST controller with `@Profile("e2e")` annotation
  - Three idempotent endpoints: `/api/test/usuarios`, `/api/test/unidades`, `/api/test/processos`
  - Uses JdbcTemplate for direct DB access
  - All operations are logged with Slf4j

- **`backend/src/main/java/sgc/sgrh/TEST-SETUP-CONTROLLER.md`** (NEW)
  - Technical documentation for maintainers
  - Endpoint specifications with examples
  - Security verification instructions
  - Maintenance guidelines

### Frontend (E2E)
- **`e2e/helpers/acoes/api-setup.ts`** (NEW)
  - TypeScript helper functions for Playwright
  - Functions: `garantirUsuario`, `garantirUnidade`, `garantirProcesso`, `setupCenarioCompleto`
  - Type-safe interfaces: `UsuarioTestData`, `UnidadeTestData`, `ProcessoTestData`

- **`e2e/helpers/dados/constantes-teste.ts`** (MODIFIED)
  - Added `TEST_IDS` constant for data-testid selectors
  - Added utility functions: `testIdSelector`, `processCardSelector`
  - Exported `USUARIOS` constant

- **`e2e/helpers/dados/index.ts`** (MODIFIED)
  - Exports new TEST_IDS and utilities

- **`e2e/helpers/acoes/index.ts`** (MODIFIED)
  - Exports all API setup helpers

- **`e2e/API-SETUP-GUIDE.md`** (NEW)
  - Comprehensive usage guide for test authors
  - Examples for all functions
  - Valid enum values reference
  - Troubleshooting section

## Key Features

### 1. Profile-Based Security
```java
@Profile("e2e")
public class TestSetupController { ... }
```
- Endpoints are ONLY available when backend runs with `--spring.profiles.active=e2e`
- Verified: Returns 404 with other profiles (local, default, test)

### 2. Idempotency
All endpoints check for existing data before inserting:
- **Usuários**: Identified by `tituloEleitoral`
- **Unidades**: Identified by `codigo`
- **Processos**: Identified by `descricao`

Response indicates whether data was created or already existed:
```json
{
  "created": true,  // or false
  "codigo": 123,
  "message": "Unidade criada"  // or "Unidade já existia"
}
```

### 3. Type Safety
TypeScript interfaces ensure correct data structure:
```typescript
interface ProcessoTestData {
  descricao: string;
  tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
  situacao: 'CRIADO' | 'EM_ANDAMENTO' | 'FINALIZADO';
  dataLimite?: string;
  unidadesCodigos: number[];
}
```

### 4. Test ID Selectors
New TEST_IDS constant for robust element selection:
```typescript
export const TEST_IDS = {
  INPUT_TITULO: 'input-titulo',
  BTN_ENTRAR: 'btn-entrar',
  PROCESSO_CARD: 'processo-card',
  // ... more IDs
};

// Helper functions
testIdSelector('btn-entrar') // returns '[data-testid="btn-entrar"]'
processCardSelector(123)      // returns '[data-testid="processo-card-123"]'
```

## Testing Performed

### Manual Testing with curl
✅ Created test unidade (codigo: 9999)
✅ Created test usuario (titulo: 88888888)
✅ Created test processo (codigo: 6)
✅ Verified idempotency (second calls returned "já existia")
✅ Verified profile isolation (404 with local profile)

### Backend Compilation
✅ Java code compiles successfully with Java 21
✅ No compilation errors or warnings

### Profile Testing
✅ e2e profile: All endpoints available
✅ local profile: All endpoints return 404 (Not Found)

## Usage Example

```typescript
import { test } from '@playwright/test';
import { setupCenarioCompleto, loginComoGestor } from './helpers';

test.describe('My Test Suite', () => {
  test.beforeEach(async ({ page }) => {
    // Quick setup via API instead of slow UI operations
    await setupCenarioCompleto(page, {
      usuario: {
        tituloEleitoral: 77777,
        nome: 'Test Gestor',
        email: 'test@test.com',
        ramal: '1234',
        unidadeCodigo: 2,
        perfis: ['GESTOR']
      },
      processo: {
        descricao: 'Test Process',
        tipo: 'MAPEAMENTO',
        situacao: 'CRIADO',
        unidadesCodigos: [2, 3]
      }
    });
  });

  test('my test case', async ({ page }) => {
    await loginComoGestor(page);
    // ... test implementation
  });
});
```

## Benefits

1. **Performance**: API setup is much faster than UI-based data creation
2. **Reliability**: Direct DB access avoids UI flakiness
3. **Safety**: @Profile("e2e") prevents accidental exposure
4. **Maintainability**: Type-safe interfaces catch errors at compile time
5. **Flexibility**: Idempotent operations work in any test order
6. **Documentation**: Comprehensive guides for both users and maintainers

## Migration Path

For existing tests:
1. Keep existing UI-based tests as-is (no breaking changes)
2. Gradually migrate to API setup where beneficial
3. Use TEST_IDS for new element selectors (data-testid)
4. Components can add data-testid attributes incrementally

## Enum Values Reference

### TipoUnidade
- INTEROPERACIONAL
- INTERMEDIARIA
- OPERACIONAL

### TipoProcesso
- MAPEAMENTO
- REVISAO
- DIAGNOSTICO

### SituacaoProcesso
- CRIADO
- EM_ANDAMENTO
- FINALIZADO

### Perfil
- ADMIN
- GESTOR
- CHEFE
- SERVIDOR

## Future Enhancements (Not in this PR)

- GitHub Actions workflows for E2E tests
- Example specs using the new helpers
- Additional test endpoints for other entities (competencias, atividades, etc.)
- data-testid attributes in Vue components

## Verification Instructions for Reviewers

1. **Check profile isolation**:
   ```bash
   ./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'
   curl http://localhost:10000/api/test/usuarios
   # Should return 200
   
   ./gradlew :backend:bootRun --args='--spring.profiles.active=local'
   curl http://localhost:10000/api/test/usuarios
   # Should return 404
   ```

2. **Test endpoint**:
   ```bash
   curl -X POST http://localhost:10000/api/test/unidades \
     -H "Content-Type: application/json" \
     -d '{"codigo":9999,"nome":"Test","sigla":"TST","tipo":"OPERACIONAL"}'
   # Should return: {"created":true,"codigo":9999,"message":"Unidade criada"}
   ```

3. **Check idempotency**:
   ```bash
   # Call same endpoint again
   # Should return: {"created":false,"codigo":9999,"message":"Unidade já existia"}
   ```

4. **Review documentation**:
   - Read `e2e/API-SETUP-GUIDE.md` for usage examples
   - Read `backend/src/main/java/sgc/sgrh/TEST-SETUP-CONTROLLER.md` for technical details

## Notes

- All changes are backward compatible
- No existing tests were modified
- No production code was changed
- All new code is isolated to test infrastructure
- Package-lock.json changes are just dependency updates from npm install

## Questions & Support

See documentation files for:
- Usage examples: `e2e/API-SETUP-GUIDE.md`
- Technical details: `backend/src/main/java/sgc/sgrh/TEST-SETUP-CONTROLLER.md`
- Troubleshooting tips in both docs
