# OpenRewrite para Reducao de `@Nullable` em DTOs

## Objetivo

Criar um primeiro lote seguro e reproduzivel para reduzir `@Nullable` redundante em DTOs do backend.

Arquivo base da receita:

- [reduzir-nullable-dtos-categoria-a.yml](/Users/leonardo/sgc/etc/openrewrite/reduzir-nullable-dtos-categoria-a.yml)

Artefato complementar:

- [nullable-dtos-categoria-b.md](/Users/leonardo/sgc/etc/docs/nullable-dtos-categoria-b.md)

## Escopo desta receita

A receita atual cobre apenas a Categoria A:

- DTOs sob `@NullMarked`;
- campos anotados com `@Nullable` que hoje sao sempre preenchidos pelo proprio mapper;
- remocao puramente mecanica, sem alterar endpoint, nome de campo ou serializacao JSON.

Ela nao cobre ainda:

- requests genericos que deveriam ser especializados por caso de uso;
- responses agregadas com semantica opcional real;
- metodos `fromEntity(...)` que aceitam entidade nula por contrato;
- mudancas de fluxo, validacao ou modelo de dominio.

## Shortlist inicial

Primeiro lote mapeado:

- [SubprocessoResumoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/SubprocessoResumoDto.java)
- [MapaResumoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/dto/MapaResumoDto.java)
- [ProcessoResumoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/dto/ProcessoResumoDto.java)

Criterio usado:

- o metodo `fromEntity(...)` preenche o campo sem branch para `null`; ou
- o metodo falha explicitamente quando a associacao obrigatoria esta ausente.

Observacao do lote atual:

- em `SubprocessoResumoDto`, apenas `codigo` entrou na Categoria A;
- `codProcesso`, `codUnidade` e `codMapa` permaneceram anulaveis porque o modelo atual ainda os expoe como potencialmente nulos e o `NullAway` confirmou isso na compilacao.

## Forma de uso

Como o `backend` agora aceita selecionar receita e arquivo por propriedade, o uso recomendado e:

```bash
./gradlew --no-configuration-cache :backend:rewriteDiscover \
  -PrewriteRecipe=sgc.java.reduzirNullableDtosCategoriaA \
  -PrewriteConfig=etc/openrewrite/reduzir-nullable-dtos-categoria-a.yml

./gradlew --no-configuration-cache :backend:rewriteDryRun \
  -PrewriteRecipe=sgc.java.reduzirNullableDtosCategoriaA \
  -PrewriteConfig=etc/openrewrite/reduzir-nullable-dtos-categoria-a.yml

./gradlew --no-configuration-cache :backend:rewriteRun \
  -PrewriteRecipe=sgc.java.reduzirNullableDtosCategoriaA \
  -PrewriteConfig=etc/openrewrite/reduzir-nullable-dtos-categoria-a.yml
```

Sem propriedades, o build continua usando a receita atual de renomeacao de repositorios.

## Validacao curta

Depois do `rewriteRun`, validar:

```bash
./gradlew --no-configuration-cache :backend:compileTestJava
./gradlew :backend:test --tests "sgc.subprocesso.dto.*"
./gradlew :backend:test --tests "sgc.mapa.dto.*"
./gradlew :backend:test --tests "sgc.processo.dto.*"
```

## Proximo lote recomendado

Se este lote passar limpo, o proximo passo seguro e abrir a Categoria B:

- DTOs de resumo com mistura de contexto minimo e contexto enriquecido;
- requests de atualizacao generica como `AtualizarMapaRequest` e `AtualizarSubprocessoRequest`;
- responses agregadas como `ContextoEdicaoResponse`.
