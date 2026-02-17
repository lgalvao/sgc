# 🚀 Plano de Simplificação - Fase 4: Consolidação de DTOs e Mappers

**Data:** 17 de Fevereiro de 2026  
**Status:** 🏗️ Em Andamento  
**Alvo:** Redução de ~30% no volume de DTOs e ~50% de Mappers.

---

## 🎯 Objetivos

1.  **Eliminar Lógica de Apresentação no Backend:** Remover campos `*Formatada` e `*Label` dos DTOs, delegando a formatação de datas e enums para o frontend.
2.  **Migração Agressiva para @JsonView:** Substituir DTOs de leitura (Response) por Entidades anotadas, usando getters sintéticos (`@JsonProperty`) para campos "achatados" (flattening).
3.  **Consolidar Requests de Campo Único:** Agrupar dezenas de Requests do módulo `subprocesso` que apenas encapsulam uma `String`, `Long` ou `LocalDate`.
4.  **Descontinuar Mappers Triviais:** Substituir MapStruct por métodos estáticos `fromEntity` em DTOs remanescentes para maior clareza e facilidade de debug.

---

## 🛠️ Estratégia de Implementação

### 4.1. Backend: Flattening em Entidades
Em vez de criar um DTO apenas para transformar `unidade.getSigla()` em `unidadeSigla`, usaremos getters sintéticos na entidade para exposição via API:

```java
// Exemplo na Entidade Alerta
@JsonView(Views.Publica.class)
@JsonProperty("unidadeOrigemSigla")
public String getUnidadeOrigemSiglaSintetica() {
    return unidadeOrigem != null ? unidadeOrigem.getSigla() : null;
}
```

### 4.2. Frontend: Formatação Descentralizada e Unificação de Tipos
Remover a dependência de strings formatadas e simplificar a árvore de tipos:
- **Unificação:** Mesclar `frontend/src/types/dtos.ts` e `tipos.ts` em um único modelo de domínio que reflita as Entidades/Views do backend.
- **Formatação Local:** O frontend passará a receber datas em formato ISO e enums brutos.
- **Utilização:** Criar `utils/formatters.ts` para centralizar a lógica de exibição, permitindo que o mesmo dado seja formatado de formas diferentes conforme a necessidade da UI.
- **Redução de Mappers:** Com a unificação de tipos, eliminaremos a necessidade de funções de conversão `toDto` / `fromDto` dentro dos services do frontend.

### 4.3. Consolidação de Requests (Módulo Subprocesso)
Substituir múltiplos records por tipos genéricos reutilizáveis no pacote `sgc.comum.dto`:

| Records Atuais (Exemplos) | Substituto Sugerido |
| :--- | :--- |
| `AceitarCadastroRequest`, `HomologarCadastroRequest`, `ApresentarSugestoesRequest` | `TextoRequest(String texto)` |
| `AlterarDataLimiteRequest` | `DataRequest(LocalDate data)` |
| `ReabrirProcessoRequest` | `JustificativaRequest(String justificativa)` |

---

## 📋 Cronograma de Tarefas

### Tarefa 4.1: Limpeza de Campos de Formatação (2 dias)
- [ ] Identificar todos os campos `*Formatada` e `*Label` nos DTOs (especialmente no módulo `processo`).
- [ ] Atualizar componentes Vue para formatar datas/enums localmente.
- [ ] Remover campos e lógica de formatação dos Mappers e DTOs Java.

### Tarefa 4.2: Migração de DTOs de Leitura (3 dias)
- [ ] **Módulo Alerta:** Eliminar `AlertaDto` e `AlertaMapper`. Usar a entidade `Alerta` com `@JsonView`.
- [ ] **Módulo Configuração:** Eliminar DTOs de resposta de parâmetros (usar entidade `Parametro`).
- [ ] **Módulo Processo:** Simplificar `ProcessoResumoDto` e `ProcessoDto` para usar a entidade diretamente onde possível via `@JsonView`.

### Tarefa 4.3: Consolidação de Requests no Subprocesso (2 dias)
- [ ] Criar DTOs comuns em `sgc.comum.dto`: `TextoRequest`, `DataRequest`, `IdRequest`.
- [ ] Refatorar os endpoints do `SubprocessoController` para usar os tipos comuns.
- [ ] Remover arquivos de request redundantes do sistema de arquivos.

### Tarefa 4.4: Extinção de Mappers MapStruct (1 dia)
- [ ] Converter mappers complexos (como `ProcessoDetalheMapper`) para lógica manual ou métodos `fromEntity` dentro do próprio DTO.
- [ ] Remover a dependência do MapStruct em módulos onde ele se tornou trivial ou desnecessário.

---

## 📊 Impacto Esperado

| Métrica | Baseline (Fase 3) | Meta Fase 4 | Redução Esperada |
| :--- | :--- | :--- | :--- |
| **DTOs (Total)** | 64 | ~45 | **-30%** |
| **Mappers (MapStruct)** | 9 | 4 | **-55%** |
| **Indireção (Camadas)** | Alta (Entity -> Mapper -> DTO) | Baixa (Entity + View) | **Significativa** |
| **Manutenibilidade** | Mudar 1 campo = 3-4 arquivos | Mudar 1 campo = 1-2 arquivos | **Alta** |

---

## ⚠️ Tabela de Decisão: Quando manter um DTO?

Ainda manteremos DTOs nos seguintes casos:
1.  **Requests Complexos:** Onde há Bean Validation (@NotBlank, @Size, @Email) em múltiplos campos simultâneos.
2.  **Agregações de Fronteira:** Onde o objeto de resposta compõe dados de 3 ou mais agregados de domínio diferentes (ex: `ProcessoDetalheDto`).
3.  **Segurança Estrita:** Casos raros onde a entidade possui campos sensíveis que `@JsonView` possa tornar arriscado por erro de configuração.

---

## ✅ Critérios de Aceite
- [ ] Redução mínima de 15 arquivos de DTO.
- [ ] Remoção de pelo menos 4 Mappers MapStruct.
- [ ] Todos os testes de integração (E2E e Integration) passando.
- [ ] Zero lógica de `DateTimeFormatter` ou labels manuais em controllers/services do backend.
