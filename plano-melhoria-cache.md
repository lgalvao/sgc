# Plano de melhoria do cache organizacional

## Objetivo

Consolidar o cache dos dados organizacionais vindos das views para que ele seja uma fonte real de leitura, tenha
invalidacao previsivel e reduza consultas redundantes sem enfraquecer a consistencia percebida pelo usuario.

## Premissas de volume

Os dados organizacionais cabem confortavelmente em memoria:

1. cerca de 1k usuarios;
2. cerca de 250 unidades;
3. menos de 500 pares perfil-unidade.

Portanto, o objetivo nao e otimizar tamanho do cache. A direcao correta e manter snapshots completos, simples e
coerentes em memoria, priorizando:

1. uma unica fonte de leitura para dados vindos das views;
2. invalidacao correta;
3. reducao de consultas redundantes;
4. codigo mais facil de entender e testar.

## Diagnostico atual

### Problema principal

A implementacao atual criou uma camada de cache das views em `CacheViewsOrganizacaoService`, mas os servicos de
negocio ainda consultam os repositorios diretamente em varios caminhos. Isso faz o cache de view ser aquecido e
recarregado, mas nem sempre consumido.

### Sintomas observados

1. `CacheViewsOrganizacaoService` cacheia leituras completas de `VW_UNIDADE`, `VW_USUARIO`, `VW_RESPONSABILIDADE` e
   `VW_USUARIO_PERFIL_UNIDADE`, mas seus metodos aparecem principalmente no aquecimento e no refresh periodico.
2. `UnidadeHierarquiaService` ainda chama `unidadeRepo.listarEstruturasAtivas()` diretamente para arvore, mapa
   filho-pai, mapa pai-filhos e arvore com elegibilidade.
3. `ResponsavelUnidadeService` ainda consulta responsabilidades e usuarios diretamente para leituras que poderiam
   partir do snapshot organizacional.
4. `UsuarioService` usa cache especifico para autorizacoes por usuario, mas a lista completa de usuarios em
   `CacheViewsOrganizacaoService` nao alimenta as consultas reais.
5. O refresh periodico carrega a mesma view de unidade mais de uma vez: primeiro pelo cache de view, depois pelos
   caches derivados de hierarquia.
6. A criacao de atribuicao temporaria invalida apenas `diagnosticoOrganizacional`, embora altere dados usados em
   elegibilidade, responsabilidade atual e caches de frontend.
7. O SSE do frontend fecha a conexao no primeiro erro, impedindo reconexao automatica do `EventSource`.
8. Alguns caches parecem redundantes ou mortos, como `usuarioPerfis`, porque o caminho real deriva perfis das
   autorizacoes.

## Decisao arquitetural recomendada

Usar `CacheViewsOrganizacaoService` como snapshot interno obrigatorio das views organizacionais.

Essa direcao e preferivel porque os dados vem de views externas, mudam fora das transacoes normais do SGC e ja existe
infraestrutura de aquecimento, refresh e SSE. O ganho vem de fazer os servicos consumirem o mesmo snapshot em vez de
manter caches paralelos que consultam as views diretamente.

### Alternativa aceitavel

Se o escopo precisar ser menor, remover os caches completos de view e manter apenas caches derivados usados de fato.
Nesse caso, tambem remover aquecimento/refresh de `vwUnidade`, `vwUsuario`, `vwResponsabilidade` e
`vwUsuarioPerfil`, evitando trabalho sem consumidor.

## Modelo alvo

### Fonte primaria de leitura

`CacheViewsOrganizacaoService` deve expor snapshots imutaveis e orientados a leitura:

1. unidades ativas;
2. usuarios;
3. responsabilidades atuais;
4. perfis/autorizacoes por usuario e unidade.

Os demais servicos devem consultar esses snapshots quando a leitura for organizacional e tolerar staleness controlada.

### Caches derivados

Caches derivados devem ser montados a partir dos snapshots:

1. arvore de unidades;
2. mapa pai -> filhos;
3. mapa filho -> pai;
4. codigos de unidades com mapa vigente;
5. autorizacoes por usuario, se continuar sendo vantajoso por chave.

### Staleness

Manter uma politica unica e explicita:

1. refresh periodico limitado a no maximo 10 minutos, ou outro intervalo definido;
2. TTL dos caches alinhado ao refresh, sem combinacoes contraditorias;
3. refresh manual administrativo;
4. invalidacao local imediata quando o proprio SGC altera dado relacionado, como atribuicao temporaria, administrador
   ou mapa vigente.

## Plano de execucao

## Fase 1 - Corrigir a base do cache

1. Transformar `CacheViewsOrganizacaoService` em servico de snapshot, nao apenas aquecimento.
2. Trocar retorno de entidade JPA em `listarTodosPerfisUnidade()` por record/DTO de leitura.
3. Criar helpers internos no snapshot para evitar filtros repetidos por titulo, unidade e sigla.
4. Documentar no proprio servico quais dados aceitam staleness e quais devem continuar indo ao banco.
5. Remover `@SuppressWarnings("EmptyMethod")` se os metodos de evict forem substituidos por invalidacao programatica
   centralizada.

### Criterios de aceite

1. O cache central nao retorna entidades mutaveis.
2. Todos os metodos publicos do snapshot tem consumidor real fora do aquecimento/refresh.
3. Testes unitarios cobrem snapshot vazio, duplicidade tolerada quando aplicavel e mapeamento dos dados.

## Fase 2 - Fazer os servicos consumirem o snapshot

1. Alterar `UnidadeHierarquiaService` para montar arvore e mapas a partir de `CacheViewsOrganizacaoService`.
2. Alterar `buscarArvoreComElegibilidade` para reutilizar unidades e responsabilidades do snapshot.
3. Avaliar `ResponsavelUnidadeService`:
   - `todasPossuemResponsavelEfetivo`;
   - `buscarResponsaveisUnidades`;
   - `buscarUnidadesOndeEhResponsavel`;
   - `buscarResponsavelAtual`.
4. Avaliar `UsuarioService`:
   - manter busca textual direta no banco, pois pesquisa por termo nao combina bem com snapshot completo;
   - usar snapshot ou cache por chave para autorizacoes/perfis, evitando duas fontes equivalentes;
   - revisar `buscarPorUnidadeLotacao` e consultas por titulo conforme volume real.
5. Avaliar `ValidadorDadosOrganizacionais` para consumir snapshots quando nao precisar de SQL diagnostico especifico.

### Criterios de aceite

1. O refresh de cache nao consulta `VW_UNIDADE` varias vezes para construir derivados.
2. Leituras repetidas de arvore, mapas e responsabilidades nao preparam novas queries de views.
3. Os testes de budget passam com limites iguais ou melhores que os atuais.

## Fase 3 - Centralizar invalidacao e refresh

1. Criar um unico ponto de invalidacao organizacional, por exemplo `CacheOrganizacaoService` ou evoluir
   `AgendadorRefreshCache` para uma API mais clara.
2. Substituir chamadas espalhadas de `@CacheEvict` por operacoes semanticamente claras:
   - `invalidarOrganizacaoCompleta`;
   - `invalidarResponsabilidades`;
   - `invalidarPerfisUsuario`;
   - `invalidarMapasVigentes`.
3. Garantir que `criarAtribuicaoTemporaria` invalide responsabilidades, arvore/elegibilidade, diagnostico e envie SSE.
4. Garantir que alteracoes de administrador invalidem autorizacoes/perfis, diagnostico e enviem SSE quando afetarem UI.
5. Garantir que `definirMapaVigente` invalide unidades com mapa e arvore de elegibilidade.
6. Evitar refresh dentro da transacao que fez a escrita; se necessario, publicar evento apos commit.

### Criterios de aceite

1. Toda escrita local que altera dados organizacionais dispara invalidacao apropriada.
2. O frontend recebe evento quando sua cache de sessao pode ficar obsoleta.
3. Nao ha combinacao de caches derivados stale depois de limpar apenas o cache base.

## Fase 4 - Ajustar SSE e cache de frontend

1. Corrigir `useCacheSync` para nao fechar permanentemente o `EventSource` no primeiro erro.
2. Manter fechamento explicito apenas no cleanup do watcher de sessao.
3. Validar se o evento `org-cache-refreshed` deve limpar:
   - cache de arvore de unidade;
   - diagnostico organizacional;
   - outros stores que usem usuario, perfil ou responsabilidade.
4. Considerar evento mais especifico apenas se houver excesso de reload no frontend.

### Criterios de aceite

1. Falha temporaria da conexao SSE nao desativa a sincronizacao da sessao.
2. Troca de usuario/perfil continua encerrando corretamente a conexao antiga.
3. Testes do composable cobrem evento recebido, cleanup e erro transitorio.

## Fase 5 - Limpeza de caches redundantes

1. Remover `CACHE_USUARIO_PERFIS` se continuar sem caminho real de producao.
2. Remover caches de view que nao forem usados apos a Fase 2.
3. Revisar nomes de cache para separar claramente:
   - snapshot de view;
   - cache derivado;
   - cache por chave.
4. Atualizar testes que validam apenas interacoes mecanicas para validar comportamento observavel.

### Criterios de aceite

1. Todo cache declarado em `CacheConfig` tem uso real e teste que justifique sua existencia.
2. Nao ha aquecimento de cache sem consumidor.
3. O refresh periodico faz apenas o trabalho necessario.

## Testes e verificacao

### Backend unitario

Executar durante as fases:

```bash
./gradlew :backend:test --tests sgc.organizacao.service.CacheViewsOrganizacaoServiceTest
./gradlew :backend:test --tests sgc.comum.cache.AgendadorRefreshCacheTest
./gradlew :backend:test --tests sgc.organizacao.service.UnidadeHierarquiaServiceTest
./gradlew :backend:test --tests sgc.organizacao.service.ResponsavelUnidadeServiceTest
./gradlew :backend:test --tests sgc.organizacao.UsuarioServiceTest
```

### Backend integracao

Executar apos consolidar consumidores do snapshot:

```bash
./gradlew :backend:test --tests sgc.integracao.OrganizacaoViewsQueryBudgetIntegrationTest
./gradlew :backend:test --tests sgc.integracao.ProcessoSubprocessoViewsQueryBudgetIntegrationTest
```

### Frontend

Executar apos ajuste do SSE:

```bash
npm run test:unit -- useCacheSync
npm run typecheck
npm run lint
```

### Dashboard de QA

Atualizar snapshot se a mudanca mexer em testes, lint, typecheck ou fluxo E2E:

```bash
npm run qa:dashboard
```

## Riscos e cuidados

1. Nao cachear entidade JPA quando um record de leitura resolver.
2. Usar filtro em memoria para leituras estruturais do snapshot; manter cuidado especial apenas com pesquisa textual,
   onde ordenacao, limite e semantica da query precisam ser preservados.
3. Nao esconder inconsistencias das views externas com deduplicacao silenciosa em fluxos que precisam diagnosticar
   duplicidade.
4. Nao recarregar caches dentro da mesma transacao de escrita se isso puder ler estado antes do commit.
5. Nao reduzir limites dos testes de budget antes de confirmar o novo comportamento em H2 e Oracle.

## Sequencia sugerida de commits

1. Consolidar snapshot e tipos de leitura imutaveis.
2. Migrar hierarquia/unidades para consumir snapshot.
3. Migrar responsabilidades e perfis/autorizacoes.
4. Centralizar invalidacao e eventos.
5. Ajustar SSE/frontend.
6. Remover caches mortos e atualizar testes de budget.

## Definicao de pronto

1. `CacheViewsOrganizacaoService` e fonte real ou foi removido.
2. Caches derivados nao consultam diretamente a mesma view ja carregada no snapshot.
3. Escritas locais invalidam todos os caches afetados.
4. SSE permanece funcional apos erro transitorio.
5. Testes unitarios, budget de queries e typecheck/lint relevantes passam.
6. O plano de cache fica refletido no codigo, nao apenas em comentarios.
