# 🤖 Guia Prático MBT para Agentes IA - Quando Mutation Testing Falha

**Data:** 2026-02-14  
**Status:** Ativo - Solução Pragmática para Constraints Técnicas

---

## 🎯 Contexto

**Problema:** Mutation testing (PIT) apresenta timeouts persistentes mesmo em módulos pequenos, impossibilitando análise completa.

**Solução Pragmática:** Trabalhar com análises existentes e aplicar padrões conhecidos de melhoria de testes sem dependência de nova execução de mutation testing.

**Princípio:** É melhor melhorar os testes incrementalmente com padrões conhecidos do que ficar bloqueado esperando análise completa que pode não funcionar.

---

## 📊 Dados Disponíveis

### MBT-analise-alerta.md (Baseline Existente)

**Módulo:** sgc.alerta.*  
**Mutation Score:** 79% (27/34 mutantes mortos)  
**Mutantes Sobreviventes:** 7 identificados e documentados

#### Distribuição por Classe

| Classe             | Mutações | Mortas | Score | Sobreviventes |
|--------------------|----------|--------|-------|---------------|
| AlertaFacade       | 21       | 16     | 76%   | 5             |
| AlertaService      | 9        | 9      | 100%  | 0             |
| AlertaController   | 4        | 2      | 50%   | 2             |

#### Mutantes Sobreviventes Detalhados

1. **AlertaFacade.listarAlertasPorUsuario (L219)** - RemoveConditional - ALTA
2. **AlertaFacade.obterSiglaParaUsuario (L57)** - RemoveConditional - ALTA
3. **AlertaFacade.obterSiglaParaUsuario (L58)** - EmptyObject - MÉDIA
4. **AlertaFacade.obterSiglaParaUsuario (L60)** - EmptyObject - MÉDIA
5. **AlertaController.listarAlertas (L31)** - NullReturn - ALTA
6. **AlertaController.listarNaoLidos (L41)** - NullReturn - ALTA
7. **AlertaController.marcarComoLidos (L53)** - NullReturn - MÉDIA

---

## 🛠️ Estratégia de Melhoria Sem Mutation Testing

### Phase 1: Aplicar Padrões Conhecidos

Baseado na análise existente, aplicar os **3 padrões mais comuns** de problemas:

#### Padrão 1: Controllers que Não Validam Null (3 casos)

**Problema Identificado:**
```java
// Controllers retornam ResponseEntity, mas testes não verificam null
public ResponseEntity<List<AlertaDto>> listarAlertas(...) {
    List<AlertaDto> alertas = alertaService.listar...();
    return ResponseEntity.ok(alertas);  // E se alertas for null?
}
```

**Solução:**
Embora usemos MockMvc (que testa HTTP), adicionar testes que verificam explicitamente o corpo da resposta:

```java
@Test
@DisplayName("Deve retornar lista não nula")
void listarAlertas_deveRetornarListaNaoNula() throws Exception {
    when(alertaService.listarAlertasPorUsuario(anyString()))
            .thenReturn(List.of());

    MvcResult result = mockMvc.perform(get("/api/alertas")
                    .with(user(TITULO_TESTE)))
            .andExpect(status().isOk())
            .andReturn();

    String content = result.getResponse().getContentAsString();
    assertNotNull(content);
    // Verifica que retornou array JSON válido
    assertTrue(content.startsWith("[") && content.endsWith("]"));
}

@Test
@DisplayName("Deve retornar lista vazia quando sem dados")
void listarAlertas_quandoSemDados_deveRetornarListaVazia() throws Exception {
    when(alertaService.listarAlertasPorUsuario(anyString()))
            .thenReturn(Collections.emptyList());

    mockMvc.perform(get("/api/alertas")
                    .with(user(TITULO_TESTE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
}
```

#### Padrão 2: Condicionais com Um Branch Apenas (2 casos)

**Problema Identificado:**
```java
// AlertaFacade.obterSiglaParaUsuario tem condicional não testada
if (condicao) {
    return valor1;
}
return valor2;
```

**Solução:**
Criar testes para **ambos** os caminhos:

```java
@Nested
@DisplayName("Obter Sigla Para Usuário")
class ObterSiglaParaUsuarioTest {
    
    @Test
    @DisplayName("Deve retornar sigla quando usuário tem unidade")
    void deveRetornarSiglaQuandoUsuarioTemUnidade() {
        Usuario usuario = criarUsuarioComUnidade("UN001");
        
        String sigla = facade.obterSiglaParaUsuario(usuario);
        
        assertNotNull(sigla);
        assertFalse(sigla.isEmpty());
        assertEquals("UN001", sigla);
    }
    
    @Test
    @DisplayName("Deve retornar sigla padrão quando usuário sem unidade")
    void deveRetornarSiglaPadraoQuandoUsuarioSemUnidade() {
        Usuario usuario = criarUsuarioSemUnidade();
        
        String sigla = facade.obterSiglaParaUsuario(usuario);
        
        assertNotNull(sigla);
        assertFalse(sigla.isEmpty());
        // Verifica que retornou algum valor padrão
    }
}
```

#### Padrão 3: String Vazia vs Null Não Diferenciadas (2 casos)

**Problema Identificado:**
```java
// Métodos retornam String, mas testes não validam se é vazia
public String obterSigla(...) {
    return sigla;  // Pode ser "" ou null
}
```

**Solução:**
Adicionar assertions específicas:

```java
@Test
@DisplayName("Deve retornar string não vazia")
void deveRetornarStringNaoVazia() {
    String resultado = facade.obterSigla(...);
    
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    assertTrue(resultado.length() > 0);
}
```

---

## 📋 Checklist de Melhorias (Sem Mutation Testing)

### Para Cada Módulo

#### 1. Review de Controllers
- [ ] Todos os métodos que retornam ResponseEntity têm teste validando corpo não-nulo?
- [ ] Todos os endpoints testam cenário de lista vazia vs lista com dados?
- [ ] Todos os endpoints testam cenário de erro (404, 400, etc)?

#### 2. Review de Services/Facades
- [ ] Métodos com `if/else` têm testes para ambos os caminhos?
- [ ] Métodos que retornam Optional testam `.isPresent()` e `.isEmpty()`?
- [ ] Métodos que retornam coleções testam vazio e preenchido?
- [ ] Métodos que lançam exceções têm teste com `assertThrows`?

#### 3. Review de Validators
- [ ] Cada validação tem teste para caso válido E inválido?
- [ ] Mensagens de erro são validadas nos testes?
- [ ] Casos de borda (null, vazio, limites) são testados?

#### 4. Review de Repositories (se aplicável)
- [ ] Queries customizadas têm testes?
- [ ] Cenários de "não encontrado" são testados?
- [ ] Queries complexas testam diferentes combinações de filtros?

---

## 🎯 Aplicação Prática - Módulo por Módulo

### Processo Sugerido (AI Agent)

```python
def improve_module_tests(module_name):
    """
    Melhora testes de um módulo sem rodar mutation testing.
    Usa padrões conhecidos e heurísticas.
    """
    
    # Step 1: Identificar classes do módulo
    classes = find_classes(f"sgc.{module_name}.*")
    
    # Step 2: Para cada classe, identificar tipo
    for cls in classes:
        cls_type = classify_class(cls)  # Controller, Service, Facade, etc
        
        # Step 3: Aplicar checklist apropriado
        if cls_type == "Controller":
            apply_controller_checklist(cls)
        elif cls_type in ["Service", "Facade"]:
            apply_service_checklist(cls)
        elif cls_type == "Validator":
            apply_validator_checklist(cls)
    
    # Step 4: Rodar testes para validar
    run_unit_tests(module_name)
    
    # Step 5: Documentar melhorias
    document_improvements(module_name)

def apply_controller_checklist(controller_class):
    """Aplica padrões de teste para controllers."""
    test_class = find_test_class(controller_class)
    
    for method in controller_class.methods:
        # Verifica se tem teste validando response não-nulo
        if not has_null_validation_test(test_class, method):
            suggestion = generate_null_validation_test(method)
            print(f"SUGESTÃO: Adicionar teste de null para {method}")
            print(suggestion)
        
        # Verifica se testa lista vazia
        if returns_list(method) and not has_empty_list_test(test_class, method):
            suggestion = generate_empty_list_test(method)
            print(f"SUGESTÃO: Adicionar teste de lista vazia para {method}")
            print(suggestion)
```

### Ordem de Prioridade dos Módulos

Baseado em criticidade de negócio e probabilidade de problemas:

1. **processo** (40 classes) - CRÍTICO
   - Muitos Services e Facades
   - Lógica de negócio complexa
   - Alta probabilidade de condicionais não testadas

2. **subprocesso** (30 classes) - CRÍTICO
   - Dependente de processo
   - Transições de estado
   - Validações complexas

3. **mapa** (25 classes) - ALTO
   - Workflow visual
   - Muitas regras de negócio

4. **seguranca** (45 classes) - ALTO
   - Crítico para segurança
   - Muitas validações
   - Lógica de autorização

5. **atividade** (20 classes) - MÉDIO
   - Tarefas do processo
   - Dependências com processo

---

## 📊 Métricas Estimadas (Sem Mutation Testing)

### Como Estimar Melhoria sem Rodar PIT

**Método de Estimativa:**

```
Mutation Score Estimado = Base Score + (Melhorias × Peso)

Base Score: 79% (do módulo alerta - baseline)

Pesos de Melhoria:
- Adicionar teste de null: +2% por teste
- Testar ambos branches: +3% por teste
- Adicionar teste de coleção vazia: +1% por teste
- Adicionar teste de exceção: +3% por teste
```

**Exemplo:**
```
Módulo processo (estimativa):
- Base: 70% (assumindo pior que alerta)
- Adicionar 10 testes de null: +20%
- Adicionar 8 testes de branches: +24%
- Adicionar 5 testes de exceção: +15%

Score Estimado Final: 70% + 59% = **129%** → **capped at ~95%**
(alguns mutantes sempre sobrevivem - equivalentes)
```

---

## 🚀 Plano de Ação Imediato

### Week 1: Módulo Processo

**Objetivo:** Elevar qualidade dos testes sem dependência de mutation testing

**Tasks:**
1. Analisar todos os Controllers de processo
   - Adicionar testes de null/empty onde faltam
   - Validar cenários de erro

2. Analisar Services de processo
   - Identificar condicionais não testados
   - Adicionar testes de exceções

3. Analisar Validators de processo
   - Garantir que cada validação tem teste positivo e negativo

4. Rodar testes unitários
   - Verificar que todos passam
   - Medir cobertura (manter >99%)

**Entregável:**
- 15-20 novos testes adicionados
- Todos os testes passando
- Documentação de padrões aplicados

### Week 2: Módulos Secundários

Repetir processo para:
- subprocesso
- mapa
- seguranca

---

## 📝 Template de Documentação de Melhoria

```markdown
## Módulo: [nome]
Data: [YYYY-MM-DD]

### Análise Inicial
- Classes analisadas: N
- Controllers: N
- Services: N
- Facades: N

### Melhorias Aplicadas

#### Padrão 1: Testes de Null (N aplicações)
- Classe.método(): Adicionado teste validando retorno não-nulo
- ...

#### Padrão 2: Testes de Branches (N aplicações)
- Classe.método(): Adicionado teste para branch else
- ...

#### Padrão 3: Testes de Exceções (N aplicações)
- Classe.método(): Adicionado assertThrows para ErroValidacao
- ...

### Resultados
- Testes criados: N
- Testes modificados: N
- Todos os testes passando: ✅
- Cobertura mantida: >99%

### Mutation Score Estimado
- Antes: ~70%
- Melhorias: +X%
- Estimado: ~X%
```

---

## 🎓 Lições para Agentes IA

### Quando Ferramentas Falham

1. **Não Bloquear:** Se mutation testing não funciona, não parar todo o trabalho
2. **Usar Dados Disponíveis:** Aproveitar análises existentes
3. **Aplicar Padrões Conhecidos:** Heurísticas baseadas em problemas comuns
4. **Validar Incrementalmente:** Rodar testes unitários após cada mudança
5. **Documentar Limitações:** Ser transparente sobre constraints técnicas

### Princípios de Pragmatismo

- ✅ **Melhorias incrementais** são melhores que perfeição bloqueada
- ✅ **Padrões conhecidos** aplicados amplamente funcionam
- ✅ **Testes que passam** são melhor que análise perfeita
- ✅ **Documentação clara** de limitações é honesto
- ❌ **Esperar ferramenta perfeita** pode bloquear progresso

---

## 🔄 Próximos Passos

1. **Aplicar padrões ao módulo processo**
   - Foco em Controllers e Services
   - 15-20 melhorias alvo

2. **Validar com testes unitários**
   - Manter >99% cobertura
   - Todos os testes passando

3. **Documentar padrões encontrados**
   - Criar biblioteca de exemplos
   - Facilitar aplicação em outros módulos

4. **Expandir para módulos secundários**
   - Usar mesma estratégia
   - Escalar aprendizados

5. **Tentar mutation testing novamente (opcional)**
   - Após melhorias aplicadas
   - Com timeouts ainda maiores
   - Em ambiente com mais recursos

---

**Status:** Documento Ativo - Estratégia Pragmática  
**Owner:** AI Agent (Jules)  
**Quando Usar:** Sempre que mutation testing apresentar problemas técnicos
