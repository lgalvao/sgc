# Scripts de Apoio do Backend

Este diretório reúne utilitários de análise, priorização e manutenção usados no backend do SGC.

## Pré-requisitos

- Executar os comandos a partir da raiz do repositório: `/Users/leonardo/sgc`, salvo quando indicado o contrário.
- Ter `Node.js`, `python3` e `bash` disponíveis no ambiente.
- Ter as dependências JavaScript instaladas na raiz do projeto, pois alguns scripts usam `xml2js`.
- Para os scripts de cobertura, ter o backend compilável e com relatório JaCoCo disponível em `backend/build/reports/jacoco/test/`.

Comando base para gerar o relatório JaCoCo manualmente:

```bash
./gradlew :backend:test :backend:jacocoTestReport
```

## Visão geral

| Script | Linguagem | Finalidade principal |
| :--- | :--- | :--- |
| `analisar-cobertura-total.cjs` | Node.js | Prioriza classes com lacunas de cobertura e gera um plano resumido em Markdown |
| `analisar-cobertura.cjs` | Node.js | Mostra uma visão tabular detalhada da cobertura por arquivo |
| `analisar-complexidade.cjs` | Node.js | Gera ranking de complexidade a partir do CSV do JaCoCo |
| `analyze_tests.py` | Python | Detecta classes sem testes unitários correspondentes |
| `auditar-verificacoes-null.js` | Node.js | Audita verificações explícitas de `null` no código Java |
| `cobertura-100.sh` | Bash | Orquestra uma jornada completa de diagnóstico para aumentar cobertura |
| `fix_fqn.py` | Python | Substitui nomes totalmente qualificados por imports em arquivos Java |
| `gerar-plano-cobertura.cjs` | Node.js | Gera um plano detalhado para buscar 100% de cobertura |
| `gerar-stub-teste.cjs` | Node.js | Cria um stub de teste para uma classe com base no relatório JaCoCo |
| `instalar-certs.sh` | Bash | Importa certificados locais no cacerts da JVM |
| `prioritize_tests.py` | Python | Prioriza classes sem testes em P1, P2 e P3 |
| `super-cobertura.cjs` | Node.js | Lista lacunas de cobertura e exporta JSON estruturado |
| `verificar-cobertura.cjs` | Node.js | Consulta cobertura global, por classe e por linhas/branches perdidas |

## Scripts

### `analisar-cobertura-total.cjs`

Gera um diagnóstico resumido das classes com cobertura abaixo de 100%, calcula um `actionScore` por classe e grava um plano em Markdown.

- Entrada principal: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- Saída: `plano-cobertura-backend.md` na raiz do repositório
- Comportamento: por padrão tenta atualizar o relatório com Gradle antes de analisar

Uso:

```bash
node backend/etc/scripts/analisar-cobertura-total.cjs
node backend/etc/scripts/analisar-cobertura-total.cjs --skip-run
```

Quando usar:

- Para obter rapidamente as classes mais caras em termos de lacunas de cobertura
- Para alimentar a geração de stubs com `gerar-stub-teste.cjs`

### `analisar-cobertura.cjs`

Lê o XML do JaCoCo e imprime uma tabela detalhada com métricas por arquivo: linhas, branches, complexidade e listas resumidas de linhas não cobertas.

- Entrada principal: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- Saída: apenas console
- Comportamento: por padrão tenta executar `:backend:jacocoTestReport` antes da análise

Uso:

```bash
node backend/etc/scripts/analisar-cobertura.cjs
node backend/etc/scripts/analisar-cobertura.cjs --skip-run
```

Quando usar:

- Para inspeção detalhada por arquivo sem gerar plano em Markdown
- Para descobrir rapidamente onde estão linhas e branches faltantes

### `analisar-complexidade.cjs`

Analisa o CSV do JaCoCo e produz um ranking de classes mais complexas com base em score composto por complexidade, branches, linhas e média por método.

- Entrada principal: `backend/build/reports/jacoco/test/jacocoTestReport.csv`
- Saída: `complexity-ranking.md` na raiz do repositório
- Dependência: o CSV do JaCoCo precisa existir

Uso:

```bash
node backend/etc/scripts/analisar-complexidade.cjs
```

Quando usar:

- Para identificar hotspots de manutenção e risco
- Para orientar priorização de testes além da cobertura bruta

### `analyze_tests.py`

Varre `src/main/java` e `src/test/java`, tenta associar cada classe a arquivos de teste convencionais e gera um relatório Markdown com itens testados e não testados por categoria.

- Convenções reconhecidas: `Test`, `CoverageTest`, `UnitTest`, `IntegrationTest`
- Saída padrão: `unit-test-report.md` no diretório corrente
- Parâmetros: `--dir` e `--output`

Uso:

```bash
python3 backend/etc/scripts/analyze_tests.py --dir backend --output analise-testes.md
```

Quando usar:

- Para identificar ausência de testes unitários por convenção de nome
- Antes de rodar `prioritize_tests.py`

### `auditar-verificacoes-null.js`

Procura ocorrências de `== null` e `!= null` no código Java do backend, classifica cada caso como `MAYBE_LEGIT` ou `POTENTIALLY_REDUNDANT` com base em contexto simples e gera dois relatórios.

- Entrada principal: `backend/src/main/java/sgc`
- Saídas:
  - `null-checks-audit.txt` na raiz do repositório
  - `null-checks-analysis.md` na raiz do repositório

Uso:

```bash
node backend/etc/scripts/auditar-verificacoes-null.js
```

Quando usar:

- Para mapear verificações de nulo candidatas a refatoração
- Para revisar excesso de checagens defensivas

### `cobertura-100.sh`

Script orquestrador que encadeia geração de relatório JaCoCo, análise detalhada, identificação de lacunas, geração de plano e priorização de testes.

Etapas executadas:

1. `:backend:test :backend:jacocoTestReport`
2. `analisar-cobertura.cjs`
3. `super-cobertura.cjs`
4. `gerar-plano-cobertura.cjs`
5. `analyze_tests.py`
6. `prioritize_tests.py`

Uso:

```bash
bash backend/etc/scripts/cobertura-100.sh
```

Arquivos gerados ao longo do fluxo:

- `cobertura-detalhada.txt`
- `cobertura_lacunas.json`
- `plano-100-cobertura.md`
- `analise-testes.md`
- `priorizacao-testes.md`

Observações:

- O script resolve seus diretórios internamente, então pode ser chamado da raiz.
- Há referências legadas no trecho final, como `gerar-testes-cobertura.cjs` e `show cat`, que não correspondem ao estado atual do diretório. Na prática, o gerador disponível hoje é `gerar-stub-teste.cjs`.

### `fix_fqn.py`

Varre arquivos Java em `src/main/java` e `src/test/java`, substitui referências totalmente qualificadas por nomes simples e adiciona os imports necessários quando não há colisão.

Exemplos de ajuste:

- `sgc.modulo.ClasseUtil` vira `ClasseUtil`
- O import correspondente é adicionado automaticamente

Regras relevantes:

- Ignora `java.lang`
- Evita colisão quando já existe import com mesmo nome simples apontando para outra classe
- Não mexe em linhas de `package`, `import`, comentários simples ou literais de string

Uso:

```bash
python3 backend/etc/scripts/fix_fqn.py
```

Quando usar:

- Após geração de código ou refatorações que deixaram muitos FQNs espalhados
- Para reduzir ruído em testes e classes Java

### `gerar-plano-cobertura.cjs`

Gera um plano de ação detalhado para atingir 100% de cobertura nas classes consideradas relevantes, com categorização por prioridade e indicação de linhas e branches não cobertos.

- Entrada principal: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- Saída: `plano-100-cobertura.md` na raiz do repositório
- Filtro interno: ignora classes geradas, DTOs simples, exceções, repositórios e outras exclusões alinhadas ao `build.gradle.kts`

Uso:

```bash
node backend/etc/scripts/gerar-plano-cobertura.cjs
```

Quando usar:

- Para planejar uma iniciativa de aumento de cobertura com backlog claro
- Quando a saída em JSON do `super-cobertura.cjs` não for suficiente

### `gerar-stub-teste.cjs`

Cria um arquivo `CoverageTest` para uma classe alvo encontrada no XML do JaCoCo. O script lê o código-fonte da classe, detecta dependências `private final` e gera um stub com `@InjectMocks` e `@Mock`.

- Entrada principal: nome simples da classe ou nome totalmente qualificado
- Dependência: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- Saída: arquivo em `backend/src/test/java/.../<Classe>CoverageTest.java`

Uso:

```bash
node backend/etc/scripts/gerar-stub-teste.cjs NomeDaClasse
node backend/etc/scripts/gerar-stub-teste.cjs sgc.modulo.servico.NomeDaClasse
```

Quando usar:

- Para acelerar o ponto de partida de um teste de cobertura
- Após identificar uma classe prioritária em `analisar-cobertura-total.cjs`

### `instalar-certs.sh`

Importa os certificados `cert-tre.cer` e `cert-for.cer` no `cacerts` da JVM usando `keytool`.

Arquivos esperados:

- `backend/etc/deploy/cert-tre.cer`
- `backend/etc/deploy/cert-for.cer`

Uso:

```bash
cd backend/etc/scripts
bash instalar-certs.sh
```

Observações:

- Este script depende do diretório corrente, porque usa caminhos relativos simples para `../deploy`.
- A importação escreve no `cacerts` da JVM e normalmente exige permissões adequadas no ambiente.
- A senha usada está fixa como `changeit`.

### `prioritize_tests.py`

Recebe um relatório Markdown de classes sem teste e reorganiza os itens em prioridades:

- `P1`: regras de negócio, validação, segurança e orquestração
- `P2`: controllers e mappers
- `P3`: restante e estruturas de menor risco

Uso:

```bash
python3 backend/etc/scripts/prioritize_tests.py --input analise-testes.md --output priorizacao-testes.md
```

Quando usar:

- Depois de gerar um relatório com `analyze_tests.py`
- Para transformar uma lista bruta de pendências em backlog de implementação

### `super-cobertura.cjs`

Gera um relatório orientado a lacunas de cobertura, considerando exclusões compatíveis com o `jacocoTestReport`, e salva tudo em JSON para consumo posterior.

- Entrada principal: `backend/build/reports/jacoco/test/jacocoTestReport.xml`
- Saída: `cobertura_lacunas.json` na raiz do repositório
- Opcional: `--run` para atualizar o relatório JaCoCo antes da análise

Uso:

```bash
node backend/etc/scripts/super-cobertura.cjs
node backend/etc/scripts/super-cobertura.cjs --run
```

Quando usar:

- Para consumo automatizado da lista de lacunas
- Como base para métricas e priorização por score

### `verificar-cobertura.cjs`

Ferramenta de consulta do relatório JaCoCo com dois modos principais:

- modo tabela: resume cobertura global e destaca classes abaixo de um limite
- modo detalhado: mostra linhas e branches perdidos por arquivo

Opções disponíveis:

- `--min=<n>`: define o limite mínimo de cobertura de linhas para filtrar classes
- `--missed` ou `--details`: exibe ranking de linhas e branches perdidos
- `--simple`: simplifica a saída no modo detalhado
- `--fail-under=<n>`: encerra com erro se a cobertura global de instruções ficar abaixo da meta
- filtro posicional: restringe por nome de pacote ou classe

Uso:

```bash
node backend/etc/scripts/verificar-cobertura.cjs
node backend/etc/scripts/verificar-cobertura.cjs sgc.subprocesso --min=95
node backend/etc/scripts/verificar-cobertura.cjs --missed
node backend/etc/scripts/verificar-cobertura.cjs --missed --simple
node backend/etc/scripts/verificar-cobertura.cjs --fail-under=90
```

Quando usar:

- Para inspeção rápida sem gerar arquivos adicionais
- Para validar meta mínima de cobertura em automações locais

## Fluxos recomendados

### Diagnóstico rápido de cobertura

```bash
./gradlew :backend:test :backend:jacocoTestReport
node backend/etc/scripts/verificar-cobertura.cjs --missed
node backend/etc/scripts/analisar-cobertura.cjs --skip-run
```

### Planejamento de aumento de cobertura

```bash
node backend/etc/scripts/super-cobertura.cjs --run
node backend/etc/scripts/gerar-plano-cobertura.cjs
node backend/etc/scripts/analisar-cobertura-total.cjs --skip-run
```

### Identificação e priorização de classes sem teste

```bash
python3 backend/etc/scripts/analyze_tests.py --dir backend --output analise-testes.md
python3 backend/etc/scripts/prioritize_tests.py --input analise-testes.md --output priorizacao-testes.md
```

### Geração inicial de stub de teste

```bash
node backend/etc/scripts/gerar-stub-teste.cjs NomeDaClasse
```

## Observações finais

- A maior parte dos scripts grava artefatos fora deste diretório, normalmente na raiz do repositório.
- Os scripts de cobertura assumem que o JaCoCo produz XML e CSV em `backend/build/reports/jacoco/test/`, o que está alinhado ao `build.gradle.kts` atual.
- Alguns utilitários parecem ter sido criados para uso exploratório local. Antes de incorporá-los em pipelines, vale revisar mensagens, nomes de arquivos e referências legadas.
