# 🎯 Guia para Alcançar 100% de Cobertura de Testes

Este guia fornece um processo sistemático e ferramentas para  alcançar 100% de cobertura de testes no projeto SGC.

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Scripts Disponíveis](#scripts-disponíveis)
3. [Fluxo de Trabalho](#fluxo-de-trabalho)
4. [Como Usar os Scripts](#como-usar-os-scripts)
5. [Interpretando os Relatórios](#interpretando-os-relatórios)
6. [Criando Testes de Cobertura](#criando-testes-de-cobertura)
7. [Dicas e Boas Práticas](#dicas-e-boas-práticas)

## 🎯 Visão Geral

O projeto possui **múltiplas ferramentas** para análise e melhoria de cobertura:

- **Análise automatizada** de lacunas
- **Priorização** de testes por importância
- **Geração automática** de esqueletos de testes
- **Relatórios detalhados** em múltiplos formatos

## 🛠️ Scripts Disponíveis

Todos os scripts estão em `backend/etc/scripts/`:

### 1. **cobertura-100.sh** (★ PRINCIPAL ★)
Script mestre que executa todo o pipeline de análise.

```bash
./backend/etc/scripts/cobertura-100.sh
```

**O que faz:**
- ✅ Roda os testes com JaCoCo
- ✅ Gera relatório JaCoCo (XML e HTML)
- ✅ Analisa lacunas de cobertura
- ✅ Identifica arquivos sem testes
- ✅ Prior iza testes por importância
- ✅ Gera plano de ação completo

**Saídas:**
- `plano-100-cobertura.md` - Plano de ação detalhado
- `cobertura-detalhada.txt` - Análise com tabelas
- `cobertura_lacunas.json` - Dados estruturados (JSON)
- `analise-testes.md` - Arquivos sem testes
- `priorizacao-testes.md` - Testes priorizados

---

### 2. **analisar-cobertura.cjs**
Análise detalhada com tabelas mostrando complexidade, linhas e branches.

```bash
node backend/etc/scripts/analisar-cobertura.cjs
```

**Características:**
- Mostra cobertura de linhas e branches
- Inclui complexidade ciclomática
- Lista linhas e branches não cobertos
- Ordena por menor cobertura

---

### 3. **super-cobertura.cjs**
Relatório focado em lacunas (arquivos < 100%).

```bash
# Apenas análise (usa relatório existente)
node backend/etc/scripts/super-cobertura.cjs

# Roda testes antes de analisar
node backend/etc/scripts/super-cobertura.cjs --run
```

**Características:**
- Mostra apenas arquivos com lacunas
- Detalha linhas perdidas
- Identifica branches parciais
- Gera JSON estruturado

---

### 4. **verificar-cobertura.cjs**
Ferramenta de consulta interativa para verificar cobertura.

```bash
# Ver classes abaixo de 99%
node backend/etc/scripts/verificar-cobertura.cjs --min=99

# Ver arquivos com mais linhas/branches perdidas
node backend/etc/scripts/verificar-cobertura.cjs --missed

# Filtrar por pacote específico
node backend/etc/scripts/verificar-cobertura.cjs processo

# Saída simplificada
node backend/etc/scripts/verificar-cobertura.cjs --missed --simple
```

---

### 5. **gerar-plano-cobertura.cjs**
Gera plano de ação estruturado em Markdown.

```bash
node backend/etc/scripts/gerar-plano-cobertura.cjs

# Com execução de testes primeiro
node backend/etc/scripts/gerar-plano-cobertura.cjs --run
```

**Características:**
- Categoriza por prioridade (P1/P2/P3)
- Lista linhas e branches específicos
- Sugere nome de arquivo de teste
- Calcula meta de cobertura

---

### 6. **analyze_tests.py**
Identifica arquivos Java sem testes correspondentes.

```bash
python3 backend/etc/scripts/analyze_tests.py \
  --dir backend \
  --output analise-testes.md
```

**Características:**
- Categoriza por tipo (Controllers, Services, etc.)
- Mostra % de arquivos testados
- Lista arquivos sem testes

---

### 7. **prioritize_tests.py**
Prioriza criação de testes baseado em importância.

```bash
python3 backend/etc/scripts/prioritize_tests.py \
  --input analise-testes.md \
  --output priorizacao-testes.md
```

**Categorias:**
- 🔴 **P1 (Crítico)**: Lógica de negócio, segurança
- 🟡 **P2 (Importante)**: Controllers, Mappers
- 🟢 **P3 (Normal)**: DTOs, Configurações

---

### 8. **gerar-testes-cobertura.cjs**
Gera esqueleto de teste de cobertura automaticamente.

```bash
# Gerar teste para uma classe
node backend/etc/scripts/gerar-testes-cobertura.cjs ProcessoFacade

# Com pacote completo
node backend/etc/scripts/gerar-testes-cobertura.cjs sgc.processo.service.ProcessoFacade

# Com informações de linhas/branches
node backend/etc/scripts/gerar-testes-cobertura.cjs ProcessoFacade \
  --lines="59,63,68,69" \
  --branches="70,80"
```

**O que gera:**
- Estrutura básica com JUnit 5
- Mockito configurado
- Nested test classes
- TODOs com linhas/branches a cobrir

---

## 🔄 Fluxo de Trabalho

### Abordagem Recomendada

```
1. ANÁLISE INICIAL
   └─> ./backend/etc/scripts/cobertura-100.sh
   
2. REVISAR PLANOS
   └─> Abrir plano-100-cobertura.md
   └─> Abrir priorizacao-testes.md
   
3. PRIORIZAR TRABALHO
   └─> Começar com P1 (críticos)
   └─> Depois P2 (importantes)
   └─> Por último P3 (normais)
   
4. PARA CADA CLASSE:
   a. Verificar se já existe teste
   b. Se não existe:
      └─> node backend/etc/scripts/gerar-testes-cobertura.cjs <Classe>
   c. Se existe mas está incompleto:
      └─> Consultar plano-100-cobertura.md para linhas/branches faltantes
   d. Implementar os testes
   e. Rodar testes: ./gradlew :backend:test
   f. Verificar cobertura: node backend/etc/scripts/verificar-cobertura.cjs <filtro>
   
5. VERIFICAR PROGRESSO
   └─> ./backend/etc/scripts/cobertura-100.sh
   └─> Comparar nova cobertura com anterior
   
6. REPETIR até atingir 100%
```

---

## 📊 Interpretando os Relatórios

### Plano de 100% Cobertura (plano-100-cobertura.md)

```markdown
### 12. `sgc.processo.service.ProcessoFacade`

- **Cobertura de Linhas:** 1.06% (93 linha(s) não cobertas)
- **Cobertura de Branches:** 0.00% (36 branch(es) não cobertos)
- **Linhas não cobertas:** 59, 63, 68, 69, 70, 71, 73, 78, ...
- **Branches não cobertos:** 70(2/2), 80(2/2), ...

**Ação necessária:** Criar ou expandir `ProcessoFacadeCoverageTest.java`
```

**Como ler:**
- **Linhas não cobertas**: Números das linhas não executadas pelos testes
- **Branches não cobertos**: `linha(missed/total)` - ex: `70(2/2)` = linha 70, 2 de 2 branches não cobertos

### Cobertura Detalhada (cobertura-detalhada.txt)

```
│ 0  │ 'sgc/mapa/MapaController.java'  │ 10 │ 28 │ 23 │ 5  │ '82.1%' │ '58, 76, 82...' │ 10 │ 5  │ 5  │ '50.0%' │ '57, 75, 81...' │
```

**Colunas:**
- **Cxn Total**: Complexidade ciclomática
- **Linhas T.**: Total de linhas executáveis
- **Linhas Cob.**: Linhas cobertas
- **% Linhas**: Percentual de cobertura de linhas
- **% Branches**: Percentual de cobertura de branches

---

## ✍️ Criando Testes de Cobertura

### Estrutura Recomendada

```java
package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacade Coverage Tests")
class ProcessoFacadeCoverageTest {

    @InjectMocks
    private ProcessoFacade target;

    @Mock
    private ProcessoRepository repository;
    
    @Mock
    private ProcessoMapper mapper;

    @Nested
    @DisplayName("Cobertura de buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("Deve cobrir caso de sucesso")
        void deveBuscarPorIdComSucesso() {
            // Arrange
            Long id = 1L;
            Processo processo = new Processo();
            when(repository.findById(id)).thenReturn(Optional.of(processo));
            when(mapper.toDto(processo)).thenReturn(new ProcessoDto());

            // Act
            ProcessoDto result = target.buscarPorId(id);

            // Assert
            assertNotNull(result);
            verify(repository).findById(id);
            verify(mapper).toDto(processo);
        }

        @Test
        @DisplayName("Deve cobrir caso de não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            // Arrange
            Long id = 999L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RecursoNaoEncontradoException.class, 
                () -> target.buscarPorId(id));
        }
    }

    @Nested
    @DisplayName("Cobertura de Branches")
    class CoberturaBranches {

        @Test
        @DisplayName("Deve cobrir branch TRUE da condição")
        void deveCobrirBranchTrue() {
            // Teste para cobrir linha 70 - branch verdadeiro
        }

        @Test
        @DisplayName("Deve cobrir branch FALSE da condição")
        void deveCobrirBranchFalse() {
            // Teste para cobrir linha 70 - branch falso
        }
    }
}
```

### Dicas para Atingir 100%

1. **Para cobrir linhas:**
   - Certifique-se de que cada linha é executada ao menos uma vez
   - Use code coverage report para ver linhas vermelhas

2. **Para cobrir branches:**
   - `if/else`: Teste condição verdadeira E falsa
   - `switch`: Teste todos os cases + default
   - `try/catch`: Teste caso de sucesso E exceção
   - `&&/||`: Teste todas as combinações
   - Operador ternário `?:`: Teste ambos os lados

3. **Para cobrir complexidade:**
   - Simplifique métodos muito complexos antes de testar
   - Ou crie testes para cada caminho possível

---

## 💡 Dicas e Boas Práticas

### 1. Ordem de Implementação

```
1º → Controllers/Facades (P2) - Alta visibilidade, APIs
2º → Services (P1) - Lógica de negócio
3º → Validators/Policies (P1) - Segurança
4º → Mappers (P2) - Transformação de dados
5º → Outros (P3) - Complementares
```

### 2. Quando NÃO é necessário 100%

Alguns arquivos podem ter cobertura < 100% justificadamente:

- **Configurações** (`Config*.java`) - Se forem apenas beans
- **DTOs/Records** simples - Apenas getters/setters gerados
- **Exceções** customizadas - Se forem apenas estrutura
- **Constantes/Enums** simples

### 3. Ferramentas Auxiliares

```bash
# Ver relatório HTML do JaCoCo (navegador)
open backend/build/reports/jacoco/test/html/index.html

# Ver testes que falharam
./gradlew :backend:test --tests "*"

# Rodar teste específico
./gradlew :backend:test --tests "*ProcessoFacadeCoverageTest"

# Ver logs detalhados
./gradlew :backend:test --info
```

### 4. Automatizando Verificação

Adicione ao CI/CD:

```yaml
# .github/workflows/coverage.yml
- name: Generate Coverage
  run: ./gradlew :backend:test :backend:jacocoTestReport

- name: Verify Coverage
  run: |
    node backend/etc/scripts/super-cobertura.cjs
    # Falhar se < X%
```

### 5. Monitoramento Contínuo

```bash
# Adicionar git hook (pre-push)
#!/bin/bash
echo "Verificando cobertura..."
./gradlew :backend:test :backend:jacocoTestReport
coverage=$(node backend/etc/scripts/super-cobertura.cjs | grep "Cobertura Global" | awk '{print $4}')
echo "Cobertura atual: $coverage"
```

---

## 🎯 Meta

**Objetivo:** 100% de cobertura em classes de lógica de negócio (Services, Facades, Validators)

**Mínimo Aceitável:** 95% global, com 100% em classes críticas

---

## 📞 Suporte

Se encontrar problemas:

1. Verifique se o relatório JaCoCo foi gerado: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
2. Execute os testes manualmente: `./gradlew :backend:clean :backend:test`
3. Verifique dependências:
   - Node.js 16+ instalado
   - Python 3.8+ instalado
   - xml2js instalado: `npm install xml2js`

## 🚀 Início Rápido

```bash
# 1. Instalar dependências (se necessário)
npm install xml2js

# 2. Executar pipeline completo
./backend/etc/scripts/cobertura-100.sh

# 3. Revisar plano
cat plano-100-cobertura.md | head -n 100

# 4. Gerar seu primeiro teste
node backend/etc/scripts/gerar-testes-cobertura.cjs ProcessoFacade

# 5. Implementar e testar
# ... edite o arquivo gerado ...
./gradlew :backend:test

# 6. Verificar progresso
node backend/etc/scripts/verificar-cobertura.cjs ProcessoFacade
```

---

✅ **Você está pronto para alcançar 100% de cobertura!**
