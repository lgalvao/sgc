# Toolkit de Scripts do SGC

Este diretório concentra a CLI oficial de automação do repositório, projetada como uma unidade coesa, moderna e de alta performance.

O ponto de entrada recomendado é:

```bash
node etc/scripts/sgc.js
```

## Princípios Arquiteturais

- **Moderno e Nativo:** 100% Node.js com ESM (ECMAScript Modules).
- **Centralização de Inteligência:** Lógicas de domínio (como parsers de cobertura) residem em `lib/dominios/` e são reutilizadas por múltiplos scripts.
- **Independência de Ambiente:** Gestão de caminhos centralizada que funciona de forma idêntica em Windows, Linux e CI.
- **Interface Padronizada:** Saída humana rica com cores e suporte nativo a JSON para automação.
- **Testabilidade:** Suite de testes robusta garantindo que mudanças na infraestrutura não quebrem as ferramentas de domínio.

## Instalação

As dependências do toolkit ficam isoladas neste diretório:

```bash
npm install --prefix etc/scripts
```

## Estrutura do Toolkit

- `sgc.js`: Orquestrador central (CLI).
- `lib/`: Infraestrutura compartilhada (caminhos, execução, logger, saída).
- `lib/dominios/`: Inteligência de negócio do toolkit (cobertura-java, cobertura-web).
- `backend/`, `frontend/`, `qa/`, `projeto/`, `codigo/`, `e2e/`: Scripts operacionais organizados por domínio.

## Comandos Principais

### Backend
```bash
node etc/scripts/sgc.js backend cobertura verificar --min=95
node etc/scripts/sgc.js backend testes analisar --dir backend --output analise.md
node etc/scripts/sgc.js backend testes priorizar --input analise.json
node etc/scripts/sgc.js backend java auditar-null
```

### Frontend
```bash
node etc/scripts/sgc.js frontend cobertura verificar --min=80
node etc/scripts/sgc.js frontend mensagens extrair
node etc/scripts/sgc.js frontend validacoes auditar
```

### QA & Dashboard
```bash
node etc/scripts/sgc.js qa snapshot coletar --perfil rapido
node etc/scripts/sgc.js qa resumo --json
node etc/scripts/sgc.js qa dashboard servir --porta 4179
```

### Gestão do Projeto
```bash
node etc/scripts/sgc.js projeto doctor --json
node etc/scripts/sgc.js projeto limpar --confirmar
node etc/scripts/sgc.js projeto setup --instalar-dependencias
```

## Detalhamento de Comandos Críticos

### `projeto doctor`
Valida a saúde do ambiente de desenvolvimento, checando versões de ferramentas (Node, NPM, Git, Java) e a integridade das pastas do repositório.

### `qa snapshot coletar`
O comando mais poderoso do toolkit. Ele orquestra testes unitários, integração, lint, typecheck e coleta de cobertura (Java e Web), gerando um `snapshot.json` que alimenta o dashboard histórico de qualidade.

### `testes analisar` (Backend)
Analisa o pareamento entre classes fonte e classes de teste, identificando lacunas no backlog real (ignorando DTOs estruturais) e cruzando dados com a cobertura do JaCoCo.

## Qualidade do Toolkit

O toolkit possui uma suíte de testes automatizados que valida a integração entre os módulos de domínio e a CLI:

```bash
npm test --prefix etc/scripts
```

Atualmente, **19 testes** garantem a estabilidade das operações críticas, incluindo simulações de limpeza, diagnósticos de ambiente e análise de cobertura.

## Estado Atual

O toolkit está em seu estado mais estável e moderno, com **Zero Dívida Técnica** em relação a módulos legados (CommonJS removido). Todas as ferramentas operacionais estão centralizadas em `etc/scripts` e seguem o padrão de nomenclatura `kebab-case`.
