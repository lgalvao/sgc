# Toolkit de Scripts do SGC

## Visão geral

`etc/scripts` concentra a CLI de automação do repositório.

Entrada principal:

```bash
node etc/scripts/sgc.js
```

## Instalação

```bash
pnpm --dir etc/scripts install
```

## Estrutura resumida

- `sgc.js`: roteador principal de comandos.
- `lib/`: utilitários comuns.
- `backend/`, `frontend/`, `codigo/`, `e2e/`, `qa/`, `projeto/`: comandos por domínio.
- `test/`: testes do toolkit.

## Comandos disponíveis

### Backend

```bash
node etc/scripts/sgc.js backend cobertura auditoria
node etc/scripts/sgc.js backend testes analisar
node etc/scripts/sgc.js backend testes priorizar
node etc/scripts/sgc.js backend testes gerar-stub
node etc/scripts/sgc.js backend java corrigir-fqn
node etc/scripts/sgc.js backend java auditar-null
node etc/scripts/sgc.js backend java instalar-certificados
```

### Frontend

```bash
node etc/scripts/sgc.js frontend cobertura auditoria
node etc/scripts/sgc.js frontend mensagens extrair
node etc/scripts/sgc.js frontend mensagens analisar
node etc/scripts/sgc.js frontend validacoes auditar
node etc/scripts/sgc.js frontend cruft auditar
node etc/scripts/sgc.js frontend cruft validar
node etc/scripts/sgc.js frontend views auditar-validacoes
node etc/scripts/sgc.js frontend test-ids listar
node etc/scripts/sgc.js frontend test-ids listar-duplicados
node etc/scripts/sgc.js frontend telas capturar
```

### Código

```bash
node etc/scripts/sgc.js codigo smells auditar
```

### E2E

```bash
node etc/scripts/sgc.js e2e limpar
```

### QA

```bash
node etc/scripts/sgc.js qa snapshot coletar --perfil rapido
node etc/scripts/sgc.js qa resumo
node etc/scripts/sgc.js qa dashboard servir
```

### Projeto

```bash
node etc/scripts/sgc.js projeto doctor
node etc/scripts/sgc.js projeto limpar --confirmar
node etc/scripts/sgc.js projeto qualidade rapido
node etc/scripts/sgc.js projeto setup --instalar-dependencias
node etc/scripts/sgc.js projeto arvore-linhas
```

## Testes do toolkit

```bash
pnpm --dir etc/scripts run test
```

## Observações

- A CLI aceita argumentos extras para comandos delegados.
- Use `node etc/scripts/sgc.js --help` para a árvore completa.
