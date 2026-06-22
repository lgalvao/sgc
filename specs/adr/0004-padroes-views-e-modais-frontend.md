# ADR 0004: Padroes de Views e Modais no Frontend

## Contexto

O frontend do SGC passou a acumular variacoes de template entre views equivalentes:

- algumas telas usavam `PageHeader`;
- outras usavam headers especializados;
- algumas views abriam `BModal` diretamente;
- outras encapsulavam o fluxo em componentes previsiveis.

Essa variacao reduz legibilidade, piora a manutencao e torna regressao estrutural mais provavel.

## Decisao

Padronizar o frontend em torno de poucos arquetipos e proibir `BModal` cru em views.

### Arquetipos de view

1. `View simples`
   `LayoutPadrao` + `PageHeader` + conteudo principal.

2. `View de formulario`
   `LayoutPadrao` + `PageHeader` + formulario + feedback local.

3. `View-shell de feature`
   `LayoutPadrao` + header especializado + root de modais da feature.

### Arquetipos de modal

1. `ModalConfirmacao`
   Para confirmacoes, perigo e comandos de aceite.

2. `ModalPadrao`
   Para conteudo customizado com rodape padronizado.

3. `ModalObservacaoAcao`
   Para fluxos com texto/justificativa mantendo o contrato visual do `ModalPadrao`.

## Regras

- views, exceto telas especiais de autenticacao/erro, devem declarar `LayoutPadrao`;
- views devem expor `PageHeader` ou um header especializado aprovado;
- views nao devem abrir `BModal` diretamente;
- modais de feature devem ficar em roots ou componentes locais previsiveis.

## Enforcement

- gate CLI: `node toolkit/sgc.js frontend views templates-validar`
- script do frontend: `npm --prefix frontend run templates:check`
- `quality:all` do frontend passa a incluir esse gate

## Consequencias

### Positivas

- maior previsibilidade de template;
- fronteiras mais claras entre shell, conteudo e fluxo;
- reducao de duplicacao estrutural em diagnostico e telas administrativas;
- regressao estrutural detectada cedo no CI/local.

### Custos

- refatoracao inicial de views legadas;
- necessidade de manter lista curta e explicita de headers especializados aprovados.
