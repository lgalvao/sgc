# Checklist de Padronização de Métodos de Repositório

## Objetivo

Organizar a padronização dos métodos de persistência do SGC, com foco em três desvios já identificados:

- uso de nomes no dialeto de métodos especiais do Spring Data em métodos com `@Query`;
- uso de nomes híbridos, misturando critério funcional com detalhe técnico de carregamento;
- propagação desses nomes para `service`, sem tradução para linguagem de domínio.

Este checklist serve como inventário, critério de decisão e guia de execução.

## Situacao atual

- Categoria A aplicada no backend com OpenRewrite.
- Categoria B executada em `sgc.organizacao.model.UnidadeMapaRepo`.
- Guardrails automatizados adicionados em `sgc.arquitetura.ArchConsistencyTest`.
- Restante do checklist continua como guia para proximas rodadas.

## Regra de decisão

### Regra 1

Se o método usa `@Query`, o nome **não deve** simular query derivada do Spring Data.

Preferir nomes em português, orientados ao caso de uso:

- `buscar...`
- `listar...`
- `contar...`
- `verificar...`
- `remover...`

### Regra 2

Se o método é derivado de verdade pelo Spring Data e está em `Repo`, ele pode permanecer **temporariamente** em inglês, desde que:

- não use `@Query`;
- não seja apenas um wrapper `default` para `findById`, `existsById` ou `findAllById`;
- não exponha detalhe técnico de fetch, grafo ou otimização no nome.

### Regra 3

Métodos `default` que apenas renomeiam `findById`, `existsById` ou `findAllById` para parecer query derivada devem ser eliminados ou convertidos para nomes semânticos em português.

### Regra 4

Detalhes técnicos de persistência não devem dominar o nome público do método.

Evitar nos nomes:

- `With...`
- `ComFetch`
- `SemFetch`
- variantes semelhantes que descrevem estratégia interna de JPA em vez de intenção de uso

### Regra 5

`Service` não deve propagar nome de repositório sem ganho de semântica.

Se um `service` só repassa `repo.findBy...`, ele deve:

- ou ser renomeado para linguagem de domínio;
- ou ser absorvido/removido se não agrega contrato real.

## Categorias

### Categoria A: corrigir obrigatoriamente

Métodos com `@Query` e nome no dialeto do Spring Data, em inglês ou híbrido.

### Categoria B: revisar e simplificar

Métodos `default` que simulam comportamento derivado sobre `findById`, `existsById` e afins.

### Categoria C: manter por ora

Métodos derivados reais do Spring Data, sem `@Query`, desde que simples e sem detalhe técnico artificial.

### Categoria D: revisar depois da renomeação dos repositórios

Métodos de `service` que vazam nomes de persistência em vez de linguagem de domínio.

## Inventário da Categoria A

### `sgc.organizacao.model.UnidadeRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findSiglasByCodigos` | Renomear | `buscarSiglasPorCodigos` | `@Query`, nome derivado artificial |
| `findAllWithHierarquia` | Renomear | `listarTodasComHierarquia` | `@Query`, mistura inglês com intenção de listagem |
| `findAllAtivasComSuperior` | Renomear | `listarAtivasComSuperior` | `@Query`, filtro explícito no JPQL |
| `findByCodigoComResponsavel` | Renomear | `buscarPorCodigoComResponsavel` | `@Query`, nome híbrido |
| `findBySiglaComResponsavel` | Renomear | `buscarPorSiglaComResponsavel` | `@Query`, nome híbrido |

### `sgc.organizacao.model.ResponsabilidadeRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByUnidadeCodigoIn` | Renomear | `listarPorCodigosUnidade` | `@Query`, nome derivado artificial |

### `sgc.organizacao.model.UsuarioRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByUnidadeLotacaoCodigo` | Renomear | `listarPorCodigoUnidadeLotacao` | `@Query`, nome derivado artificial |
| `findByTituloComUnidadeLotacao` | Renomear | `buscarPorTituloComUnidadeLotacao` | `@Query`, nome híbrido |
| `findByTitulosComUnidadeLotacao` | Renomear | `listarPorTitulosComUnidadeLotacao` | `@Query`, nome híbrido |

### `sgc.alerta.model.AlertaUsuarioRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByUsuarioAndAlertas` | Renomear | `listarPorUsuarioEAlertas` | `@Query`, nome derivado artificial |

### `sgc.mapa.model.AtividadeRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByMapaCodigoSemFetch` | Renomear | `listarPorMapaSemRelacionamentos` | `@Query`, nome híbrido e técnico |
| `findWithConhecimentosByMapa_Codigo` | Renomear | `listarPorMapaComConhecimentos` | `@Query`, mistura inglês e detalhe técnico |
| `findBySubprocessoCodigo` | Renomear | `listarPorCodigoSubprocesso` | `@Query`, nome derivado artificial |

### `sgc.mapa.model.CompetenciaRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findCompetenciaAndAtividadeIdsByMapaCodigo` | Renomear | `listarCodigosCompetenciaEAtividadePorMapa` | `@Query`, inglês e projeção explícita |
| `findByMapaCodigoSemFetch` | Renomear | `listarPorMapaSemRelacionamentos` | `@Query`, nome híbrido e técnico |

### `sgc.mapa.model.ConhecimentoRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByMapaCodigo` | Renomear | `listarPorMapa` | `@Query`, nome derivado artificial |

### `sgc.subprocesso.model.MovimentacaoRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findBySubprocessoCodigoOrderByDataHoraDesc` | Renomear | `listarPorSubprocessoOrdenadasPorDataHoraDesc` | `@Query`, ordem explicitada no nome derivado |
| `findFirstBySubprocessoCodigoOrderByDataHoraDesc` | Renomear | `buscarUltimaPorSubprocesso` | `@Query`, caso de uso mais claro que a mecânica de ordenação |

### `sgc.subprocesso.model.SubprocessoRepo`

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findByProcessoCodigoComUnidade` | Renomear | `listarPorProcessoComUnidade` | `@Query`, nome híbrido |
| `findByProcessoCodigoAndUnidadeCodigoAndSituacaoInComUnidade` | Renomear | `listarPorProcessoUnidadeESituacoesComUnidade` | `@Query`, nome excessivamente acoplado ao parser do Spring Data |
| `findByProcessoCodigoAndUnidadeCodigoInWithUnidade` | Renomear | `listarPorProcessoEUnidadesComUnidade` | `@Query`, mistura inglês e detalhe técnico |
| `findByProcessoCodigoAndSituacaoInWithUnidade` | Renomear | `listarPorProcessoESituacoesComUnidade` | `@Query`, mistura inglês e detalhe técnico |

## Inventário da Categoria B

### `sgc.organizacao.model.UnidadeMapaRepo`

Status atual:

- `findAllUnidadeCodigos` foi substituido por `listarTodosCodigosUnidade`.
- `findByUnidadeCodigo`, `existsByUnidadeCodigo` e `findAllByUnidadeCodigoIn` foram removidos.
- Chamadas migraram para `findById`, `existsById` e `findAllById`.

Inventario historico desta rodada:

| Método atual | Ação | Nome sugerido inicial | Observação |
|---|---|---|---|
| `findAllUnidadeCodigos` | Renomear | `listarTodosCodigosUnidade` | usa `@Query`, mas o problema principal é nome artificial |
| `findByUnidadeCodigo` | Revisar | eliminar ou renomear para `buscarPorCodigoUnidade` | `default` sobre `findById` |
| `existsByUnidadeCodigo` | Revisar | eliminar ou renomear para `existeParaUnidade` | `default` sobre `existsById` |
| `findAllByUnidadeCodigoIn` | Revisar | eliminar ou renomear para `listarPorCodigosUnidade` | `default` sobre `findAllById` |

## Inventário da Categoria C

Métodos derivados reais, sem `@Query`, que podem ficar temporariamente como exceção controlada:

### `sgc.parametros.model.ParametroRepo`

- `findByChave`

### `sgc.subprocesso.model.AnaliseRepo`

- `findBySubprocessoCodigoOrderByDataHoraDesc`
- `findBySubprocessoCodigo`

### `sgc.organizacao.model.UnidadeRepo`

- `findBySiglaAndSituacao`
- `findBySigla`
- `findByUnidadeSuperiorCodigoAndSituacao`
- `findByUnidadeSuperiorCodigo`
- `findByTituloTitularAndSituacao`
- `findByTituloTitular`

### `sgc.mapa.model.AtividadeRepo`

- `findByMapa_Codigo`

### `sgc.alerta.model.AlertaRepo`

- `findByProcessoCodigo`

### `sgc.organizacao.model.ResponsabilidadeRepo`

- `findByUsuarioTitulo`

### `sgc.mapa.model.CompetenciaRepo`

- `findByMapa_Codigo`
- `deleteByMapa_Codigo`

### `sgc.subprocesso.model.SubprocessoRepo`

- `findByMapa_Codigo`
- `findByProcessoCodigoAndUnidadeCodigo`
- `existsByProcessoCodigoAndUnidadeCodigoIn`
- `countByProcessoCodigo`
- `countByProcessoCodigoAndSituacaoIn`

### `sgc.mapa.model.ConhecimentoRepo`

- `findByAtividade_Codigo`

### `sgc.organizacao.model.UsuarioPerfilRepo`

- `findByUsuarioTitulo`
- `findByUnidadeCodigoIn`

## Inventário da Categoria D

Pontos em que o nome problemático do repositório vaza para `service`.

### `sgc.subprocesso.service.SubprocessoConsultaService`

| Método do service | Dependência atual | Ação posterior |
|---|---|---|
| `listarEntidadesPorProcesso` | `findByProcessoCodigoComUnidade` | alinhar após renomeação do repo |
| `listarEntidadesPorProcessoEUnidades` | `findByProcessoCodigoAndUnidadeCodigoInWithUnidade` | alinhar após renomeação do repo |
| `listarPorProcessoESituacoes` | `findByProcessoCodigoAndSituacaoInWithUnidade` | alinhar após renomeação do repo |
| `listarPorProcessoEUnidadeCodigosESituacoes` | `findByProcessoCodigoAndUnidadeCodigoInWithUnidade` | reduzir dependência do nome técnico |
| `listarPorProcessoUnidadeESituacoes` | `findByProcessoCodigoAndUnidadeCodigoAndSituacaoInComUnidade` | alinhar após renomeação do repo |

### `sgc.mapa.service.MapaManutencaoService`

| Método do service | Dependência atual | Ação posterior |
|---|---|---|
| `atividadesMapaCodigoSemRels` | `findByMapaCodigoSemFetch` | renomear em cadeia |
| `atividadesMapaCodigoComConhecimentos` | `findWithConhecimentosByMapa_Codigo` | renomear em cadeia |
| `competenciasCodMapaSemRels` | `findByMapaCodigoSemFetch` | renomear em cadeia |
| `codigosAssociacoesCompetenciaAtividade` | `findCompetenciaAndAtividadeIdsByMapaCodigo` | tornar o caso de uso mais explícito |
| `conhecimentosCodMapa` | `findByMapaCodigo` | renomear em cadeia |

### `sgc.organizacao.service.UsuarioService`

| Método do service | Dependência atual | Ação posterior |
|---|---|---|
| `buscarOpt` | `findByTituloComUnidadeLotacao` | alinhar após renomeação do repo |
| `buscarComAtribuicoesOpt` | `findByTituloComUnidadeLotacao` | revisar se o nome do service ainda faz sentido |
| `buscarPorUnidadeLotacao` | `findByUnidadeLotacaoCodigo` | alinhar após renomeação do repo |
| `buscarPorTitulos` | `findByTitulosComUnidadeLotacao` | alinhar após renomeação do repo |

### `sgc.organizacao.service.UnidadeService`

| Método do service | Dependência atual | Ação posterior |
|---|---|---|
| `buscarPorCodigo` | `findByCodigoComResponsavel` | alinhar após renomeação do repo |
| `buscarPorSigla` | `findBySiglaComResponsavel` | alinhar após renomeação do repo |
| `todasComHierarquia` | `findAllWithHierarquia` | alinhar após renomeação do repo |
| `buscarSiglasPorCodigos` | `findSiglasByCodigos` | alinhar após renomeação do repo |
| `verificarMapaVigente` | `existsByUnidadeCodigo` | revisar wrapper artificial |
| `buscarReferenciaMapaVigente` | `findByUnidadeCodigo` | revisar wrapper artificial |
| `buscarTodosCodigosUnidadesComMapa` | `findAllUnidadeCodigos` | alinhar após renomeação do repo |
| `buscarMapasPorUnidades` | `findAllByUnidadeCodigoIn` | revisar wrapper artificial |

## Ordem sugerida de execução

### Etapa 1

Renomear apenas a Categoria A, sem mudar comportamento.

Critério de aceite:

- mesma query;
- mesmos parâmetros;
- mesmos retornos;
- mesmas chamadas atualizadas;
- testes ajustados.

### Etapa 2

Eliminar ou absorver a Categoria B.

Critério de aceite:

- não restar método `default` que apenas espelha `findById`, `existsById` ou `findAllById`;
- chamadas migrarem para nome semântico ou para API base do `JpaRepository`.

### Etapa 3

Revisar a Categoria D.

Critério de aceite:

- `service` falar em linguagem de domínio;
- reduzir wrappers pass-through sem contrato real.

### Etapa 4

Reavaliar a Categoria C.

Decisão futura:

- manter exceção controlada para query derivada real;
- ou encapsular gradualmente tudo em nomes em português, quando houver valor suficiente.

## Checklist operacional

- [ ] Nenhum método com `@Query` usando prefixo `find`, `exists`, `count` ou `delete`
- [ ] Nenhum método com `@Query` usando `With...`, `ComFetch`, `SemFetch` ou equivalente técnico como parte principal do nome
- [ ] Nenhum método `default` em `Repo` apenas adaptando `findById`, `existsById` ou `findAllById`
- [ ] Nomes de repositório em português quando a implementação for manual
- [ ] Nomes de `service` revisados após renomeação dos `Repo`
- [ ] Chamadas em testes atualizadas junto com os nomes
- [ ] Comentários e JavaDoc atualizados para refletir os novos nomes
- [ ] Nova regra registrada em revisão de código: `@Query` exige nome semântico em português
- [ ] Guardrails de arquitetura mantidos verdes para impedir regressão de `@Query` com nome derivado e wrappers `default` artificiais

## Observação final

O principal ganho aqui não é apenas idioma. A padronização separa:

- consulta derivada do Spring Data;
- consulta manual com JPQL/SQL;
- detalhe técnico de carregamento;
- caso de uso de negócio.

Hoje essas quatro coisas aparecem misturadas em vários nomes.
