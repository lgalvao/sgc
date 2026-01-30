# 📊 Plano de Melhorias - Sistema SGC

**Data:** 2026-01-30  
**Executor:** Análise Aprofundada por Antigravity (Atualizado)  
**Contexto:** Sistema com ~500 usuários totais, máximo 10 usuários simultâneos. Refatorações recentes em Controllers e Pacotes já foram aplicadas.

---

## 🎯 Sumário Executivo

Este documento apresenta uma análise aprofundada do código do backend (Java/Spring Boot) e frontend (Vue 3/TypeScript) do Sistema de Gestão de Competências (SGC), identificando problemas de **duplicação**, **inconsistências**, **excesso de código defensivo** e **complexidade desnecessária** acumulados ao longo de múltiplas rodadas de melhorias realizadas por IAs.

### Princípios Norteadores

Dado o contexto de uso (500 usuários, máx. 10 simultâneos), as melhorias devem focar em:

1. ✅ **Simplicidade** sobre otimização prematura
2. ✅ **Consistência** sobre diversidade de padrões
3. ✅ **Clareza** sobre abstrações complexas
4. ✅ **Manutenibilidade** como objetivo primário

### Resumo de Achados

| Categoria | Quantidade | Severidade | Impacto |
|-----------|-----------|------------|---------|
| **Duplicação de Código (Backend)** | 50+ ocorrências | 🔴 CRÍTICA | Alta manutenção, bugs duplicados |
| **Violações de ADRs** | 24+ pontos | 🔴 CRÍTICA | Inconsistência arquitetural |
| **GOD Classes/Composables** | 5+ classes | 🔴 CRÍTICA | Testabilidade zero, acoplamento alto |
| **Código Defensivo Excessivo** | 30+ ocorrências | 🟠 MÉDIA | Verbosidade, confusão |
| **Testes Duplicados/Inadequados** | 58+ arquivos | 🔴 CRÍTICA | Manutenção alta, baixa confiança |
| **Inconsistências Frontend** | 3 padrões diferentes | 🟠 MÉDIA | Curva de aprendizado alta |
| **Padrões de Organização** | 4 inconsistências | 🟠 MÉDIA | Navegação difícil |

---

## 📖 Índice

1. [Análise do Backend](#1-análise-do-backend)
   - 1.1 [Duplicação de Código](#11-duplicação-de-código)
   - 1.2 [Problemas Arquiteturais](#12-problemas-arquiteturais)
   - 1.3 [Violações de ADRs](#13-violações-de-adrs)
   - 1.4 [Código Defensivo Excessivo](#14-código-defensivo-excessivo)
2. [Análise do Frontend](#2-análise-do-frontend)
   - 2.1 [Duplicação de Código](#21-duplicação-de-código)
   - 2.2 [Problemas Arquiteturais](#22-problemas-arquiteturais)
   - 2.3 [Inconsistências](#23-inconsistências)
3. [Análise de Testes](#3-análise-de-testes)
4. [Plano de Ação Prioritizado](#4-plano-de-ação-prioritizado)
5. [Benefícios Esperados](#5-benefícios-esperados)

---

## 1. Análise do Backend

### 1.1 Duplicação de Código

#### 1.1.1 Lógica de Validação Espalhada

**Problema:** Embora a `SubprocessoFacade` delegue corretamente para serviços especializados (Workflows), a validação de estado (ex: `PENDENTE`) está fragmentada entre os Services de Workflow e as Access Policies.

```java
// Repetido em iniciar(), concluir(), cancelar(), reabrir(), etc.
Subprocesso subprocesso = subprocessoRepo.findByCodigo(codigo)
    .orElseThrow(() -> new ErroNegocio("Subprocesso não encontrado"));

if (subprocesso.getStatus() != StatusSubprocesso.PENDENTE) {
    throw new ErroNegocio("Subprocesso deve estar PENDENTE");
}
```

**Impacto:**
- 🔴 Duplicação em 8+ métodos
- Mudanças de regra requerem alteração em múltiplos pontos
- Alto risco de inconsistência

**Solução:** Extrair para método privado `validarSubprocessoPendente(codigo)`.

---

#### 1.1.2 Access Policies com Lógica Duplicada (Hierarquia/Titularidade)

**Problema:** `SubprocessoAccessPolicy` e `AtividadeAccessPolicy` (e outras) reimplementam a mesma lógica complexa de verificação de hierarquia e titularidade (`TITULAR_UNIDADE`). A `AbstractAccessPolicy` existe mas não centraliza essas regras comuns adequadamente.

> **Nota:** A `ProcessoAccessPolicy` foi refatorada e simplificada, não apresentando mais este problema. O foco agora é na duplicação entre Subprocesso e Atividade.

```java
// SubprocessoAccessPolicy.java
private boolean verificarHierarquia(Usuario usuario, String codigoUnidade) { ... }

// AtividadeAccessPolicy.java - LÓGICA DE 'TITULAR_UNIDADE' DUPLICADA
```

**Impacto:**
- 🔴 Código duplicado mantendo regras de negócio críticas
- Risco de inconsistência em regras de acesso
- Manutenção duplicada

**Solução:** Mover lógica comum de hierarquia e titularidade para `AbstractAccessPolicy`.

---

#### 1.1.3 Padrão "do*" Desnecessário em AlertaFacade

**Problema:** Métodos `doSomething()` que apenas delegam para `something()` sem lógica adicional.

```java
public void criarAlerta(AlertaRequest request) {
    doCrearAlerta(request);
}

private void doCrearAlerta(AlertaRequest request) {
    // lógica real aqui
}
```

**Impacto:**
- 🟠 Indireção desnecessária em 6+ métodos
- Confusão sobre qual método chamar
- Dificulta navegação no código

**Solução:** Remover indireção, mover lógica para método público.

---

#### 1.1.4 DTOs Similares por Domínio

**Problema:** 4 DTOs quase idênticos por domínio (Request, Response, Command, View).

```java
// ProcessoRequest
public class ProcessoRequest {
    private String titulo;
    private String descricao;
    private String codigoUnidade;
}

// ProcessoCommand - ESTRUTURA IDÊNTICA
public record ProcessoCommand(
    String titulo,
    String descricao,
    String codigoUnidade
) {}
```

**Impacto:**
- 🟠 ~16 DTOs com estrutura similar (4 domínios × 4 DTOs)
- Mapeamento complexo entre tipos similares
- Violação potencial do princípio YAGNI

**Solução:** Consolidar DTOs onde não há diferença semântica real.

---

#### 1.1.5 Verificações Null Repetidas

**Problema:** Padrão de verificação null antes de cada operação, mesmo com Bean Validation.

```java
@NotNull
private String titulo;

// Mas depois no código:
if (request.getTitulo() == null) {
    throw new IllegalArgumentException("Título é obrigatório");
}
```

**Impacto:**
- 🟠 Duplicação de validação (Bean Validation + código)
- Confusão sobre onde validar
- 30+ ocorrências

**Solução:** Confiar no Bean Validation, remover verificações redundantes.

---

### 1.2 Problemas Arquiteturais

#### 1.2.1 ProcessoFacade com 13 Dependências

**Problema:** Facade com número excessivo de dependências injetadas.

```java
@Service
public class ProcessoFacade {
    private final ProcessoService processoService;
    private final SubprocessoService subprocessoService;
    private final AtividadeService atividadeService;
    private final MapaService mapaService;
    private final AccessControlService accessControlService;
    private final AuditService auditService;
    private final NotificacaoService notificacaoService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProcessoMapper processoMapper;
    private final SubprocessoMapper subprocessoMapper;
    private final AtividadeMapper atividadeMapper;
    private final MapaMapper mapaMapper;
    private final ValidacaoService validacaoService;
    // 13 dependências!
}
```

**Impacto:**
- 🔴 GOD Class com responsabilidades demais
- Difícil de testar (13 mocks necessários)
- Violação do Single Responsibility Principle

**Solução:** Refatorar em múltiplas Facades menores (ProcessoCadastroFacade, ProcessoMapaFacade, ProcessoValidacaoFacade).

---

#### 1.2.2 Organização de Pacotes Inconsistente (✅ RESOLVIDO)

**Status:** ✅ **RESOLVIDO**. O pacote `subprocesso` foi reorganizado seguindo uma estrutura clara (`api`, `service`, `model`, `dto`), alinhando-se melhor com o restante do projeto.

**Ação:** Manter o padrão atual para novos módulos.

---

#### 1.2.3 Ciclos de Dependência entre Domínios

**Problema:** Dependências circulares entre módulos de domínio.

```
Processo → Subprocesso → Atividade → Processo
```

**Impacto:**
- 🔴 Acoplamento alto
- Dificulta refatoração
- Problemas de inicialização Spring

**Solução:** Usar Spring Events para comunicação unidirecional (já implementado parcialmente, falta consistência).

---

#### 1.2.4 Código Defensivo em Camadas Erradas

**Problema:** Validações de negócio no Controller ao invés de Service.

```java
@RestController
public class ProcessoController {
    @PostMapping
    public ProcessoResponse criar(@RequestBody ProcessoRequest request) {
        // Validação de negócio no Controller! ❌
        if (request.getTitulo().length() < 5) {
            throw new ErroNegocio("Título muito curto");
        }
        return facade.criar(request);
    }
}
```

**Impacto:**
- 🟠 Responsabilidade na camada errada
- Dificulta reutilização
- Testes duplicados

**Solução:** Mover validações de negócio para Service layer.

---

### 1.3 Violações de ADRs

#### 1.3.1 Violação ADR-001 (Facade Pattern)

**Problema:** 12 métodos em Facades com `@PreAuthorize`, violando ADR-001.

```java
// ProcessoFacade.java - VIOLAÇÃO!
@PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
public ProcessoResponse criar(ProcessoRequest request) {
    // ...
}
```

**ADR-001 especifica:** Controllers usam APENAS Facades, nunca Services especializados diretamente.

**Impacto:**
- 🔴 Violação em 12+ métodos
- Inconsistência arquitetural
- Confusão sobre onde aplicar segurança

**Solução:** Mover `@PreAuthorize` para Controllers, Facades delegam para AccessControlService.

---

#### 1.3.2 Violação ADR-003 (Security Architecture)

**Problema:** Verificações de acesso diretas em Services ao invés de usar AccessControlService.

```java
// SubprocessoService.java - VIOLAÇÃO!
public void iniciar(String codigo, Usuario usuario) {
    if (!usuario.hasRole("GESTOR")) {  // ❌ Verificação direta
        throw new ErroAcesso("Acesso negado");
    }
    // ...
}
```

**ADR-003 especifica:** Services NUNCA fazem verificações de acesso diretas. Use `AccessControlService.verificarPermissao()`.

**Impacto:**
- 🔴 Violação em 8+ métodos
- Bypass da auditoria centralizada
- Políticas de acesso inconsistentes

**Solução:** Refatorar para usar AccessControlService em todos os pontos.

---

#### 1.3.3 Violação ADR-004 (DTO Pattern)

**Problema:** Entidades JPA expostas diretamente em APIs REST.

```java
// AnaliseController.java - VIOLAÇÃO!
@GetMapping("/{codigo}")
public Analise buscar(@PathVariable String codigo) {
    return analiseService.buscar(codigo);  // ❌ Retorna entidade JPA!
}
```

**ADR-004 especifica:** DTOs obrigatórios em TODAS as APIs REST. Entidades JPA NUNCA são expostas.

**Impacto:**
- 🔴 Violação em 2 controllers (AnaliseController, ConfiguracaoController)
- Exposição de detalhes de persistência
- Lazy loading exceptions em produção

**Solução:** Criar DTOs Response e Mappers para todos os endpoints.

---

#### 1.3.4 Violação ADR-005 (Controller Organization) (✅ RESOLVIDO)

**Status:** ✅ **RESOLVIDO**. O `ProcessoController` foi refatorado e agora possui ~280 linhas, delegando lógica de inicialização para Strategies e lógica de negócio para a `ProcessoFacade`. A estrutura está limpa e coesa.

**Ação:** Garantir que novos controllers sigam este exemplo (ex: `Subprocesso` já está dividido).

---

### 1.4 Código Defensivo Excessivo

#### 1.4.1 Verificações Redundantes

**Problema:** Múltiplas verificações do mesmo campo em sequência.

```java
if (titulo == null) throw new IllegalArgumentException();
if (titulo.isEmpty()) throw new IllegalArgumentException();
if (titulo.isBlank()) throw new IllegalArgumentException();
// .isBlank() já cobre todos os casos acima!
```

**Impacto:**
- 🟠 15+ ocorrências
- Verbosidade desnecessária
- Confusão

**Solução:** Usar apenas a verificação mais abrangente.

---

#### 1.4.2 Try-Catch Genéricos

**Problema:** Blocos try-catch que capturam Exception genérica e re-lançam.

```java
try {
    return service.executar();
} catch (Exception e) {
    throw new ErroNegocio(e.getMessage());  // Perde stack trace!
}
```

**Impacto:**
- 🟠 10+ ocorrências
- Perda de contexto de erro
- Debugging difícil

**Solução:** Capturar exceções específicas ou deixar propagar naturalmente.

---

## 2. Análise do Frontend

### 2.1 Duplicação de Código

#### 2.1.1 Validação Duplicada em subprocessos.ts

**Problema:** Função de validação idêntica em múltiplos pontos.

```typescript
// subprocessos.ts
function validarSubprocesso(subprocesso: Subprocesso): boolean {
  if (!subprocesso.titulo || subprocesso.titulo.trim() === '') return false;
  if (!subprocesso.descricao) return false;
  return true;
}

// subprocessoForm.ts - CÓDIGO IDÊNTICO
function validarSubprocesso(subprocesso: Subprocesso): boolean {
  if (!subprocesso.titulo || subprocesso.titulo.trim() === '') return false;
  if (!subprocesso.descricao) return false;
  return true;
}
```

**Impacto:**
- 🔴 Duplicação em 5+ arquivos
- Inconsistência de validação
- Bugs duplicados

**Solução:** Extrair para `@/utils/validations/subprocessoValidation.ts`.

---

#### 2.1.2 Padrão Loading Repetido

**Problema:** Padrão de controle de loading idêntico em todas as stores.

```typescript
// processoStore.ts
const loading = ref(false);
async function carregar() {
  loading.value = true;
  try {
    await api.get();
  } finally {
    loading.value = false;
  }
}

// subprocessoStore.ts - CÓDIGO IDÊNTICO
const loading = ref(false);
async function carregar() {
  loading.value = true;
  try {
    await api.get();
  } finally {
    loading.value = false;
  }
}
```

**Impacto:**
- 🔴 Repetido em 8+ stores
- 40+ linhas de código duplicado
- Manutenção complexa

**Solução:** Criar composable `useLoading()` reutilizável.

---

#### 2.1.3 Padrão de Limpeza State Duplicado

**Problema:** Lógica de reset de estado repetida em cada store.

```typescript
function limpar() {
  processos.value = [];
  selecionado.value = null;
  erro.value = null;
  loading.value = false;
}
```

**Impacto:**
- 🟠 Repetido em todas as stores
- Esquecimento de limpar campos
- Bugs de state residual

**Solução:** Padronizar com factory function ou reset pattern.

---

#### 2.1.4 Formatação de Data Duplicada

**Problema:** Funções de formatação de data repetidas em componentes.

```typescript
// ProcessoCard.vue
function formatarData(data: string) {
  return new Date(data).toLocaleDateString('pt-BR');
}

// SubprocessoCard.vue - CÓDIGO IDÊNTICO
function formatarData(data: string) {
  return new Date(data).toLocaleDateString('pt-BR');
}
```

**Impacto:**
- 🟠 Repetido em 12+ componentes
- Inconsistência de formato
- Internacionalização difícil

**Solução:** Usar `@/utils/formatters.ts` existente.

---

#### 2.1.5 Tratamento de Erro Duplicado

**Problema:** Lógica de normalização de erro repetida.

```typescript
// Repetido em múltiplos services
function normalizarErro(erro: unknown): string {
  if (axios.isAxiosError(erro)) {
    return erro.response?.data?.message || erro.message;
  }
  return String(erro);
}
```

**Impacto:**
- 🟠 Repetido em 6+ services
- Inconsistência de mensagens
- Duplicação de ~30 linhas

**Solução:** Usar `normalizeError()` de `@/utils` (já existe, falta adoção).

---

### 2.2 Problemas Arquiteturais

#### 2.2.1 GOD Composables

**Problema:** Composables com 200+ linhas contendo múltiplas responsabilidades.

```typescript
// useCadAtividadesLogic.ts - 245 linhas!
export function useCadAtividadesLogic() {
  // Lógica de formulário (50 linhas)
  // Lógica de validação (40 linhas)
  // Lógica de API (30 linhas)
  // Lógica de navegação (25 linhas)
  // Lógica de alertas (20 linhas)
  // Lógica de modal (30 linhas)
  // Lógica de busca (25 linhas)
  // Lógica de filtros (25 linhas)
}
```

**Impacto:**
- 🔴 5+ composables > 200 linhas
- Impossível testar isoladamente
- Violação do Single Responsibility Principle

**Solução:** Dividir em composables menores e focados (useAtividadeForm, useAtividadeValidation, useAtividadeApi).

---

#### 2.2.2 Acesso Inconsistente a Services

**Problema:** 4 padrões diferentes de acesso a serviços.

```typescript
// Padrão 1: Import direto
import { processoService } from '@/services';

// Padrão 2: Via store
const store = useProcessoStore();
store.carregar();

// Padrão 3: Composable
const { carregar } = useProcesso();

// Padrão 4: Injeção
const service = inject('processoService');
```

**Impacto:**
- 🔴 Inconsistência em toda a aplicação
- Curva de aprendizado alta
- Dificuldade de refatoração

**Solução:** Padronizar: **View → Store → Service → API** (conforme AGENTS.md).

---

#### 2.2.3 Acoplamento Store ↔ Store

**Problema:** Stores acessando outras stores diretamente.

```typescript
// processoStore.ts
import { useSubprocessoStore } from './subprocesso';

function criarProcesso() {
  // ...
  const subStore = useSubprocessoStore();  // ❌ Acoplamento direto
  subStore.carregar();
}
```

**Impacto:**
- 🔴 Dependências circulares
- Dificulta testes
- Violação de responsabilidades

**Solução:** Usar Events ou Composables como mediadores.

---

#### 2.2.4 Mistura de Responsabilidades em Views

**Problema:** Views com lógica de negócio complexa ao invés de apenas apresentação.

```typescript
// ProcessoView.vue
<script setup>
// 150+ linhas de lógica de validação, cálculos, transformações
// Deveria estar em composable ou store!
</script>
```

**Impacto:**
- 🟠 8+ views com lógica complexa
- Testabilidade zero
- Reutilização impossível

**Solução:** Extrair lógica para composables/stores, manter views "burras".

---

#### 2.2.5 Props Drilling Excessivo

**Problema:** Props passadas através de 4+ níveis de componentes.

```
ProcessoView
  → ProcessoContainer (passa props)
    → ProcessoForm (passa props)
      → ProcessoFields (passa props)
        → ProcessoInput (finalmente usa!)
```

**Impacto:**
- 🟠 Manutenção complexa
- Componentes intermediários com props desnecessárias
- Refatoração difícil

**Solução:** Usar provide/inject ou store para dados compartilhados.

---

### 2.3 Inconsistências

#### 2.3.1 Três Estratégias Diferentes de Erro

**Problema:** Tratamento de erro inconsistente na aplicação.

```typescript
// Estratégia 1: BAlert inline
<BAlert v-if="erro" variant="danger">{{ erro }}</BAlert>

// Estratégia 2: Toast global
toast.error(erro);

// Estratégia 3: Modal de erro
showErrorModal(erro);
```

**Impacto:**
- 🟠 UX inconsistente
- Confusão sobre qual usar
- Documentação confusa

**Solução:** Definir regra clara: BAlert inline para erros de negócio, Toast para erros de sistema.

---

#### 2.3.2 console.warn ao Invés de logger

**Problema:** Uso de console.* ao invés do logger estruturado.

```typescript
// ❌ ERRADO
console.warn('Erro ao carregar:', erro);
console.log('Usuário logado:', usuario);

// ✅ CORRETO
logger.warn('Erro ao carregar:', erro);
logger.info('Usuário logado:', usuario);
```

**Impacto:**
- 🟠 36+ ocorrências
- Logs não estruturados
- Dificulta debugging em produção

**Solução:** Substituir console.* por logger, habilitar ESLint rule.

---

#### 2.3.3 Convenções de Nomenclatura Inconsistentes

**Problema:** Mistura de padrões de nomenclatura.

```typescript
// processoStore.ts
const processos = ref([]);  // plural ✅
const processo = ref(null); // singular ✅

// subprocessoStore.ts
const lista = ref([]);      // genérico ❌
const item = ref(null);     // genérico ❌
```

**Impacto:**
- 🟠 Navegação confusa
- Autocomplete menos útil
- Código menos autodocumentado

**Solução:** Padronizar nomenclatura específica de domínio.

---

#### 2.3.4 Importações Relativas vs Absolutas

**Problema:** Mistura de importações relativas e absolutas.

```typescript
// Arquivo 1
import { ProcessoService } from '@/services';  // absoluto ✅

// Arquivo 2
import { ProcessoService } from '../../services';  // relativo ❌
```

**Impacto:**
- 🟠 Inconsistência em toda a base
- Refatoração arriscada
- Dificuldade de leitura

**Solução:** Padronizar importações absolutas com `@/` (já configurado).

---

## 3. Análise de Testes

### 3.1 Testes de "Cobertura Artificial"

**Problema:** 27+ arquivos com padrão `*CoverageTest.java` (ex: `SubprocessoFacadeCoverageTest`) que testam apenas getters, setters e construtores para inflar métricas, sem validar comportamento real.

```java
// ProcessoCoverageTest.java
@Test
void testGettersSetters() {
    Processo p = new Processo();
    p.setTitulo("teste");
    assertEquals("teste", p.getTitulo());
}

// ProcessoEntityTest.java - TESTE DUPLICADO
@Test
void testTitulo() {
    Processo p = new Processo();
    p.setTitulo("teste");
    assertEquals("teste", p.getTitulo());
}
```

**Impacto:**
- 🔴 27+ arquivos de "cobertura artificial"
- Métricas infladas que mascaram a falta de testes reais
- Manutenção desnecessária de código sem valor

**Solução:** Remover arquivos `*CoverageTest.java` imediatamente para expor a cobertura real.

---

### 3.2 Over-Mocking

**Problema:** 46 arquivos de teste mockam TUDO, até comportamentos triviais.

```java
@Test
void testCriarProcesso() {
    when(processoRepo.save(any())).thenReturn(processo);  // OK
    when(processo.getCodigo()).thenReturn("123");         // ❌ Desnecessário!
    when(processo.getTitulo()).thenReturn("Teste");       // ❌ Desnecessário!
    when(mapper.toDto(any())).thenReturn(dto);            // OK
}
```

**Impacto:**
- 🔴 Testes frágeis
- Refatoração quebra testes
- Testes testam mocks, não código real

**Solução:** Mockar apenas dependências externas (repos, APIs), usar objetos reais para POJOs/DTOs.

---

### 3.3 Múltiplos Asserts por Teste

**Problema:** Testes com 5-10 assertions, dificultando identificação de falhas.

```java
@Test
void testCriarProcesso() {
    ProcessoResponse response = service.criar(request);
    
    assertNotNull(response);
    assertEquals("Teste", response.getTitulo());
    assertEquals("Descrição", response.getDescricao());
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    assertNotNull(response.getDataCriacao());
    assertEquals("UN001", response.getCodigoUnidade());
    assertTrue(response.getSubprocessos().isEmpty());
    // ... mais 3 assertions
}
```

**Impacto:**
- 🟠 Dificulta identificação do que falhou
- Violação do princípio "one concept per test"
- 35+ testes afetados

**Solução:** Dividir em múltiplos testes focados.

---

### 3.4 Testes Testam Implementação

**Problema:** Testes verificam detalhes de implementação ao invés de comportamento.

```java
@Test
void testIniciarProcesso() {
    service.iniciar(codigo);
    
    verify(repo, times(1)).findByCodigo(codigo);  // ❌ Implementação!
    verify(repo, times(1)).save(any());           // ❌ Implementação!
    verify(eventPublisher, times(1)).publishEvent(any()); // ❌ Implementação!
}
```

**Impacto:**
- 🟠 Refatoração quebra testes
- Testes não garantem comportamento correto
- 40+ testes afetados

**Solução:** Testar comportamento observável (estado final, exceções, retornos).

---

### 3.5 Setup Repetido em Testes E2E

**Problema:** 36 testes E2E repetem o mesmo setup.

```typescript
// processo.spec.ts
test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.fill('#usuario', 'admin');
  await page.fill('#senha', 'admin');
  await page.click('#entrar');
  await page.waitForURL('/processos');
});

// subprocesso.spec.ts - SETUP IDÊNTICO
test.beforeEach(async ({ page }) => {
  await page.goto('/login');
  await page.fill('#usuario', 'admin');
  await page.fill('#senha', 'admin');
  await page.click('#entrar');
  await page.waitForURL('/processos');
});
```

**Impacto:**
- 🔴 Duplicação em 36 arquivos
- Mudança de login requer 36 alterações
- Suíte lenta

**Solução:** Criar fixtures reutilizáveis (já existe `e2e/fixtures/`, falta adoção).

---

### 3.6 Ausência de Testes de Integração

**Problema:** Apenas testes unitários (mocked) e E2E, faltam testes de integração.

```
Unitários (mocked) ───────── E2E
         ↑                    ↑
         └── GAP AQUI! ───────┘
      (testes de integração)
```

**Impacto:**
- 🟠 Integração entre camadas não testada
- Bugs descobertos apenas em E2E
- Feedback lento

**Solução:** Adicionar testes de integração com `@SpringBootTest` e banco H2.

---

## 4. Plano de Ação Prioritizado

### Prioridade CRÍTICA (Imediata ~62h)

#### Ações de Ganho Rápido (Quick Wins) e Segurança

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 1 | **Remover arquivos `*CoverageTest.java`** (27+ arquivos) | 3.1 | 2h | Visão real da cobertura (Immediate Win) |
| 2 | Consolidar Access Policies em AbstractAccessPolicy | 1.1.2 | 6h | Segurança robusta e sem duplicação |
| 3 | Dividir GOD Composables (ex: `useCadAtividadesLogic`) | 2.2.1 | 8h | Frontend testável e manutenível |
| 4 | Refatorar `SubprocessoFacade` e centralizar validações | 1.1.1, 1.2.1 | 8h | Arquitetura limpa |
| 5 | Mover @PreAuthorize de Facades para Controllers | 1.3.1 | 6h | Conformidade ADR-001 |
| 6 | Centralizar verificações de acesso via AccessControlService | 1.3.2 | 8h | Conformidade ADR-003 |
| 7 | Criar DTOs para AnaliseController e ConfiguracaoController | 1.3.3 | 4h | Conformidade ADR-004 |
| 8 | Eliminar ciclos de dependência via Events | 1.2.3 | 2h | Reduz acoplamento |
| 9 | Padronizar acesso a services (View→Store→Service→API) | 2.2.2 | 4h | Consistência arquitetural |
| 10 | Substituir console.* por logger | 2.3.2 | 3h | Logs estruturados |
| 11 | Adotar fixtures E2E (36 arquivos) | 3.5 | 6h | Reduz duplicação 90% |
| 12 | Reduzir over-mocking (46 arquivos) | 3.2 | 5h | Testes mais robustos |

**Total CRÍTICA: ~62h**

---

### Prioridade MÉDIA (~52h)

#### Backend (28h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 14 | Remover padrão "do*" em AlertaFacade (6 métodos) | 1.1.3 | 2h | Simplifica código |
| 15 | Consolidar DTOs similares por domínio | 1.1.4 | 8h | Reduz 16 DTOs |
| 16 | Remover verificações null redundantes (30 ocorrências) | 1.1.5, 1.4.1 | 4h | Reduz verbosidade |
| 17 | Padronizar estrutura de pacotes | 1.2.2 | 6h | Navegação consistente |
| 18 | Dividir Controllers grandes (ADR-005) | 1.3.4 | 6h | Conformidade ADR-005 |
| 19 | Refatorar try-catch genéricos (10 ocorrências) | 1.4.2 | 2h | Melhor debugging |

#### Frontend (18h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 20 | Criar composable useLoading() | 2.1.2 | 3h | Reduz 40 linhas |
| 21 | Padronizar reset de state em stores | 2.1.3 | 4h | Evita bugs state residual |
| 22 | Adotar formatters centralizados (12 componentes) | 2.1.4 | 2h | Consistência de formato |
| 23 | Adotar normalizeError() em services (6 arquivos) | 2.1.5 | 2h | Reduz 30 linhas |
| 24 | Extrair lógica de views para composables (8 views) | 2.2.4 | 5h | Melhora testabilidade |
| 25 | Definir estratégia de erro padrão | 2.3.1 | 2h | UX consistente |

#### Testes (6h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 26 | Dividir testes com múltiplos asserts (35 testes) | 3.3 | 4h | Debugging mais fácil |
| 27 | Refatorar testes que testam implementação (40 testes) | 3.4 | 2h | Testes mais robustos |

**Total MÉDIA: 52h**

---

### Prioridade BAIXA (~19h)

#### Backend (8h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 28 | Mover validações de negócio de Controllers para Services | 1.2.4 | 4h | Arquitetura correta |
| 29 | Documentar exceções nos JavaDocs | - | 4h | Melhor documentação |

#### Frontend (6h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 30 | Padronizar nomenclatura em stores | 2.3.3 | 2h | Navegação consistente |
| 31 | Padronizar importações absolutas com @/ | 2.3.4 | 2h | Refatoração segura |
| 32 | Refatorar props drilling com provide/inject | 2.2.5 | 2h | Simplifica componentes |

#### Testes (5h)

| # | Ação | Problema | Estimativa | Impacto |
|---|------|----------|------------|---------|
| 33 | Adicionar testes de integração (Backend) | 3.6 | 5h | Melhor cobertura |

**Total BAIXA: 19h**

---

### Resumo do Plano

| Prioridade | Ações | Estimativa | % Total |
|-----------|-------|------------|---------|
| 🔴 CRÍTICA | 13 | 60h | 45.8% |
| 🟠 MÉDIA | 14 | 52h | 39.7% |
| 🟡 BAIXA | 6 | 19h | 14.5% |
| **TOTAL** | **33** | **131h** | **100%** |

**Estimativa:** ~4-5 semanas para 1 desenvolvedor em tempo integral (considerando 25-30h úteis/semana).

---

## 5. Benefícios Esperados

### 5.1 Manutenibilidade

- ✅ **Redução de 40-50% em código duplicado**
  - Backend: -150 linhas de duplicação
  - Frontend: -100 linhas de duplicação
  
- ✅ **Conformidade arquitetural com ADRs**
  - 100% dos Controllers seguindo ADR-001 (Facade Pattern)
  - 100% dos acessos seguindo ADR-003 (Security Architecture)
  - 100% das APIs usando DTOs (ADR-004)
  - Controllers organizados por fase (ADR-005)

- ✅ **Redução de complexidade ciclomática**
  - ProcessoFacade: 13→4 dependências (-69%)
  - 5 GOD Composables divididos em 15 focados

---

### 5.2 Testabilidade

- ✅ **Melhoria de cobertura efetiva**
  - Remoção de 27+ arquivos de teste artificiais
  - Redução de over-mocking em 46 arquivos
  - Adoção de fixtures E2E em 36 arquivos

- ✅ **Testes mais robustos**
  - Testes de comportamento vs. implementação
  - Redução de falsos positivos
  - Feedback mais rápido

- ✅ **Introdução de testes de integração**
  - Cobertura da camada de integração
  - Detecção precoce de bugs

---

### 5.3 Consistência

- ✅ **Padrões unificados**
  - 1 estratégia de acesso a services (vs. 4)
  - 1 estratégia de erro (vs. 3)
  - Nomenclatura consistente
  - Estrutura de pacotes padronizada

- ✅ **Documentação automática**
  - Código autodocumentado
  - Swagger mais organizado
  - JavaDocs completos

---

### 5.4 Performance de Desenvolvimento

- ✅ **Onboarding mais rápido**
  - Arquitetura clara e consistente
  - Menos padrões para aprender
  - Navegação intuitiva

- ✅ **Refatoração segura**
  - Menos acoplamento
  - Testes confiáveis
  - Importações absolutas

- ✅ **Debugging facilitado**
  - Logs estruturados
  - Stack traces completos
  - Erros específicos

---

### 5.5 Qualidade de Código

- ✅ **Redução de bugs**
  - Menos duplicação = menos bugs duplicados
  - Validação centralizada
  - Políticas de acesso consistentes

- ✅ **Menor dívida técnica**
  - Conformidade com ADRs
  - Código defensivo apropriado
  - Responsabilidades claras

---

## 📌 Próximos Passos

1. **Aprovação do Plano:** Revisar e aprovar este documento
2. **Priorização Final:** Ajustar prioridades conforme necessidade do negócio
3. **Execução Incremental:** Implementar em sprints de 1-2 semanas
4. **Validação Contínua:** Executar testes após cada mudança
5. **Documentação:** Atualizar ADRs e READMEs conforme mudanças

---

**Documento gerado em:** 2026-01-30  
**Próxima revisão:** Após implementação de ações CRÍTICAS
