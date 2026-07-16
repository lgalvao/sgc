# Guia de Escrita de especificações de casos de uso (CDUs)

Este guia define o formato canônico mínimo para os arquivos `/specs/cdu/cdu-xx.md`.

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
- A seção de atores deve usar exatamente o título `## Atores` e listar um ou mais atores em itens.
- A seção de pré-condições deve usar exatamente o título `## Pré-condições`.
- A seção de fluxo principal deve usar exatamente o título `## Fluxo principal`.
- A numeração do fluxo principal deve ser contínua, sem reinício, sem repetição e sem regressão.
- Elementos observáveis de interface devem ser escritos entre crases, ex.: `Painel`, `Salvar`, `Atividades e conhecimentos`.
- Mensagens literais mostradas ao usuário devem ser escritos entre aspas duplas, ex.: "Preencha a descrição", "Confirma a homologação?".
- Rótulos e elementos de interface não devem ser escritos entre aspas duplas quando o item for um elemento observável da
  interface. Nesses casos, use crases.
- Perfis devem ser escritos em maiúsculas: ADMIN, GESTOR, CHEFE, SERVIDOR.
- Placeholders devem usar caixa alta entre dois `:`, por exemplo: `:SIGLA_UNIDADE:`, `:DATA_LIMITE:`, `:URL_SISTEMA:`.
- Referências para outros casos de uso ou documentos devem usar links de Markdown.
