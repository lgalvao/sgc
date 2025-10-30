# Reorganização de Perfis Spring - Resumo

**Data:** 2025-10-30  
**Objetivo:** Simplificar e organizar os perfis Spring do projeto

## Mudanças Realizadas

### 1. Renomeação do Perfil `jules` → `e2e`

**Motivação:**
- O perfil `jules` foi criado originalmente para execução com agentes assíncronos
- Como essa abordagem foi abandonada, o nome não refletia mais o propósito
- `e2e` é mais descritivo e indica claramente seu uso para testes End-to-End

**Arquivos alterados:**
- ✅ `application-jules.yml` → `application-e2e.yml`
- ✅ `JulesSecurityConfig.java` → `E2eSecurityConfig.java`
- ✅ `AGENTS.md` (documentação atualizada)
- ✅ `licoes-aprendidas.md` (referências atualizadas)

### 2. Perfis Consolidados

Após a reorganização, temos **4 perfis distintos e bem definidos**:

| Perfil | Propósito | Banco | Data.sql | Segurança |
|--------|-----------|-------|----------|-----------|
| **default** | Produção/Homologação | PostgreSQL | ✅ | ❌* |
| **local** | Desenvolvimento manual | H2 | ❌ | ❌* |
| **e2e** | Testes E2E Playwright | H2 | ✅ | ❌ |
| **test** | Testes JUnit (auto) | H2 | ❌ | ❌ |

\* Segurança temporariamente desabilitada (SecurityConfig com @Profile("disabled-for-now"))

### 3. Documentação Criada

**Novo arquivo:** `reqs/PROFILES.md`
- Guia completo de todos os perfis
- Quando usar cada um
- Características técnicas detalhadas
- Troubleshooting comum
- Fluxos de trabalho recomendados

## Estado Atual da Segurança

⚠️ **IMPORTANTE:**

**Configuração temporária para desenvolvimento:**
```java
// SecurityConfig.java
@Profile("disabled-for-now")  // DESABILITADO

// E2eSecurityConfig.java  
// SEM @Profile - ATIVO para TODOS os perfis
public class E2eSecurityConfig {
    // Permite tudo (anyRequest().permitAll())
}
```

**Implicações:**
- ✅ **Bom:** Desenvolvimento e testes E2E funcionam sem fricção
- ✅ **Bom:** Não há conflitos de beans SecurityFilterChain
- ❌ **Ruim:** Não adequado para produção (sem autenticação real)

**Próximos passos para produção:**
1. Implementar filtro JWT com validação real
2. Reativar SecurityConfig: `@Profile("!e2e")`
3. Adicionar profile ao E2eSecurityConfig: `@Profile("e2e")`
4. Testar que cada perfil carrega a configuração correta

## Benefícios da Reorganização

### 1. **Clareza**
- Nome `e2e` é auto-explicativo
- Cada perfil tem propósito bem definido
- Documentação centralizada em PROFILES.md

### 2. **Manutenibilidade**
- Fácil identificar qual perfil usar em cada situação
- Configurações isoladas por caso de uso
- Menos confusão sobre "qual perfil rodar"

### 3. **Consistência**
- Padrão de nomenclatura claro: `default`, `local`, `e2e`, `test`
- Todos seguem a mesma estrutura YAML
- Documentação padronizada

## Comandos Atualizados

### Desenvolvimento Local
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

### Testes E2E
```bash
# Backend
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e' > backend.log 2>&1 &

# Testes
npm run test:e2e
```

### Testes Unitários
```bash
./gradlew :backend:test
# Perfil 'test' ativo automaticamente
```

### Produção/Homologação
```bash
./gradlew :backend:bootRun
# Usa perfil default (PostgreSQL)
```

## Checklist de Verificação

Após backend restart, verificar:

- [ ] Backend inicia sem erros
- [ ] Perfil `e2e` carrega data.sql (usuários de teste disponíveis)
- [ ] Perfil `local` NÃO carrega data.sql (banco vazio)
- [ ] Apenas um bean SecurityFilterChain está ativo
- [ ] Testes E2E passam (cdu-01.spec.ts)
- [ ] Testes JUnit passam (./gradlew :backend:test)

## Próximas Ações Recomendadas

### Curto Prazo
- [ ] Atualizar outros testes E2E para usar perfil `e2e` (se houver referências antigas)
- [ ] Verificar se há scripts/CI que referenciam perfil `jules`
- [ ] Testar todos os perfis pelo menos uma vez

### Médio Prazo
- [ ] Implementar autenticação JWT real
- [ ] Reativar SecurityConfig com @Profile adequado
- [ ] Criar testes de integração específicos para autenticação

### Longo Prazo
- [ ] Considerar perfil específico para CI/CD
- [ ] Avaliar se perfil `local` precisa de variantes (com/sem dados)
- [ ] Documentar estratégia de deploy por ambiente

## Referências

- `PROFILES.md` - Guia completo de perfis
- `licoes-aprendidas.md` - Lições aprendidas dos testes E2E
- `AGENTS.md` - Instruções para agentes de IA

---

**Conclusão:** A reorganização simplificou a estrutura de perfis, tornando-a mais intuitiva e fácil de manter. O perfil `e2e` agora tem um nome que reflete claramente seu propósito.
