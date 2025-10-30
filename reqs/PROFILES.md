# Spring Profiles - Guia de Uso

Este documento descreve os diferentes perfis Spring disponíveis no projeto e quando utilizá-los.

## Perfis Disponíveis

### 1. `default` (Sem perfil especificado)

**Quando usar:** Ambiente de produção ou homologação

**Características:**
- Banco de dados: PostgreSQL (localhost:5432)
- Porta: 10000
- DDL: `create` (cria schema na inicialização)
- Data: Carrega `data.sql` sempre
- Segurança: **DESABILITADA TEMPORARIAMENTE** (SecurityConfig com @Profile("disabled-for-now"))

**Iniciar:**
```bash
./gradlew :backend:bootRun
```

---

### 2. `local`

**Quando usar:** Desenvolvimento local manual (recomendado para desenvolvimento diário)

**Características:**
- Banco de dados: H2 em memória (mais rápido, sem dependências externas)
- DDL: `create-drop` (recria schema a cada restart)
- Data: **NÃO carrega** data.sql (`mode: never`)
- Logging: Menos verboso
- Ideal para: Testar rapidamente mudanças sem poluir banco de dados

**Iniciar:**
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

**Nota:** Como não carrega data.sql, você precisará criar dados manualmente via API ou SQL scripts específicos.

---

### 3. `e2e`

**Quando usar:** Testes End-to-End com Playwright

**Características:**
- Banco de dados: H2 em memória
- DDL: `create-drop` com `defer-datasource-initialization: true`
- Data: **CARREGA** data.sql (`mode: always`)
- Segurança: **PERMISSIVA** - todos os endpoints liberados (E2eSecurityConfig)
- Porta: 10000
- Shutdown: graceful
- Devtools: desabilitado (para estabilidade nos testes)

**Iniciar:**
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e' > backend.log 2>&1 &
```

**Importante:**
- Este perfil **deve** ser usado para rodar testes E2E
- Carrega todos os usuários de teste do `data.sql`
- Segurança desabilitada permite testes sem autenticação real
- Backend deve ser iniciado em modo detached (com `&` no final)

**Usuários de teste disponíveis:**
- Admin: titulo `6`, senha `123`
- Gestor: titulo `8`, senha `123`
- Chefe SGP: titulo `2`, senha `123`
- Multi-perfil: titulo `999999999999`, senha `123`

---

### 4. `test`

**Quando usar:** Testes unitários e de integração JUnit (automático)

**Características:**
- Banco de dados: H2 em memória (testdb)
- DDL: `create-drop` com `defer-datasource-initialization: true`
- Data: **NÃO carrega** data.sql (`mode: never`)
- Segurança: Desabilitada via @Profile("!test") no SecurityConfig
- Configuração: `src/test/resources/application.properties`

**Uso:**
```bash
./gradlew :backend:test
```

**Nota:** Testes JUnit criam seus próprios dados usando arquivos em `src/test/resources/`:
- `create-test-data.sql`
- `insert-test-data.sql`
- `data-test.sql`

Este perfil é **automaticamente** ativado quando você roda testes com JUnit.

---

## Comparação Rápida

| Característica | default | local | e2e | test |
|----------------|---------|-------|-----|------|
| Banco | PostgreSQL | H2 | H2 | H2 |
| Carrega data.sql | ✅ | ❌ | ✅ | ❌ |
| Segurança | ❌* | ✅** | ❌ | ❌ |
| Quando usar | Produção/Homolog | Dev manual | E2E tests | Unit tests |
| Porta | 10000 | 10000 | 10000 | N/A |

\* Temporariamente desabilitada
\*\* Habilitada mas sem implementação real de JWT

---

## Fluxo de Trabalho Recomendado

### Para desenvolvimento local:
```bash
# Terminal 1: Backend
./gradlew :backend:bootRun --args='--spring.profiles.active=local'

# Terminal 2: Frontend
cd frontend
npm run dev
```

### Para testes E2E:
```bash
# Terminal 1: Backend (detached)
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e' > backend.log 2>&1 &

# Terminal 2: Rodar testes
npm run test:e2e

# Parar backend
kill %1  # ou pkill java
```

### Para testes unitários de backend:
```bash
./gradlew :backend:test
# Perfil 'test' ativado automaticamente
```

---

## Configuração de Segurança Atual

⚠️ **ATENÇÃO:** A segurança está temporariamente em um estado de transição:

- `SecurityConfig.java`: @Profile("disabled-for-now") - desabilitado
- `E2eSecurityConfig.java`: Ativo para todos os perfis - permissivo

**Estado atual:**
- ✅ Bom para desenvolvimento e testes E2E
- ❌ **NÃO adequado para produção**

**Próximos passos:**
1. Implementar validação JWT real
2. Reativar SecurityConfig com perfil adequado (ex: @Profile("!e2e"))
3. Manter E2eSecurityConfig apenas com @Profile("e2e")

---

## Troubleshooting

### Problema: "Usuário não encontrado" nos testes E2E
**Solução:** Verifique se está usando o perfil `e2e` que carrega o data.sql

### Problema: Testes JUnit falhando com dados duplicados
**Solução:** O perfil `test` não carrega data.sql. Use os scripts em `src/test/resources/`

### Problema: 401 Unauthorized nos testes E2E
**Solução:** Verifique se E2eSecurityConfig está ativo (sem @Profile ou com @Profile("e2e"))

### Problema: Bean SecurityFilterChain duplicado
**Solução:** Apenas um SecurityConfig pode estar ativo. Verifique os @Profile em ambos os arquivos

---

## Histórico de Mudanças

**2025-10-30:**
- Renomeado perfil `jules` para `e2e` (mais descritivo)
- Renomeado `JulesSecurityConfig` para `E2eSecurityConfig`
- Documentação consolidada de todos os perfis
- SecurityConfig temporariamente desabilitado com @Profile("disabled-for-now")
