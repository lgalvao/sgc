# Plano de Otimizacao de Views no Backend

## Objetivo

Reduzir o custo de acesso as views organizacionais complexas no backend, sem depender inicialmente de alteracoes no Oracle. O foco deste plano e otimizar:

- modelagem JPA
- formato das consultas
- padrao de carregamento de dados
- cache de aplicacao
- observabilidade das consultas

As views mais sensiveis identificadas em [backend/etc/sql/ddl_views.sql](/C:/sgc/backend/etc/sql/ddl_views.sql) sao:

- `VW_UNIDADE`
- `VW_USUARIO`
- `VW_RESPONSABILIDADE`
- `VW_USUARIO_PERFIL_UNIDADE`

`VW_VINCULACAO_UNIDADE` saiu do escopo de otimizacao JPA nesta etapa porque o mapeamento foi removido do backend e ela nao participa dos fluxos atuais de producao.

## Diagnostico

Hoje o backend mapeia views complexas como entidades JPA com relacionamentos navegaveis. Isso aparece principalmente em:

- [backend/src/main/java/sgc/organizacao/model/Unidade.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Unidade.java)
- [backend/src/main/java/sgc/organizacao/model/Usuario.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Usuario.java)
- [backend/src/main/java/sgc/organizacao/model/Responsabilidade.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Responsabilidade.java)
- [backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java)

Esse desenho traz alguns custos:

- a view ja chega derivada e pesada do banco
- o Hibernate adiciona joins, proxies e gerenciamento de contexto de persistencia
- servicos passam a navegar no grafo ORM em vez de consumir somente os dados necessarios
- consultas com `join fetch` sobre views encadeadas aumentam muito o trabalho do banco
- metodos amplos, como `findAll()`, ficam caros mesmo para casos de uso simples

O ponto mais sensivel hoje e [backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java), especialmente o metodo `listarTodasComHierarquia()`, que faz `left join fetch` de relacionamentos sobre `VW_UNIDADE`, `VW_RESPONSABILIDADE` e `VW_USUARIO`.

## Estado Atual Confirmado

Levantamento no backend em 2026-04-04:

- a arvore de unidades e a arvore com elegibilidade usam [backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java), com carga ampla de hierarquia
- o login usa `VW_USUARIO_PERFIL_UNIDADE` e navega pela associacao para `Unidade`
- `ComumRepo` ainda permite `find` e criteria generica sobre views pesadas, escondendo o custo real do acesso
- o diagnostico organizacional ja usa JDBC direto para perfis, mas ainda depende de carga ampla de unidades
- nao ha cache declarativo no modulo organizacional neste momento

## Rodada Atual

### Melhoria iniciada

- eliminar N+1 residual em `VW_RESPONSABILIDADE`, `VW_USUARIO` e `VW_UNIDADE`
- trocar relatorios e atribuicoes temporarias para carga em lote
- reaproveitar caches do Spring para verificacoes de hierarquia sem consulta por nivel
- remover lookup repetido de `VW_UNIDADE` em painel, historico de analises e inicio de processo
- substituir o uso restante de `listarTodasComHierarquia()` em `ProcessoService` pelo mapa cacheado de hierarquia
- centralizar a unidade `ADMIN` em metodo dedicado com cache agressivo do Spring
- preservar os contratos HTTP e os DTOs atuais

### Fora desta rodada

- remocao ampla das associacoes JPA restantes
- observabilidade fina por metrica de query no banco

## Diretriz Principal

Views complexas devem ser tratadas como modelos de leitura, e nao como entidades ricas com relacionamentos navegaveis.

Na pratica:

- manter `@Immutable` para reforcar somente leitura
- reduzir ou eliminar associacoes JPA entre views
- preferir consultas por projecao ou DTO
- carregar apenas colunas necessarias para cada caso de uso
- mover a composicao final para a camada de servico

## Estrategia de Otimizacao

## 1. Simplificar o mapeamento das views

### Problema

As entidades de view hoje possuem relacionamentos como:

- `@ManyToOne`
- `@OneToOne`
- `@OneToMany`

Isso aparece, por exemplo, em:

- [backend/src/main/java/sgc/organizacao/model/Unidade.java#L48](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Unidade.java#L48)
- [backend/src/main/java/sgc/organizacao/model/Usuario.java#L55](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Usuario.java#L55)
- [backend/src/main/java/sgc/organizacao/model/Responsabilidade.java#L41](/C:/sgc/backend/src/main/java/sgc/organizacao/model/Responsabilidade.java#L41)
- [backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java#L34](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioPerfil.java#L34)

### Acao recomendada

Transformar gradualmente essas entidades em modelos mais achatados:

- manter os campos escalares da propria view
- substituir referencias de objeto por codigos simples quando possivel
- evitar colecoes derivadas de view, como `subunidades`

### Resultado esperado

- menos joins implicitos
- menos risco de `lazy loading` acidental
- consultas mais previsiveis
- menor custo de hidratacao no Hibernate

## 2. Trocar entidades navegaveis por projecoes e DTOs

### Problema

Casos de uso de leitura normalmente nao precisam de um grafo completo. Eles precisam de um subconjunto estavel de campos.

### Acao recomendada

Criar consultas dedicadas por caso de uso usando:

- projecoes por interface do Spring Data
- DTOs com `record`
- JPQL enxuta
- consulta nativa quando simplificar a leitura

### Exemplos de uso

Para hierarquia de unidades:

- `codigo`
- `nome`
- `sigla`
- `tipo`
- `situacao`
- `unidadeSuperiorCodigo`

Para responsavel de unidade:

- `unidadeCodigo`
- `tituloTitular`
- `usuarioTituloResponsavel`
- `tipoResponsabilidade`
- `dataInicio`
- `dataFim`

Para perfis:

- `usuarioTitulo`
- `perfil`
- `unidadeCodigo`

### Resultado esperado

- SQL menor
- menor trafego de dados entre banco e aplicacao
- menos objetos em memoria
- servicos mais simples e explicitos

## 3. Reescrever consultas amplas que usam `join fetch`

### Problema

O metodo [backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java#L27](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java#L27) busca todas as unidades ativas e ainda faz fetch de:

- unidade superior
- responsabilidade
- usuario da responsabilidade

Em views encadeadas, isso tende a ampliar muito o custo.

### Acao recomendada

Substituir consultas amplas por duas ou tres consultas menores e deterministicas:

1. buscar a estrutura basica da hierarquia
2. buscar responsabilidades em lote
3. buscar usuarios necessarios em lote

Depois montar o resultado em memoria no servico.

### Resultado esperado

- menos explosao de join
- melhor controle do volume de dados
- menor risco de consulta monstruosa gerada pelo ORM

## 4. Eliminar `findAll()` em views pesadas

### Problema

Chamadas genericas em views grandes podem trazer muito mais dados do que o caso de uso precisa. Exemplo:

- [backend/src/main/java/sgc/organizacao/service/UsuarioService.java#L46](/C:/sgc/backend/src/main/java/sgc/organizacao/service/UsuarioService.java#L46)

### Acao recomendada

Substituir `findAll()` por:

- paginação
- filtros obrigatorios
- metodos especificos por necessidade funcional

### Resultado esperado

- menor custo por request
- menor consumo de memoria
- menor risco de degradacao conforme a base crescer

## 5. Introduzir cache de aplicacao para leituras quase estaticas

### Problema

Hierarquia organizacional, perfis e responsabilidades sao lidos com frequencia e tendem a mudar pouco durante o dia.

### Acao recomendada

Adicionar cache com Spring Cache nas leituras mais reutilizadas, especialmente em:

- [backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java#L29](/C:/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java#L29)
- [backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java#L78](/C:/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java#L78)
- consultas de perfis por usuario
- consultas de responsavel atual por unidade

### Recomendacoes de desenho

- usar chaves de cache pequenas e deterministicas
- definir invalidacao explicita quando houver alteracoes internas que impactem as views consumidas
- evitar cache em metodos muito genericos ou pouco repetidos

### Resultado esperado

- menos repeticao de leitura nas mesmas views
- latencia melhor em endpoints organizacionais
- alivio de carga no banco

## 6. Separar claramente leitura externa de escrita interna

### Problema

Hoje o ORM sugere que tudo faz parte de um unico grafo de dominio, mas parte importante desses dados vem de views derivadas de sistemas externos.

### Acao recomendada

Adotar uma divisao mais explicita:

- tabelas internas do SGC continuam como entidades JPA completas
- views externas passam a ser somente fonte de leitura
- a composicao entre dados internos e externos ocorre nos servicos

### Resultado esperado

- menor acoplamento
- modelo mais honesto com a origem dos dados
- manutencao mais simples

## 7. Medir antes e depois

### Problema

Sem medicao, fica dificil comprovar ganho e priorizar os pontos certos.

### Acao recomendada

Levantar pelo menos:

- tempo medio das consultas principais
- quantidade de queries por request
- volume de linhas retornadas
- endpoints que mais acessam essas views

### Ferramentas possiveis

- logs SQL com tempo
- metricas por metodo de servico
- datasource proxy ou observabilidade equivalente
- analise de endpoints mais lentos

### Resultado esperado

- priorizacao orientada por evidencias
- comparacao objetiva antes e depois
- melhor conversa com o DBA na segunda fase

## Ordem de Implementacao Sugerida

## Fase 1. Ganho rapido com baixo risco

- mapear os endpoints e servicos que mais usam `VW_UNIDADE`, `VW_USUARIO` e `VW_RESPONSABILIDADE`
- remover `findAll()` desnecessarios
- adicionar paginação onde couber
- adicionar medicao basica de tempo e quantidade de consultas
- introduzir cache para hierarquia e leituras repetidas

## Fase 2. Refactor estrutural de leitura

- substituir `listarTodasComHierarquia()` por projecao enxuta nos fluxos de arvore
- criar DTOs especificos para responsaveis de unidade
- criar DTOs especificos para perfis por usuario
- reduzir relacionamentos JPA entre views

## Fase 3. Consolidacao

- revisar servicos para eliminar navegacao de grafo ORM sobre views
- padronizar consultas de leitura por caso de uso
- documentar quais views permanecem como entidade simples e quais passam a projecao apenas

## Primeiros Alvos no Codigo

Os pontos mais promissores para iniciar sao:

- [backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UnidadeRepo.java)
- [backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java](/C:/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java)
- [backend/src/main/java/sgc/organizacao/service/ResponsavelUnidadeService.java](/C:/sgc/backend/src/main/java/sgc/organizacao/service/ResponsavelUnidadeService.java)
- [backend/src/main/java/sgc/organizacao/service/UsuarioService.java](/C:/sgc/backend/src/main/java/sgc/organizacao/service/UsuarioService.java)
- [backend/src/main/java/sgc/organizacao/model/UsuarioRepo.java](/C:/sgc/backend/src/main/java/sgc/organizacao/model/UsuarioRepo.java)

## Decisoes Arquiteturais Recomendadas

- nao usar entidade JPA rica para view complexa
- nao usar associacoes bidirecionais entre views, salvo necessidade muito clara
- preferir leitura por caso de uso em vez de reuso excessivo de entidade
- usar cache somente em dados realmente estaveis e de alto reuso
- manter a camada de servico responsavel pela composicao final

## Riscos e Cuidados

- refactors parciais podem manter parte do custo se o servico continuar navegando em relacionamentos antigos
- cache sem invalidacao clara pode gerar leitura desatualizada
- consultas projetadas demais podem se multiplicar sem padronizacao minima

Para reduzir esses riscos:

- migrar por fluxo funcional, nao por classe isolada
- medir impacto por endpoint
- documentar os novos padroes de leitura no modulo organizacional

## Definicao de Sucesso

Considerar a otimizacao bem-sucedida quando:

- os principais endpoints organizacionais reduzirem tempo medio de resposta
- cair a quantidade de queries por request
- desaparecer a dependencia de `join fetch` sobre views complexas
- leituras repetidas de hierarquia e perfis passarem a ser atendidas por cache ou por consultas enxutas

## Proximo Passo Sugerido

Iniciar pela hierarquia de unidades:

1. criar uma projecao simples para leitura de `VW_UNIDADE`
2. refatorar a montagem de arvore em [backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/service/UnidadeHierarquiaService.java)
3. remover a dependencia de `join fetch` entre `VW_UNIDADE`, `VW_RESPONSABILIDADE` e `VW_USUARIO` nesse fluxo
4. medir o ganho

Esse caminho tem boa chance de entregar impacto rapido com risco controlado.
