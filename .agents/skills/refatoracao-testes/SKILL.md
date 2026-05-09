---
name: refatoracao-testes
description: Use quando o objetivo for melhorar a qualidade e efetividade dos testes no SGC. Indicado para substituir testes acoplados à implementação (reflexão, métodos privados) por testes comportamentais sobre a API pública, padronizar assertions com AssertJ, remover cenários impossíveis ou redundantes, consolidar arquivos de teste fragmentados e — quando necessário — ajustar o código de produção para aumentar testabilidade.
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

8. **Reflexão persistente sinaliza lacuna de design.**
   Se após classificar uma ocorrência de reflexão você concluir que não existe caminho público para exercitá-la E o
   comportamento coberto é relevante (segurança, negócio), avalie alterar o código de produção antes de aceitar a perda
   de cobertura. Consulte a seção "Alterações em código de produção para testabilidade".

## Bons alvos de refatoração

### Backend (Java/JUnit/Mockito)

- `ReflectionTestUtils.invokeMethod(service, "metodoPrivado", args)` — remover ou substituir por teste via API pública
- `getDeclaredMethod` / `setAccessible` — mesmo tratamento
- `ReflectionTestUtils.getField(objeto, "campoPrivado")` para inspecionar ou injetar estado interno — avaliar se o
  design de produção precisa de ajuste (construtor package-private, collaborator injetável)
- `ReflectionTestUtils.setField(bean, "propriedade", valor)` para simular configuração — preferir
  `@TestPropertySource` ou `@SpringBootTest(properties = "...")` quando a propriedade já é externalizável
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

## Alterações em código de produção para testabilidade

Às vezes o problema não está no teste, mas no código de produção que não foi projetado para ser testado sem reflexão.
Quando a classificação de uma ocorrência de reflexão resultar em "Privado com lacuna real e sem caminho público
viável", considere aplicar um dos padrões abaixo antes de aceitar a perda de cobertura.

### Critérios para alterar o código de produção

Só altere produção para testabilidade quando **todas** as condições forem verdadeiras:

1. O comportamento coberto tem relevância real (segurança, regra de negócio, resiliência).
2. Não existe caminho público que exercite o mesmo branch sem reflexão.
3. A mudança não aumenta a complexidade acidental do código (não adiciona abstrações sem benefício de design).
4. A mudança é pequena e localizada (construtor adicional, propriedade externalizável).

### Padrão 1 — Construtor package-private para testes

Quando uma classe precisa de parâmetros internos configuráveis (limites, relógios, seeds), adicione um construtor
package-private que receba esses parâmetros. O construtor público normal permanece para uso em produção via Spring.

**Exemplo do SGC — `LimitadorTentativasLogin`:**

```java
// Construtor público — usado pelo Spring
@Autowired
public LimitadorTentativasLogin(Environment environment, Clock clock) { ... }

// Construtor package-private — usado apenas em testes unitários
LimitadorTentativasLogin(Environment environment, int maxCacheEntries, Clock clock) { ... }
```

```java
// No teste: cria instância com limite pequeno para testar evicção de cache
LimitadorTentativasLogin limitador = new LimitadorTentativasLogin(environment, 5, clock);
```

Use este padrão para: `Clock`, limites numéricos, tamanhos de cache, timeouts.
Não use para: substituir beans Spring inteiros, simular infraestrutura (banco, fila).

### Padrão 2 — Propriedade externalizável via `@TestPropertySource`

Quando `ReflectionTestUtils.setField(bean, "propriedade", valor)` é usado para variar uma configuração lida via
`@Value`, converta a propriedade para externalizável e use `@TestPropertySource` ou
`@SpringBootTest(properties = "...")` no teste.

**Antes (reflexão):**
```java
ReflectionTestUtils.setField(loginController, "cookieSecure", true);
invokeMethod(loginController, "adicionarCookieJwt", response, "token");
```

**Depois (propriedade externalizável + MockMvc):**
```java
@WebMvcTest(LoginController.class)
@TestPropertySource(properties = "aplicacao.cookies.secure=true")
class LoginControllerCookieSecureTest {
    // testa comportamento via endpoint, não via método privado
}
```

Use quando: a propriedade já tem `@Value` com chave nomeada e o comportamento afetado por ela é relevante para testes.
Não use quando: a propriedade é apenas detalhe operacional sem impacto em comportamento testável.

### Padrão 3 — Método package-private observável

Quando um estado interno precisa ser observado para verificar um invariante de negócio (ex: tamanho de cache, número
de conexões abertas), adicione um método package-private **somente leitura** que exponha esse estado.

**Exemplo do SGC — `LimitadorTentativasLogin`:**

```java
// Método package-private — visível apenas para testes no mesmo pacote
int getCacheSize() {
    return tentativasPorIp.size();
}
```

```java
// No teste:
assertThat(limitador.getCacheSize()).isEqualTo(5);
```

**Guardrails:**
- Só exponha estado que representa um invariante verificável de fora (tamanho, contagem).
- Nunca exponha o estado interno diretamente (não retorne `Map<String, Deque<...>>`).
- Não adicione setters package-private — setters tornam o estado mutável por testes, que é pior que reflexão.

### Padrão 4 — Collaborator injetável

Quando um método privado contém lógica com efeitos colaterais observáveis mas não testáveis via API pública, extraia
esse comportamento como um collaborator injetável via construtor.

**Quando usar:** a classe tem lógica de "envio" ou "remoção" que só pode ser verificada inspecionando estado interno
com reflexão (ex: `RegistroSseEmitter.transmitir` remover emitters com falha).

**Exemplo conceitual:**

```java
// Antes: transmitir() chama emitter.send() diretamente — não testável sem HTTP real
public void transmitir(String evento) {
    for (SseEmitter emitter : emissores) {
        try { emitter.send(...); }
        catch (IOException e) { emissores.remove(emitter); }
    }
}

// Depois: injeta um Sender funcional — testável com lambda mock
@FunctionalInterface
interface SseSender {
    void send(SseEmitter emitter, String evento) throws IOException;
}

// Construtor package-private para testes
RegistroSseEmitter(SseSender sender) { this.sender = sender; }
```

Use este padrão com moderação: só quando o comportamento é relevante E não existe outra forma. Evite criar interfaces
só para satisfazer testes se o design de produção não se beneficiar da abstração.

### O que NÃO fazer para aumentar testabilidade

- **Não torne métodos privados públicos** só para testá-los diretamente.
- **Não adicione getters para campos internos** (ex: `getEmissores()`) — isso expõe implementação.
- **Não crie interfaces só para mock** (ex: `ILoginController`) sem benefício de design real.
- **Não injete dependências desnecessárias** só para poder substituí-las em testes.
- **Não altere a visibilidade de campos** de `private` para `protected` ou package — use construtor.

## Fluxo recomendado

### 1. Mapear os problemas reais

Para cada arquivo de teste:

```bash
grep -rn "ReflectionTestUtils\|getDeclaredMethod\|setAccessible\|invokeMethod" \
  backend/src/test --include="*.java"

grep -rn "import static org.junit.jupiter.api.Assertions" \
  backend/src/test --include="*.java"
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
- **Privado com lacuna real e caminho público viável**: método privado com lógica não coberta, mas exercitável pela
  API pública → **adicionar teste via API pública que exercite o caminho**
- **Privado com lacuna real e sem caminho público**: comportamento relevante (segurança/negócio) que não pode ser
  exercitado sem reflexão → **avaliar alteração no código de produção** (ver seção acima)
- **Configuração via `setField`**: `ReflectionTestUtils.setField` para variar propriedade `@Value` →
  **converter para `@TestPropertySource` ou propriedade nomeada**
- **Assertion fraca**: `assertNotNull` / `assertTrue(x != null)` / `toBeTruthy()` → **fortalecer assertion**
- **Cenário impossível**: setup que não pode ser alcançado via API pública → **remover**
- **Import órfão**: import de `Assertions.*` ou `ReflectionTestUtils` sem uso restante → **remover**

### 3. Executar a refatoração

Por arquivo, na ordem:
1. Remover testes de método privado (e identificar se geram lacuna)
2. Se lacuna real sem caminho público: avaliar alteração em produção primeiro
3. Adicionar testes comportamentais para cobrir lacunas identificadas
4. Migrar assertions JUnit → AssertJ
5. Remover imports órfãos
6. Consolidar arquivos fragmentados da mesma classe, se aplicável

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
- se algum código de produção foi alterado para testabilidade e qual padrão foi aplicado;
- próximo arquivo ou padrão de maior risco.

## Lições aprendidas — casos reais do SGC

### `RegistroSseEmitter` — estado interno não acessível sem reflexão

**Problema:** os testes originais usavam `ReflectionTestUtils.getField(registroSseEmitter, "emissores")` para
injetar mocks na lista interna e verificar remoções. Ao tentar substituir por testes comportamentais, descobriu-se
que `SseEmitter.complete()` e `completeWithError()` lançam `IllegalStateException` sem backing HTTP real —
impossibilitando exercitar o callback `onError` em testes unitários puros.

**Decisão tomada:** os testes de remoção foram simplificados para verificar apenas que `transmitir()` não lança
exceção — aceitando perda parcial de cobertura por não haver alteração de produção no momento.

**Alternativa preferível (não implementada nesta rodada):** adicionar construtor package-private em
`RegistroSseEmitter` que aceite um `SseSender` funcional, permitindo injetar um sender mock que simule `IOException`
sem precisar de `SseEmitter` real com backing HTTP.

### `LoginController` — `@Value` cookieSecure não variável em MockMvc

**Problema:** testes originais usavam `ReflectionTestUtils.setField(loginController, "cookieSecure", true/false)`
para testar os dois branches do flag de segurança dos cookies. Via MockMvc, o campo assume sempre o valor do
`application.properties` (ou o default `false`), não sendo possível testar `cookieSecure=true` sem reflexão.

**Decisão tomada:** o teste de `cookieSecure=false` foi mantido via MockMvc (verifica `cookie.getSecure() == false`).
O teste de `cookieSecure=true` foi deixado como lacuna a cobrir com `@TestPropertySource`.

**Alternativa preferível:** criar uma classe `@WebMvcTest` separada anotada com
`@TestPropertySource(properties = "aplicacao.cookies.secure=true")` para cobrir o branch `true` sem reflexão.

### `LimitadorTentativasLogin` — `ReflectionTestUtils.setField` para trocar `Environment`

**Problema:** `SecurityVulnerabilityIntegrationTest` usava reflexão para substituir o bean `environment` do
`LimitadorTentativasLogin` e forçar o limitador a funcionar em perfil de integração. Isso acoplava o teste à
estrutura interna do componente.

**Decisão tomada:** o teste de integração foi removido porque o mesmo comportamento (cache cheio + novo IP) já
estava coberto por `LimitadorTentativasLoginTest` em nível unitário, usando o construtor package-private com
`maxCacheEntries` configurável.

**Lição:** quando um teste de integração usa reflexão para simular condições de ambiente, verifique primeiro se um
teste unitário com construtor package-private não cobre o mesmo invariante de forma mais simples e confiável.

### `E2eControllerTest` — `getDeclaredMethod` em método privado de fixture

**Problema:** dois testes usavam `getDeclaredMethod("criarProcessoFixture")` + `setAccessible(true)` para exercitar
um método privado de infraestrutura de testes (não de produção). O comportamento já era coberto indiretamente pelos
testes públicos `deveCriarProcessoMapeamentoFixtureIniciado` e `deveCriarProcessoRevisaoFixture`.

**Decisão tomada:** testes removidos — eram redundantes com os testes via API pública do controller.

**Lição:** verifique se o método privado que está sendo testado via reflexão é de produção ou de fixture de teste.
Se for de fixture, a cobertura extra não tem valor e pode ser removida sem reservas.

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
- O comportamento sem cobertura é relevante o suficiente para justificar alterar o código de produção?
- Se sim, qual dos padrões de testabilidade (construtor package-private, `@TestPropertySource`, collaborator) é o
  menos invasivo para o código de produção?
- O arquivo resultado da consolidação ficou legível com `@Nested` por cenário?
- Todos os imports inutilizados foram removidos?

## Guardrails específicos do SGC

- Todo código, mensagem, DisplayName e comentário em **português brasileiro** correto (incluindo acentos).
- Use `codigo`, não `id`, mesmo em variáveis de teste.
- Mantenha os nomes de variáveis de teste coerentes com os nomes do domínio (`subprocesso`, `processo`, `unidade`,
  `usuario`).
- Não remova testes de regra de acesso que usam `permissionEvaluator.verificarPermissao` — esses cobrem contratos de
  segurança reais.
- Não remova verificações de `verify(repo).save(...)` para operações de persistência com efeito colateral.
- Se Gradle falhar com erro de cache ao compilar testes, repita com `--no-configuration-cache`.
- Ao alterar código de produção para testabilidade: não altere visibilidade de campos, não adicione setters, mantenha
  construtores package-private sem `@Autowired`.

## Saída esperada

Ao usar este skill, entregue:

- lista dos testes removidos e a razão de cada remoção;
- lista dos testes adicionados ou fortalecidos;
- lista de alterações em código de produção para testabilidade (se houver), com o padrão aplicado;
- resultado da suite após as mudanças (N testes passando);
- lacunas de cobertura identificadas e não cobertas nesta rodada (com proposta de como cobrir);
- próximo arquivo ou padrão de maior risco a atacar.
