---
name: refatoracao-testes
description: Use quando o objetivo for melhorar a qualidade e efetividade dos testes no SGC. Indicado para substituir testes acoplados à implementação (reflexão, métodos privados) por testes comportamentais sobre a API pública, padronizar assertions com AssertJ, remover cenários impossíveis ou redundantes e consolidar arquivos de teste fragmentados.
---

# Refatoração de Testes no SGC

Use este skill para rodadas de melhoria incremental dos testes de backend (JUnit/Mockito) e frontend (Vitest), tratando
qualidade de teste como propriedade de primeiro nível — não como efeito colateral de coverage.

## Fontes de verdade

Antes de refatorar, confirme:

- `AGENTS.md` — convenções do projeto (idioma, nomenclatura, limites de parâmetros)
- `etc/reqs` — comportamentos exigidos que os testes devem continuar cobrindo
- `etc/docs/regras-acesso.md` — regras de acesso que precisam de cobertura comportamental

No SGC, um teste que cobre comportamento real vale mais que dez que testam detalhes de implementação.

## Quando usar

Use este skill quando houver:

- testes que chamam métodos privados via `ReflectionTestUtils.invokeMethod`, `getDeclaredMethod` ou `setAccessible`;
- assertions JUnit (`assertEquals`, `assertTrue`, `assertThrows`) misturadas com AssertJ no mesmo arquivo;
- testes cujo único sinal de falha seria "o método privado mudou de nome";
- cenários de teste que nunca podem ocorrer via API pública (setup impossível, estado nunca alcançável);
- múltiplos arquivos de teste para a mesma classe-alvo com cobertura duplicada;
- testes de cobertura artificial criados apenas para atingir branch coverage (comentário "branch X false");
- assertions fracas: `assertNotNull(resultado)` sem verificar o conteúdo relevante;
- testes que mockam collaborators que já não existem após consolidações arquiteturais.

Não use este skill quando:

- a tarefa principal for simplificar código de produção; nesse caso prefira `simplificacao-codigo`;
- o problema for performance; prefira `otimizacao-por-monitoramento`.

## Objetivo

Aumentar a efetividade dos testes preservando:

- cobertura de comportamentos observáveis pela API pública;
- cenários de erro com significado de negócio (ErroValidacao, ErroAcessoNegado, ErroEntidadeNaoEncontrada);
- contratos HTTP verificados por testes de controller via MockMvc;
- regras de acesso e permissões cobertas por testes comportamentais com mocks de permissão.

## Princípios

1. **Testar contratos, não implementações.**
   Um teste deve quebrar quando o comportamento externo mudar, não quando um detalhe interno for renomeado.

2. **Métodos privados não têm contrato.**
   Se um método privado precisa de teste direto, é sinal de que ele pode ser um collaborator explícito ou que o teste
   comportamental está incompleto. Prefira cobri-lo exercendo a API pública que o chama.

3. **Assertions que informam.**
   `assertThat(resultado.descricao()).isEqualTo("Processo A")` informa o que falhou.
   `assertNotNull(resultado)` não informa nada de útil. Prefira assertions que descrevem o comportamento esperado.

4. **Um arquivo por classe-alvo.**
   Arquivos de teste fragmentados para a mesma classe levam a cobertura duplicada e lacunas não percebidas. Consolide.

5. **Cenários impossíveis geram falsa confiança.**
   Se o setup de um teste requer um estado que nunca pode ser alcançado via API pública, o teste não cobre nenhum risco
   real. Remova-o.

6. **AssertJ como padrão único.**
   Não misture JUnit Assertions com AssertJ no mesmo arquivo. Padronize em AssertJ: `assertThat`, `assertThatThrownBy`,
   `assertThatCode`, `hasMessageContaining`, encadeamento fluente.

7. **Remover importações órfãs.**
   Quando um teste que usava `ReflectionTestUtils` ou `assertThrows` for removido, remova também o import correspondente.

## Bons alvos de refatoração

### Backend (Java/JUnit/Mockito)

- `ReflectionTestUtils.invokeMethod(service, "metodoPrivado", args)` — remover ou substituir por teste via API pública
- `getDeclaredMethod` / `setAccessible` — mesmo tratamento
- `assertEquals(x, y)` onde `y` é resultado de chamada complexa — migrar para `assertThat(y).isEqualTo(x)`
- `assertThrows(X.class, () -> ...)` seguido de verificação do `ex` capturado — unificar em `assertThatThrownBy`
- Testes com comentário `// branch NNN false` ou `// para atingir branch do Jacoco` — avaliar remoção
- Classes `@Nested` cujo único propósito é testar método privado extraível como inner class
- Imports de `org.junit.jupiter.api.Assertions.*` em arquivo que já usa AssertJ

### Frontend (TypeScript/Vitest)

- `vi.spyOn(instance, '_metodoInterno')` — substituir por teste via função pública ou evento
- Testes que importam e testam funções não exportadas (acesso via `__esModule`)
- `expect(x).toBeTruthy()` sem verificar o valor real — migrar para `expect(x).toBe(true)` ou assertion mais específica
- `expect(wrapper.html()).toContain(...)` para verificar lógica de negócio — preferir `wrapper.find(...).exists()` ou
  props/emits

## Alvos ruins (não refatorar desta forma)

- Não substitua teste de método privado por teste de controller só para "ter alguma cobertura" se o caminho público não
  exercita o branch real.
- Não remova cenário de erro com mensagem de negócio só porque ele usa `assertThrows`; migre a assertion, não o cenário.
- Não consolide arquivos de teste se a fusão tornar o arquivo maior que ~600 linhas sem estrutura `@Nested` clara.
- Não troque `verify(mock).metodo(any())` por nada — verificações de colaboração são válidas para comportamentos com
  efeito colateral (salvar, enviar notificação, registrar movimentação).
- Não remova mocks de repositório ou service só porque "parece redundante"; confirme se o caminho ainda é exercitado.

## Fluxo recomendado

### 1. Mapear os problemas reais

Para cada arquivo de teste:

```bash
grep -n "ReflectionTestUtils\|getDeclaredMethod\|setAccessible\|invokeMethod" \
  backend/src/test/**/*.java

grep -n "import static org.junit.jupiter.api.Assertions" \
  backend/src/test/**/*.java
```

Para frontend:
```bash
grep -rn "spyOn.*_\|__esModule\|toBeTruthy\|toBeFalsy" \
  frontend/src/**/*.spec.ts
```

### 2. Classificar cada ocorrência

Para cada ocorrência encontrada, classifique:

- **Privado sem substituto**: método privado que só seria exercitado via reflexão, sem caminho público equivalente →
  **remover**
- **Privado com substituto**: método privado cujo comportamento já é coberto por teste via API pública →
  **remover o teste de reflexão, manter o comportamental**
- **Privado com lacuna real**: método privado com lógica não coberta por nenhum teste comportamental → **adicionar
  teste via API pública que exercite o caminho**
- **Assertion fraca**: `assertNotNull` / `assertTrue(x != null)` / `toBeTruthy()` → **fortalecer assertion**
- **Cenário impossível**: setup que não pode ser alcançado via API pública → **remover**
- **Import órfão**: import de `Assertions.*` ou `ReflectionTestUtils` sem uso restante → **remover**

### 3. Executar a refatoração

Por arquivo, na ordem:
1. Remover testes de método privado (e identificar se geram lacuna)
2. Adicionar testes comportamentais para cobrir lacunas identificadas
3. Migrar assertions JUnit → AssertJ
4. Remover imports órfãos
5. Consolidar arquivos fragmentados da mesma classe, se aplicável

### 4. Validar

```bash
# Backend — compilar apenas os testes primeiro (mais rápido)
./gradlew --no-configuration-cache :backend:compileTestJava

# Backend — rodar só os arquivos alterados
./gradlew --no-daemon --no-configuration-cache :backend:test \
  --tests "sgc.pacote.AlgumTesteAlterado"

# Backend — rodar suite completa ao final
./gradlew --no-daemon --no-configuration-cache :backend:test

# Frontend
npx vitest run <arquivos-alterados> --reporter=dot --no-color
npm run typecheck
npm run lint
```

### 5. Registrar

Ao final da rodada, registre:
- quantos testes foram removidos e por qual razão;
- quantos testes comportamentais foram adicionados;
- se alguma lacuna de cobertura real foi identificada e coberta;
- próximo arquivo ou padrão de maior risco.

## Padrões AssertJ para o SGC

### Exceções de negócio

```java
// Antes (JUnit)
ErroValidacao ex = assertThrows(ErroValidacao.class, () -> service.executar(req));
assertEquals("mensagem esperada", ex.getMessage());

// Depois (AssertJ)
assertThatThrownBy(() -> service.executar(req))
    .isInstanceOf(ErroValidacao.class)
    .hasMessageContaining("mensagem esperada");
```

### Exceções com estado

```java
assertThatThrownBy(() -> service.iniciar(1L, req))
    .isInstanceOf(ErroValidacao.class)
    .satisfies(ex -> {
        ErroValidacao e = (ErroValidacao) ex;
        assertThat(e.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(e.getMessage()).isEqualTo("erro esperado");
    });
```

### Coleções

```java
// Antes
assertEquals(2, resultado.size());
assertTrue(resultado.stream().anyMatch(r -> r.descricao().equals("X")));

// Depois
assertThat(resultado).hasSize(2);
assertThat(resultado).extracting(ResultadoDto::descricao).contains("X");
```

### Booleanos

```java
// Antes
assertTrue(res.habilitarAcessoCadastro());
assertFalse(res.habilitarAcessoMapa());

// Depois
assertThat(res.habilitarAcessoCadastro()).isTrue();
assertThat(res.habilitarAcessoMapa()).isFalse();
```

### Verificação de colaboração (manter com Mockito)

```java
// Manter — verifica efeito colateral real
verify(notificacaoService).enfileirar(argThat(cmd ->
    cmd.destinatario().equals("destino@tre-pe.jus.br")
));
verify(analiseRepo).save(argThat(a -> a.getUnidadeCodigo().equals(1L)));
```

## Perguntas de decisão

- Este teste quebra quando o comportamento externo muda ou apenas quando o nome interno muda?
- O cenário deste teste pode ser atingido via chamada à API pública do service ou controller?
- A assertion atual informa o que falhou, ou só diz "algo falhou"?
- Há um teste comportamental que já cobre o mesmo caminho? (se sim, o teste de reflexão é redundante)
- Ao remover este teste, qual comportamento real fica sem cobertura?
- O arquivo resultado da consolidação ficou legível com `@Nested` por cenário?
- Todos os imports inutilizados foram removidos?

## Guardrails específicos do SGC

- Todo código, mensagem, DisplayName e comentário em **português brasileiro**.
- Use `codigo`, não `id`, mesmo em variáveis de teste.
- Mantenha os nomes de variáveis de teste coerentes com os nomes do domínio (`subprocesso`, `processo`, `unidade`,
  `usuario`).
- Não remova testes de regra de acesso que usam `permissionEvaluator.verificarPermissao` — esses cobrem contratos de
  segurança reais.
- Não remova verificações de `verify(repo).save(...)` para operações de persistência com efeito colateral.
- Se Gradle falhar com erro de cache ao compilar testes, repita com `--no-configuration-cache`.

## Saída esperada

Ao usar este skill, entregue:

- lista dos testes removidos e a razão de cada remoção;
- lista dos testes adicionados ou fortalecidos;
- resultado da suite após as mudanças (N testes passando);
- próximo arquivo ou padrão de maior risco a atacar.
