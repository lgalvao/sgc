# 🎯 Plano para Alcançar 100% de Cobertura de Testes

**Gerado em:** 2026-02-13

## 📊 Situação Atual

- **Cobertura Global de Linhas:** 99.98%
- **Cobertura Global de Branches:** 98.76%
- **Total de Arquivos Analisados:** 236
- **Arquivos com Cobertura < 100%:** 10
- **Arquivos com 100% de Cobertura:** 226

## 🎯 Objetivo

Alcançar **100% de cobertura** em todas as classes relevantes do projeto.

## 📋 Progresso por Categoria

- **🔴 CRÍTICO - Lógica de negócio central:** 0 arquivo(s) pendente(s)
- **🟡 IMPORTANTE - API e transformação de dados:** 0 arquivo(s) pendente(s)
- **🟢 NORMAL - Entidades e utilitários:** 10 arquivo(s) pendente(s)

---

## 🔴 CRÍTICO - Lógica de negócio central

✅ **Todos os arquivos desta categoria têm 100% de cobertura!**

## 🟡 IMPORTANTE - API e transformação de dados

✅ **Todos os arquivos desta categoria têm 100% de cobertura!**

## 🟢 NORMAL - Entidades e utilitários

**Total:** 10 arquivo(s) com lacunas

### 1. `sgc.organizacao.mapper.UsuarioMapper`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 75.00% (3 branch(es) não cobertos)
- **Branches não cobertos:** 40(1/2), 45(1/2), 54(1/2)

**Ação necessária:** Criar ou expandir `UsuarioMapperCoverageTest.java` para cobrir todas as linhas e branches.

### 2. `sgc.comum.config.ConfigOpenApi`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 50.00% (3 branch(es) não cobertos)
- **Branches não cobertos:** 18(1/2), 19(1/2), 20(1/2)

**Ação necessária:** Criar ou expandir `ConfigOpenApiCoverageTest.java` para cobrir todas as linhas e branches.

### 3. `sgc.subprocesso.mapper.SubprocessoDetalheMapper`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 87.50% (2 branch(es) não cobertos)
- **Branches não cobertos:** 49(1/4), 86(1/2)

**Ação necessária:** Criar ou expandir `SubprocessoDetalheMapperCoverageTest.java` para cobrir todas as linhas e branches.

### 4. `sgc.mapa.service.MapaSalvamentoService`

- **Cobertura de Linhas:** 98.94% (1 linha(s) não cobertas)
- **Cobertura de Branches:** 100.00% (0 branch(es) não cobertos)
- **Linhas não cobertas:** 128

**Ação necessária:** Criar ou expandir `MapaSalvamentoServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 5. `sgc.processo.service.ProcessoDetalheBuilder`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 88.89% (2 branch(es) não cobertos)
- **Branches não cobertos:** 71(1/4), 83(1/2)

**Ação necessária:** Criar ou expandir `ProcessoDetalheBuilderCoverageTest.java` para cobrir todas as linhas e branches.

### 6. `sgc.processo.listener.EventoProcessoListener`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 97.67% (1 branch(es) não cobertos)
- **Branches não cobertos:** 179(1/6)

**Ação necessária:** Criar ou expandir `EventoProcessoListenerCoverageTest.java` para cobrir todas as linhas e branches.

### 7. `sgc.alerta.mapper.AlertaMapper`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 75.00% (1 branch(es) não cobertos)
- **Branches não cobertos:** 36(1/2)

**Ação necessária:** Criar ou expandir `AlertaMapperCoverageTest.java` para cobrir todas as linhas e branches.

### 8. `sgc.seguranca.login.LoginController`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 95.83% (1 branch(es) não cobertos)
- **Branches não cobertos:** 64(1/2)

**Ação necessária:** Criar ou expandir `LoginControllerCoverageTest.java` para cobrir todas as linhas e branches.

### 9. `sgc.mapa.service.CopiaMapaService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 96.15% (1 branch(es) não cobertos)
- **Branches não cobertos:** 126(1/2)

**Ação necessária:** Criar ou expandir `CopiaMapaServiceCoverageTest.java` para cobrir todas as linhas e branches.

### 10. `sgc.mapa.service.MapaManutencaoService`

- **Cobertura de Linhas:** 100.00% (0 linha(s) não cobertas)
- **Cobertura de Branches:** 90.00% (1 branch(es) não cobertos)
- **Branches não cobertos:** 80(1/2)

**Ação necessária:** Criar ou expandir `MapaManutencaoServiceCoverageTest.java` para cobrir todas as linhas e branches.


---

## 🛠️ Scripts Disponíveis

Use os seguintes scripts em `backend/etc/scripts/` para auxiliar:

1. `node super-cobertura.cjs --run` - Gera relatório de lacunas
2. `node verificar-cobertura.cjs --missed` - Lista arquivos com mais gaps
3. `node analisar-cobertura.cjs` - Análise detalhada com tabelas
4. `python3 analyze_tests.py` - Identifica arquivos sem testes
5. `python3 prioritize_tests.py` - Prioriza criação de testes

