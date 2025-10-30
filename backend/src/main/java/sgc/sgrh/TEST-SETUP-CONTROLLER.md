# TestSetupController - Endpoints de Teste E2E

## Visão Geral

O `TestSetupController` fornece endpoints REST para criar dados de teste de forma programática durante a execução de testes E2E com Playwright.

## Segurança

**IMPORTANTE**: Este controller está ativo APENAS quando o profile `e2e` está ativo.

```java
@Profile("e2e")
public class TestSetupController { ... }
```

Isso garante que os endpoints **NÃO estarão disponíveis** em:
- Profile `local` (desenvolvimento)
- Profile `default` (produção)
- Profile `test` (testes unitários)

### Verificação

```bash
# Com profile e2e - endpoints disponíveis
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'
curl http://localhost:10000/api/test/usuarios
# Retorna: 200 OK

# Com profile local - endpoints NÃO disponíveis
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
curl http://localhost:10000/api/test/usuarios
# Retorna: 404 Not Found
```

## Endpoints

### 1. POST /api/test/usuarios

Cria um usuário de teste com perfis.

**Request Body:**
```json
{
  "tituloEleitoral": 77777,
  "nome": "Teste Gestor",
  "email": "teste.gestor@test.com",
  "ramal": "5555",
  "unidadeCodigo": 2,
  "perfis": ["GESTOR", "CHEFE"]
}
```

**Response:**
```json
{
  "created": true,
  "tituloEleitoral": 77777,
  "message": "Usuário criado"
}
```

**Idempotência**: Se o usuário já existir (mesmo `tituloEleitoral`), retorna:
```json
{
  "created": false,
  "tituloEleitoral": 77777,
  "message": "Usuário já existia"
}
```

### 2. POST /api/test/unidades

Cria uma unidade organizacional de teste.

**Request Body:**
```json
{
  "codigo": 9999,
  "nome": "Unidade Teste",
  "sigla": "UTEST",
  "tipo": "OPERACIONAL",
  "unidadeSuperiorCodigo": 2
}
```

**Valores válidos para `tipo`:**
- `INTEROPERACIONAL`
- `INTERMEDIARIA`
- `OPERACIONAL`

**Response:**
```json
{
  "created": true,
  "codigo": 9999,
  "message": "Unidade criada"
}
```

**Idempotência**: Se a unidade já existir (mesmo `codigo`), retorna:
```json
{
  "created": false,
  "codigo": 9999,
  "message": "Unidade já existia"
}
```

### 3. POST /api/test/processos

Cria um processo de teste com unidades participantes.

**Request Body:**
```json
{
  "descricao": "Processo Teste API",
  "tipo": "MAPEAMENTO",
  "situacao": "CRIADO",
  "dataLimite": "2025-12-31T23:59:59",
  "unidadesCodigos": [2, 3, 8]
}
```

**Valores válidos para `tipo`:**
- `MAPEAMENTO`
- `REVISAO`
- `DIAGNOSTICO`

**Valores válidos para `situacao`:**
- `CRIADO`
- `EM_ANDAMENTO`
- `FINALIZADO`

**Response:**
```json
{
  "created": true,
  "codigo": 6,
  "message": "Processo criado"
}
```

**Idempotência**: Se o processo já existir (mesma `descricao`), retorna:
```json
{
  "created": false,
  "codigo": 6,
  "message": "Processo já existia"
}
```

## Implementação

O controller usa `JdbcTemplate` para inserções diretas no banco de dados:

```java
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile("e2e")
@Slf4j
public class TestSetupController {
    private final JdbcTemplate jdbcTemplate;
    
    // Métodos idempotentes que verificam existência antes de inserir
}
```

### Por que JdbcTemplate?

1. **Performance**: Inserções diretas são mais rápidas que via JPA
2. **Simplicidade**: Não precisa de repositórios ou services
3. **Controle**: Permite inserções exatas sem lógica de negócio
4. **Isolamento**: Não interfere com o código de produção

## Idempotência

Todos os endpoints são idempotentes:

1. **Verificam** se o registro já existe
2. **Inserem** apenas se não existir
3. **Retornam** status indicando se foi criado ou já existia

Isso permite que os testes chamem os endpoints múltiplas vezes sem erros:

```typescript
// Pode ser chamado em beforeEach sem problemas
test.beforeEach(async ({ page }) => {
  await garantirUsuario(page, { tituloEleitoral: 12345, ... });
  await garantirProcesso(page, { descricao: "Teste", ... });
});
```

## Logging

O controller usa Slf4j para log de todas as operações:

```
INFO sgc.sgrh.TestSetupController : Usuário de teste criado: Teste Gestor (77777)
INFO sgc.sgrh.TestSetupController : Perfil GESTOR adicionado ao usuário 77777
INFO sgc.sgrh.TestSetupController : Unidade de teste criada: UTEST (9999)
INFO sgc.sgrh.TestSetupController : Processo de teste criado: Processo Teste (6)
```

## Testes

### Teste Manual com curl

```bash
# Iniciar backend com profile e2e
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'

# Criar unidade
curl -X POST http://localhost:10000/api/test/unidades \
  -H "Content-Type: application/json" \
  -d '{
    "codigo": 9999,
    "nome": "Unidade Teste",
    "sigla": "UTEST",
    "tipo": "OPERACIONAL",
    "unidadeSuperiorCodigo": 2
  }'

# Criar usuário
curl -X POST http://localhost:10000/api/test/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "tituloEleitoral": 88888,
    "nome": "Teste API",
    "email": "teste@test.com",
    "ramal": "9999",
    "unidadeCodigo": 2,
    "perfis": ["ADMIN"]
  }'

# Criar processo
curl -X POST http://localhost:10000/api/test/processos \
  -H "Content-Type: application/json" \
  -d '{
    "descricao": "Processo Teste",
    "tipo": "MAPEAMENTO",
    "situacao": "CRIADO",
    "unidadesCodigos": [2, 3]
  }'
```

### Teste com Playwright

Veja `e2e/API-SETUP-GUIDE.md` para exemplos completos de uso com Playwright.

## Manutenção

### Adicionar Novo Endpoint

1. Adicione o método no `TestSetupController`
2. Use `@PostMapping` e siga o padrão de idempotência
3. Adicione logging com `log.info`
4. Atualize esta documentação
5. Adicione helper correspondente em `e2e/helpers/acoes/api-setup.ts`

### Modificar Estrutura de Dados

Se as tabelas mudarem:
1. Atualize as queries SQL no controller
2. Atualize as interfaces TypeScript em `api-setup.ts`
3. Atualize os exemplos nesta documentação

## Referências

- Helpers TypeScript: `e2e/helpers/acoes/api-setup.ts`
- Guia de uso E2E: `e2e/API-SETUP-GUIDE.md`
- Profile e2e config: `backend/src/main/resources/application-e2e.yml`
