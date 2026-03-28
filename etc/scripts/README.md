# Toolkit de Scripts do SGC

Este diretório concentra a CLI oficial de automação do repositório.

O ponto de entrada recomendado é:

```bash
node etc/scripts/sgc.js
```

## Princípios

- um único ponto de entrada
- Node.js com ESM
- nomes em português e `kebab-case`
- uso de bibliotecas externas para CLI, execução, limpeza e diagnóstico
- saída humana por padrão e JSON quando fizer sentido

## Instalação

As dependências do toolkit ficam isoladas neste diretório:

```bash
npm install --prefix etc/scripts
```

## Comandos principais

### Backend

```bash
node etc/scripts/sgc.js backend cobertura verificar --min=95
node etc/scripts/sgc.js backend cobertura plano
node etc/scripts/sgc.js backend testes analisar --dir backend --output analise-testes.md --output-json analise-testes.json
node etc/scripts/sgc.js backend java corrigir-fqn --dry-run
```

### Frontend

```bash
node etc/scripts/sgc.js frontend cobertura verificar
node etc/scripts/sgc.js frontend mensagens extrair
node etc/scripts/sgc.js frontend mensagens analisar
node etc/scripts/sgc.js frontend validacoes auditar
```

### QA

```bash
node etc/scripts/sgc.js qa snapshot coletar --perfil rapido
node etc/scripts/sgc.js qa resumo
node etc/scripts/sgc.js qa resumo --json
```

### Projeto

```bash
node etc/scripts/sgc.js projeto doctor
node etc/scripts/sgc.js projeto doctor --json
node etc/scripts/sgc.js projeto limpar
node etc/scripts/sgc.js projeto limpar --confirmar
node etc/scripts/sgc.js projeto qualidade rapido
node etc/scripts/sgc.js projeto setup --instalar-dependencias
node etc/scripts/sgc.js projeto arvore-linhas --depth 2
```

### Código e E2E

```bash
node etc/scripts/sgc.js codigo comentarios limpar-ai --dry-run
node etc/scripts/sgc.js e2e limpar
```

## Comandos de projeto

### `projeto doctor`

Valida comandos e arquivos essenciais do ambiente:

- `node`, `npm`, `git`
- presença de `gradlew`
- presença dos módulos `frontend`, `backend` e do próprio toolkit
- presença opcional de `java`, `keytool` e `node_modules`

Use `--json` para automação.

### `projeto limpar`

Lista artefatos gerados pelo toolkit, por QA e por relatórios temporários. Por padrão apenas mostra a prévia.

Use `--confirmar` para remover de fato.

### `projeto qualidade`

Executa os perfis consolidados definidos no Gradle:

- `rapido`
- `backend`
- `frontend`
- `all`

### `projeto setup`

Executa o bootstrap centralizado do ambiente do projeto.

Opções úteis:

- `--instalar-dependencias`
- `--instalar-playwright`
- `--importar-certificados`

## QA Dashboard

O dashboard de QA continua tendo como fonte de verdade:

- `etc/qa-dashboard/latest/ultimo-snapshot.json`
- `etc/qa-dashboard/latest/ultimo-resumo.md`

O comando `qa resumo` lê esses artefatos normalizados, com fallback para o último `runs/**/snapshot.json`.

## Testes do toolkit

O toolkit tem sua própria suíte inicial de testes automatizados:

```bash
npm test --prefix etc/scripts
```

Os testes atuais cobrem:

- ajuda da CLI raiz
- despacho de comandos do backend
- `qa resumo` com fixture
- `projeto doctor --json`
- `projeto limpar` em modo prévia e remoção real

## Estado atual

`backend/etc/scripts` e `frontend/etc/scripts` ainda existem como implementação legada de alguns comandos já expostos na CLI raiz. O uso recomendado, a documentação e os testes ficam centralizados em `etc/scripts`.

Os arquivos em `etc/scripts/legado/` são transitórios e não fazem parte da superfície pública do toolkit. Eles devem ser portados para ESM ou removidos.
