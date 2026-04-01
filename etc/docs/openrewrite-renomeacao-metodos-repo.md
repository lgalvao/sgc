# OpenRewrite para Renomeacao de Metodos de Repositorio

## Objetivo

Deixar a renomeacao da Categoria A reproduzivel e menos dependente de edicao manual.

Arquivo base da receita:

- [renomear-metodos-repo-categoria-a.yml](/Users/leonardo/sgc/etc/openrewrite/renomear-metodos-repo-categoria-a.yml)

Checklist relacionado:

- [checklist-padronizacao-metodos-repo.md](/Users/leonardo/sgc/etc/docs/checklist-padronizacao-metodos-repo.md)

## Escopo desta receita

A receita atual cobre apenas a Categoria A:

- metodos em `Repo`;
- com `@Query`;
- com nome derivado ou hibrido;
- sem mudanca de comportamento.

Ela nao cobre ainda:

- remocao de wrappers `default`;
- ajustes estruturais em `service`;
- mudancas de semantica;
- Categoria C.

## Estrategia recomendada

### Passo 1

Aplicar a receita em um unico modulo por vez.

Ordem sugerida:

1. `organizacao`
2. `mapa`
3. `subprocesso`

### Passo 2

Depois da aplicacao, validar residuos:

- nomes antigos ainda presentes;
- mocks e verificacoes de teste;
- JavaDoc e comentarios antigos;
- eventuais colisões com overloads ou nomes preexistentes.

### Passo 3

Rodar validacao curta a cada lote:

```bash
./gradlew :backend:compileTestJava
./gradlew :backend:test --tests "sgc.organizacao.*"
```

Adaptar o filtro do teste ao modulo alterado.

## Forma de uso

O projeto ainda nao tem plugin do OpenRewrite configurado no Gradle raiz.

Antes de aplicar a receita, o ideal e:

1. criar uma branch dedicada;
2. configurar o plugin do OpenRewrite no `build.gradle.kts` ou `build.gradle`;
3. apontar para a receita em `etc/openrewrite/renomear-metodos-repo-categoria-a.yml`;
4. executar primeiro em modo de inspecao;
5. so depois aplicar de fato.

Neste projeto, use as tarefas do Rewrite sem configuration cache:

```bash
./gradlew --no-configuration-cache :backend:rewriteDiscover
./gradlew --no-configuration-cache :backend:rewriteDryRun
./gradlew --no-configuration-cache :backend:rewriteRun
```

## Observacoes importantes

- `ChangeMethodName` e adequado para esse lote porque a mudanca e mecanica.
- Em interfaces de repositorio e chamadas Java, a chance de acerto e alta.
- Ainda assim, eu recomendo revisar manualmente os pontos de maior acoplamento em testes Mockito e em chamadas encadeadas de `service`.
- Para os casos da Categoria B, OpenRewrite continua util, mas a receita deve ser outra, porque ali nao e apenas rename.

## Proximo artefato recomendado

Se formos seguir por esse caminho, o proximo passo seguro e montar uma segunda receita, separada, para a Categoria B:

- localizar wrappers `default` artificiais;
- substituir chamadas;
- remover os wrappers de forma controlada.
