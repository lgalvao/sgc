# 🔒 Plano de Refatoração de Segurança - Guia Rápido

📄 **Documento Principal**: [`security-refactoring-plan.md`](./security-refactoring-plan.md)

---

## 🎯 Objetivo

Consolidar e padronizar o controle de acesso do SGC, eliminando inconsistências entre abordagens declarativas e programáticas.

## 📊 Status Atual

- ❌ **22 arquivos** com lógica de segurança dispersa
- ❌ **~15 endpoints** sem controle de acesso
- ❌ **6 padrões diferentes** de verificação
- ❌ **Zero auditoria** de decisões de acesso

## 🎯 Meta Final

- ✅ **5 arquivos** centralizados com lógica de segurança
- ✅ **0 endpoints** sem controle
- ✅ **1 padrão** único e consistente
- ✅ **100% auditado** todas as decisões de acesso

## 📅 Cronograma

| Sprint | Duração | Foco | Status |
|--------|---------|------|--------|
| Sprint 1 | 3-5 dias | Infraestrutura base | ✅ Concluído |
| Sprint 2 | 5-7 dias | Migração subprocessos | 🚧 Em Andamento |
| Sprint 3 | 4-6 dias | Processos e atividades | ⏳ Pendente |
| Sprint 4 | 3-4 dias | Auditoria e testes | ⏳ Pendente |
| Sprint 5 | 2-3 dias | Refinamento | ⏳ Pendente |

**Total Estimado**: 17-25 dias

## ��️ Arquitetura Nova

```
Controllers (com @PreAuthorize)
         ↓
AccessControlService (centralizado)
         ↓
   AccessPolicies
   - ProcessoAccessPolicy
   - SubprocessoAccessPolicy
   - AtividadeAccessPolicy
   - MapaAccessPolicy
         ↓
Services (SEM verificações de acesso)
```

## 🚀 Como Executar

### Pré-requisitos
1. Ler o documento completo: [`security-refactoring-plan.md`](./security-refactoring-plan.md)
2. Aprovar o plano com stakeholders
3. Alocar recursos (1-2 desenvolvedores)

### Execução
1. Executar **Sprint 1 completo**
2. Validar (testes passando, sem regressões)
3. Code review e aprovação
4. Repetir para Sprints 2-5

### Validação Entre Sprints
```bash
# Backend tests
cd backend && ./gradlew test

# E2E tests
npm run test:e2e

# Static analysis
cd backend && ./gradlew check

# Coverage
cd backend && ./gradlew jacocoTestReport
```

## 📋 Principais Componentes Novos

1. **`AccessControlService`** - Hub central de verificações
2. **`Acao` enum** - ~40 ações possíveis no sistema
3. **`AccessPolicy<T>`** - Políticas por tipo de recurso
4. **`HierarchyService`** - Gerencia hierarquia de unidades
5. **`AccessAuditService`** - Logging de todas as decisões

## 🎓 Para Saber Mais

- **Inventário Completo**: Seção 2 do documento principal
- **Arquitetura Detalhada**: Seção 3 do documento principal
- **Exemplos de Código**: Seção 5 do documento principal
- **Templates Reutilizáveis**: Apêndices B e C do documento principal

## ⚠️ Riscos Principais

| Risco | Mitigação |
|-------|-----------|
| Quebrar funcionalidade | Testes E2E + sprints incrementais |
| Permissões muito restritivas | Validação com stakeholders |
| Permissões muito permissivas | Code review rigoroso + testes |

## 📞 Suporte

Para dúvidas sobre o plano:
1. Consulte [`security-refactoring-plan.md`](./security-refactoring-plan.md)
2. Revise os exemplos de código na Seção 5
3. Use os templates nos Apêndices

---

**Criado em**: 2026-01-08  
**Versão**: 1.0  
**Status**: 🚧 Em Execução - Sprint 1 Concluído

## Histórico de Execução

### Sprint 1: Infraestrutura Base (Concluído em 2026-01-08)

**Componentes Criados:**
- ✅ `Acao` enum com 47 ações do sistema
- ✅ `HierarchyService` para gerenciar hierarquia de unidades
- ✅ `AccessAuditService` para logging de decisões de acesso
- ✅ `AccessPolicy<T>` interface para políticas de acesso
- ✅ `AccessControlService` (skeleton) como hub central

**Testes Criados:**
- ✅ `HierarchyServiceTest` - 13 testes, 100% aprovado
- ✅ `AccessAuditServiceTest` - 5 testes, 100% aprovado
- ✅ `AccessControlServiceTest` - 4 testes, 100% aprovado
- ✅ Total: 22 testes passando

**Localização dos arquivos:**
- Código: `/backend/src/main/java/sgc/seguranca/acesso/`
- Testes: `/backend/src/test/java/sgc/seguranca/acesso/`

**Próximos Passos:**
- Sprint 2: Implementar `SubprocessoAccessPolicy` e migrar verificações de subprocessos

### Sprint 2: Migração de Verificações de Subprocesso (Em Andamento - Continuado em 2026-01-08)

**Componentes Criados:**
- ✅ `SubprocessoAccessPolicy` com 26 ações mapeadas
  - CRUD básico (8 ações)
  - Workflow de cadastro (5 ações)
  - Workflow de revisão de cadastro (5 ações)
  - Operações de mapa (10 ações)
  - Diagnóstico (2 ações)
- ✅ 5 requisitos de hierarquia implementados:
  - `NENHUM`: Sem verificação de hierarquia
  - `MESMA_UNIDADE`: Usuário na mesma unidade
  - `MESMA_OU_SUBORDINADA`: Usuário na mesma unidade ou superior
  - `SUPERIOR_IMEDIATA`: Usuário na unidade superior imediata
  - `TITULAR_UNIDADE`: Usuário é o titular da unidade
- ✅ `package-info.java` com `@NullMarked` para conformidade arquitetural

**Testes Criados:**
- ✅ `SubprocessoAccessPolicyTest` - 21 testes adicionais
- ✅ Cobertura de cenários CRUD, Cadastro, Revisão, Mapa e Diagnóstico
- ✅ Total acumulado: 43 testes passando
- ✅ Teste de arquitetura passando (@NullMarked compliance)

**Em Andamento:**
- ✅ Atualizar `SubprocessoCadastroWorkflowService` para usar `AccessControlService`
- ✅ Atualizar `SubprocessoMapaWorkflowService` para usar `AccessControlService`
- ✅ Atualizar `ImpactoMapaService` para usar `AccessControlService`
- ✅ Deprecar `SubprocessoPermissoesService.validar()` e `.calcularPermissoes()`
- ✅ Deprecar `MapaAcessoService.verificarAcessoImpacto()`
- ⏳ Validar com testes backend completos
- ⏳ Validar com testes E2E

**Estatísticas Sprint 2:**
- **Services migrados**: 3 (SubprocessoCadastroWorkflowService, SubprocessoMapaWorkflowService, ImpactoMapaService)
- **Métodos migrados**: 16 métodos de workflow
- **Services deprecados**: 2 (SubprocessoPermissoesService, MapaAcessoService)
- **Linhas de código de acesso manual removidas**: ~50 linhas
- **Testes passando**: 31 testes

