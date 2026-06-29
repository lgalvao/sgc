# Guia de Escrita dos CDUs

Este guia define o formato canônico mínimo para os arquivos `specs/cdu-xx.md`.

## Estrutura obrigatória

Todo CDU deve conter, nesta ordem:

```md
# CDU-XX - Título do caso de uso

## Atores

- ATOR1
- ATOR2

## Pré-condições
- ...

## Fluxo principal

1. ...
```

## Convenções de escrita

- O nome do arquivo deve seguir o padrão `cdu-xx.md`.
- O título deve seguir o padrão `# CDU-XX - ...`, com o mesmo número do arquivo.
- A seção de atores deve usar exatamente o título `## Atores`.
- A seção de atores deve listar um ou mais atores em itens de lista.
- A seção de pré-condições deve usar exatamente o título `## Pré-condições`.
- A seção de fluxo principal deve usar exatamente o título `## Fluxo principal`.
- A numeração do fluxo principal deve ser contínua, sem reinício, repetição ou regressão.
- Situações do sistema devem ser escritas entre aspas simples, por exemplo: `'Em andamento'`.
- Elementos observáveis de interface devem ser escritos entre crases, por exemplo: `Painel`, `Salvar`,
  `Atividades e conhecimentos`.
- Perfis devem ser escritos em maiúsculas: `ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`.
- Placeholders devem usar caixa alta entre colchetes, por exemplo: `[SIGLA_UNIDADE]`, `[DATA_LIMITE]`.
- Referências para outros casos de uso ou documentos devem usar links do Markdown.

## Escopo do CDU

- O CDU deve descrever o contrato funcional observável do caso de uso.
- O CDU pode incluir mensagens, telas, botões e situações, quando isso fizer parte do contrato observável.
- O CDU não deve depender de detalhes de implementação, como nome de componente, enum, store, classe, endpoint ou
  estratégia técnica interna.

## Documentos complementares

- Regras transversais de acesso devem ficar em [design/acesso.md](design/acesso.md).
- Regras transversais de design e UX devem ficar em `specs/design/`.
- Convenções gerais de testes devem ficar em [_testes.md](_testes.md).
