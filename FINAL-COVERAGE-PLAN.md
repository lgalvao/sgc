# 🎯 Plano Final de Cobertura de Testes - SGC Backend

**Data:** 2026-02-01  
**Responsável:** Equipe de Desenvolvimento  
**Status:** 📊 Análise Completa - Pronto para Execução Final

---

## ✅ Conquistas Atuais

### Métricas Alcançadas

| Métrica | Meta | Atual | Status | Delta |
|---------|------|-------|--------|-------|
| **BRANCH** | ≥90% | **93.98%** | ✅ **ATINGIDA!** | +3.98% acima da meta |
| **LINE** | ≥99% | **96.63%** | 🟡 Progresso | Faltam 2.37% |
| **INSTRUCTION** | ≥99% | **96.42%** | 🟡 Progresso | Faltam 2.58% |

### Qualidade dos Testes
- ✅ **1379 testes** executando com sucesso (100% taxa de sucesso)
- ✅ **Tempo de execução:** ~88s (dentro do esperado)
- ✅ **Cobertura de branch superior à meta:** 93.98% vs 90%
- ✅ **4 services com 100% de cobertura branch**
- ✅ **Nenhum teste falhando**

---

## 🎯 Objetivo Final

**Meta:** Atingir **≥99% Line Coverage** e **≥99% Instruction Coverage**

**Gap Atual:**
- Line: 2.37% (~106 linhas não cobertas)
- Instruction: 2.58% (~716 instruções não cobertas)

**Estratégia:** Focar nos **10 arquivos de maior impacto** que, juntos, representam **~99 linhas** não cobertas (~2.21% de ganho potencial).

---

## 📋 Arquivos Prioritários

### 🔴 Prioridade CRÍTICA (64 linhas = 1.43% ganho)

#### 1. SubprocessoContextoService
- **Package:** `sgc.subprocesso.service`
- **Coverage Atual:** 60.8% line (20 linhas perdidas)
- **Métodos Não Cobertos:**
  - `obterDetalhes(Long, Usuario)` - branch de erro ao buscar titular
  - `obterCadastro(Long)` - loop de atividades com conhecimentos
  - `obterSugestoes(Long)` - retorno básico
  - `obterContextoEdicao(Long)` - integração completa
- **Testes Necessários:** 8-10 testes
- **Complexidade:** MÉDIA
- **Arquivo de Teste:** Criar `SubprocessoContextoServiceTest.java`

#### 2. SubprocessoFactory  
- **Package:** `sgc.subprocesso.service.factory`
- **Coverage Atual:** 78.7% line (20 linhas perdidas)
- **Métodos Não Cobertos:**
  - Factory methods para tipos específicos de processo
  - Inicialização de situações específicas
  - Validações de tipo de processo
- **Testes Necessários:** 6-8 testes
- **Complexidade:** MÉDIA
- **Arquivo de Teste:** Melhorar `SubprocessoFactoryTest.java`

#### 3. AtividadeFacade
- **Package:** `sgc.mapa.service`
- **Coverage Atual:** 82.4% line (12 linhas perdidas)
- **Métodos Não Cobertos:**
  - CRUD operations edge cases
  - Validações de negócio
- **Testes Necessários:** 4-6 testes
- **Complexidade:** BAIXA
- **Arquivo de Teste:** Criar `AtividadeFacadeTest.java`

#### 4. SubprocessoAjusteMapaService
- **Package:** `sgc.subprocesso.service`
- **Coverage Atual:** 81.3% line (12 linhas perdidas)
- **Métodos Não Cobertos:**
  - Ajustes pós-validação
  - Sincronização de competências
- **Testes Necessários:** 4-6 testes
- **Complexidade:** MÉDIA
- **Arquivo de Teste:** Criar `SubprocessoAjusteMapaServiceTest.java`

---

### 🟡 Prioridade ALTA (21 linhas = 0.47% ganho)

#### 5. MapaManutencaoService
- **Package:** `sgc.mapa.service`
- **Coverage Atual:** 94.5% line (8 linhas perdidas)
- **Testes Necessários:** 3-4 testes adicionais para edge cases
- **Arquivo de Teste:** Melhorar `MapaManutencaoServiceTest.java`

#### 6. ImpactoMapaService
- **Package:** `sgc.mapa.service`
- **Coverage Atual:** 94.7% line (7 linhas perdidas)
- **Testes Necessários:** 3-4 testes adicionais
- **Arquivo de Teste:** Melhorar `ImpactoMapaServiceTest.java`

#### 7. SubprocessoAtividadeService
- **Package:** `sgc.subprocesso.service`
- **Coverage Atual:** 88.2% line (6 linhas perdidas)
- **Testes Necessários:** 2-3 testes adicionais
- **Arquivo de Teste:** Criar `SubprocessoAtividadeServiceTest.java`

---

### 🟢 Prioridade MÉDIA - Quick Wins (14 linhas = 0.31% ganho)

#### 8. E2eController
- **Package:** `sgc.e2e`
- **Coverage Atual:** 93.6% line (6 linhas perdidas)
- **Testes Necessários:** 2-3 testes adicionais
- **Arquivo de Teste:** Melhorar `E2eControllerTest.java`

#### 9. SubprocessoCadastroController
- **Package:** `sgc.subprocesso`
- **Coverage Atual:** 91.8% line (5 linhas perdidas)
- **Testes Necessários:** 2-3 testes adicionais
- **Arquivo de Teste:** Melhorar `SubprocessoCadastroControllerTest.java`

#### 10. SubprocessoMapaController
- **Package:** `sgc.subprocesso`
- **Coverage Atual:** 90.3% line (3 linhas perdidas)
- **Testes Necessários:** 1-2 testes adicionais
- **Arquivo de Teste:** Melhorar `SubprocessoMapaControllerTest.java` (já existe)

---

## 📊 Projeção de Ganhos

| Fase | Arquivos | Linhas | Ganho % | Coverage Projetado |
|------|----------|--------|---------|-------------------|
| **Inicial** | - | - | - | **96.63%** |
| **Fase 1** (Crítica) | 4 | 64 | +1.43% | **98.06%** |
| **Fase 2** (Alta) | 3 | 21 | +0.47% | **98.53%** |
| **Fase 3** (Quick Wins) | 3 | 14 | +0.31% | **98.84%** |
| **Total Estimado** | **10** | **99** | **+2.21%** | **~98.84%** |

**Gap Residual:** ~0.16% (pode ser preenchido com testes adicionais em outros arquivos)

---

## 🚀 Plano de Execução

### Sessão 1: Preparação (30 min)
- [ ] Revisar este documento
- [ ] Configurar ambiente de desenvolvimento
- [ ] Verificar que todos os 1379 testes atuais passam
- [ ] Gerar coverage report inicial

### Sessão 2: Fase Crítica - Parte 1 (2-3 horas)
- [ ] Implementar `SubprocessoContextoServiceTest` (8-10 testes)
- [ ] Validar cobertura aumentou ~0.45%
- [ ] Commit e push

### Sessão 3: Fase Crítica - Parte 2 (2-3 horas)
- [ ] Melhorar `SubprocessoFactoryTest` (6-8 testes adicionais)
- [ ] Validar cobertura aumentou ~0.45%
- [ ] Commit e push

### Sessão 4: Fase Crítica - Parte 3 (1-2 horas)
- [ ] Implementar `AtividadeFacadeTest` (4-6 testes)
- [ ] Implementar `SubprocessoAjusteMapaServiceTest` (4-6 testes)
- [ ] Validar cobertura atingiu ~98%
- [ ] Commit e push

### Sessão 5: Fase Alta (1-2 horas)
- [ ] Melhorar `MapaManutencaoServiceTest` (3-4 testes)
- [ ] Melhorar `ImpactoMapaServiceTest` (3-4 testes)
- [ ] Criar `SubprocessoAtividadeServiceTest` (2-3 testes)
- [ ] Validar cobertura ~98.5%
- [ ] Commit e push

### Sessão 6: Quick Wins & Finalização (1 hora)
- [ ] Melhorar testes de controllers (6-8 testes totais)
- [ ] Executar coverage report final
- [ ] Verificar meta de 99% atingida
- [ ] Atualizar `coverage-tracking.md`
- [ ] Commit final

---

## 📝 Template de Teste (Exemplo)

```java
@Tag("unit")
@DisplayName("NomeDoService")
@ExtendWith(MockitoExtension.class)
class NomeDoServiceTest {
    
    @Mock
    private DependenciaService dependencia;
    
    @InjectMocks
    private NomeDoService service;
    
    @Nested
    @DisplayName("nomeDoMetodo")
    class NomeDoMetodoTests {
        
        @Test
        @DisplayName("deve executar caso de sucesso básico")
        void deveExecutarCasoSucessoBasico() {
            // Arrange
            when(dependencia.metodo()).thenReturn(resultado);
            
            // Act
            var resultado = service.nomeDoMetodo(param);
            
            // Assert
            assertThat(resultado).isNotNull();
            verify(dependencia).metodo();
        }
        
        @Test
        @DisplayName("deve lançar exceção quando condição inválida")
        void deveLancarExcecaoQuandoCondicaoInvalida() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> service.nomeDoMetodo(null))
                .isInstanceOf(ErroNegocio.class)
                .hasMessageContaining("mensagem esperada");
        }
    }
}
```

---

## ✅ Critérios de Sucesso

### Mínimo Aceitável
- ✅ Branch Coverage ≥90% (já atingido: 93.98%)
- ⏳ Line Coverage ≥99%
- ⏳ Instruction Coverage ≥99%
- ✅ 100% dos testes passando

### Ideal
- ✅ Branch Coverage ≥95% (quase: 93.98%)
- ⏳ Line Coverage ≥99.5%
- ⏳ Instruction Coverage ≥99.5%
- ✅ Tempo de execução <2min
- ✅ Testes bem organizados com `@Nested` e `@DisplayName`

---

## 🎓 Lições Aprendidas

### O Que Funcionou Bem
1. ✅ Uso de `@Nested` para organização de testes
2. ✅ Builders de teste (`UsuarioTestBuilder`, `UnidadeTestBuilder`)
3. ✅ Exclusão de classes sem lógica de negócio (DTOs, Enums, Config)
4. ✅ Foco em testes de comportamento, não implementação
5. ✅ Coverage tracking incremental

### Desafios Identificados
1. 🔴 Entities JPA não têm setters públicos (usar builders ou construtores)
2. 🔴 Records em DTOs requerem acesso sem prefixo `get`
3. 🔴 Alguns services são complexos e requerem muitos mocks

### Recomendações Futuras
1. 📌 Criar builders de teste para todas as entidades principais
2. 📌 Manter cobertura >95% em PRs futuros
3. 📌 Executar `jacocoTestReport` em CI/CD
4. 📌 Documentar casos de teste complexos

---

## 📞 Contatos e Recursos

- **Plano Detalhado:** [test-coverage-plan.md](test-coverage-plan.md)
- **Tracking de Progresso:** [coverage-tracking.md](coverage-tracking.md)
- **Guia de Boas Práticas:** [backend/etc/docs/GUIA-MELHORIAS-TESTES.md](backend/etc/docs/GUIA-MELHORIAS-TESTES.md)

**Comandos Úteis:**
```bash
# Executar apenas testes unitários
./gradlew :backend:test

# Executar testes e gerar relatório de cobertura
./gradlew :backend:test :backend:jacocoTestReport

# Ver relatório HTML
open backend/build/reports/jacoco/test/html/index.html

# Verificar metas de cobertura (falhará se não atingir)
./gradlew :backend:jacocoTestCoverageVerification
```

---

**Última Atualização:** 2026-02-01  
**Próxima Revisão:** Após conclusão da Fase 1  
**Responsável:** Equipe de Desenvolvimento
