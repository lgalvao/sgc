# Data Alignment Report: SQL vs JPA vs DTO

Este relatório detalha as descobertas da análise realizada nos scripts SQL (`/backend/etc/sql`), modelos JPA e DTOs (Backend e Frontend) para verificar o alinhamento em termos de nomes, tipos, nullability e validações.

**Status da Análise:** ✅ Completo e Verificado  
**Data da Análise:** 2026-02-05  
**Última Verificação:** 2026-02-05 (Revisão contra código-fonte atual)  
**Arquivos Analisados:**
- `/backend/etc/sql/ddl_tabelas.sql` (449 linhas)
- `/backend/etc/sql/ddl_views.sql` (288 linhas)
- Entidades JPA em `/backend/src/main/java/sgc/*/model/`
- DTOs Backend em `/backend/src/main/java/sgc/*/dto/`
- DTOs Frontend em `/frontend/src/types/dtos.ts`
- Mappers Frontend em `/frontend/src/mappers/processos.ts`
- Requisitos em `/etc/reqs/`

---

## Notas Importantes sobre Oracle DATE

> ⚠️ **Esclarecimento técnico:** Diferentemente do MySQL/PostgreSQL, o tipo `DATE` do Oracle **inclui componente de hora** (ano, mês, dia, hora, minuto, segundo). Portanto, as discrepâncias de tipo DATE vs LocalDateTime documentadas abaixo **NÃO causam perda de dados no Oracle**. Contudo, para clareza semântica e compatibilidade cross-database, recomenda-se alinhar os tipos.

---

## 1. SQL Schema vs JPA Entities

### 1.1 Table `PROCESSO`
#### Achado 1: Discrepância de Nullability - `data_limite`
- **SQL:** `data_limite DATE NULL` (linha 35, ddl_tabelas.sql)
- **JPA:** `@Column(name = "data_limite", nullable = false)` (linha 32, Processo.java)
- **Impacto:** Validação Java é mais restritiva que o banco. O banco permite NULL, mas a aplicação rejeita.
- **Severidade:** ⚠️ MÉDIA - Pode causar inconsistência se dados forem inseridos diretamente no banco.
- **Recomendação:** SQL deve ser alterado para `NOT NULL` para alinhar com regra de negócio.

#### Achado 2: Discrepância de Tipo - `data_limite`
- **SQL:** `data_limite DATE NULL`
- **JPA:** `private LocalDateTime dataLimite;` (linha 33, Processo.java)
- **Impacto:** ~~JPA armazena timestamp completo mas SQL define apenas DATE~~ **Oracle DATE inclui hora**, portanto não há perda de dados.
- **Severidade:** 🟡 BAIXA - Apenas inconsistência semântica (DATE vs TIMESTAMP no DDL)
- **Evidência:**
  ```sql
  -- SQL (ddl_tabelas.sql:35)
  data_limite      DATE NULL,
  ```
  ```java
  // JPA (Processo.java:32-33)
  @Column(name = "data_limite", nullable = false)
  private LocalDateTime dataLimite;
  ```
- **Recomendação:** Para clareza, considerar mudar SQL para `TIMESTAMP` ou JPA para `LocalDate`.

### 1.2 Table `UNIDADE_PROCESSO`
#### Achado: Colunas de Snapshot Ignoradas pelo JPA
- **SQL:** Tabela define colunas de snapshot: `nome`, `sigla`, `matricula_titular`, `titulo_titular`, `data_inicio_titularidade`, `tipo`, `situacao`, `unidade_superior_codigo` (linhas 59-71, ddl_tabelas.sql)
- **JPA:** Mapeada apenas como `@JoinTable` para relacionamento `@ManyToMany` com `Unidade` (linhas 46-54, Processo.java)
- **Impacto:** As colunas de snapshot NUNCA são populadas ou gerenciadas pelo Hibernate. A funcionalidade de snapshot está completamente não implementada no backend.
- **Severidade:** ⚠️ ADIADA - Funcionalidade planejada mas não requerida nos requisitos atuais
- **Verificação de Requisitos:** Não há menção a "snapshot" nos documentos de requisitos em `/etc/reqs/`.
- **Evidência:**
  ```sql
  -- SQL define 10 colunas, sendo 8 de snapshot (ddl_tabelas.sql:59-71)
  CREATE TABLE UNIDADE_PROCESSO (
      processo_codigo          NUMBER NOT NULL,
      unidade_codigo           NUMBER NOT NULL,
      nome                     VARCHAR2(255) NULL,      -- SNAPSHOT
      sigla                    VARCHAR2(20)  NULL,      -- SNAPSHOT
      matricula_titular        VARCHAR2(8)   NULL,      -- SNAPSHOT
      titulo_titular           VARCHAR2(12)  NULL,      -- SNAPSHOT
      data_inicio_titularidade DATE NULL,               -- SNAPSHOT
      tipo                     VARCHAR2(20)  NULL,      -- SNAPSHOT
      situacao                 VARCHAR2(20)  NULL,      -- SNAPSHOT
      unidade_superior_codigo  NUMBER NULL,             -- SNAPSHOT
      -- ...
  );
  ```
  ```java
  // JPA usa apenas como join table (Processo.java:46-54)
  @ManyToMany
  @JoinTable(
      name = "unidade_processo",
      schema = "sgc",
      joinColumns = @JoinColumn(name = "processo_codigo"),
      inverseJoinColumns = @JoinColumn(name = "unidade_codigo"))
  @BatchSize(size = 50)
  @Builder.Default
  private Set<Unidade> participantes = new HashSet<>();
  ```
- **Recomendação:** Decidir: (1) Criar entidade `UnidadeProcesso` para implementar snapshots, ou (2) Remover colunas de snapshot do SQL se não forem necessárias.

### 1.3 Table `SUBPROCESSO`
#### Achado 1: Discrepância de Nullability - `unidade_codigo`
- **SQL:** `unidade_codigo NUMBER NULL` (linha 102, ddl_tabelas.sql)
- **JPA:** `@JoinColumn(name = "unidade_codigo", nullable = false)` (linha 31, Subprocesso.java)
- **Impacto:** Validação Java é mais restritiva. Inserções diretas no banco podem criar registros sem unidade que causarão exceções no JPA.
- **Severidade:** ⚠️ MÉDIA
- **Recomendação:** SQL deve ser `NOT NULL` - subprocesso sem unidade é inválido por definição.

#### Achado 2: Discrepância de Nullability - `situacao`
- **SQL:** `situacao VARCHAR2(50) NULL` (linha 107, ddl_tabelas.sql)
- **JPA:** `@Column(name = "situacao", length = 50, nullable = false)` com `@Builder.Default` (linhas 50-52, Subprocesso.java)
- **Impacto:** JPA força valor não-nulo (default = `NAO_INICIADO`), mas banco permite NULL.
- **Severidade:** 🟡 BAIXA - Default do Builder previne maioria dos casos
- **Recomendação:** SQL deve ser `NOT NULL` para consistência.

#### Achado 3: Discrepância de Tipo - Datas Limite
- **SQL:** 
  - `data_limite_etapa1 DATE NULL` (linha 103)
  - `data_limite_etapa2 DATE NULL` (linha 105)
- **JPA:** 
  - `private LocalDateTime dataLimiteEtapa1;` (linha 37, Subprocesso.java)
  - `private LocalDateTime dataLimiteEtapa2;` (linha 44, Subprocesso.java)
- **Impacto:** ~~Perda do componente de hora~~ **Oracle DATE inclui hora**, sem perda de dados.
- **Severidade:** 🟡 BAIXA - Apenas inconsistência semântica
- **Evidência:**
  ```sql
  -- SQL (ddl_tabelas.sql:103-107)
  data_limite_etapa1 DATE NULL,
  data_fim_etapa1    TIMESTAMP NULL,
  data_limite_etapa2 DATE NULL,
  data_fim_etapa2    TIMESTAMP NULL,
  situacao           VARCHAR2(50) NULL,
  ```
  ```java
  // JPA (Subprocesso.java:31-52)
  @JoinColumn(name = "unidade_codigo", nullable = false)
  private Unidade unidade;
  
  @Column(name = "data_limite_etapa1", nullable = false)
  private LocalDateTime dataLimiteEtapa1;
  
  @Column(name = "data_limite_etapa2")
  private LocalDateTime dataLimiteEtapa2;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "situacao", length = 50, nullable = false)
  @lombok.Builder.Default
  private SituacaoSubprocesso situacao = SituacaoSubprocesso.NAO_INICIADO;
  ```

### 1.4 Table `ANALISE`
#### ~~Achado 1: Discrepância de Nullability - `subprocesso_codigo`~~ ✅ CORRIGIDO
- **SQL:** `subprocesso_codigo NUMBER NOT NULL` (linha 222, ddl_tabelas.sql)
- **JPA:** `@JoinColumn(name = "subprocesso_codigo", nullable = false)` (linha 27, Analise.java)
- **Status:** ✅ **ALINHADO** - JPA agora tem `nullable = false` conforme verificado no código atual.

#### Achado 2: Discrepância de Tamanho - `acao`
- **SQL:** `acao VARCHAR2(100) NULL` (linha 225, ddl_tabelas.sql)
- **JPA:** `@Column(name = "acao", length = 20)` (linha 37, Analise.java)
- **Impacto:** JPA aceita até 20 caracteres mas banco permite 100. Divergência de validação.
- **Severidade:** 🟡 BAIXA - Valores enum são curtos (ex: `ACEITE`, `DEVOLUCAO`), sem risco prático.
- **Recomendação:** Opcional - alinhar ambos para 50 caracteres para consistência.

#### Achado 3: Discrepância CRÍTICA de Tamanho - `motivo`
- **SQL:** `motivo VARCHAR2(200) NULL` (linha 228, ddl_tabelas.sql)
- **JPA:** `@Column(name = "motivo", length = 500)` (linha 46, Analise.java)
- **Impacto:** JPA valida até 500 caracteres mas banco trunca em 200. Tentativa de salvar motivo com 201-500 caracteres causará `DataTruncationException`.
- **Severidade:** 🔴 CRÍTICA - Perda de dados silenciosa ou erro em runtime
- **Evidência:**
  ```sql
  -- SQL (ddl_tabelas.sql:220-229)
  CREATE TABLE ANALISE (
      codigo             NUMBER GENERATED ALWAYS AS IDENTITY START WITH 1 INCREMENT BY 1 NOT NULL,
      subprocesso_codigo NUMBER NOT NULL,
      data_hora          TIMESTAMP NULL,
      tipo               VARCHAR2(20)  NULL,
      acao               VARCHAR2(100) NULL,
      usuario_titulo     VARCHAR2(12)  NULL,
      unidade_codigo     NUMBER NULL,
      motivo             VARCHAR2(200) NULL,       -- 🔴 200 chars < JPA 500
      observacoes        VARCHAR2(500) NULL,
      -- ...
  );
  ```
  ```java
  // JPA (Analise.java:46-47)
  @Column(name = "motivo", length = 500)    // 🔴 500 > 200 do SQL
  private String motivo;
  ```
- **Recomendação:** Alterar SQL para `VARCHAR2(500)` para alinhar com JPA.

### 1.5 Table `MOVIMENTACAO`
#### ~~Achado 1: Coluna AUSENTE no SQL - `observacoes`~~ ❌ ERRO NO RELATÓRIO ORIGINAL
- **Status:** ❌ **RELATÓRIO INCORRETO** - A entidade JPA `Movimentacao.java` **NÃO possui** campo `observacoes`.
- **Verificação:** Analisando o código atual em `backend/src/main/java/sgc/subprocesso/model/Movimentacao.java`:
  ```java
  // Campos atuais da entidade Movimentacao (verificado 2026-02-05):
  private Subprocesso subprocesso;
  private LocalDateTime dataHora;
  private Unidade unidadeOrigem;
  private Unidade unidadeDestino;
  private String descricao;
  private Usuario usuario;
  // NÃO há campo observacoes!
  ```
- **Requisitos:** Os casos de uso em `/etc/reqs/` (cdu-07, cdu-09, cdu-13, etc.) especificam apenas `descrição` para movimentações, não `observacoes`.
- **Conclusão:** Este achado era baseado em informação desatualizada ou incorreta. **Nenhuma ação necessária.**

#### Achado 2: Múltiplas Discrepâncias de Nullability
- **SQL:** Campos NULL (linhas 358-361):
  - `data_hora TIMESTAMP NULL`
  - `unidade_origem_codigo NUMBER NULL`
  - `unidade_destino_codigo NUMBER NULL`
  - `usuario_titulo VARCHAR2(12) NULL`
- **JPA:** Todos marcados como `nullable = false` (linhas 27-28, 31-32, 35-36, 42-43, Movimentacao.java)
- **Impacto:** Validação Java é mais restritiva. Inserções diretas podem causar exceções.
- **Severidade:** ⚠️ MÉDIA
- **Verificação de Requisitos:** Os casos de uso (cdu-09.md, cdu-13.md, etc.) sempre especificam valores para todos estes campos ao criar movimentações.
- **Recomendação:** SQL deve ter todos estes campos como `NOT NULL` para alinhar com regras de negócio.

### 1.6 View `VW_VINCULACAO_UNIDADE`
#### Achado: ID com Valores NULL - `unidade_anterior_codigo`
- **SQL View:** Query inicia com `START WITH t.COD_UNID_TSE_ANT IS NULL` para unidades raiz (linha 37, ddl_views.sql)
- **JPA:** Campo `unidadeAnteriorCodigo` marcado como `@Id` e `nullable = false` (linhas 23-25, VinculacaoUnidade.java)
- **Impacto:** JPA NÃO suporta valores NULL em Primary Keys. Buscar unidades raiz via esta entidade falhará com exceção ou retornará dados incorretos.
- **Severidade:** 🔴 CRÍTICA - Funcionalidade quebrada para unidades raiz
- **Evidência:**
  ```sql
  -- SQL View (ddl_views.sql:24-59)
  CREATE OR REPLACE VIEW VW_VINCULACAO_UNIDADE (
      unidade_atual_codigo, 
      unidade_anterior_codigo,    -- ✅ Pode ser NULL para raiz
      demais_unidades_historicas
  ) AS
  WITH HistoricoCompleto AS (
    SELECT t.CD, t.COD_UNID_TSE_ANT, ...
    FROM SRH2.UNIDADE_TSE t
    START WITH t.COD_UNID_TSE_ANT IS NULL  -- 🔴 Unidades raiz têm anterior NULL
    CONNECT BY NOCYCLE PRIOR t.CD = t.COD_UNID_TSE_ANT
  )
  -- ...
  ```
  ```java
  // JPA (VinculacaoUnidade.java:17-26)
  @IdClass(VinculacaoUnidadeId.class)
  public class VinculacaoUnidade {
      @Id
      @Column(name = "unidade_atual_codigo", nullable = false)
      private Long unidadeAtualCodigo;
  
      @Id
      @Column(name = "unidade_anterior_codigo", nullable = false)  // 🔴 Mas pode ser NULL!
      private Long unidadeAnteriorCodigo;
  ```
- **Recomendação:** 
  1. **Opção A:** Modificar a view para usar valor sentinela (ex: 0) em vez de NULL
  2. **Opção B:** Usar surrogate ID único na entidade JPA
  3. **Opção C:** Usar `@EmbeddedId` com tratamento de Optional

### 1.7 Table `ATRIBUICAO_TEMPORARIA`
#### Achado: Múltiplas Discrepâncias de Nullability
- **SQL:** Campos NULL (linhas 257-262, ddl_tabelas.sql):
  - `unidade_codigo NUMBER NULL`
  - `usuario_matricula VARCHAR2(8) NULL`
  - `usuario_titulo VARCHAR2(12) NULL`
  - `data_inicio DATE NULL`
  - `data_termino DATE NULL`
- **JPA:** Todos marcados como `nullable = false` (linhas 24-36, AtribuicaoTemporaria.java)
- **Impacto:** Validação Java é mais restritiva.
- **Severidade:** ⚠️ MÉDIA
- **Recomendação:** SQL deve ter todos estes campos como `NOT NULL` - atribuição temporária sem estes valores é inválida.

#### Achado: Discrepância de Tipo - Datas
- **SQL:** 
  - `data_inicio DATE NULL` (linha 260, ddl_tabelas.sql)
  - `data_termino DATE NULL` (linha 261, ddl_tabelas.sql)
- **JPA:** 
  - `private LocalDateTime dataInicio;` (AtribuicaoTemporaria.java)
  - `private LocalDateTime dataTermino;` (AtribuicaoTemporaria.java)
- **Impacto:** ~~Perda do componente de hora~~ **Oracle DATE inclui hora**, sem perda de dados.
- **Severidade:** 🟡 BAIXA - Apenas inconsistência semântica

---

## 2. Backend DTOs vs Frontend Interfaces

### 2.1 `UnidadeParticipanteDto` - Incompatibilidade de Nome de Campo
#### Achado: Campo `codigo` vs `codUnidade`
- **Backend DTO:** `ProcessoDetalheDto.UnidadeParticipanteDto` usa `codUnidade` (linha 53, ProcessoDetalheDto.java)
- **Frontend DTO:** `UnidadeParticipanteDto` define `codigo` (linha 62, dtos.ts)
- **Frontend Mapper:** Tenta mapear `dto.codigo` para `model.codUnidade` (linha 9, processos.ts)
- **Impacto:** Backend envia JSON com `codUnidade`, mas frontend espera `codigo`. O mapeamento `dto.codigo` retorna `undefined`, resultando em `model.codUnidade = undefined`. Isso QUEBRA a identificação de unidades na view de processos.
- **Severidade:** 🔴 CRÍTICA - Funcionalidade de visualização de processos quebrada
- **Evidência:**
  ```java
  // Backend (ProcessoDetalheDto.java:47-58)
  public static class UnidadeParticipanteDto {
      @Builder.Default
      private final List<UnidadeParticipanteDto> filhos = new ArrayList<>();
      
      private String nome;
      private String sigla;
      private Long codUnidade;              // ✅ Backend usa codUnidade
      private Long codUnidadeSuperior;
      // ...
  }
  ```
  ```typescript
  // Frontend DTO (dtos.ts:61-70)
  export interface UnidadeParticipanteDto {
      codigo: number;                       // ❌ Frontend espera codigo
      sigla?: string;
      nome?: string;
      codSubprocesso?: number;
      // ...
  }
  ```
  ```typescript
  // Frontend Mapper (processos.ts:4-16)
  export function mapUnidadeParticipanteDtoToFrontend(
      dto: UnidadeParticipanteDto,
  ): UnidadeParticipante {
      return {
          ...dto,
          codUnidade: dto.codigo,           // 🔴 dto.codigo é undefined!
          codSubprocesso: dto.codSubprocesso || 0,
          // ...
      } as UnidadeParticipante;
  }
  ```
- **Correção:** Alinhar o nome do campo:
  - **Opção 1 (Recomendada):** Renomear frontend `codigo` → `codUnidade` em dtos.ts e ajustar mapper
  - **Opção 2:** Renomear backend `codUnidade` → `codigo` em ProcessoDetalheDto.java

### 2.2 `Analise` DTOs
- **Observation:** `Analise` related DTOs in the frontend often use `any` in mappers, which bypasses type checking and obscures mismatches between backend `LocalDateTime` (string in JSON) and frontend expected formats.

---

## 3. Resumo dos Achados Verificados

### Classificação por Severidade

#### 🔴 CRÍTICA (3 achados - Quebra funcionalidade ou perda de dados)
| # | Entidade/DTO | Campo | Tipo de Problema | Descrição | Status |
|---|:-------------|:------|:-----------------|:----------|:-------|
| 1 | `Analise` | `motivo` | Validação/Tamanho | JPA length=500 > SQL VARCHAR2(200). Truncamento ou exceção | ⏳ Pendente |
| 2 | `VinculacaoUnidade` | `unidadeAnteriorCodigo` | JPA/ID | Marcado @Id mas pode ser NULL em unidades raiz | ⏳ Pendente |
| 3 | `UnidadeParticipanteDto` | `codigo` vs `codUnidade` | Naming/Contrato | Backend envia `codUnidade`, frontend espera `codigo` | ⏳ Pendente |

#### ⚠️ MÉDIA (5 achados - Inconsistência entre camadas, SQL mais permissivo que JPA)
| # | Entidade | Campo | Tipo de Problema | Descrição | Status |
|---|:---------|:------|:-----------------|:----------|:-------|
| 4 | `Processo` | `dataLimite` | Nullability | SQL NULL vs JPA NOT NULL | ⏳ SQL a corrigir |
| 5 | `Subprocesso` | `unidadeCodigo` | Nullability | SQL NULL vs JPA NOT NULL | ⏳ SQL a corrigir |
| 6 | `Movimentacao` | 4 campos | Nullability | SQL NULL vs JPA NOT NULL | ⏳ SQL a corrigir |
| 7 | `AtribuicaoTemporaria` | 5 campos | Nullability | SQL NULL vs JPA NOT NULL | ⏳ SQL a corrigir |
| 8 | `UNIDADE_PROCESSO` | Snapshot cols | Lógica/Mapeamento | 8 colunas não usadas | ⏳ Decisão pendente |

#### 🟡 BAIXA (4 achados - Risco mitigado ou sem impacto prático)
| # | Entidade | Campo | Tipo de Problema | Descrição | Status |
|---|:---------|:------|:-----------------|:----------|:-------|
| 9 | `Processo` | `dataLimite` | Tipo DATE/TIMESTAMP | Oracle DATE inclui hora - sem perda | ℹ️ Informativo |
| 10 | `Subprocesso` | `dataLimiteEtapa*` | Tipo DATE/TIMESTAMP | Oracle DATE inclui hora - sem perda | ℹ️ Informativo |
| 11 | `AtribuicaoTemporaria` | `dataInicio/Termino` | Tipo DATE/TIMESTAMP | Oracle DATE inclui hora - sem perda | ℹ️ Informativo |
| 12 | `Analise` | `acao` | Tamanho | JPA 20 < SQL 100, mas enum é curto | ℹ️ Opcional |

#### ✅ CORRIGIDOS/REMOVIDOS
| # | Achado Original | Status | Notas |
|---|:----------------|:-------|:------|
| - | `Analise.subprocesso` nullability | ✅ Corrigido | JPA agora tem `nullable = false` |
| - | `Movimentacao.observacoes` ausente | ❌ Erro | Campo não existe no JPA - relatório estava errado |

### Estatísticas Atualizadas
- **Total de Achados Válidos:** 12
- **Críticos:** 3 (25%)
- **Médios:** 5 (42%)
- **Baixos:** 4 (33%)
- **Corrigidos/Removidos:** 2

---

## 4. Recomendações Priorizadas

### Prioridade 1 - CRÍTICA (Implementar Imediatamente)

1. **Corrigir `Analise.motivo` length mismatch**
   - Ver seção 7 para SQL recomendado

2. **Alinhar `UnidadeParticipanteDto` backend/frontend**
   - Renomear frontend `codigo` → `codUnidade` em `/frontend/src/types/dtos.ts`
   - Atualizar mapper em `/frontend/src/mappers/processos.ts`

3. **Refatorar `VinculacaoUnidade` para suportar unidades raiz**
   - Opção mais simples: modificar a view para usar 0 em vez de NULL para unidades raiz

### Prioridade 2 - MÉDIA (Implementar em Sprint Atual)

4. **Sincronizar nullability constraints no SQL**
   - Tornar campos NOT NULL no SQL onde JPA exige
   - Ver seção 7 para SQL recomendado

### Prioridade 3 - BAIXA/ADIADA

5. **Decidir sobre colunas snapshot de UNIDADE_PROCESSO**
   - Se necessário: criar entidade `UnidadeProcesso`
   - Se não necessário: remover colunas do SQL

6. **Alinhar tipos DATE/TIMESTAMP (opcional)**
   - Apenas para clareza semântica - não há impacto funcional no Oracle

---

## 5. Metodologia da Análise

### Ferramentas e Técnicas Utilizadas
1. **Análise Estática de Código:**
   - Comparação manual linha-a-linha entre DDL SQL e anotações JPA
   - Busca por padrões usando `grep` e `find`
   - Inspeção de DTOs backend (Java) e frontend (TypeScript)
   - Validação contra requisitos em `/etc/reqs/`

2. **Arquivos Analisados:**
   - **SQL Schema:** `/backend/etc/sql/ddl_tabelas.sql` (449 linhas, 17 tabelas)
   - **SQL Views:** `/backend/etc/sql/ddl_views.sql` (288 linhas, 6 views)
   - **JPA Entities:** 15 entidades em `/backend/src/main/java/sgc/*/model/`
   - **Backend DTOs:** Múltiplos DTOs em `/backend/src/main/java/sgc/*/dto/`
   - **Frontend DTOs:** `/frontend/src/types/dtos.ts`
   - **Frontend Mappers:** `/frontend/src/mappers/processos.ts`
   - **Requisitos:** 40 arquivos em `/etc/reqs/`

3. **Critérios de Verificação:**
   - ✅ Nomes de colunas/campos
   - ✅ Tipos de dados (VARCHAR vs String, DATE vs TIMESTAMP, etc.)
   - ✅ Tamanhos de campos (length)
   - ✅ Constraints de nullability (NULL vs NOT NULL)
   - ✅ Mapeamentos JPA (@Column, @JoinColumn, @ManyToMany, etc.)
   - ✅ Compatibilidade de DTOs entre backend e frontend
   - ✅ Alinhamento com requisitos documentados

---

## 6. Conclusão

### Sumário Executivo
A análise revisada revelou **12 discrepâncias válidas** entre SQL, JPA e DTOs, sendo:
- **3 críticas** que necessitam correção imediata
- **5 de média severidade** relacionadas a nullability no SQL
- **4 de baixa severidade** sem impacto prático

Dois achados do relatório original foram **corrigidos ou removidos**:
- O campo `Analise.subprocesso` já foi corrigido no JPA
- O campo `Movimentacao.observacoes` nunca existiu - era um erro no relatório original

### Impacto no Sistema
1. **Funcionalidades Potencialmente Afetadas:**
   - Truncamento de motivo de análise (>200 chars) se SQL não for atualizado
   - Visualização de processos no frontend (campo undefined)
   - Consulta de unidades raiz via VinculacaoUnidade

2. **Riscos Mitigados:**
   - Perda de hora em campos DATE: **não ocorre no Oracle**
   - Campo observacoes em Movimentacao: **não existe, não há problema**

### Próximos Passos Recomendados
1. ✅ **Imediato:** Implementar correções de Prioridade 1 (3 items)
2. 📅 **Sprint Atual:** Implementar correções de Prioridade 2 (1 item - nullability SQL)
3. 📋 **Backlog:** Avaliar necessidade de snapshots em UNIDADE_PROCESSO
4. 🔄 **Contínuo:** Estabelecer processo de validação automática SQL↔JPA

---

## 7. Alterações Recomendadas para SQL

> ⚠️ **IMPORTANTE:** Este SQL **NÃO foi aplicado** aos scripts. Deve ser revisado e aplicado manualmente após validação.

### 7.1 Correção CRÍTICA - ANALISE.motivo

```sql
-- Aumentar tamanho de ANALISE.motivo para alinhar com JPA (500 chars)
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linha 228
ALTER TABLE ANALISE MODIFY motivo VARCHAR2(500);
```

**Justificativa:** O JPA permite até 500 caracteres, mas o SQL atual trunca em 200. Isso pode causar `DataTruncationException` ou perda de dados silenciosa.

### 7.2 Correções de Nullability - Alinhamento com JPA

```sql
-- =============================================================================
-- PROCESSO - data_limite deve ser NOT NULL (JPA exige)
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linha 35
-- =============================================================================
ALTER TABLE PROCESSO MODIFY data_limite DATE NOT NULL;

-- =============================================================================
-- SUBPROCESSO - unidade_codigo e situacao devem ser NOT NULL (JPA exige)
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linhas 102, 107
-- =============================================================================
ALTER TABLE SUBPROCESSO MODIFY unidade_codigo NUMBER NOT NULL;
ALTER TABLE SUBPROCESSO MODIFY situacao VARCHAR2(50) NOT NULL;

-- =============================================================================
-- MOVIMENTACAO - todos os campos de FK e timestamp devem ser NOT NULL (JPA exige)
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linhas 358-361
-- =============================================================================
ALTER TABLE MOVIMENTACAO MODIFY data_hora TIMESTAMP NOT NULL;
ALTER TABLE MOVIMENTACAO MODIFY unidade_origem_codigo NUMBER NOT NULL;
ALTER TABLE MOVIMENTACAO MODIFY unidade_destino_codigo NUMBER NOT NULL;
ALTER TABLE MOVIMENTACAO MODIFY usuario_titulo VARCHAR2(12) NOT NULL;

-- =============================================================================
-- ATRIBUICAO_TEMPORARIA - todos os campos obrigatórios devem ser NOT NULL (JPA exige)
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linhas 257-261
-- =============================================================================
ALTER TABLE ATRIBUICAO_TEMPORARIA MODIFY unidade_codigo NUMBER NOT NULL;
ALTER TABLE ATRIBUICAO_TEMPORARIA MODIFY usuario_matricula VARCHAR2(8) NOT NULL;
ALTER TABLE ATRIBUICAO_TEMPORARIA MODIFY usuario_titulo VARCHAR2(12) NOT NULL;
ALTER TABLE ATRIBUICAO_TEMPORARIA MODIFY data_inicio DATE NOT NULL;
ALTER TABLE ATRIBUICAO_TEMPORARIA MODIFY data_termino DATE NOT NULL;
```

**Justificativa:** O JPA já valida estes campos como `nullable = false`. O SQL deve refletir a mesma regra para evitar inconsistências em inserções diretas no banco.

### 7.3 Correção Opcional - VW_VINCULACAO_UNIDADE

```sql
-- =============================================================================
-- Opção A: Modificar view para usar 0 em vez de NULL para unidades raiz
-- Arquivo: /backend/etc/sql/ddl_views.sql
-- =============================================================================
CREATE OR REPLACE VIEW VW_VINCULACAO_UNIDADE (
    unidade_atual_codigo, 
    unidade_anterior_codigo, 
    demais_unidades_historicas
) AS
SELECT 
    u.CD AS unidade_atual_codigo,
    NVL(u.COD_UNID_TSE_ANT, 0) AS unidade_anterior_codigo,  -- 0 para raiz
    -- ... resto da query
FROM SRH2.UNIDADE_TSE u
-- ...
```

**Justificativa:** JPA não suporta NULL em campos @Id. Usar valor sentinela 0 resolve o problema.

### 7.4 Correção Opcional - Remoção de Colunas Snapshot (se não necessárias)

```sql
-- =============================================================================
-- SE DECIDIDO que snapshots não são necessários, remover colunas
-- Arquivo: /backend/etc/sql/ddl_tabelas.sql, linhas 63-70
-- =============================================================================
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN nome;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN sigla;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN matricula_titular;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN titulo_titular;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN data_inicio_titularidade;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN tipo;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN situacao;
ALTER TABLE UNIDADE_PROCESSO DROP COLUMN unidade_superior_codigo;
```

**Justificativa:** Estas colunas nunca são populadas pelo JPA atual. Removê-las simplifica o schema se a funcionalidade não for implementada.

---

## Apêndice: Ferramentas para Prevenção

### Sugestões para Evitar Futuras Divergências
1. **Schema Migration Tools:** Usar Liquibase ou Flyway para versionar schema
2. **JPA Schema Validation:** Ativar `hibernate.hbm2ddl.auto=validate` em ambientes de teste
3. **Contract Testing:** Testes automatizados para DTOs backend/frontend
4. **Code Reviews:** Checklist incluindo verificação SQL↔JPA
5. **Linting:** ESLint/TSLint para detectar uso de `any` em mappers TypeScript

---

**Relatório gerado em:** 2026-02-05  
**Última Verificação:** 2026-02-05  
**Versão:** 3.0 (Verificada e Corrigida)  
**Autor:** Análise Automatizada + Revisão Manual  
**Status:** ✅ Completo, Verificado e com Recomendações SQL
