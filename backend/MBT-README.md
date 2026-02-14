# 🧬 Mutation-Based Testing (MBT) - Documentação

**Sistema de Gestão de Conhecimento (SGC) - Backend**

---

## 📚 Documentos Disponíveis

Este diretório contém toda a documentação relacionada a Mutation-Based Testing (MBT) no SGC.

### 📖 Documentos Principais

| Documento                                        | Descrição                                      | Quando Usar                          |
|--------------------------------------------------|------------------------------------------------|--------------------------------------|
| **[MBT-plan.md](MBT-plan.md)**                   | Plano completo de implementação em 6 fases     | Entender estratégia e roadmap        |
| **[MBT-quickstart.md](MBT-quickstart.md)**       | Guia rápido para desenvolvedores               | Começar a usar MBT no dia a dia      |
| **[MBT-baseline.md](MBT-baseline.md)**           | Baseline inicial e resultados da amostra       | Entender situação atual              |
| **[MBT-progress.md](MBT-progress.md)**           | Rastreamento de progresso por sprint           | Acompanhar evolução                  |

---

## 🚀 Quick Start

### Para Desenvolvedores

1. **Ler primeiro:** [MBT-quickstart.md](MBT-quickstart.md)
2. **Rodar mutation testing:**
   ```bash
   cd backend
   ./gradlew mutationTestModulo -PtargetModule=processo
   ```
3. **Ver relatório:** Abrir `backend/build/reports/pitest/index.html`

### Para Gestores/Tech Leads

1. **Ler primeiro:** [MBT-plan.md](MBT-plan.md)
2. **Acompanhar progresso:** [MBT-progress.md](MBT-progress.md)
3. **Revisar baseline:** [MBT-baseline.md](MBT-baseline.md)

---

## 🎯 O que é MBT?

**Mutation-Based Testing (MBT)** é uma técnica que avalia a **qualidade dos testes** introduzindo pequenas mudanças (mutações) no código-fonte e verificando se os testes detectam essas mudanças.

### Por que MBT?

O SGC tem **100% de cobertura de código** (JaCoCo), mas os testes foram gerados por IA e podem não ser efetivos:

```java
// ❌ Este teste dá 100% de cobertura, mas não valida nada!
@Test
void testCriar() {
    service.criar(request);  // Linha executada ✓
    // Sem assertions - código pode estar quebrado e teste passa!
}

// ✅ Este teste é efetivo (mutation score ~95%)
@Test
void deveCriarComStatusPendente() {
    ProcessoResponse response = service.criar(request);
    
    assertNotNull(response);
    assertEquals(StatusProcesso.PENDENTE, response.getStatus());
    assertEquals(request.getTitulo(), response.getTitulo());
}
```

**MBT detecta testes ineficazes** que passam mesmo quando o código está incorreto.

---

## 📊 Status Atual

### Baseline (2026-02-14)

| Métrica                    | Valor                  | Status |
|----------------------------|------------------------|--------|
| **Configuração PIT**       | ✅ Concluída           | ✅      |
| **Mutation Score (Amostra)**| 79% (módulo alerta)   | 🟡     |
| **Meta Global**            | >85%                   | 🎯     |
| **Fase Atual**             | Sprint 1 - Baseline    | 🟡     |

### Próximos Passos

1. ⏳ Executar análise completa do projeto
2. ⏳ Categorizar mutantes sobreviventes (A/B/C/D)
3. ⏳ Priorizar top 20 mutantes críticos
4. ⏳ Implementar melhorias no módulo processo

---

## 🛠️ Comandos Disponíveis

```bash
# Mutation testing completo (~2-4h)
./gradlew mutationTest

# Por módulo (~2-5min) - RECOMENDADO
./gradlew mutationTestModulo -PtargetModule=processo

# Incremental - apenas mudanças (~1-3min)
./gradlew mutationTestIncremental
```

**Módulos disponíveis:**
- `processo`, `subprocesso`, `mapa`, `atividade`
- `alerta`, `organizacao`, `notificacao`, `analise`, `seguranca`

---

## 📈 Roadmap

### Fase 1: Configuração ✅ (Concluída)
- ✅ PIT configurado
- ✅ Tarefas Gradle criadas
- ✅ Documentação completa
- ✅ Análise de amostra

### Fase 2: Baseline ⏳ (Em Andamento)
- ⏳ Análise completa
- ⏳ Categorização de mutantes
- ⏳ Priorização de ações

### Fase 3: Melhorias Processo 🔜 (Próxima)
- 🔜 Corrigir mutantes categoria A
- 🔜 Elevar score para >80%

### Fase 4: Expansão 🔜
- 🔜 Melhorias em outros módulos
- 🔜 Score global >85%

### Fase 5: Refinamento 🔜
- 🔜 Mutadores STRONGER
- 🔜 Otimização de performance

### Fase 6: CI/CD 🔜
- 🔜 Integração ao pipeline
- 🔜 Thresholds automatizados

---

## 🎓 Recursos de Aprendizado

### Documentação Interna

- [MBT-plan.md](MBT-plan.md) - Seção "O que é MBT?"
- [MBT-quickstart.md](MBT-quickstart.md) - Exemplos práticos
- [GUIA-MELHORIAS-TESTES.md](etc/docs/GUIA-MELHORIAS-TESTES.md) - Padrões de teste

### Recursos Externos

- [PIT Official Documentation](https://pitest.org/)
- [Mutation Testing: A Comprehensive Survey](https://pitest.org/quickstart/basic_concepts/)
- [JUnit 5 + PIT Integration](https://pitest.org/quickstart/junit5/)

---

## 📊 Métricas e KPIs

### Métricas Rastreadas

1. **Mutation Score Global** - Percentual de mutantes mortos
2. **Mutation Score por Módulo** - Score de cada módulo
3. **Mutantes por Categoria** - A (crítico) até D (baixo)
4. **Tempo de Execução** - Performance do MBT
5. **Cobertura de Teste** - JaCoCo (mantida em 99%)

### Metas

| Métrica                | Meta Curto Prazo (1 mês) | Meta Longo Prazo (2 meses) |
|------------------------|--------------------------|----------------------------|
| Mutation Score Global  | >75%                     | >85%                       |
| Módulos >85% Score     | >40%                     | >75%                       |
| Tempo Execução Full    | <30min                   | <20min                     |

---

## 🐛 Troubleshooting

### Problema: Build falha com "Java 21 required"

**Solução:**
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
./gradlew mutationTest
```

### Problema: Mutation testing muito lento

**Solução:**
1. Use `mutationTestModulo` para módulos específicos
2. Use `mutationTestIncremental` para mudanças recentes
3. Evite `mutationTest` completo durante desenvolvimento

### Problema: Como interpretar mutation score baixo?

**Resposta:**
- **<70%:** Testes ineficazes, muitas melhorias necessárias
- **70-84%:** Bom, mas precisa melhorias
- **≥85%:** Excelente, testes robustos

Veja [MBT-quickstart.md](MBT-quickstart.md) para padrões de correção.

---

## 🤝 Contribuindo

### Como Melhorar os Testes

1. **Executar MBT:**
   ```bash
   ./gradlew mutationTestModulo -PtargetModule=<seu-modulo>
   ```

2. **Analisar relatório:**
   - Abrir `backend/build/reports/pitest/index.html`
   - Identificar linhas vermelhas (mutantes sobreviventes)

3. **Corrigir testes:**
   - Adicionar assertions faltantes
   - Testar casos de erro (null, vazio, exceções)
   - Testar ambos os caminhos (if/else)

4. **Validar:**
   ```bash
   ./gradlew mutationTestModulo -PtargetModule=<seu-modulo>
   # Score deve aumentar
   ```

5. **Commitar:**
   - Incluir mutation score no PR description
   - Mencionar mutantes corrigidos

### Code Review Checklist

- [ ] Mutation score >85% no módulo modificado
- [ ] Novos testes têm assertions efetivas
- [ ] Casos de erro estão cobertos
- [ ] Ambos os caminhos de condicionais estão testados

---

## 📞 Suporte

### Canais

- **Dúvidas Técnicas:** Slack #backend-quality
- **Documentação:** Este repositório
- **Issues:** GitHub Issues com tag `mutation-testing`

### Contatos

- **Tech Lead:** [Nome] - Revisão de qualidade
- **Engineering Manager:** [Nome] - Aprovação de metas

---

## 📝 Changelog

### 2026-02-14 - v1.0

- ✅ PIT configurado (v1.18.1)
- ✅ Tarefas Gradle criadas
- ✅ Documentação completa criada
- ✅ Análise de amostra executada (alerta module: 79%)
- ✅ Baseline estabelecido

---

## 🎯 TL;DR

**Para começar agora:**

1. **Ler:** [MBT-quickstart.md](MBT-quickstart.md) (5 minutos)
2. **Rodar:**
   ```bash
   cd backend
   ./gradlew mutationTestModulo -PtargetModule=processo
   ```
3. **Ver relatório:** `backend/build/reports/pitest/index.html`
4. **Melhorar testes** onde houver mutantes sobreviventes (vermelho)

**Meta:** Mutation score >85% = Testes robustos e confiáveis

---

**Última Atualização:** 2026-02-14  
**Versão:** 1.0  
**Status:** ✅ Documentação Completa - Pronto para Uso
