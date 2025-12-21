# Sprint 3: Fixtures/Builders e Deduplicação de Setup

**Baseado em:** `analise-junit-nova.md` - Onda 3

## Contexto do Projeto SGC

### Arquitetura de Domínio
O SGC possui entidades complexas organizadas por módulos em `backend/src/main/java/sgc/`:
- **processo** - `Processo` (orquestrador)
- **subprocesso** - `Subprocesso`, `Movimentacao` (workflow)
- **mapa** - `Mapa`, `Competencia` 
- **atividade** - `Atividade`, `Conhecimento`
- **analise** - `Analise` (auditoria de revisões)
- **sgrh** - `Usuario`, `Perfil`
- **unidade** - `Unidade` (hierarquia organizacional)
- **alerta** - `Alerta`
- **painel** - Agregações e visualizações

### Problemas Comuns nos Testes
- **Magic numbers**: IDs hardcoded como `99L`, `1L`, `2L`
- **Duplicação**: Mesmo setup de entidades repetido em múltiplos testes
- **Credenciais fixas**: Usuários de teste criados manualmente
- **Builders ad-hoc**: Cada teste constrói objetos de forma diferente

### Padrão Fixture/Builder
Criar classes utilitárias reutilizáveis:
```java
// Exemplo: UsuarioTestFixture.java
public class UsuarioTestFixture {
    public static Usuario usuarioPadrao() {
        return Usuario.builder()
            .cpf("12345678901")
            .nome("Usuario Teste")
            .email("teste@example.com")
            .build();
    }
    
    public static Usuario usuarioComPerfil(Perfil perfil) {
        return usuarioPadrao().toBuilder()
            .perfil(perfil)
            .build();
    }
}
```

### Localização Sugerida
Criar pacote: `backend/src/test/java/sgc/fixture/` ou `backend/src/test/java/sgc/testdata/`

## Objetivo
Reduzir duplicação de código de setup e eliminar "magic numbers" nos testes.

## Tarefas
- Identificar entidades frequentemente usadas nos testes (Processo, Subprocesso, Mapa, Usuario, etc).
- Criar pacote `fixture` ou `testdata` em `backend/src/test/java/sgc/`.
- Implementar builders/factories reutilizáveis para entidades complexas.
- Extrair setup comum duplicado para métodos `@BeforeEach` ou fixtures.
- Substituir magic numbers por constantes com nomes descritivos.
- Atualizar testes para usar fixtures em vez de construção manual.

## Comandos de Verificação

### Identificar magic numbers comuns
```bash
grep -R "99L\|1L\|2L\|3L" backend/src/test --include="*.java" | wc -l
```

### Verificar duplicação de setup (exemplo)
```bash
grep -R "new Usuario" backend/src/test --include="*.java" | wc -l
```

### Executar testes após refatoração
```bash
./gradlew :backend:test
```

### Verificar que fixtures estão sendo usadas
```bash
find backend/src/test/java/sgc/fixture -name "*.java" 2>/dev/null | wc -l
```

## Estrutura Sugerida de Fixtures

```
backend/src/test/java/sgc/
├── fixture/
│   ├── ProcessoFixture.java
│   ├── SubprocessoFixture.java
│   ├── MapaFixture.java
│   ├── AtividadeFixture.java
│   ├── UsuarioFixture.java
│   └── UnidadeFixture.java
```

## Exemplo de Uso em Teste

```java
@BeforeEach
void setUp() {
    usuario = UsuarioFixture.usuarioPadrao();
    processo = ProcessoFixture.processoAtivo();
}

@Test
@DisplayName("Deve criar subprocesso quando processo está ativo")
void deveCriarSubprocessoQuandoProcessoEstaAtivo() {
    // Arrange
    Subprocesso subprocesso = SubprocessoFixture.novo(processo, usuario);
    
    // Act
    var resultado = service.criar(subprocesso);
    
    // Assert
    assertThat(resultado).isNotNull();
}
```

## Critérios de Aceite
- `./gradlew :backend:test` passa sem erros.
- Redução significativa de duplicação nas classes alvo (métricas antes/depois).
- Uso de builders/fixtures em vez de construção manual repetitiva.
- Fixtures criadas em pacote dedicado e reutilizáveis entre testes.

---

## Diretrizes para agentes de IA (Regras de Ouro)

1. **PRs Pequenos:** Um tema por PR.
2. **Critérios Universais de Aceite:**
   - `./gradlew test` (ou `mvn test`) passa.
   - Não aumentar flakiness (nenhum teste novo com `Thread.sleep`).
   - Não reintroduzir `Strictness.LENIENT`.
   - Sem hardcode em integração sem criação explícita.
3. **Não refatorar produção** a menos que estritamente necessário para o teste.

## Guia de Estilo (Obrigatório)

### Estrutura AAA
```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange
    // Act
    // Assert
}
```

### Nomenclatura
- **Método:** `deve{Acao}Quando{Condicao}`
- **Variáveis:** Português, descritivas.
- **Agrupamento:** `@Nested` por feature/fluxo.

### Mockito
- **Proibido:** `Strictness.LENIENT` (padrão).
- **Preferência:** Stubs locais.

## Checklist de Revisão

- [ ] Testes passam local/CI.
- [ ] `LENIENT` não aparece no diff.
- [ ] Não houve adição de `Thread.sleep`.
- [ ] Integração não depende de seed global sem setup explícito.
- [ ] PR descreve comandos executados e métricas simples (grep/contagem de arquivos).
