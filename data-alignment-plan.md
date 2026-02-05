# Data Alignment Implementation Plan

Este plano detalha as ações necessárias para corrigir as discrepâncias identificadas no `data-alignment-report.md`, organizadas em sprints executáveis por um AI Agent.

**Criado em:** 2026-02-05  
**Baseado em:** data-alignment-report.md v3.0  
**Tracking:** Ver `data-alignment-tracking.md` para acompanhamento de progresso

---

## Visão Geral

| Sprint | Foco | Itens | Estimativa |
|--------|------|-------|------------|
| Sprint 1 | Correções Críticas Frontend | 1 | 30 min |
| Sprint 2 | Correções Críticas SQL | 1 | 15 min |
| Sprint 3 | Correções Nullability SQL | 4 | 45 min |
| Sprint 4 | Correção View Vinculação | 1 | 30 min |
| Sprint 5 | Decisão Snapshot (Manual) | 1 | Decisão |

---

## Sprint 1: Correções Críticas - Frontend DTO

**Objetivo:** Corrigir o mismatch de nomes entre backend e frontend que quebra a identificação de unidades.

### Task 1.1: Atualizar Frontend DTO
**Arquivo:** `/frontend/src/types/dtos.ts`

**Ação:**
```
Localizar a interface UnidadeParticipanteDto (linha ~62)
Renomear campo "codigo: number" para "codUnidade: number"
Adicionar campo "codUnidadeSuperior?: number" se não existir
```

**Verificação:**
```bash
grep -n "codUnidade" frontend/src/types/dtos.ts
```

### Task 1.2: Atualizar Frontend Mapper
**Arquivo:** `/frontend/src/mappers/processos.ts`

**Ação:**
```
Localizar função mapUnidadeParticipanteDtoToFrontend (linha ~4)
Alterar "codUnidade: dto.codigo" para "codUnidade: dto.codUnidade"
```

**Verificação:**
```bash
grep -n "dto.codUnidade" frontend/src/mappers/processos.ts
```

### Task 1.3: Executar Testes Frontend
**Comando:**
```bash
cd frontend && npm run test
cd frontend && npm run typecheck
```

**Critério de Sucesso:** Todos os testes passam, sem erros de type.

---

## Sprint 2: Correção Crítica - SQL ANALISE.motivo

**Objetivo:** Aumentar o tamanho de ANALISE.motivo para alinhar com JPA.

### Task 2.1: Atualizar DDL
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`

**Ação:**
```
Localizar linha ~228 contendo "motivo VARCHAR2(200)"
Alterar para "motivo VARCHAR2(500)"
```

**Busca prévia:**
```bash
grep -n "motivo.*VARCHAR2" backend/etc/sql/ddl_tabelas.sql
```

### Task 2.2: Verificar Consistência
**Verificação:**
```bash
# Confirmar que JPA e SQL agora têm mesmo tamanho
grep -n "length = 500" backend/src/main/java/sgc/analise/model/Analise.java
grep -n "VARCHAR2(500)" backend/etc/sql/ddl_tabelas.sql | grep motivo
```

**Critério de Sucesso:** Ambos mostram 500.

---

## Sprint 3: Correções Nullability - SQL

**Objetivo:** Alinhar constraints NULL/NOT NULL do SQL com as validações JPA.

### Task 3.1: Atualizar PROCESSO
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`

**Ação:**
```
Localizar linha ~35 contendo "data_limite DATE NULL"
Alterar para "data_limite DATE NOT NULL"
```

### Task 3.2: Atualizar SUBPROCESSO
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`

**Ações:**
```
Linha ~102: Alterar "unidade_codigo NUMBER NULL" para "unidade_codigo NUMBER NOT NULL"
Linha ~107: Alterar "situacao VARCHAR2(50) NULL" para "situacao VARCHAR2(50) NOT NULL"
```

### Task 3.3: Atualizar MOVIMENTACAO
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`

**Ações (linhas ~358-361):**
```
Alterar "data_hora TIMESTAMP NULL" para "data_hora TIMESTAMP NOT NULL"
Alterar "unidade_origem_codigo NUMBER NULL" para "unidade_origem_codigo NUMBER NOT NULL"
Alterar "unidade_destino_codigo NUMBER NULL" para "unidade_destino_codigo NUMBER NOT NULL"
Alterar "usuario_titulo VARCHAR2(12) NULL" para "usuario_titulo VARCHAR2(12) NOT NULL"
```

### Task 3.4: Atualizar ATRIBUICAO_TEMPORARIA
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`

**Ações (linhas ~257-261):**
```
Alterar "unidade_codigo NUMBER NULL" para "unidade_codigo NUMBER NOT NULL"
Alterar "usuario_matricula VARCHAR2(8) NULL" para "usuario_matricula VARCHAR2(8) NOT NULL"
Alterar "usuario_titulo VARCHAR2(12) NULL" para "usuario_titulo VARCHAR2(12) NOT NULL"
Alterar "data_inicio DATE NULL" para "data_inicio DATE NOT NULL"
Alterar "data_termino DATE NULL" para "data_termino DATE NOT NULL"
```

### Task 3.5: Verificar Consistência
**Comando:**
```bash
# Verificar que não há mais NULL onde deveria ser NOT NULL
grep -E "(data_limite|unidade_codigo|situacao|data_hora|usuario_titulo|data_inicio|data_termino).*NULL" backend/etc/sql/ddl_tabelas.sql
```

**Critério de Sucesso:** Campos modificados aparecem como NOT NULL.

---

## Sprint 4: Correção View Vinculação

**Objetivo:** Permitir que unidades raiz sejam consultadas via VinculacaoUnidade.

### Task 4.1: Avaliar Uso Atual
**Comando:**
```bash
# Verificar onde VinculacaoUnidade é usada
grep -r "VinculacaoUnidade" backend/src --include="*.java" | grep -v "test"
```

### Task 4.2: Modificar View (Opção A - Valor Sentinela)
**Arquivo:** `/backend/etc/sql/ddl_views.sql`

**Ação:**
```
Localizar a view VW_VINCULACAO_UNIDADE (~linha 24)
Na cláusula SELECT, alterar:
  u.COD_UNID_TSE_ANT AS unidade_anterior_codigo
Para:
  NVL(u.COD_UNID_TSE_ANT, 0) AS unidade_anterior_codigo
```

### Task 4.3: Atualizar JPA para Tratar Valor Sentinela
**Arquivo:** `/backend/src/main/java/sgc/organizacao/model/VinculacaoUnidade.java`

**Ação:**
```
Adicionar método helper:
  public boolean isUnidadeRaiz() {
      return unidadeAnteriorCodigo != null && unidadeAnteriorCodigo == 0L;
  }
```

### Task 4.4: Executar Testes
**Comando:**
```bash
cd backend && ./gradlew test --tests "*VinculacaoUnidade*"
```

---

## Sprint 5: Decisão sobre Snapshot UNIDADE_PROCESSO

**Objetivo:** Decidir e implementar ação sobre colunas de snapshot não utilizadas.

> ⚠️ **REQUER DECISÃO HUMANA** - Este sprint não pode ser executado automaticamente.

### Task 5.1: Decisão Necessária
**Pergunta ao usuário:**
```
As colunas de snapshot em UNIDADE_PROCESSO (nome, sigla, matricula_titular, etc.) 
devem ser:

A) Implementadas no JPA (criar entidade UnidadeProcesso)?
B) Removidas do SQL (se não forem necessárias)?
C) Mantidas como estão (para implementação futura)?
```

### Task 5.2 (Se Opção A): Implementar Entidade
**Ações:**
1. Criar `/backend/src/main/java/sgc/processo/model/UnidadeProcesso.java`
2. Modificar `Processo.java` para usar `@OneToMany` com `UnidadeProcesso`
3. Criar mapper para popular snapshots
4. Adicionar testes

### Task 5.3 (Se Opção B): Remover Colunas
**Arquivo:** `/backend/etc/sql/ddl_tabelas.sql`
**Ação:** Remover colunas de snapshot da tabela UNIDADE_PROCESSO

### Task 5.4 (Se Opção C): Documentar
**Ação:** Adicionar comentário no código explicando que colunas são para uso futuro.

---

## Verificação Final

Após completar todos os sprints:

### Checklist de Verificação
```bash
# 1. Build completo
cd backend && ./gradlew build

# 2. Testes backend
cd backend && ./gradlew test

# 3. Typecheck frontend
cd frontend && npm run typecheck

# 4. Testes frontend
cd frontend && npm run test

# 5. Verificar consistência SQL↔JPA
grep -c "NOT NULL" backend/etc/sql/ddl_tabelas.sql
```

### Atualização do Tracking
Após cada sprint, atualizar o arquivo `data-alignment-tracking.md` com:
- Status de cada task
- Data de conclusão
- Notas relevantes

---

## Notas para o AI Agent

1. **Sempre fazer backup** antes de modificar arquivos SQL
2. **Executar testes** após cada task quando disponível
3. **Reportar erros** imediatamente se alguma verificação falhar
4. **Não prosseguir** para o próximo sprint se o atual tiver falhas
5. **Sprint 5 requer input humano** - pausar e perguntar antes de executar
