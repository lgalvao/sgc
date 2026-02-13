# 🎯 Plano para Alcançar 100% de Cobertura de Testes

**Gerado em:** 2026-02-13

## 📊 Situação Atual

- **Cobertura Global de Linhas:** 100.00%
- **Cobertura Global de Branches:** 99.67%
- **Total de Arquivos Analisados:** 236
- **Arquivos com Cobertura < 100%:** 2
- **Arquivos com 100% de Cobertura:** 234

## 🎯 Objetivo

Alcançar **100% de cobertura** em todas as classes relevantes do projeto.

## 📋 Progresso por Categoria

- **🔴 CRÍTICO - Lógica de negócio central:** 0 arquivo(s) pendente(s)
- **🟡 IMPORTANTE - API e transformação de dados:** 0 arquivo(s) pendente(s)
- **🟢 NORMAL - Entidades e utilitários:** 2 arquivo(s) pendente(s)

---

## 🔴 CRÍTICO - Lógica de negócio central

✅ **Todos os arquivos desta categoria têm 100% de cobertura!**

## 🟡 IMPORTANTE - API e transformação de dados

✅ **Todos os arquivos desta categoria têm 100% de cobertura!**

## 🟢 NORMAL - Entidades e utilitários

**Total:** 2 arquivo(s) com lacunas

### 1. `sgc.comum.config.ConfigOpenApi`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 50.00% (3 branch(es) não cobertos)
- **Branches não cobertos:** 18(1/2), 19(1/2), 20(1/2)

**Ação necessária:** Criar ou expandir `ConfigOpenApiCoverageTest.java` para cobrir todas as linhas e branches.

### 2. `sgc.processo.service.ProcessoDetalheBuilder`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 94.44% (1 branch(es) não cobertos)
- **Branches não cobertos:** 71(1/4)

**Ação necessária:** Criar ou expandir `ProcessoDetalheBuilderCoverageTest.java` para cobrir todas as linhas e branches.


---

## 🛠️ Scripts Disponíveis

Use os seguintes scripts em `backend/etc/scripts/` para auxiliar:

1. `node super-cobertura.cjs --run` - Gera relatório de lacunas
2. `node verificar-cobertura.cjs --missed` - Lista arquivos com mais gaps
3. `node analisar-cobertura.cjs` - Análise detalhada com tabelas
4. `python3 analyze_tests.py` - Identifica arquivos sem testes
5. `python3 prioritize_tests.py` - Prioriza criação de testes

