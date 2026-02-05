# Data Alignment Report: SQL vs JPA vs DTO

This report details the findings of the analysis performed on the SQL scripts (`/backend/etc/sql`), JPA models, and DTOs (Backend and Frontend) to verify their alignment in terms of names, types, nullability, and validations.

## 1. SQL Schema vs JPA Entities

### 1.1 Table `PROCESSO`
- **Discrepancy (Nullability):** `data_limite` is defined as `DATE NULL` in SQL, but mapped as `nullable = false` in `Processo.java`.
- **Finding:** Java validation is stricter than the database.

### 1.2 Table `UNIDADE_PROCESSO`
- **Critical Mismatch (Mapping):** This table is defined with snapshot columns (`nome`, `sigla`, `matricula_titular`, etc.) in SQL. However, in JPA (`Processo.java`), it is used only as a simple `@JoinTable` for a `@ManyToMany` relationship with `Unidade`.
- **Impact:** The snapshot columns are never populated or managed by Hibernate, rendering the snapshot feature unimplemented in the backend.

### 1.3 Table `SUBPROCESSO`
- **Discrepancy (Nullability):** `unidade_codigo` and `situacao` are `NULL` in SQL but `nullable = false` in JPA.
- **Finding:** Java enforcement is stricter.

### 1.4 Table `ANALISE`
- **Discrepancy (Nullability):** `subprocesso_codigo` is `NOT NULL` in SQL, but JPA mapping (`@JoinColumn`) lacks `nullable = false`, defaulting to `true`.
- **Discrepancy (Length):** `acao` is `VARCHAR2(100)` in SQL vs `length = 20` in JPA.
- **Critical Discrepancy (Length/Validation):** `motivo` is `VARCHAR2(200)` in SQL vs `length = 500` in JPA.
- **Impact:** Attempting to save a motive longer than 200 characters will result in a database error (`DataTruncationException` or similar), even though JPA validation would pass.

### 1.5 Table `MOVIMENTACAO`
- **Critical Mismatch (Missing Column):** `observacoes` field exists in JPA (`length = 500`) but is **missing** from the SQL definition of the `MOVIMENTACAO` table.
- **Discrepancy (Nullability):** `data_hora`, `unidade_origem_codigo`, `unidade_destino_codigo`, and `usuario_titulo` are `NULL` in SQL but `nullable = false` in JPA.

### 1.6 View `VW_VINCULACAO_UNIDADE`
- **Critical Mismatch (JPA ID):** `unidade_anterior_codigo` can be `NULL` in the database (for root units), but it is marked as `@Id` and `nullable = false` in `VinculacaoUnidade.java`.
- **Impact:** JPA does not support NULL values in Primary Keys. Fetching root units via this entity will likely fail.

---

## 2. Backend DTOs vs Frontend Interfaces

### 2.1 `UnidadeParticipanteDto` (Process Detail)
- **Critical Mismatch (Field Name):**
    - **Backend:** Uses `codUnidade`.
    - **Frontend DTO (`dtos.ts`):** Defines `codigo`.
    - **Frontend Mapper (`processos.ts`):** Tries to map `dto.codigo` to `model.codUnidade`.
- **Impact:** Since the backend sends `codUnidade`, the frontend's `dto.codigo` will be `undefined`, and the resulting model's `codUnidade` will also be `undefined`. This breaks unit identification in the process view.

### 2.2 `Analise` DTOs
- **Observation:** `Analise` related DTOs in the frontend often use `any` in mappers, which bypasses type checking and obscures mismatches between backend `LocalDateTime` (string in JSON) and frontend expected formats.

---

## 3. Summary of Critical Findings

| Entity / DTO | Field | Issue Type | Description |
| :--- | :--- | :--- | :--- |
| `Processo` | `unidade_processo` | Logic / Mapping | Snapshot columns in DB are ignored by JPA. |
| `Analise` | `motivo` | Validation | JPA length (500) exceeds DB length (200). |
| `Movimentacao` | `observacoes` | Schema | Field exists in JPA but missing in DB. |
| `VinculacaoUnidade` | `unidadeAnteriorCodigo` | JPA / ID | Marked as ID/NotNull but can be NULL in DB. |
| `UnidadeParticipanteDto` | `codUnidade` / `codigo` | Naming | Field name mismatch between Backend and Frontend DTO. |

## 4. Recommendations

1.  **Synchronize Nullability:** Update JPA mappings to reflect DB nullability where appropriate, or update SQL scripts to include `NOT NULL` constraints where Java requires them.
2.  **Fix Length Mismatches:** Specifically the `Analise.motivo` field should be synchronized (either 200 or 500 in both places).
3.  **Add Missing Columns:** Add `observacoes` to the `MOVIMENTACAO` table.
4.  **Rename DTO Fields:** Align `UnidadeParticipanteDto` to use `codUnidade` consistently or `codigo` consistently across both layers.
5.  **Refactor VinculacaoUnidade:** Use a surrogate ID or a composite key that handles optionality if root units need to be represented.
