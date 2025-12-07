# Plano de Refatoração do Frontend SGC

**Versão:** 2.0 (Adaptado para Agentes de IA)  
**Data:** 07 de dezembro de 2025  
**Objetivo:** Remover lógica de negócio, validações complexas, filtragens e ordenações desnecessárias do frontend, transferindo essas responsabilidades para o backend, mantendo o frontend como um client otimizado e limpo.

---

## INSTRUÇÕES PARA AGENTES DE IA

Este documento foi estruturado para ser executado por agentes de IA. Cada tarefa inclui:

- **Contexto**: O que precisa ser feito e por quê
- **Verificação**: Como validar que a tarefa foi completada corretamente
- **Comandos**: Comandos específicos para executar
- **Arquivos**: Lista exata de arquivos a modificar/criar
- **Critérios de Sucesso**: Métricas objetivas e mensuráveis

### Regras Fundamentais para Agentes

1. **SEMPRE** leia o `AGENTS.md` antes de começar qualquer tarefa
2. **SEMPRE** execute os testes após modificações:
   - Frontend: `cd frontend && npm run typecheck && npm run lint`
   - Backend: `./gradlew :backend:test`
   - E2E: `npm test` (apenas se houver mudanças em funcionalidades)
3. **NUNCA** modifique código não relacionado à tarefa específica
4. **SEMPRE** use Português Brasileiro em todo o código
5. **SEMPRE** siga as convenções de nomenclatura do projeto (veja AGENTS.md)
6. **SEMPRE** crie commits pequenos e focados após cada subtarefa
7. **SEMPRE** verifique que os builds passam antes de prosseguir

### Ordem de Execução Recomendada

Para agentes de IA executando este plano:

1. **Leia este documento inteiro primeiro** - Entenda o contexto completo
2. **Escolha uma Fase** (ver seção 8) - Comece pela Fase 1 se não houver direcionamento
3. **Para cada tarefa na fase**:
   - Leia a seção relevante (1-7)
   - Execute a "Refatoração Recomendada"
   - Execute os "Comandos de Verificação"
   - Valide os "Critérios de Sucesso"
   - Commit o código
4. **Ao final da fase** - Execute teste completo E2E

### Comandos Essenciais de Verificação

```bash
# Verificar status do repositório
git status

# Verificar tipagem TypeScript do frontend
cd frontend && npm run typecheck

# Verificar linting do frontend
cd frontend && npm run lint

# Executar testes unitários do backend
./gradlew :backend:test

# Executar teste específico do backend
./gradlew :backend:test --tests "sgc.processo.ProcessoServiceTest"

# Executar build completo
./gradlew build

# Executar testes E2E (somente quando necessário)
npm test

# Executar teste E2E específico
npx playwright test tests/processo.spec.ts
```

### Template de Commit para Agentes

```
refactor(módulo): descrição breve da mudança

- Detalhe 1
- Detalhe 2
- Detalhe 3

Refs: #issue (se aplicável)
```

---

## Sumário Executivo

Após análise detalhada dos 14 views e 23 componentes Vue.js, identificamos múltiplas áreas onde o frontend contém lógica que deveria estar no backend. Este documento detalha todas as mudanças necessárias para criar uma separação clara de responsabilidades entre frontend e backend, garantindo que:

1. **Backend:** Gerencia regras de negócio, validações, filtragens complexas, ordenações e cálculos
2. **Frontend:** Foca em apresentação, interação do usuário e comunicação com a API

**Total de Linhas Analisadas:** ~8.065 linhas em componentes e views  
**Áreas Críticas Identificadas:** 47 pontos de melhoria

### Como Usar Este Documento (Guia para Agentes)

**Se você recebeu uma tarefa específica:**
1. Localize a seção correspondente (use Ctrl+F com o nome do arquivo ou ID da tarefa)
2. Leia o "Problema Atual" para entender o contexto
3. Siga os "Passos de Implementação" na ordem
4. Execute cada "Comando de Verificação" após implementar
5. Marque cada item da "Checklist de Implementação"
6. Verifique todos os "Critérios de Sucesso" antes de finalizar

**Se você vai implementar uma fase completa:**
1. Vá para a seção 8 "Plano de Implementação Sugerido"
2. Escolha a fase (recomenda-se começar pela Fase 1)
3. Para cada tarefa na fase, execute o processo acima
4. Ao final da fase, execute teste E2E completo: `npm test`

**Se você está analisando o escopo geral:**
1. Leia o "Sumário Executivo"
2. Revise a seção 7 "Resumo de Endpoints Necessários"
3. Consulte a seção 9 "Métricas de Sucesso"
4. Veja a seção 10 "Riscos e Mitigações"

**Importante:**
- NUNCA pule os comandos de verificação
- SEMPRE execute testes após cada mudança
- SEMPRE consulte AGENTS.md para convenções
- Faça commits pequenos e frequentes
- Se encontrar um problema, documente e peça orientação

---

## 1. Validações de Negócio no Frontend

### 1.1. CadAtividades.vue

**ID da Tarefa:** REF-001  
**Prioridade:** Alta  
**Estimativa:** 2-3 horas

**Problema Atual:**
- **Linhas 631-635:** Validação de atividades sem conhecimento feita no frontend
- **Linhas 669-697:** Lógica complexa de validação de situação antes de disponibilizar
- **Linhas 683-694:** Validação e montagem de mensagens de erro no frontend

```typescript
// Código atual (frontend)
function validarAtividades(): Atividade[] {
  return atividades.value.filter(
    (atividade) => atividade.conhecimentos.length === 0,
  );
}

function disponibilizarCadastro() {
  // Validação de situação
  if (!sub || sub.situacaoSubprocesso !== situacaoEsperada) {
    feedbackStore.show("Ação não permitida", ...);
    return;
  }
  // Validação de atividades sem conhecimento
  atividadesSemConhecimento.value = validarAtividades();
  if (atividadesSemConhecimento.value.length > 0) {
    // Monta mensagem de erro no frontend
    const atividadesDescricoes = atividadesSemConhecimento.value
      .map((a) => `- ${a.descricao}`)
      .join("\n");
    feedbackStore.show("Atividades Incompletas", ...);
    return;
  }
}
```

**Refatoração Recomendada:**

1. **Criar endpoint no backend:** `POST /api/subprocessos/{id}/disponibilizar-cadastro`
2. **Backend deve:**
   - Validar situação do subprocesso
   - Validar completude das atividades (todas devem ter conhecimentos)
   - Retornar erro HTTP 400 com detalhes estruturados se validação falhar
   - Executar a disponibilização se tudo estiver OK

```typescript
// Código refatorado (frontend)
async function disponibilizarCadastro() {
  if (!codSubrocesso.value) return;
  
  try {
    await subprocessosStore.disponibilizarCadastro(codSubrocesso.value);
    await router.push("/painel");
  } catch (error) {
    // O backend retorna erro estruturado com detalhes
    // O interceptor do Axios já trata e exibe
  }
}
```

**Backend esperado:**
```java
// Response de erro estruturado
{
  "status": 400,
  "message": "Cadastro não pode ser disponibilizado",
  "details": {
    "atividadesSemConhecimento": [
      {"codigo": 1, "descricao": "Desenvolver APIs"},
      {"codigo": 2, "descricao": "Fazer testes"}
    ],
    "situacaoAtual": "CADASTRO_EM_ANDAMENTO",
    "situacaoEsperada": "CADASTRO_EM_ANDAMENTO"
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadAtividades.vue`
- `frontend/src/stores/subprocessos.ts`
- `frontend/src/services/subprocessoService.ts`
- `backend/src/main/java/sgc/subprocesso/` (novo endpoint)

**Passos de Implementação para o Agente:**

1. **Criar endpoint no backend** (`backend/src/main/java/sgc/subprocesso/SubprocessoController.java`):
   ```java
   @PostMapping("/{id}/disponibilizar-cadastro")
   public ResponseEntity<Void> disponibilizarCadastro(@PathVariable Integer id) {
       subprocessoService.disponibilizarCadastro(id);
       return ResponseEntity.ok().build();
   }
   ```

2. **Implementar lógica no service** (`backend/src/main/java/sgc/subprocesso/SubprocessoService.java`):
   - Validar situação do subprocesso
   - Buscar todas as atividades do subprocesso
   - Validar que todas têm conhecimentos associados
   - Lançar `ErroDadosInvalidos` com detalhes se falhar
   - Executar disponibilização se OK

3. **Criar DTO de erro estruturado** (se ainda não existir):
   ```java
   // backend/src/main/java/sgc/comum/erros/DetalhesErroValidacao.java
   public class DetalhesErroValidacao {
       private String campo;
       private String mensagem;
       private Object valorRejeitado;
       // getters, setters, construtores
   }
   ```

4. **Atualizar service no frontend** (`frontend/src/services/subprocessoService.ts`):
   ```typescript
   async disponibilizarCadastro(codSubprocesso: number): Promise<void> {
     await apiClient.post(`/api/subprocessos/${codSubprocesso}/disponibilizar-cadastro`);
   }
   ```

5. **Atualizar store** (`frontend/src/stores/subprocessos.ts`):
   ```typescript
   async disponibilizarCadastro(codSubprocesso: number) {
     await subprocessoService.disponibilizarCadastro(codSubprocesso);
   }
   ```

6. **Simplificar view** (`frontend/src/views/CadAtividades.vue`):
   - Remover funções `validarAtividades()` (linhas 631-635)
   - Simplificar `disponibilizarCadastro()` para apenas chamar store e navegar
   - Remover lógica de validação de situação (linhas 669-697)

**Comandos de Verificação:**

```bash
# 1. Verificar que o código compila (backend)
./gradlew :backend:compileJava

# 2. Executar testes do módulo subprocesso
./gradlew :backend:test --tests "sgc.subprocesso.*"

# 3. Verificar tipagem TypeScript (frontend)
cd frontend && npm run typecheck

# 4. Verificar lint (frontend)
cd frontend && npm run lint

# 5. Executar servidor e testar manualmente
./gradlew bootRun
# Em outro terminal:
cd frontend && npm run dev
# Navegar para CadAtividades e testar disponibilização
```

**Critérios de Sucesso:**

- [ ] Endpoint `POST /api/subprocessos/{id}/disponibilizar-cadastro` existe e responde
- [ ] Backend valida situação correta antes de disponibilizar
- [ ] Backend valida que todas atividades têm conhecimentos
- [ ] Backend retorna erro 400 com detalhes estruturados em caso de falha
- [ ] Frontend chama endpoint e trata erro genérico via interceptor
- [ ] View CadAtividades.vue tem ~50-70 linhas a menos
- [ ] Todos os testes passam: `./gradlew :backend:test`
- [ ] TypeScript compila sem erros: `npm run typecheck`
- [ ] Lint passa sem erros: `npm run lint`
- [ ] Funcionalidade de disponibilizar cadastro continua funcionando

**Testes a Criar:**

```java
// backend/src/test/java/sgc/subprocesso/SubprocessoServiceTest.java
@Test
void deveDisponibilizarCadastroQuandoValido() {
    // Arrange: criar subprocesso com atividades válidas
    // Act: chamar disponibilizarCadastro
    // Assert: verificar que situação mudou
}

@Test
void deveLancarErroQuandoAtividadeSemConhecimento() {
    // Arrange: criar subprocesso com atividade sem conhecimento
    // Act & Assert: verificar que lança ErroDadosInvalidos
}

@Test
void deveLancarErroQuandoSituacaoInvalida() {
    // Arrange: criar subprocesso em situação incorreta
    // Act & Assert: verificar que lança ErroOperacaoInvalida
}
```

**Checklist de Implementação:**

- [ ] Ler e entender código atual em CadAtividades.vue (linhas 631-697)
- [ ] Criar endpoint POST /api/subprocessos/{id}/disponibilizar-cadastro
- [ ] Implementar validações no SubprocessoService
- [ ] Criar testes unitários para o service (mínimo 3 testes)
- [ ] Atualizar subprocessoService.ts
- [ ] Atualizar store subprocessos.ts
- [ ] Simplificar CadAtividades.vue removendo validações
- [ ] Executar `./gradlew :backend:test` - DEVE PASSAR
- [ ] Executar `npm run typecheck` - DEVE PASSAR
- [ ] Executar `npm run lint` - DEVE PASSAR
- [ ] Testar manualmente a funcionalidade
- [ ] Commit com mensagem: "refactor(subprocesso): move validação de disponibilização para backend"

---

### 1.2. CadMapa.vue

**ID da Tarefa:** REF-002  
**Prioridade:** Alta  
**Estimativa:** 1-2 horas

**Problema Atual:**
- **Linhas 452-453:** Validação de campos obrigatórios no frontend
- **Sem validação de negócio:** Permite criar competência sem atividades associadas

```typescript
// Código atual
async function adicionarCompetenciaEFecharModal() {
  if (
    !novaCompetencia.value.descricao ||
    atividadesSelecionadas.value.length === 0
  ) return;
  // ... continua
}
```

**Refatoração Recomendada:**

1. **Endpoint existente:** `POST /api/mapas/{id}/competencias`
2. **Backend deve validar:**
   - Descrição não vazia
   - Pelo menos uma atividade associada
   - Atividades pertencem ao mapa correto
   - Não há duplicação de competências (se aplicável)

```typescript
// Código refatorado (frontend) - simplificado
async function adicionarCompetenciaEFecharModal() {
  const competencia: Competencia = {
    codigo: competenciaSendoEditada.value?.codigo ?? undefined,
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: atividadesSelecionadas.value,
  };

  try {
    if (competenciaSendoEditada.value) {
      await mapasStore.atualizarCompetencia(codSubrocesso.value, competencia);
    } else {
      await mapasStore.adicionarCompetencia(codSubrocesso.value, competencia);
    }
    await mapasStore.buscarMapaCompleto(codSubrocesso.value);
    fecharModal();
  } catch {
    // Erro tratado pelo interceptor
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadMapa.vue`
- `backend/src/main/java/sgc/mapa/dto/` (validações Bean Validation)

**Passos de Implementação para o Agente:**

1. **Adicionar validações no DTO** (`backend/src/main/java/sgc/mapa/dto/CompetenciaRequest.java` ou similar):
   ```java
   public class CompetenciaRequest {
       @NotBlank(message = "Descrição da competência é obrigatória")
       @Size(min = 3, max = 500, message = "Descrição deve ter entre 3 e 500 caracteres")
       private String descricao;
       
       @NotEmpty(message = "Pelo menos uma atividade deve ser associada à competência")
       private List<Integer> codigosAtividades;
       
       // getters, setters
   }
   ```

2. **Atualizar Controller** para usar `@Valid`:
   ```java
   @PostMapping("/{idMapa}/competencias")
   public ResponseEntity<CompetenciaDTO> adicionarCompetencia(
       @PathVariable Integer idMapa,
       @Valid @RequestBody CompetenciaRequest request
   ) {
       CompetenciaDTO competencia = mapaService.adicionarCompetencia(idMapa, request);
       return ResponseEntity.ok(competencia);
   }
   ```

3. **Implementar validações adicionais no service** se necessário:
   - Validar que atividades pertencem ao mapa correto
   - Validar que não há duplicação de descrição (se aplicável)
   - Validar que atividades existem no sistema

4. **Simplificar frontend** (`frontend/src/views/CadMapa.vue`):
   - Remover validação manual nas linhas 452-453
   - Simplificar método `adicionarCompetenciaEFecharModal()`
   - Confiar no backend para validações

**Comandos de Verificação:**

```bash
# 1. Compilar backend
./gradlew :backend:compileJava

# 2. Executar testes do módulo mapa
./gradlew :backend:test --tests "sgc.mapa.*"

# 3. Verificar frontend
cd frontend && npm run typecheck && npm run lint

# 4. Teste manual
./gradlew bootRun
# Navegar para CadMapa e tentar criar competência sem atividades
```

**Critérios de Sucesso:**

- [ ] DTO tem anotações `@NotBlank` e `@NotEmpty`
- [ ] Controller usa `@Valid` no parâmetro do DTO
- [ ] Backend retorna erro 400 quando descrição vazia
- [ ] Backend retorna erro 400 quando lista de atividades vazia
- [ ] Frontend removeu validação manual (linhas 452-453)
- [ ] Mensagem de erro do backend é clara e em português
- [ ] Testes unitários passam
- [ ] TypeCheck e Lint passam
- [ ] Funcionalidade continua operacional

**Testes a Criar:**

```java
@Test
void deveLancarErroQuandoDescricaoVazia() {
    CompetenciaRequest request = new CompetenciaRequest();
    request.setDescricao("");
    request.setCodigosAtividades(List.of(1, 2));
    
    assertThrows(MethodArgumentNotValidException.class, () -> {
        mapaController.adicionarCompetencia(1, request);
    });
}

@Test
void deveLancarErroQuandoSemAtividades() {
    CompetenciaRequest request = new CompetenciaRequest();
    request.setDescricao("Competência Teste");
    request.setCodigosAtividades(Collections.emptyList());
    
    assertThrows(MethodArgumentNotValidException.class, () -> {
        mapaController.adicionarCompetencia(1, request);
    });
}
```

**Checklist de Implementação:**

- [ ] Localizar ou criar CompetenciaRequest.java
- [ ] Adicionar anotações Bean Validation
- [ ] Atualizar MapaController com @Valid
- [ ] Criar/atualizar testes unitários (mínimo 2)
- [ ] Simplificar CadMapa.vue removendo validações
- [ ] Executar `./gradlew :backend:test` - DEVE PASSAR
- [ ] Executar `npm run typecheck && npm run lint` - DEVE PASSAR
- [ ] Testar manualmente cenários de erro
- [ ] Commit: "refactor(mapa): adiciona validações Bean Validation para competências"

---

### 1.3. CadProcesso.vue

**ID da Tarefa:** REF-003  
**Prioridade:** Alta  
**Estimativa:** 2-3 horas

**Problema Atual:**
- **Linhas 296-315:** Validação complexa de dados no frontend antes de salvar
- **Linhas 302-305:** Filtragem de unidades elegíveis no frontend
- **Linhas 376-380:** Mesma filtragem duplicada em outro método

```typescript
// Código atual
async function salvarProcesso() {
  if (!descricao.value) {
    mostrarAlerta('danger', "Dados incompletos", "Preencha a descrição.");
    return;
  }

  // Filtragem no frontend
  const unidadesFiltradas = unidadesSelecionadas.value.filter(id => {
    const unidade = findUnidadeById(id, unidadesStore.unidades);
    return unidade && unidade.isElegivel;
  });

  if (unidadesFiltradas.length === 0) {
    mostrarAlerta('danger', "Dados incompletos", "Pelo menos uma unidade...");
    return;
  }
  // ... mais validações
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Validar campos obrigatórios (descrição, tipo, data limite, unidades)
   - Validar elegibilidade das unidades internamente
   - Validar data limite (não pode ser passada)
   - Retornar erro estruturado com campo específico que falhou

2. **Frontend deve:**
   - Apenas enviar os dados
   - Tratar erros genericamente

```typescript
// Código refatorado
async function salvarProcesso() {
  try {
    const request: CriarProcessoRequest = {
      descricao: descricao.value,
      tipo: tipo.value as TipoProcesso,
      dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
      unidades: unidadesSelecionadas.value, // Backend filtra elegíveis
    };
    
    if (processoEditando.value) {
      await processosStore.atualizarProcesso(processoEditando.value.codigo, request);
    } else {
      await processosStore.criarProcesso(request);
    }
    
    await router.push("/painel");
  } catch {
    // Erro tratado automaticamente
  }
}
```

**Backend esperado:**
```java
// DTO com validações
public class CriarProcessoRequest {
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotNull(message = "Tipo é obrigatório")
    private TipoProcesso tipo;
    
    @NotNull(message = "Data limite é obrigatória")
    @FutureOrPresent(message = "Data limite deve ser presente ou futura")
    private LocalDateTime dataLimiteEtapa1;
    
    @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
    private List<Integer> unidades;
}

// No serviço
public Processo criarProcesso(CriarProcessoRequest request) {
    // Filtra apenas unidades elegíveis
    List<Integer> unidadesElegiveis = filtrarUnidadesElegiveis(
        request.getUnidades(), 
        request.getTipo()
    );
    
    if (unidadesElegiveis.isEmpty()) {
        throw new ErroDadosInvalidos(
            "Nenhuma unidade elegível foi selecionada para o tipo de processo " + request.getTipo()
        );
    }
    // ... continua
}
```

**Arquivos Afetados:**
- `frontend/src/views/CadProcesso.vue`
- `frontend/src/stores/processos.ts`
- `backend/src/main/java/sgc/processo/dto/CriarProcessoRequest.java`
- `backend/src/main/java/sgc/processo/ProcessoService.java`

**Passos de Implementação para o Agente:**

1. **Criar/Atualizar DTO com validações** (`CriarProcessoRequest.java`):
   ```java
   public class CriarProcessoRequest {
       @NotBlank(message = "Descrição é obrigatória")
       @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
       private String descricao;
       
       @NotNull(message = "Tipo é obrigatório")
       private TipoProcesso tipo;
       
       @NotNull(message = "Data limite é obrigatória")
       @FutureOrPresent(message = "Data limite deve ser presente ou futura")
       private LocalDateTime dataLimiteEtapa1;
       
       @NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")
       private List<Integer> codigosUnidades;
       
       // getters, setters, builder
   }
   ```

2. **Implementar validação de elegibilidade no service** (`ProcessoService.java`):
   ```java
   public Processo criarProcesso(CriarProcessoRequest request) {
       // Filtrar apenas unidades elegíveis baseado no tipo
       List<Integer> unidadesElegiveis = filtrarUnidadesElegiveis(
           request.getCodigosUnidades(), 
           request.getTipo()
       );
       
       if (unidadesElegiveis.isEmpty()) {
           throw new ErroDadosInvalidos(
               "Nenhuma unidade elegível foi selecionada para o tipo de processo " 
               + request.getTipo().getLabel()
           );
       }
       
       // Continuar com criação...
   }
   
   private List<Integer> filtrarUnidadesElegiveis(
       List<Integer> codigosUnidades, 
       TipoProcesso tipo
   ) {
       return codigosUnidades.stream()
           .map(cod -> unidadeRepo.findById(cod).orElse(null))
           .filter(Objects::nonNull)
           .filter(unidade -> verificarElegibilidade(unidade, tipo))
           .map(Unidade::getCodigo)
           .toList();
   }
   
   private boolean verificarElegibilidade(Unidade unidade, TipoProcesso tipo) {
       // Implementar regra de elegibilidade
       // Ex: INTEROPERACIONAL só aceita unidades de certo nível
       return unidade.isElegivel(); // ou lógica mais complexa
   }
   ```

3. **Atualizar Controller** para usar `@Valid`:
   ```java
   @PostMapping
   public ResponseEntity<ProcessoDTO> criarProcesso(
       @Valid @RequestBody CriarProcessoRequest request
   ) {
       Processo processo = processoService.criarProcesso(request);
       return ResponseEntity.ok(processoMapper.toDTO(processo));
   }
   ```

4. **Simplificar frontend** (`CadProcesso.vue`):
   - Remover método `validarDados()` ou similar
   - Remover filtragem de unidades elegíveis (linhas 302-305, 376-380)
   - Simplificar `salvarProcesso()` para apenas enviar dados

**Comandos de Verificação:**

```bash
# 1. Compilar e testar backend
./gradlew :backend:clean :backend:test --tests "sgc.processo.*"

# 2. Verificar frontend
cd frontend && npm run typecheck && npm run lint

# 3. Executar teste E2E específico (se existir)
npx playwright test tests/processo.spec.ts

# 4. Teste manual completo
./gradlew bootRun
# Em outro terminal:
cd frontend && npm run dev
```

**Critérios de Sucesso:**

- [ ] DTO `CriarProcessoRequest` tem todas as validações Bean Validation
- [ ] Backend valida elegibilidade de unidades
- [ ] Backend retorna erro 400 com mensagem clara quando dados inválidos
- [ ] Backend retorna erro 400 quando nenhuma unidade elegível
- [ ] Frontend CadProcesso.vue tem ~100 linhas a menos
- [ ] Frontend não faz filtragem de elegibilidade
- [ ] Todos os testes passam
- [ ] Funcionalidade de criar processo continua operacional
- [ ] Mensagens de erro são em português e compreensíveis

**Testes a Criar/Atualizar:**

```java
// ProcessoServiceTest.java
@Test
void deveCriarProcessoComUnidadesElegiveis() {
    // Arrange
    CriarProcessoRequest request = CriarProcessoRequest.builder()
        .descricao("Processo de Teste")
        .tipo(TipoProcesso.MAPEAMENTO)
        .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
        .codigosUnidades(List.of(1, 2, 3))
        .build();
    
    // Act
    Processo processo = processoService.criarProcesso(request);
    
    // Assert
    assertNotNull(processo);
    assertTrue(processo.getUnidadesParticipantes().size() > 0);
}

@Test
void deveLancarErroQuandoNenhumaUnidadeElegivel() {
    CriarProcessoRequest request = CriarProcessoRequest.builder()
        .descricao("Processo de Teste")
        .tipo(TipoProcesso.INTEROPERACIONAL) // Tipo restritivo
        .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
        .codigosUnidades(List.of(999)) // Unidade não elegível
        .build();
    
    assertThrows(ErroDadosInvalidos.class, () -> {
        processoService.criarProcesso(request);
    });
}

@Test
void deveLancarErroQuandoDescricaoVazia() {
    // Teste de validação Bean Validation
}

@Test
void deveLancarErroQuandoDataPassada() {
    // Teste de validação @FutureOrPresent
}
```

**Checklist de Implementação:**

- [ ] Localizar/criar CriarProcessoRequest.java
- [ ] Adicionar todas as anotações de validação Bean Validation
- [ ] Implementar método `filtrarUnidadesElegiveis` no ProcessoService
- [ ] Implementar método `verificarElegibilidade` no ProcessoService
- [ ] Atualizar ProcessoController com @Valid
- [ ] Criar/atualizar testes unitários (mínimo 4 testes)
- [ ] Remover validações do frontend (CadProcesso.vue linhas 296-315)
- [ ] Remover filtragem de elegibilidade do frontend (linhas 302-305, 376-380)
- [ ] Simplificar método `salvarProcesso()` no frontend
- [ ] Executar `./gradlew :backend:test` - DEVE PASSAR
- [ ] Executar `npm run typecheck && npm run lint` - DEVE PASSAR
- [ ] Testar criação de processo com unidades não elegíveis (deve falhar gracefully)
- [ ] Testar criação de processo com dados válidos (deve funcionar)
- [ ] Commit: "refactor(processo): move validações e filtro de elegibilidade para backend"

---

### 1.4. DiagnosticoEquipe.vue

**ID da Tarefa:** REF-004  
**Prioridade:** Alta (Módulo Completo Faltando)  
**Estimativa:** 6-8 horas (inclui criação do módulo backend)

**Problema Atual:**
- **Linhas 225-230:** Validação de avaliações pendentes no frontend
- **Linha 176-186:** Inicialização com valores padrão no frontend (domínio 3, importância 3)
- **TODO na linha 243:** Comentário indica que falta implementação no backend
- **CRÍTICO:** Não existe módulo `diagnostico` no backend

```typescript
// Código atual
const avaliacoesPendentes = computed(() => {
  return competencias.value.filter((comp) => {
    const aval = avaliacoes.value[comp.codigo];
    return !aval || aval.importancia === 0 || aval.dominio === 0;
  });
});

function confirmarFinalizacao() {
  // TODO: Implementar chamada real ao backend para finalizar diagnóstico
  feedbackStore.show("Diagnóstico finalizado", ...);
  router.push("/painel");
}
```

**Refatoração Recomendada:**

1. **Criar endpoints:**
   - `POST /api/diagnosticos/{subprocessoId}/avaliacoes` - Salvar avaliações individuais
   - `POST /api/diagnosticos/{subprocessoId}/finalizar` - Finalizar diagnóstico
   - `GET /api/diagnosticos/{subprocessoId}/avaliacoes` - Buscar avaliações existentes

2. **Backend deve:**
   - Validar que todas as competências foram avaliadas antes de finalizar
   - Validar que valores de importância e domínio estão entre 1-5
   - Calcular automaticamente gaps e criticidades
   - Gerar alertas e notificações automaticamente

```typescript
// Código refatorado
// Salvar avaliações incrementalmente
async function salvarAvaliacao(competenciaId: number) {
  try {
    await diagnosticoService.salvarAvaliacao(codSubprocesso.value, {
      competenciaId,
      importancia: avaliacoes.value[competenciaId].importancia,
      dominio: avaliacoes.value[competenciaId].dominio,
      observacoes: avaliacoes.value[competenciaId].observacoes,
    });
  } catch {
    // Erro tratado
  }
}

// Finalizar
async function confirmarFinalizacao() {
  try {
    await diagnosticoService.finalizarDiagnostico(codSubprocesso.value);
    await router.push("/painel");
  } catch (error) {
    // Backend retorna erro se há avaliações pendentes
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/DiagnosticoEquipe.vue`
- `frontend/src/services/` (novo `diagnosticoService.ts`)
- `backend/src/main/java/sgc/` (novo módulo `diagnostico/`)

**Passos de Implementação para o Agente:**

**IMPORTANTE:** Esta tarefa requer criar um módulo backend completo. Siga a estrutura dos módulos existentes (processo, mapa, subprocesso).

**Fase 1: Criar Estrutura do Módulo Backend**

1. **Criar estrutura de pacotes**:
   ```bash
   mkdir -p backend/src/main/java/sgc/diagnostico
   mkdir -p backend/src/main/java/sgc/diagnostico/dto
   mkdir -p backend/src/test/java/sgc/diagnostico
   ```

2. **Criar entidade Diagnostico**:
   ```java
   // backend/src/main/java/sgc/diagnostico/Diagnostico.java
   @Entity
   @Table(name = "diagnostico")
   public class Diagnostico {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Integer codigo;
       
       @ManyToOne
       @JoinColumn(name = "cod_subprocesso")
       private Subprocesso subprocesso;
       
       @Enumerated(EnumType.STRING)
       private SituacaoDiagnostico situacao; // NAO_INICIADO, EM_ANDAMENTO, FINALIZADO
       
       @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL)
       private List<AvaliacaoCompetencia> avaliacoes = new ArrayList<>();
       
       private LocalDateTime dataFinalizacao;
       private LocalDateTime dataCriacao;
       
       // getters, setters, builder
   }
   ```

3. **Criar entidade AvaliacaoCompetencia**:
   ```java
   // backend/src/main/java/sgc/diagnostico/AvaliacaoCompetencia.java
   @Entity
   @Table(name = "avaliacao_competencia")
   public class AvaliacaoCompetencia {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Integer codigo;
       
       @ManyToOne
       @JoinColumn(name = "cod_diagnostico")
       private Diagnostico diagnostico;
       
       @ManyToOne
       @JoinColumn(name = "cod_competencia")
       private Competencia competencia;
       
       @Min(1) @Max(5)
       private Integer importancia; // 1-5
       
       @Min(1) @Max(5)
       private Integer dominio; // 1-5
       
       @Column(length = 1000)
       private String observacoes;
       
       private LocalDateTime dataAvaliacao;
       
       // Campos calculados (podem ser @Transient ou persistidos)
       @Transient
       public Integer getGap() {
           return importancia - dominio;
       }
       
       @Transient
       public Integer getCriticidade() {
           return importancia * Math.abs(getGap());
       }
       
       // getters, setters
   }
   ```

4. **Criar repositórios**:
   ```java
   // DiagnosticoRepo.java
   public interface DiagnosticoRepo extends JpaRepository<Diagnostico, Integer> {
       Optional<Diagnostico> findBySubprocessoCodigo(Integer codSubprocesso);
   }
   
   // AvaliacaoCompetenciaRepo.java
   public interface AvaliacaoCompetenciaRepo extends JpaRepository<AvaliacaoCompetencia, Integer> {
       List<AvaliacaoCompetencia> findByDiagnosticoCodigo(Integer codDiagnostico);
       Optional<AvaliacaoCompetencia> findByDiagnosticoCodigoAndCompetenciaCodigo(
           Integer codDiagnostico, 
           Integer codCompetencia
       );
   }
   ```

5. **Criar DTOs**:
   ```java
   // dto/AvaliacaoRequest.java
   public class AvaliacaoRequest {
       @NotNull(message = "Código da competência é obrigatório")
       private Integer codigoCompetencia;
       
       @NotNull(message = "Importância é obrigatória")
       @Min(value = 1, message = "Importância deve ser entre 1 e 5")
       @Max(value = 5, message = "Importância deve ser entre 1 e 5")
       private Integer importancia;
       
       @NotNull(message = "Domínio é obrigatório")
       @Min(value = 1, message = "Domínio deve ser entre 1 e 5")
       @Max(value = 5, message = "Domínio deve ser entre 1 e 5")
       private Integer dominio;
       
       @Size(max = 1000, message = "Observações não podem ter mais de 1000 caracteres")
       private String observacoes;
   }
   
   // dto/AvaliacaoDTO.java
   public class AvaliacaoDTO {
       private Integer codigo;
       private Integer codigoCompetencia;
       private String descricaoCompetencia;
       private Integer importancia;
       private Integer dominio;
       private Integer gap;
       private Integer criticidade;
       private String observacoes;
       private LocalDateTime dataAvaliacao;
   }
   ```

**Fase 2: Implementar Service**

6. **Criar DiagnosticoService**:
   ```java
   @Service
   public class DiagnosticoService {
       private final DiagnosticoRepo diagnosticoRepo;
       private final AvaliacaoCompetenciaRepo avaliacaoRepo;
       private final SubprocessoRepo subprocessoRepo;
       private final CompetenciaRepo competenciaRepo;
       
       public DiagnosticoDTO buscarOuCriarDiagnostico(Integer codSubprocesso) {
           // Busca ou cria diagnóstico para o subprocesso
       }
       
       public AvaliacaoDTO salvarAvaliacao(Integer codSubprocesso, AvaliacaoRequest request) {
           // Valida e salva avaliação
           Diagnostico diagnostico = buscarDiagnostico(codSubprocesso);
           
           // Verifica se já existe avaliação para esta competência
           Optional<AvaliacaoCompetencia> existente = avaliacaoRepo
               .findByDiagnosticoCodigoAndCompetenciaCodigo(
                   diagnostico.getCodigo(), 
                   request.getCodigoCompetencia()
               );
           
           AvaliacaoCompetencia avaliacao = existente.orElse(new AvaliacaoCompetencia());
           avaliacao.setDiagnostico(diagnostico);
           avaliacao.setCompetencia(competenciaRepo.findById(request.getCodigoCompetencia())
               .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada")));
           avaliacao.setImportancia(request.getImportancia());
           avaliacao.setDominio(request.getDominio());
           avaliacao.setObservacoes(request.getObservacoes());
           avaliacao.setDataAvaliacao(LocalDateTime.now());
           
           avaliacao = avaliacaoRepo.save(avaliacao);
           return mapearParaDTO(avaliacao);
       }
       
       public List<AvaliacaoDTO> buscarAvaliacoes(Integer codSubprocesso) {
           // Retorna todas as avaliações do diagnóstico
       }
       
       public void finalizarDiagnostico(Integer codSubprocesso) {
           Diagnostico diagnostico = buscarDiagnostico(codSubprocesso);
           
           // Validar que todas as competências foram avaliadas
           List<Competencia> competencias = buscarCompetenciasDoSubprocesso(codSubprocesso);
           List<AvaliacaoCompetencia> avaliacoes = avaliacaoRepo
               .findByDiagnosticoCodigo(diagnostico.getCodigo());
           
           if (avaliacoes.size() < competencias.size()) {
               throw new ErroDadosInvalidos(
                   "Diagnóstico não pode ser finalizado. " +
                   (competencias.size() - avaliacoes.size()) + 
                   " competências ainda não foram avaliadas."
               );
           }
           
           // Validar que todas as avaliações têm valores válidos
           boolean temAvaliacaoIncompleta = avaliacoes.stream()
               .anyMatch(a -> a.getImportancia() == null || 
                             a.getDominio() == null ||
                             a.getImportancia() < 1 || 
                             a.getDominio() < 1);
           
           if (temAvaliacaoIncompleta) {
               throw new ErroDadosInvalidos(
                   "Diagnóstico não pode ser finalizado. " +
                   "Há avaliações com valores inválidos."
               );
           }
           
           diagnostico.setSituacao(SituacaoDiagnostico.FINALIZADO);
           diagnostico.setDataFinalizacao(LocalDateTime.now());
           diagnosticoRepo.save(diagnostico);
           
           // TODO: Gerar alertas e notificações
       }
   }
   ```

**Fase 3: Criar Controller**

7. **Criar DiagnosticoController**:
   ```java
   @RestController
   @RequestMapping("/api/diagnosticos")
   public class DiagnosticoController {
       private final DiagnosticoService diagnosticoService;
       
       @GetMapping("/{codSubprocesso}")
       public ResponseEntity<DiagnosticoDTO> buscar(@PathVariable Integer codSubprocesso) {
           return ResponseEntity.ok(diagnosticoService.buscarOuCriarDiagnostico(codSubprocesso));
       }
       
       @PostMapping("/{codSubprocesso}/avaliacoes")
       public ResponseEntity<AvaliacaoDTO> salvarAvaliacao(
           @PathVariable Integer codSubprocesso,
           @Valid @RequestBody AvaliacaoRequest request
       ) {
           return ResponseEntity.ok(diagnosticoService.salvarAvaliacao(codSubprocesso, request));
       }
       
       @GetMapping("/{codSubprocesso}/avaliacoes")
       public ResponseEntity<List<AvaliacaoDTO>> buscarAvaliacoes(
           @PathVariable Integer codSubprocesso
       ) {
           return ResponseEntity.ok(diagnosticoService.buscarAvaliacoes(codSubprocesso));
       }
       
       @PostMapping("/{codSubprocesso}/finalizar")
       public ResponseEntity<Void> finalizar(@PathVariable Integer codSubprocesso) {
           diagnosticoService.finalizarDiagnostico(codSubprocesso);
           return ResponseEntity.ok().build();
       }
   }
   ```

**Fase 4: Atualizar Frontend**

8. **Criar diagnosticoService.ts**:
   ```typescript
   // frontend/src/services/diagnosticoService.ts
   import apiClient from './apiClient';
   
   export interface AvaliacaoRequest {
     codigoCompetencia: number;
     importancia: number;
     dominio: number;
     observacoes?: string;
   }
   
   export interface AvaliacaoDTO {
     codigo: number;
     codigoCompetencia: number;
     descricaoCompetencia: string;
     importancia: number;
     dominio: number;
     gap: number;
     criticidade: number;
     observacoes?: string;
     dataAvaliacao: string;
   }
   
   export const diagnosticoService = {
     async salvarAvaliacao(
       codSubprocesso: number, 
       avaliacao: AvaliacaoRequest
     ): Promise<AvaliacaoDTO> {
       const response = await apiClient.post(
         `/api/diagnosticos/${codSubprocesso}/avaliacoes`,
         avaliacao
       );
       return response.data;
     },
     
     async buscarAvaliacoes(codSubprocesso: number): Promise<AvaliacaoDTO[]> {
       const response = await apiClient.get(
         `/api/diagnosticos/${codSubprocesso}/avaliacoes`
       );
       return response.data;
     },
     
     async finalizarDiagnostico(codSubprocesso: number): Promise<void> {
       await apiClient.post(`/api/diagnosticos/${codSubprocesso}/finalizar`);
     }
   };
   ```

9. **Atualizar DiagnosticoEquipe.vue**:
   - Remover validação `avaliacoesPendentes` computed (linhas 225-230)
   - Remover inicialização com valores padrão (linhas 176-186)
   - Implementar método `confirmarFinalizacao()` (linha 243)
   - Salvar avaliações incrementalmente ao alterar valores

**Comandos de Verificação:**

```bash
# 1. Criar migration do banco (se necessário)
# Adicionar em backend/src/main/resources/db/migration/

# 2. Compilar backend
./gradlew :backend:compileJava

# 3. Executar testes do módulo
./gradlew :backend:test --tests "sgc.diagnostico.*"

# 4. Verificar frontend
cd frontend && npm run typecheck && npm run lint

# 5. Teste manual completo
./gradlew bootRun
cd frontend && npm run dev
# Navegar para DiagnosticoEquipe e testar fluxo completo
```

**Critérios de Sucesso:**

- [ ] Pacote `sgc.diagnostico` criado com estrutura completa
- [ ] Entidades `Diagnostico` e `AvaliacaoCompetencia` criadas
- [ ] Repositórios criados e funcionais
- [ ] DTOs com validações Bean Validation
- [ ] Service implementa toda lógica de negócio
- [ ] Controller expõe 4 endpoints REST
- [ ] Testes unitários cobrem service (mínimo 80%)
- [ ] Frontend usa novo service
- [ ] Validação de pendências removida do frontend
- [ ] Finalização funciona e valida backend
- [ ] Todos os testes passam
- [ ] Funcionalidade end-to-end operacional

**Testes a Criar:**

```java
// DiagnosticoServiceTest.java
@Test
void deveCriarDiagnosticoQuandoNaoExiste() { }

@Test
void deveSalvarAvaliacaoComDadosValidos() { }

@Test
void deveAtualizarAvaliacaoExistente() { }

@Test
void deveLancarErroQuandoImportanciaInvalida() { }

@Test
void deveLancarErroQuandoDominioInvalido() { }

@Test
void deveFinalizarDiagnosticoQuandoTodasAvaliacoesCompletas() { }

@Test
void deveLancarErroAoFinalizarComAvaliacoesPendentes() { }

@Test
void deveLancarErroAoFinalizarComAvaliacoesInvalidas() { }
```

**Checklist de Implementação:**

- [ ] Criar estrutura de pacotes backend
- [ ] Criar entidade Diagnostico
- [ ] Criar entidade AvaliacaoCompetencia
- [ ] Criar enum SituacaoDiagnostico
- [ ] Criar repositórios
- [ ] Criar DTOs com validações
- [ ] Implementar DiagnosticoService completo
- [ ] Criar DiagnosticoController
- [ ] Criar testes unitários (mínimo 8 testes)
- [ ] Criar diagnosticoService.ts no frontend
- [ ] Atualizar DiagnosticoEquipe.vue
- [ ] Remover validações do frontend
- [ ] Executar `./gradlew :backend:test` - DEVE PASSAR
- [ ] Executar `npm run typecheck && npm run lint` - DEVE PASSAR
- [ ] Testar fluxo completo manualmente
- [ ] Criar README.md no pacote diagnostico explicando o módulo
- [ ] Commit: "feat(diagnostico): implementa módulo completo de diagnóstico"

**Observações Importantes:**

- Este é o módulo mais complexo da refatoração
- Pode requerer migration de banco de dados
- Coordenar com módulos `mapa`, `subprocesso` e `competencia`
- Considerar criar o módulo em múltiplos commits:
  - Commit 1: Entidades e repositórios
  - Commit 2: Service e lógica de negócio
  - Commit 3: Controller e endpoints
  - Commit 4: Frontend integration
  - Commit 5: Testes

---

### 1.5. OcupacoesCriticas.vue

**Problema Atual:**
- **Linhas 258-262:** Validação de formulário no frontend
- **Linhas 238-307:** Toda a lógica de gerenciamento de ocupações no frontend (sem backend)
- **Dados não persistidos:** Ocupações críticas são apenas locais

```typescript
// Código atual - tudo no frontend
const ocupacoesCriticas = ref<Array<{
  nome: string;
  descricao: string;
  nivelCriticidade: number;
  competenciasCriticas: string[];
}>>([]);

function adicionarOcupacao() {
  if (!novaOcupacao.value.nome.trim() || !novaOcupacao.value.descricao.trim()) {
    feedbackStore.show("Dados incompletos", ...);
    return;
  }
  ocupacoesCriticas.value.push({ ... }); // Apenas local!
}
```

**Refatoração Recomendada:**

1. **Criar endpoints:**
   - `GET /api/diagnosticos/{subprocessoId}/ocupacoes-criticas` - Listar
   - `POST /api/diagnosticos/{subprocessoId}/ocupacoes-criticas` - Criar
   - `DELETE /api/diagnosticos/{subprocessoId}/ocupacoes-criticas/{id}` - Remover
   - `POST /api/diagnosticos/{subprocessoId}/ocupacoes-criticas/finalizar` - Finalizar

2. **Backend deve:**
   - Persistir ocupações críticas no banco
   - Validar dados (nome, descrição obrigatórios, criticidade 1-5)
   - Relacionar com competências do mapa
   - Gerar relatórios baseados nas ocupações

```typescript
// Código refatorado
const ocupacoesCriticas = computed(() => 
  diagnosticoStore.ocupacoesCriticas
);

async function adicionarOcupacao() {
  try {
    await diagnosticoService.criarOcupacaoCritica(codSubprocesso.value, {
      nome: novaOcupacao.value.nome,
      descricao: novaOcupacao.value.descricao,
      nivelCriticidade: novaOcupacao.value.nivelCriticidade,
      competenciasCriticas: novaOcupacao.value.competenciasCriticas,
    });
    
    // Recarrega lista
    await diagnosticoStore.buscarOcupacoesCriticas(codSubprocesso.value);
    limparFormulario();
  } catch {
    // Erro tratado
  }
}
```

**Arquivos Afetados:**
- `frontend/src/views/OcupacoesCriticas.vue`
- `frontend/src/stores/` (novo `diagnostico.ts`)
- `frontend/src/services/diagnosticoService.ts`
- `backend/src/main/java/sgc/diagnostico/` (novo módulo completo)

---

## 2. Filtragens e Ordenações Complexas

### 2.1. ArvoreUnidades.vue

**Problema Atual:**
- **Linhas 134-147:** Filtragem complexa para ocultar SEDOC no frontend
- **Linhas 150-181:** Lógica complexa de tri-state checkbox no frontend
- **Linhas 218-267:** Algoritmo de seleção hierárquica com regras especiais para INTEROPERACIONAL

```typescript
// Código atual - lógica complexa no frontend
const unidadesExibidas = computed(() => {
  const filtradas = props.unidades.filter(props.filtrarPor);
  const lista: Unidade[] = [];
  
  for (const u of filtradas) {
    // Oculta SEDOC mas mostra filhas
    if (u.sigla === 'SEDOC' || u.codigo === 1) {
      if (u.filhas) lista.push(...u.filhas);
    } else {
      lista.push(u);
    }
  }
  return lista;
});

function toggle(unidade: Unidade, checked: boolean) {
  // Algoritmo complexo de 40+ linhas para gerenciar seleção hierárquica
  const newSelection = new Set(unidadesSelecionadasLocal.value);
  const idsToToggle = [unidade.codigo, ...getTodasSubunidades(unidade)];
  
  if (checked) {
    idsToToggle.forEach(id => {
      const unidadeParaAdicionar = findUnidadeById(id);
      if (unidadeParaAdicionar?.isElegivel) {
        newSelection.add(id);
      }
    });
  } else {
    idsToToggle.forEach(id => newSelection.delete(id));
  }
  
  updateAncestors(unidade, newSelection); // Mais 25 linhas de lógica
  // ...
}
```

**Refatoração Recomendada:**

1. **Backend deve fornecer:**
   - Endpoint: `GET /api/unidades/arvore?tipoProcesso={tipo}&ocultarRaiz=true`
   - Árvore já filtrada (sem SEDOC)
   - Flag `isElegivel` já calculada por tipo de processo
   - Metadados para simplificar a UI

2. **Frontend deve:**
   - Manter lógica visual de tri-state (é UI, não negócio)
   - Simplificar algoritmo de seleção
   - Remover filtragem manual

```typescript
// Código refatorado - simplificado
const unidadesExibidas = computed(() => {
  // Backend já retorna sem SEDOC
  return unidadesStore.arvoreUnidadesFiltrada;
});

function toggle(unidade: Unidade, checked: boolean) {
  // Lógica simplificada - apenas visual
  const newSelection = new Set(unidadesSelecionadasLocal.value);
  
  if (checked) {
    adicionarUnidadeEFilhas(unidade, newSelection);
  } else {
    removerUnidadeEFilhas(unidade, newSelection);
  }
  
  unidadesSelecionadasLocal.value = Array.from(newSelection);
}
```

**Nota:** A lógica de tri-state checkbox pode permanecer no frontend, pois é uma questão de UX/apresentação. O que deve ir para o backend são as regras de elegibilidade e filtros de negócio.

**Arquivos Afetados:**
- `frontend/src/components/ArvoreUnidades.vue`
- `frontend/src/stores/unidades.ts`
- `backend/src/main/java/sgc/unidade/UnidadeController.java`

---

### 2.2. TabelaProcessos.vue

**Problema Atual:**
- **Linhas 78-94:** Formatação de enums no frontend
- **Linhas 69-71:** Controle de ordenação no frontend

```typescript
// Código atual
function formatarSituacao(situacao: string): string {
  const mapa: Record<string, string> = {
    EM_ANDAMENTO: "Em Andamento",
    FINALIZADO: "Finalizado",
    CRIADO: "Criado",
  };
  return mapa[situacao] || situacao;
}

function formatarTipo(tipo: string): string {
  const mapa: Record<string, string> = {
    MAPEAMENTO: "Mapeamento",
    REVISAO: "Revisão",
    DIAGNOSTICO: "Diagnóstico",
  };
  return mapa[tipo] || tipo;
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Retornar DTOs com campos já formatados para exibição
   - Exemplo: `situacaoLabel`, `tipoLabel`
   - Suportar ordenação via query params: `?sortBy=descricao&sortOrder=asc`

```typescript
// DTO do backend
interface ProcessoResumoDTO {
  codigo: number;
  descricao: string;
  tipo: "MAPEAMENTO" | "REVISAO" | "DIAGNOSTICO";
  tipoLabel: string; // "Mapeamento", "Revisão", "Diagnóstico"
  situacao: "CRIADO" | "EM_ANDAMENTO" | "FINALIZADO";
  situacaoLabel: string; // "Criado", "Em Andamento", "Finalizado"
  unidadesParticipantes: number;
  dataFinalizacao?: string;
}
```

```typescript
// Frontend simplificado
<template #cell(situacao)="data">
  {{ data.item.situacaoLabel }}
</template>

<template #cell(tipo)="data">
  {{ data.item.tipoLabel }}
</template>
```

**Alternativa:** Manter formatação no frontend usando constantes centralizadas, pois é apresentação. Mas ordenação deve ser backend.

**Arquivos Afetados:**
- `frontend/src/components/TabelaProcessos.vue`
- `backend/src/main/java/sgc/processo/dto/ProcessoResumoDTO.java`

---

### 2.3. ProcessoView.vue

**Problema Atual:**
- **Linhas 117-140:** Formatação complexa de dados hierárquicos no frontend
- **Linha 106-111:** Definição de colunas e larguras no frontend (poderia vir do backend)

```typescript
// Código atual
function formatarDadosParaArvore(dados: UnidadeParticipante[]): TreeTableItem[] {
  if (!dados) return [];
  return dados.map((item) => ({
    id: item.codUnidade,
    nome: `${item.sigla} - ${item.nome}`,
    situacao: item.situacaoSubprocesso || "Não iniciado",
    dataLimite: formatarData(item.dataLimite || null),
    unidadeAtual: item.sigla,
    clickable: true,
    expanded: true,
    children: item.filhos ? formatarDadosParaArvore(item.filhos) : [],
  }));
}
```

**Refatoração Recomendada:**

Backend já retorna estrutura hierárquica, mas pode melhorar:

1. **Backend deve:**
   - Incluir campos formatados: `dataLimiteFormatada`, `nomeCompleto`
   - Incluir flags: `isClickable`, `isExpanded` (baseado em regras)

```typescript
// DTO melhorado do backend
interface UnidadeParticipanteDTO {
  codUnidade: number;
  sigla: string;
  nome: string;
  nomeCompleto: string; // "SIGLA - Nome"
  situacaoSubprocesso: string;
  situacaoLabel: string; // "Cadastro em andamento"
  dataLimite: string; // ISO
  dataLimiteFormatada: string; // "01/12/2025"
  isClickable: boolean; // Baseado em permissões
  filhos: UnidadeParticipanteDTO[];
}
```

```typescript
// Frontend simplificado
function formatarDadosParaArvore(dados: UnidadeParticipanteDTO[]): TreeTableItem[] {
  return dados.map(item => ({
    id: item.codUnidade,
    nome: item.nomeCompleto,
    situacao: item.situacaoLabel,
    dataLimite: item.dataLimiteFormatada,
    unidadeAtual: item.sigla,
    clickable: item.isClickable,
    expanded: true,
    children: formatarDadosParaArvore(item.filhos),
  }));
}
```

**Arquivos Afetados:**
- `frontend/src/views/ProcessoView.vue`
- `backend/src/main/java/sgc/processo/dto/UnidadeParticipanteDTO.java`

---

## 3. Lógica de Negócio e Regras Complexas

### 3.1. ImpactoMapaModal.vue

**Problema Atual:**
- **Linhas 183-194:** Formatação de tipo de impacto no frontend (deveria vir do backend)
- **Modal carrega dados mas backend já faz o cálculo** - OK, mas pode melhorar resposta

```typescript
// Código atual
function formatTipoImpacto(tipo: TipoImpactoCompetencia): string {
  switch (tipo) {
    case TipoImpactoCompetencia.ATIVIDADE_REMOVIDA:
      return "Atividade Removida";
    case TipoImpactoCompetencia.ATIVIDADE_ALTERADA:
      return "Atividade Alterada";
    // ...
  }
}
```

**Refatoração Recomendada:**

Backend já calcula impactos, mas deve retornar labels:

```typescript
// DTO melhorado
interface ImpactoMapaDTO {
  temImpactos: boolean;
  atividadesInseridas: AtividadeImpactoDTO[];
  atividadesRemovidas: AtividadeImpactoDTO[];
  atividadesAlteradas: AtividadeImpactoDTO[];
  competenciasImpactadas: CompetenciaImpactadaDTO[];
}

interface CompetenciaImpactadaDTO {
  codigo: number;
  descricao: string;
  tipoImpacto: "ATIVIDADE_REMOVIDA" | "ATIVIDADE_ALTERADA" | "IMPACTO_GENERICO";
  tipoImpactoLabel: string; // "Atividade Removida"
  atividadesAfetadas: string[];
}
```

**Arquivos Afetados:**
- `frontend/src/components/ImpactoMapaModal.vue`
- `backend/src/main/java/sgc/mapa/dto/ImpactoMapaDTO.java`

---

### 3.2. utils/index.ts

**Problema Atual:**
- **Linhas 78-153:** Parser complexo de datas em múltiplos formatos
- **Linhas 17-56:** Mapeamentos de situações e labels (duplica backend)

```typescript
// Código atual - 75+ linhas de lógica de parsing de datas
export function parseDate(dateInput: string | number | Date | null | undefined): Date | null {
  // Múltiplos formatos: ISO, timestamps, DD/MM/YYYY, etc.
  // Lógica complexa de validação e conversão
  // ...
}

// Mapeamentos duplicados
const backendLabels: Record<string, string> = {
  NAO_INICIADO: "Não iniciado",
  MAPEAMENTO_CADASTRO_EM_ANDAMENTO: "Cadastro em andamento",
  // ... 30+ linhas
};
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - SEMPRE retornar datas em ISO 8601
   - SEMPRE incluir campos formatados quando necessário: `dataFormatada`, `dataHoraFormatada`
   - Incluir labels junto com enums

2. **Frontend deve:**
   - Usar `parseDate` apenas para inputs do usuário
   - Remover mapeamentos (usar DTOs do backend)
   - Simplificar utilitários de data

```typescript
// utils/index.ts refatorado - 80% menor
export function formatDateBR(isoDate: string): string {
  if (!isoDate) return "Não informado";
  return new Date(isoDate).toLocaleDateString("pt-BR");
}

export function formatDateForInput(isoDate: string): string {
  if (!isoDate) return "";
  return isoDate.split('T')[0]; // YYYY-MM-DD
}

// Mapeamentos removidos - usar DTOs do backend
```

**Arquivos Afetados:**
- `frontend/src/utils/index.ts`
- Todos os DTOs do backend (adicionar campos `*Label`, `*Formatada`)

---

### 3.3. Stores - Lógica de Transformação

**Problema Atual (Geral):**
Muitas stores fazem transformações e cálculos que poderiam vir do backend:

- `mapas.ts`: Cálculos de impacto (já no backend, mas pode melhorar)
- `processos.ts`: Filtragens de subprocessos elegíveis
- `atividades.ts`: Indexação por subprocesso (pode ser otimizada)

**Refatoração Recomendada:**

1. **Stores devem:**
   - Ser cache simples de dados do backend
   - Gerenciar estado local de UI (modais abertos, loading, etc.)
   - Chamar services e armazenar respostas

2. **Não devem:**
   - Fazer cálculos complexos
   - Filtrar dados de negócio (backend deve retornar filtrado)
   - Transformar enums (backend deve retornar labels)

**Exemplo - processos.ts:**

```typescript
// Atual - store busca e depois frontend filtra
const subprocessosElegiveis = computed(() => {
  return state.listaSubprocessosElegiveis || [];
});

// Refatorado - backend já retorna filtrados
async function buscarSubprocessosElegiveis(codProcesso: number) {
  state.listaSubprocessosElegiveis = await processoService
    .buscarSubprocessosElegiveis(codProcesso);
}
```

**Arquivos Afetados:**
- `frontend/src/stores/*.ts` (revisar todos)
- Múltiplos endpoints backend (adicionar filtros via query params)

---

## 4. Permissões e Autorizações

### 4.1. SubprocessoView.vue e ProcessoView.vue

**Problema Atual:**
- **ProcessoView.vue linhas 142-166:** Lógica de permissão de navegação no frontend
- **SubprocessoView.vue linha 96-100:** Validação de permissões localmente

```typescript
// Código atual - ProcessoView.vue
function abrirDetalhesUnidade(item: any) {
  if (item && item.clickable) {
    const perfilUsuario = perfilStore.perfilSelecionado;
    if (perfilUsuario === "ADMIN" || perfilUsuario === "GESTOR") {
      router.push({ ... });
    } else if (
      (perfilUsuario === "CHEFE" || perfilUsuario === "SERVIDOR") &&
      perfilStore.unidadeSelecionada === item.id
    ) {
      router.push({ ... });
    }
  }
}
```

**Refatoração Recomendada:**

1. **Backend deve:**
   - Incluir permissões em cada DTO: `podeVisualizar`, `podeEditar`, `podeNavegar`
   - Validar permissões em TODOS os endpoints (não confiar no frontend)

2. **Frontend deve:**
   - Usar flags do backend para habilitar/desabilitar UI
   - Router guards podem verificar permissões gerais

```typescript
// Frontend refatorado
function abrirDetalhesUnidade(item: UnidadeTreeItem) {
  if (item.permissoes.podeNavegar) {
    router.push({
      name: "Subprocesso",
      params: {
        codProcesso: codProcesso.value,
        siglaUnidade: item.unidadeAtual,
      },
    });
  }
}
```

```java
// Backend - UnidadeParticipanteDTO
public class UnidadeParticipanteDTO {
    // ... campos existentes
    
    private PermissoesUnidade permissoes;
    
    public static class PermissoesUnidade {
        private boolean podeNavegar;
        private boolean podeEditar;
        private boolean podeVisualizar;
        
        // Calculado baseado em perfil do usuário e situação
        public static PermissoesUnidade calcular(
            Perfil perfil, 
            SituacaoSubprocesso situacao,
            Integer unidadeUsuario,
            Integer unidadeTarget
        ) {
            // Lógica centralizada de permissões
        }
    }
}
```

**Arquivos Afetados:**
- `frontend/src/views/ProcessoView.vue`
- `frontend/src/views/SubprocessoView.vue`
- Todos os DTOs backend (adicionar objeto `permissoes`)
- `backend/src/main/java/sgc/comum/` (novo `PermissaoHelper.java`)

---

## 5. Formatação e Apresentação (Zona Cinzenta)

Alguns itens estão na "zona cinzenta" entre frontend e backend. Recomendações:

### 5.1. Formatação de Datas e Números

**Recomendação:** Backend retorna ISO 8601, frontend formata para exibição usando `Intl`

**Motivo:** Internacionalização futura, locale do navegador

**Exceção:** Para relatórios e exports, backend pode formatar

### 5.2. Badges e Classes CSS

**Recomendação:** Frontend mantém mapeamento de situação → classe CSS

**Motivo:** É puramente apresentação/tema visual

**Implementação:**
```typescript
// frontend/src/constants/situacoes.ts - OK manter
export const CLASSES_BADGE_SITUACAO = {
  NAO_INICIADO: "bg-secondary",
  EM_ANDAMENTO: "bg-primary",
  // ...
};
```

### 5.3. Ordenação de Tabelas

**Recomendação:** Backend implementa ordenação via query params, frontend chama endpoint

**Implementação:**
```typescript
// Frontend
async function ordenar(campo: string) {
  await processosStore.buscarProcessos({
    sortBy: campo,
    sortOrder: ordem.value,
  });
}

// Backend
@GetMapping
public Page<ProcessoResumo> listar(
    @RequestParam(required = false) String sortBy,
    @RequestParam(required = false) String sortOrder,
    Pageable pageable
) {
    // Aplica ordenação
}
```

---

## 6. Casos Especiais e TODOs Pendentes

### 6.1. TODOs Encontrados no Código

1. **DiagnosticoEquipe.vue linha 243:**
```typescript
// TODO: Implementar chamada real ao backend para finalizar diagnóstico
```
**Ação:** Criar endpoint `POST /api/diagnosticos/{id}/finalizar`

2. **OcupacoesCriticas.vue linha 298:**
```typescript
// TODO: Implementar chamada real ao backend para finalizar identificação
```
**Ação:** Criar endpoint `POST /api/diagnosticos/{id}/ocupacoes-criticas/finalizar`

3. **CadMapa.vue linha 536:**
```typescript
// TODO: Adicionar redirecionamento para o painel
```
**Ação:** Adicionar `router.push('/painel')` após sucesso

### 6.2. Módulo Diagnóstico Inexistente

**Observação:** As views `DiagnosticoEquipe.vue` e `OcupacoesCriticas.vue` não têm backend correspondente.

**Ação Requerida:**
1. Criar módulo completo: `backend/src/main/java/sgc/diagnostico/`
2. Entidades: `Diagnostico`, `AvaliacaoCompetencia`, `OcupacaoCritica`
3. Repositórios, Serviços, Controllers, DTOs
4. Integração com módulo `mapa` e `subprocesso`

**Arquivos a Criar:**
- `backend/src/main/java/sgc/diagnostico/Diagnostico.java`
- `backend/src/main/java/sgc/diagnostico/DiagnosticoService.java`
- `backend/src/main/java/sgc/diagnostico/DiagnosticoController.java`
- `backend/src/main/java/sgc/diagnostico/dto/AvaliacaoDTO.java`
- `backend/src/main/java/sgc/diagnostico/dto/OcupacaoCriticaDTO.java`

---

## 7. Resumo de Endpoints Necessários

### Novos Endpoints

| Endpoint | Método | Descrição | Prioridade |
|----------|--------|-----------|------------|
| `/api/diagnosticos/{id}/avaliacoes` | POST | Salvar avaliação de competência | Alta |
| `/api/diagnosticos/{id}/avaliacoes` | GET | Buscar avaliações existentes | Alta |
| `/api/diagnosticos/{id}/finalizar` | POST | Finalizar diagnóstico | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas` | GET | Listar ocupações críticas | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas` | POST | Criar ocupação crítica | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas/{ocupacaoId}` | DELETE | Remover ocupação crítica | Alta |
| `/api/diagnosticos/{id}/ocupacoes-criticas/finalizar` | POST | Finalizar identificação | Alta |
| `/api/subprocessos/{id}/disponibilizar-cadastro` | POST | Disponibilizar cadastro (com validações) | Alta |
| `/api/unidades/arvore` | GET | Árvore filtrada com flags de elegibilidade | Média |
| `/api/processos?sortBy=&sortOrder=` | GET | Listar com ordenação | Média |

### Endpoints a Melhorar

| Endpoint Atual | Melhoria Necessária | Prioridade |
|----------------|---------------------|------------|
| `POST /api/mapas/{id}/competencias` | Adicionar validações completas | Alta |
| `POST /api/processos` | Validar elegibilidade de unidades | Alta |
| `GET /api/processos/{id}` | Incluir permissões calculadas | Alta |
| `GET /api/processos/{id}/subprocessos-elegiveis` | Já existe, documentar melhor | Baixa |
| `GET /api/mapas/{id}/impacto` | Incluir labels formatados | Média |

---

## 8. Plano de Implementação Sugerido

### INSTRUÇÕES PARA AGENTES: Como Executar as Fases

**Antes de iniciar qualquer fase:**
1. Leia `AGENTS.md` na raiz do repositório
2. Execute `git status` e `git pull` para garantir repositório atualizado
3. Crie uma branch específica: `git checkout -b refactor/fase-X-nome`
4. Revise todas as tarefas da fase

**Durante a execução:**
- Faça commits pequenos após cada tarefa concluída
- Execute testes após cada mudança
- Não pule verificações
- Documente problemas encontrados

**Ao finalizar a fase:**
- Execute suite completa de testes: `./gradlew build && npm test`
- Revise todos os commits
- Crie PR com descrição detalhada
- Aguarde code review

---

### Fase 1: Validações Críticas (2-3 semanas)

**Objetivo:** Mover validações de negócio para backend  
**Tarefas:** REF-001, REF-002, REF-003  
**Prioridade:** ALTA - Começar por esta fase

#### Tarefas da Fase 1

1. **REF-001: CadAtividades.vue** (Seção 1.1)
   - Endpoint: `POST /api/subprocessos/{id}/disponibilizar-cadastro`
   - Tempo estimado: 2-3 horas
   - Arquivos: 4 arquivos (backend + frontend)

2. **REF-002: CadMapa.vue** (Seção 1.2)
   - Validações Bean Validation em CompetenciaRequest
   - Tempo estimado: 1-2 horas
   - Arquivos: 2 arquivos (backend DTO + frontend view)

3. **REF-003: CadProcesso.vue** (Seção 1.3)
   - Endpoint: `POST /api/processos` (melhorar existente)
   - Filtro de elegibilidade no backend
   - Tempo estimado: 2-3 horas
   - Arquivos: 4 arquivos

#### Ordem de Execução Recomendada

```
REF-002 → REF-001 → REF-003
(mais simples) → (média) → (mais complexa)
```

#### Comandos de Verificação da Fase 1

```bash
# Após concluir todas as tarefas da Fase 1

# 1. Build completo
./gradlew clean build

# 2. Testes backend
./gradlew :backend:test

# 3. Frontend typecheck e lint
cd frontend && npm run typecheck && npm run lint

# 4. Testes E2E relacionados
npx playwright test tests/cadastro.spec.ts
npx playwright test tests/processo.spec.ts

# 5. Verificar redução de linhas
git diff --stat origin/main frontend/src/views/CadAtividades.vue
git diff --stat origin/main frontend/src/views/CadMapa.vue
git diff --stat origin/main frontend/src/views/CadProcesso.vue
```

#### Critérios de Sucesso da Fase 1

- [ ] Todos os testes passam: `./gradlew build` sem erros
- [ ] TypeCheck passa: `npm run typecheck` sem erros
- [ ] Lint passa: `npm run lint` sem erros
- [ ] Testes E2E de cadastro passam
- [ ] Frontend reduzido em ~200 linhas (verificar com `git diff --stat`)
- [ ] Backend tem novos testes unitários (mínimo 9 testes)
- [ ] Mensagens de erro todas em português
- [ ] Funcionalidades continuam operacionais

**Entregáveis:**
- Endpoints de cadastro validam dados completamente
- Frontend simplificado (remove 200+ linhas de validação)
- Mensagens de erro estruturadas
- Documentação atualizada

---

### Fase 2: Módulo Diagnóstico (3-4 semanas)

**Objetivo:** Criar backend para funcionalidades de diagnóstico  
**Tarefas:** REF-004, REF-005 (OcupacoesCriticas)  
**Prioridade:** ALTA - Funcionalidade crítica sem backend

**ATENÇÃO:** Esta fase requer criar módulo backend completo do zero

#### Tarefas da Fase 2

1. **REF-004: DiagnosticoEquipe.vue** (Seção 1.4)
   - Criar módulo completo `sgc.diagnostico`
   - Endpoints para avaliações
   - Tempo estimado: 6-8 horas
   - Arquivos: 15+ arquivos novos

2. **REF-005: OcupacoesCriticas.vue** (Seção 1.5)
   - Estender módulo diagnóstico
   - Endpoints para ocupações críticas
   - Tempo estimado: 4-6 horas
   - Arquivos: 8+ arquivos

#### Estrutura do Módulo a Criar

```
backend/src/main/java/sgc/diagnostico/
├── Diagnostico.java (entidade)
├── AvaliacaoCompetencia.java (entidade)
├── OcupacaoCritica.java (entidade)
├── SituacaoDiagnostico.java (enum)
├── DiagnosticoRepo.java
├── AvaliacaoCompetenciaRepo.java
├── OcupacaoCriticaRepo.java
├── DiagnosticoService.java
├── DiagnosticoController.java
├── dto/
│   ├── DiagnosticoDTO.java
│   ├── AvaliacaoRequest.java
│   ├── AvaliacaoDTO.java
│   ├── OcupacaoCriticaRequest.java
│   └── OcupacaoCriticaDTO.java
└── README.md (documentação do módulo)

backend/src/test/java/sgc/diagnostico/
├── DiagnosticoServiceTest.java
└── DiagnosticoControllerTest.java
```

#### Ordem de Execução Recomendada

**Dia 1-2: Estrutura e Entidades**
1. Criar estrutura de pacotes
2. Criar entidades (Diagnostico, AvaliacaoCompetencia)
3. Criar enums
4. Criar repositórios
5. Commit: "feat(diagnostico): cria entidades e repositórios"

**Dia 3-4: Service e Lógica de Negócio**
1. Criar DTOs com validações
2. Implementar DiagnosticoService
3. Criar testes unitários do service
4. Commit: "feat(diagnostico): implementa service de avaliações"

**Dia 5-6: Controller e Endpoints**
1. Criar DiagnosticoController
2. Testar endpoints com Postman/Insomnia
3. Criar testes de integração
4. Commit: "feat(diagnostico): adiciona endpoints REST"

**Dia 7-8: Integração Frontend**
1. Criar diagnosticoService.ts
2. Atualizar DiagnosticoEquipe.vue
3. Testar fluxo E2E completo
4. Commit: "feat(diagnostico): integra frontend com backend"

**Dia 9-10: Ocupações Críticas**
1. Estender entidades (OcupacaoCritica)
2. Implementar lógica no service
3. Atualizar controller
4. Atualizar OcupacoesCriticas.vue
5. Commit: "feat(diagnostico): adiciona ocupações críticas"

#### Comandos de Verificação da Fase 2

```bash
# Verificar estrutura criada
tree backend/src/main/java/sgc/diagnostico/
tree backend/src/test/java/sgc/diagnostico/

# Build e testes
./gradlew :backend:compileJava
./gradlew :backend:test --tests "sgc.diagnostico.*"

# Coverage (se configurado)
./gradlew :backend:jacocoTestReport
# Verificar que módulo diagnostico tem >80% cobertura

# Frontend
cd frontend && npm run typecheck && npm run lint

# E2E
npx playwright test tests/diagnostico.spec.ts
```

#### Critérios de Sucesso da Fase 2

- [ ] Módulo `sgc.diagnostico` existe e está completo
- [ ] 5 entidades criadas (Diagnostico, AvaliacaoCompetencia, OcupacaoCritica, etc)
- [ ] 6+ endpoints REST funcionais
- [ ] Cobertura de testes >80% no módulo
- [ ] Frontend integrado e funcional
- [ ] Dados persistem no banco
- [ ] Validações funcionam no backend
- [ ] README.md do módulo criado
- [ ] Testes E2E passam

**Entregáveis:**
- Módulo `diagnostico` completo
- Views funcionando com persistência real
- Relatórios de diagnóstico (se aplicável)
- Documentação do módulo

---

### Fase 3: Otimização de DTOs (2 semanas)

**Objetivo:** Enriquecer DTOs com dados formatados  
**Tarefas:** Múltiplas, espalhadas pelas seções 2 e 3  
**Prioridade:** MÉDIA

**ATENÇÃO:** Esta fase toca muitos arquivos. Fazer incrementalmente.

#### Tarefas da Fase 3

1. **Adicionar campos `*Label` em todos os enums**
   - TipoProcesso, SituacaoProcesso, SituacaoSubprocesso, etc
   - Tempo: 1-2 horas
   - Pattern:
   ```java
   public enum TipoProcesso {
       MAPEAMENTO("Mapeamento"),
       REVISAO("Revisão"),
       DIAGNOSTICO("Diagnóstico");
       
       private final String label;
       
       TipoProcesso(String label) { this.label = label; }
       public String getLabel() { return label; }
   }
   ```

2. **Adicionar campos formatados em DTOs de datas**
   - ProcessoDTO, SubprocessoDTO, etc
   - Pattern:
   ```java
   public class ProcessoDTO {
       private LocalDateTime dataLimiteEtapa1;
       private String dataLimiteEtapa1Formatada; // "01/12/2025"
       
       // No mapper
       dto.setDataLimiteEtapa1Formatada(
           DateTimeFormatter.ofPattern("dd/MM/yyyy")
               .format(processo.getDataLimiteEtapa1())
       );
   }
   ```

3. **Adicionar objeto `permissoes` em DTOs principais**
   - ProcessoDTO, UnidadeParticipanteDTO
   - Ver seção 4.1 para detalhes

4. **Remover mapeamentos do frontend**
   - Simplificar `utils/index.ts`
   - Remover funções de formatação duplicadas
   - Usar campos do DTO diretamente

#### Ordem de Execução Recomendada

```
1. Enums com labels (mais fácil, baixo risco)
2. Datas formatadas nos DTOs principais
3. Objeto permissões (mais complexo)
4. Limpeza do frontend
```

#### Comandos de Verificação da Fase 3

```bash
# Verificar que DTOs têm campos formatados
grep -r "Formatada" backend/src/main/java/sgc/*/dto/

# Verificar que enums têm labels
grep -r "getLabel()" backend/src/main/java/sgc/*/

# Build e testes
./gradlew build

# Verificar redução de utils/index.ts
git diff --stat origin/main frontend/src/utils/index.ts
# Deve mostrar redução significativa (~66%)
```

#### Critérios de Sucesso da Fase 3

- [ ] Todos os enums têm método `getLabel()`
- [ ] DTOs principais têm campos `*Formatada` para datas
- [ ] DTOs principais têm objeto `permissoes` quando aplicável
- [ ] Frontend `utils/index.ts` reduzido em ~150 linhas
- [ ] Mapeamentos removidos do frontend
- [ ] Todos os testes passam
- [ ] Funcionalidades continuam operacionais

**Entregáveis:**
- DTOs autocontidos
- Frontend 30% mais simples
- Menos duplicação de código

---

### Fase 4: Filtragens e Ordenações (1-2 semanas)

**Objetivo:** Backend fornece dados filtrados e ordenados  
**Tarefas:** Seções 2.1, 2.2, 2.3  
**Prioridade:** MÉDIA

#### Tarefas da Fase 4

1. **Adicionar query params de ordenação em endpoints de listagem**
   ```java
   @GetMapping
   public Page<ProcessoDTO> listar(
       @RequestParam(required = false) String sortBy,
       @RequestParam(required = false) String sortOrder,
       Pageable pageable
   ) {
       // Implementar ordenação
   }
   ```

2. **Melhorar endpoint de árvore de unidades** (Seção 2.1)
   - Adicionar filtros por tipo de processo
   - Calcular flag `isElegivel` no backend
   - Retornar árvore sem SEDOC se solicitado

3. **Adicionar paginação onde necessário**
   - Endpoints que retornam listas grandes
   - Usar `Pageable` do Spring

4. **Simplificar componentes do frontend**
   - Remover lógica de ordenação local
   - Remover filtragem complexa

#### Comandos de Verificação da Fase 4

```bash
# Testar endpoints com query params
curl "http://localhost:8080/api/processos?sortBy=descricao&sortOrder=asc"
curl "http://localhost:8080/api/unidades/arvore?tipoProcesso=MAPEAMENTO&ocultarRaiz=true"

# Build e testes
./gradlew build

# Verificar que frontend foi simplificado
git diff --stat origin/main frontend/src/components/ArvoreUnidades.vue
```

#### Critérios de Sucesso da Fase 4

- [ ] Endpoints principais aceitam `sortBy` e `sortOrder`
- [ ] Endpoint de árvore filtra e calcula elegibilidade
- [ ] Paginação implementada onde necessário
- [ ] Frontend simplificado (menos lógica de filtro/sort)
- [ ] Performance melhorada (medido com DevTools)
- [ ] Testes passam

**Entregáveis:**
- APIs REST completas com suporte a sort/filter
- Performance melhorada
- Frontend apenas consome dados

---

### Fase 5: Refatoração de Stores (1 semana)

**Objetivo:** Simplificar stores  
**Tarefas:** Seção 3.3  
**Prioridade:** BAIXA (mas importante para manutenibilidade)

#### Tarefas da Fase 5

1. **Revisar cada store** (`frontend/src/stores/*.ts`)
2. **Remover computeds complexos** - mover para backend
3. **Padronizar estrutura** de todas as stores
4. **Atualizar testes** de stores

#### Template de Store Simplificada

```typescript
// stores/exemplo.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { exemploService } from '@/services/exemploService';

export const useExemploStore = defineStore('exemplo', () => {
  // State
  const items = ref<Item[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  // Actions (apenas chama service e armazena)
  async function buscarItems() {
    loading.value = true;
    error.value = null;
    try {
      items.value = await exemploService.buscar();
    } catch (e) {
      error.value = 'Erro ao buscar items';
      throw e;
    } finally {
      loading.value = false;
    }
  }
  
  // Computed simples (apenas formatação de UI)
  const itemsOrdenados = computed(() => 
    items.value.slice().sort((a, b) => a.ordem - b.ordem)
  );
  
  return {
    items,
    loading,
    error,
    itemsOrdenados,
    buscarItems
  };
});
```

#### Comandos de Verificação da Fase 5

```bash
# Verificar tamanho das stores
wc -l frontend/src/stores/*.ts

# TypeCheck e lint
cd frontend && npm run typecheck && npm run lint

# Testes unitários das stores (se existirem)
cd frontend && npm run test:unit
```

#### Critérios de Sucesso da Fase 5

- [ ] Todas as stores seguem padrão consistente
- [ ] Computeds complexos removidos
- [ ] Stores têm apenas state, actions simples, e getters básicos
- [ ] Código 40% mais simples (medido por linhas)
- [ ] Testes atualizados
- [ ] TypeCheck passa

**Entregáveis:**
- Stores 40% mais simples
- Código mais manutenível
- Melhor separação de responsabilidades

---

### Resumo das Fases para Planejamento

| Fase | Prioridade | Tempo | Complexidade | Risco |
|------|-----------|-------|--------------|-------|
| Fase 1: Validações | ALTA | 2-3 sem | Média | Baixo |
| Fase 2: Diagnóstico | ALTA | 3-4 sem | Alta | Médio |
| Fase 3: DTOs | MÉDIA | 2 sem | Média | Baixo |
| Fase 4: Filtros/Sort | MÉDIA | 1-2 sem | Baixa | Baixo |
| Fase 5: Stores | BAIXA | 1 sem | Baixa | Muito Baixo |
| **TOTAL** | - | **9-12 sem** | - | - |

### Estratégia de Execução para Agentes

**Opção 1: Sequencial (Recomendado para agentes autônomos)**
```
Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5
```
- Cada fase é completada antes de iniciar próxima
- Menor risco de conflitos
- Mais fácil de testar incrementalmente

**Opção 2: Paralela (Requer coordenação)**
```
Fase 1 + Fase 3 (em paralelo)
↓
Fase 2
↓
Fase 4 + Fase 5 (em paralelo)
```
- Mais rápido
- Requer múltiplos agentes ou branches
- Maior risco de conflitos de merge

---

## 9. Métricas de Sucesso

### Redução de Código Frontend

**Objetivo:** Reduzir 25-35% do código de lógica de negócio no frontend

| Arquivo | Linhas Atuais | Linhas Estimadas Pós-Refatoração | Redução |
|---------|---------------|----------------------------------|---------|
| CadAtividades.vue | 724 | ~550 | 24% |
| CadMapa.vue | 546 | ~400 | 27% |
| CadProcesso.vue | 436 | ~300 | 31% |
| DiagnosticoEquipe.vue | 262 | ~180 | 31% |
| OcupacoesCriticas.vue | 307 | ~200 | 35% |
| ArvoreUnidades.vue | 305 | ~220 | 28% |
| utils/index.ts | 235 | ~80 | 66% |
| **Total Estimado** | **~2.800** | **~1.930** | **~31%** |

### Aumento de Cobertura Backend

**Objetivo:** Garantir 80%+ cobertura de testes em novos módulos

- Validações: 100% cobertura
- Módulo diagnóstico: 85%+ cobertura
- DTOs e mappers: 90%+ cobertura

### Melhoria de Performance

**Objetivo:** Reduzir chamadas de API desnecessárias

- Menos re-fetches (dados vêm completos)
- Paginação implementada onde necessário
- Cache de dados formatados

---

## 10. Riscos e Mitigações

### Risco 1: Quebra de Funcionalidades Existentes

**Probabilidade:** Média  
**Impacto:** Alto

**Mitigação:**
- Implementar mudanças incrementalmente
- Manter testes E2E passando a cada fase
- Feature flags para novas implementações
- Testes de regressão extensivos

### Risco 2: Incompatibilidade de DTOs

**Probabilidade:** Baixa  
**Impacto:** Médio

**Mitigação:**
- Versionar endpoints se necessário
- Manter backward compatibility temporariamente
- Documentar breaking changes claramente

### Risco 3: Aumento de Complexidade Backend

**Probabilidade:** Média  
**Impacto:** Médio

**Mitigação:**
- Seguir arquitetura em camadas rigorosamente
- Documentar regras de negócio
- Code reviews obrigatórias
- Testes unitários abrangentes

---

## 11. Checklist de Implementação

### INSTRUÇÕES PARA AGENTES: Como Usar Esta Checklist

Esta seção fornece um template de checklist que deve ser seguido **para cada tarefa** do plano de refatoração.

**Como usar:**
1. Copie a checklist relevante (view/component ou endpoint)
2. Marque cada item conforme completa
3. NÃO pule itens mesmo que pareçam óbvios
4. Use esta checklist para validação final antes de commit

---

### Checklist: Para Cada View/Component Refatorado

**Preparação:**
- [ ] Li o `AGENTS.md` e entendo convenções do projeto
- [ ] Li a seção específica deste documento para a tarefa
- [ ] Entendo o problema atual (li código existente)
- [ ] Identifiquei validações de negócio a mover
- [ ] Identifiquei filtragens/ordenações a mover
- [ ] Identifiquei mapeamentos duplicados
- [ ] Criei branch específica: `git checkout -b refactor/nome-da-tarefa`

**Backend - Endpoints:**
- [ ] Criar/atualizar endpoint backend necessário
- [ ] Adicionar anotação `@Valid` nos parâmetros
- [ ] Implementar toda lógica de validação no service
- [ ] Lançar exceções apropriadas (ErroApi hierarchy)
- [ ] Retornar DTOs (nunca entidades JPA)
- [ ] Endpoint retorna mensagens em português
- [ ] Testado manualmente com Postman/Insomnia (se disponível)

**Backend - DTOs:**
- [ ] Criar/atualizar DTOs com Bean Validation
- [ ] Adicionar `@NotNull`, `@NotBlank`, `@NotEmpty` onde apropriado
- [ ] Adicionar `@Min`, `@Max`, `@Size` onde apropriado
- [ ] Adicionar campos `*Label` para enums
- [ ] Adicionar campos `*Formatada` para datas
- [ ] DTOs têm JavaDoc explicando seu propósito
- [ ] Usar Português em todos os nomes e mensagens

**Backend - Service:**
- [ ] Implementar validações de negócio
- [ ] Implementar filtragens complexas
- [ ] Implementar cálculos (se aplicável)
- [ ] Lançar exceções apropriadas com mensagens claras
- [ ] Método tem JavaDoc explicando regras de negócio
- [ ] Service é testável (usa injeção de dependência)

**Backend - Testes:**
- [ ] Criar testes unitários do service (mínimo 3-5 testes)
- [ ] Teste: cenário válido (happy path)
- [ ] Teste: validação falha (cada campo obrigatório)
- [ ] Teste: regra de negócio falha
- [ ] Teste: exceções são lançadas corretamente
- [ ] Executar `./gradlew :backend:test --tests "NomeTest"` - PASSA
- [ ] Cobertura >80% no método testado (verificar se possível)

**Frontend - Service:**
- [ ] Atualizar/criar método no service TypeScript
- [ ] Service retorna Promise tipada corretamente
- [ ] Service usa apiClient (Axios configurado)
- [ ] Service não faz validações de negócio
- [ ] Service tem JSDoc explicando uso

**Frontend - Store:**
- [ ] Atualizar método na store Pinia
- [ ] Store apenas chama service e armazena resultado
- [ ] Store não faz validações de negócio
- [ ] Store não faz filtragens complexas
- [ ] Store não faz cálculos de negócio
- [ ] Manter loading/error state se aplicável

**Frontend - View/Component:**
- [ ] Remover validações de negócio
- [ ] Remover filtragens complexas  
- [ ] Remover mapeamentos duplicados (usar DTO)
- [ ] Simplificar métodos (menos linhas)
- [ ] Manter apenas lógica de UI/UX
- [ ] Usar try/catch e confiar em interceptor Axios
- [ ] Componente permanece em Português

**Verificação Final:**
- [ ] Executar `git status` - revisar arquivos modificados
- [ ] Executar `git diff` - revisar mudanças linha por linha
- [ ] Executar `./gradlew :backend:compileJava` - SUCESSO
- [ ] Executar `./gradlew :backend:test` - TODOS PASSAM
- [ ] Executar `cd frontend && npm run typecheck` - SEM ERROS
- [ ] Executar `cd frontend && npm run lint` - SEM ERROS
- [ ] Testar funcionalidade manualmente - FUNCIONA
- [ ] Testar cenários de erro manualmente - ERROS CLAROS
- [ ] Verificar que mensagens são em português
- [ ] Ler código modificado - está limpo e claro?
- [ ] View/Component tem menos linhas que antes?

**Documentação:**
- [ ] Atualizar README.md do módulo (se aplicável)
- [ ] Adicionar comentários em lógica complexa (se necessário)
- [ ] Atualizar seção de API em documentação (se aplicável)

**Git:**
- [ ] Criar commit com mensagem descritiva
- [ ] Mensagem segue padrão: `refactor(módulo): descrição`
- [ ] Commit contém apenas arquivos relacionados à tarefa
- [ ] Push para branch remota
- [ ] Verificar que CI passa (se configurado)

---

### Checklist: Para Cada Novo Endpoint

**Planejamento:**
- [ ] Endpoint está documentado neste plano (seção 7)
- [ ] Entendo o propósito do endpoint
- [ ] Sei quais validações são necessárias
- [ ] Sei qual resposta retornar (DTO definido)
- [ ] Sei quais erros podem ocorrer

**Contrato da API:**
- [ ] Especificar método HTTP (GET/POST/PUT/DELETE)
- [ ] Especificar URL path (ex: `/api/recurso/{id}/acao`)
- [ ] Especificar parâmetros de path (@PathVariable)
- [ ] Especificar parâmetros de query (@RequestParam)
- [ ] Especificar body (Request DTO)
- [ ] Especificar resposta (Response DTO ou void)
- [ ] Especificar códigos de status (200, 400, 404, etc)

**Request DTO:**
- [ ] Criar classe Request DTO (se necessário)
- [ ] Adicionar Bean Validation annotations
- [ ] Mensagens de validação em português
- [ ] Campos têm tipos corretos (não usar String para tudo)
- [ ] DTO tem JavaDoc
- [ ] DTO segue convenções do projeto (PascalCase)

**Response DTO:**
- [ ] Criar/usar classe Response DTO
- [ ] DTO tem todos os campos necessários
- [ ] DTO tem campos formatados (*Label, *Formatada)
- [ ] DTO não expõe dados sensíveis
- [ ] DTO tem JavaDoc
- [ ] Mapper criado/atualizado (MapStruct)

**Controller:**
- [ ] Criar método no Controller
- [ ] Anotação de mapping correta (@GetMapping, @PostMapping, etc)
- [ ] Path correto e RESTful
- [ ] Usar @Valid em Request DTOs
- [ ] Usar @PathVariable e @RequestParam quando necessário
- [ ] Retornar ResponseEntity tipado
- [ ] Código de status correto (ok(), created(), etc)
- [ ] JavaDoc explicando endpoint
- [ ] Seguir padrão do projeto (veja AGENTS.md seção 3.2)

**Service:**
- [ ] Implementar lógica no Service (não no Controller)
- [ ] Validar todos os dados de entrada
- [ ] Validar regras de negócio
- [ ] Lançar exceções apropriadas
- [ ] Usar transações (@Transactional) se necessário
- [ ] Logs apropriados (se aplicável)
- [ ] Retornar DTO ou entidade (mapper converte)

**Validações:**
- [ ] Campos obrigatórios validados (@NotNull, @NotBlank, @NotEmpty)
- [ ] Tamanhos validados (@Size, @Min, @Max)
- [ ] Formatos validados (@Email, @Pattern, etc)
- [ ] Regras de negócio validadas no service
- [ ] Mensagens de erro claras e em português
- [ ] Exceções corretas lançadas (ErroApi hierarchy)

**Autorização:**
- [ ] Verificar se endpoint precisa autenticação
- [ ] Verificar permissões do usuário (se aplicável)
- [ ] Incluir objeto `permissoes` no DTO (se aplicável)
- [ ] Retornar 401/403 quando não autorizado

**Testes Unitários:**
- [ ] Teste: happy path (cenário válido)
- [ ] Teste: cada validação Bean Validation
- [ ] Teste: cada regra de negócio
- [ ] Teste: cenários de exceção
- [ ] Teste: autorização (se aplicável)
- [ ] Mock de dependências externas
- [ ] Todos os testes passam
- [ ] Cobertura >80% no método

**Testes de Integração (se necessário):**
- [ ] Teste: endpoint responde corretamente
- [ ] Teste: banco de dados persiste dados
- [ ] Teste: transações funcionam
- [ ] Usar @SpringBootTest e @AutoConfigureMockMvc
- [ ] Usar profile de teste (H2)

**Documentação:**
- [ ] Endpoint documentado com comentários JavaDoc
- [ ] Swagger/OpenAPI atualizado (auto-gerado)
- [ ] Adicionar a collection Postman/Insomnia (se usado)
- [ ] Atualizar seção 7 deste documento se necessário

**Verificação Final:**
- [ ] Compilar: `./gradlew :backend:compileJava` - SUCESSO
- [ ] Testar: `./gradlew :backend:test` - TODOS PASSAM
- [ ] Testar endpoint manualmente (Postman/curl)
- [ ] Testar cenários de erro (dados inválidos)
- [ ] Verificar resposta JSON está correta
- [ ] Verificar mensagens de erro em português
- [ ] Commit: `feat(modulo): adiciona endpoint X`

---

### Checklist: Finalização de Fase

Após completar todas as tarefas de uma fase, use esta checklist:

**Revisão de Código:**
- [ ] Revisar todos os commits da fase
- [ ] Commits têm mensagens claras e descritivas
- [ ] Cada commit compila e testa isoladamente
- [ ] Sem código comentado deixado para trás
- [ ] Sem TODOs ou FIXMEs novos (a menos que documentados)
- [ ] Sem console.log() ou System.out.println() esquecidos

**Testes Completos:**
- [ ] Backend: `./gradlew clean build` - SUCESSO
- [ ] Backend: Todos os testes passam
- [ ] Frontend: `npm run typecheck` - SEM ERROS
- [ ] Frontend: `npm run lint` - SEM ERROS  
- [ ] E2E: `npm test` - TODOS OS TESTES RELEVANTES PASSAM
- [ ] Teste manual de cada funcionalidade modificada
- [ ] Teste de regressão (funcionalidades não modificadas ainda funcionam)

**Métricas:**
- [ ] Verificar redução de linhas: `git diff --stat origin/main`
- [ ] Frontend reduzido conforme esperado (ver seção 9)
- [ ] Backend cresceu de forma controlada (apenas o necessário)
- [ ] Cobertura de testes mantida ou melhorada

**Documentação:**
- [ ] README.md atualizado (se necessário)
- [ ] AGENTS.md atualizado (se novas convenções)
- [ ] Este plano atualizado (se descobriu algo novo)
- [ ] Comentários de código adequados
- [ ] JavaDoc/JSDoc completo

**Pull Request:**
- [ ] Criar PR com título descritivo
- [ ] Descrição do PR lista todas as mudanças
- [ ] Descrição menciona testes executados
- [ ] Descrição menciona métricas (linhas reduzidas, etc)
- [ ] Screenshots de mudanças de UI (se aplicável)
- [ ] Marcar reviewers apropriados
- [ ] Linkar issues relacionadas

**Comunicação:**
- [ ] Documentar quaisquer problemas encontrados
- [ ] Documentar decisões técnicas tomadas
- [ ] Documentar desvios do plano original
- [ ] Sugerir melhorias para próximas fases

---

## 12. Conclusão

Este plano de refatoração visa transformar o frontend SGC de um protótipo com lógica de negócio misturada para uma aplicação production-ready com separação clara de responsabilidades.

### Benefícios Esperados

1. **Manutenibilidade:** Código frontend 30% mais simples e focado em UI
2. **Consistência:** Regras de negócio centralizadas no backend
3. **Performance:** Menos processamento no cliente, dados otimizados
4. **Segurança:** Validações no servidor, não contornáveis
5. **Testabilidade:** Lógica de negócio 100% testável no backend
6. **Escalabilidade:** Fácil adicionar novos clientes (mobile, API pública)

### Esforço Total Estimado

**9-12 semanas** (2-3 sprints de 3-4 semanas)

### Priorização

Começar pela **Fase 1 (validações críticas)** pois tem maior impacto na segurança e qualidade.

---

## 13. Guia Rápido para Agentes de IA

### Se você está começando agora

1. **Leia primeiro:**
   - [ ] AGENTS.md (convenções do projeto)
   - [ ] Este documento completo (entenda o contexto)
   - [ ] Seção 8 (Plano de Implementação)

2. **Escolha sua tarefa:**
   - Se não especificado: Comece pela Fase 1, Tarefa REF-002 (mais simples)
   - Se especificado: Vá para a seção indicada

3. **Execute:**
   - Siga "Passos de Implementação para o Agente"
   - Execute cada "Comando de Verificação"
   - Marque cada item da "Checklist"
   - Verifique "Critérios de Sucesso"

4. **Valide:**
   - Testes passam?
   - TypeCheck passa?
   - Lint passa?
   - Funcionalidade operacional?

5. **Finalize:**
   - Commit com mensagem apropriada
   - Avance para próxima tarefa ou peça orientação

### Comandos Essenciais (Resumo)

```bash
# Verificação rápida completa
./gradlew :backend:compileJava && \
./gradlew :backend:test && \
cd frontend && \
npm run typecheck && \
npm run lint

# Se tudo passar, você está pronto para commit
git add .
git commit -m "refactor(modulo): descrição"
git push
```

### Quando Pedir Ajuda

Peça orientação se:
- Testes falharem após 2 tentativas de correção
- Encontrar código que contradiz AGENTS.md
- Não entender uma regra de negócio
- Precisar modificar mais de 10 arquivos para uma tarefa simples
- Encontrar problema de design/arquitetura
- Migration de banco de dados for necessária

### Boas Práticas para Agentes

**FAÇA:**
- ✅ Leia toda a seção relevante antes de começar
- ✅ Execute testes após cada mudança
- ✅ Faça commits pequenos e frequentes
- ✅ Use Português em todo código e mensagens
- ✅ Siga convenções do AGENTS.md religiosamente
- ✅ Documente decisões não óbvias
- ✅ Teste manualmente funcionalidades críticas

**NÃO FAÇA:**
- ❌ Pular comandos de verificação
- ❌ Modificar código não relacionado
- ❌ Criar novas convenções sem aprovação
- ❌ Usar inglês em código/mensagens
- ❌ Fazer commits gigantes
- ❌ Ignorar testes falhando
- ❌ Deixar TODO/FIXME sem documentar

---

## 14. Referências Rápidas

### Estrutura de Arquivos Backend

```
backend/src/main/java/sgc/
├── comum/           # Exceções, DTOs base, utilitários
│   └── erros/       # ErroApi, ErroDadosInvalidos, etc
├── processo/        # Processos (MAPEAMENTO, REVISAO, DIAGNOSTICO)
├── subprocesso/     # Workflows de cada unidade
├── mapa/            # Mapas de competências
├── atividade/       # CRUD de atividades
├── diagnostico/     # [CRIAR NA FASE 2] Diagnósticos
├── unidade/         # Estrutura organizacional
├── notificacao/     # Notificações por email
├── alerta/          # Alertas UI
└── painel/          # Dashboards
```

### Estrutura de Arquivos Frontend

```
frontend/src/
├── components/      # Componentes reutilizáveis
├── views/          # Páginas da aplicação
├── stores/         # Estado global (Pinia)
├── services/       # Comunicação com API
├── router/         # Rotas (Vue Router)
├── utils/          # Funções utilitárias
└── types/          # Definições TypeScript
```

### Exceções Comuns (Backend)

```java
// Usar estas exceções da hierarquia ErroApi
throw new ErroEntidadeNaoEncontrada("Recurso não encontrado");
throw new ErroDadosInvalidos("Dados inválidos: " + detalhes);
throw new ErroOperacaoInvalida("Operação não permitida");
throw new ErroAutenticacao("Não autenticado");
throw new ErroAutorizacao("Sem permissão");
```

### Padrão de Service (Backend)

```java
@Service
@Transactional
public class ExemploService {
    private final ExemploRepo repo;
    
    public ExemploDTO criar(ExemploRequest request) {
        // 1. Validar regras de negócio
        validarRegras(request);
        
        // 2. Criar entidade
        Exemplo exemplo = new Exemplo();
        exemplo.setDescricao(request.getDescricao());
        
        // 3. Persistir
        exemplo = repo.save(exemplo);
        
        // 4. Converter para DTO e retornar
        return ExemploMapper.toDTO(exemplo);
    }
    
    private void validarRegras(ExemploRequest request) {
        // Bean Validation já validou campos obrigatórios
        // Aqui validamos regras de negócio
        if (regra1Violada()) {
            throw new ErroDadosInvalidos("Mensagem clara em português");
        }
    }
}
```

### Padrão de Store (Frontend)

```typescript
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { exemploService } from '@/services/exemploService';

export const useExemploStore = defineStore('exemplo', () => {
  const items = ref<Item[]>([]);
  const loading = ref(false);
  
  async function buscar() {
    loading.value = true;
    try {
      items.value = await exemploService.buscar();
    } finally {
      loading.value = false;
    }
  }
  
  return { items, loading, buscar };
});
```

### Padrão de Service (Frontend)

```typescript
// services/exemploService.ts
import apiClient from './apiClient';

export interface ExemploDTO {
  codigo: number;
  descricao: string;
}

export const exemploService = {
  async buscar(): Promise<ExemploDTO[]> {
    const response = await apiClient.get('/api/exemplos');
    return response.data;
  },
  
  async criar(dados: Partial<ExemploDTO>): Promise<ExemploDTO> {
    const response = await apiClient.post('/api/exemplos', dados);
    return response.data;
  }
};
```

---

**Documento elaborado por:** GitHub Copilot  
**Data de elaboração:** 07 de dezembro de 2025  
**Versão:** 2.0 (Adaptado para Agentes de IA)  
**Status:** Guia de Implementação para Agentes

**Changelog:**
- **v2.0 (07/12/2025):** Adaptado para uso por agentes de IA
  - Adicionadas instruções específicas para agentes
  - Adicionados comandos de verificação detalhados
  - Adicionados critérios de sucesso mensuráveis
  - Adicionadas checklists completas
  - Adicionados passos de implementação detalhados
  - Adicionado guia rápido e referências
- **v1.0 (07/12/2025):** Versão inicial para equipe humana
