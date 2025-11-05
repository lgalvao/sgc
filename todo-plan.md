# Plano de Ação para TODOs

Este documento detalha o plano de ação para os comentários `// TODO` encontrados no código.

## Backend

### `sgc.alerta`

- **Arquivo:** `AlertaService.java`
- **TODO:** `// TODO essa exceção precisa subir pra camada de controle`
- **Análise:** A exceção lançada em caso de falha ao marcar um alerta como lido ou não lido é uma `UnsupportedOperationException`, que não é tratada de forma específica, resultando em um erro 500. É necessário criar uma exceção de negócio específica e tratá-la no `RestExceptionHandler` para retornar um status HTTP mais apropriado.
- **Plano:**
    1. Criar uma exceção de negócio específica, como `AlteracaoStatusAlertaException`.
    2. Atualizar o `AlertaService` para lançar `AlteracaoStatusAlertaException` em vez de `UnsupportedOperationException`.
    3. Adicionar um tratamento para `AlteracaoStatusAlertaException` no `RestExceptionHandler`, retornando um status HTTP 409 (Conflict).
    4. Criar um teste de integração para simular a falha e garantir que o status HTTP 409 seja retornado.

### `sgc.comum`

- **Arquivo:** `BeanUtil.java`
- **TODO:** `// TODO essa classe está me cheirando a gambiarra. Precisa mesmo?`
- **Análise:** A classe `BeanUtil` permite o acesso a beans do Spring de forma estática, o que é um antipadrão e pode indicar problemas de design. Seu uso principal é na fábrica de contexto de segurança para testes (`WithMockChefeSecurityContextFactory`), o que sugere que a necessidade de acessar beans gerenciados pelo Spring em classes não gerenciadas pode ser a causa do problema.
- **Plano:**
    1. Investigar a fundo o uso de `BeanUtil`, principalmente na `WithMockChefeSecurityContextFactory`.
    2. Buscar alternativas para a injeção de dependência na `WithMockChefeSecurityContextFactory`.
    3. Se possível, refatorar a `WithMockChefeSecurityContextFactory` para que o Spring a gerencie, eliminando a necessidade de `BeanUtil`.
    4. Após a refatoração, remover a classe `BeanUtil`.

- **Arquivo:** `HealthController.java`
- **TODO:** `// TODO Verificar se é usado mesmo. Senão, apagar.`
- **Análise:** O `HealthController` expõe um endpoint `/health` que retorna "OK". É preciso verificar se esse endpoint está sendo utilizado por algum sistema de monitoramento ou se é redundante.
- **Plano:**
    1. Pesquisar na base de código e na documentação do projeto por referências ao endpoint `/health`.
    2. Se nenhuma referência for encontrada, remover o `HealthController`.

- **Arquivo:** `Config.java`
- **TODO:** `// TODO verificar se nao é melhor juntar com 'ConfigAplicacao'`
- **Análise:** A classe `Config` e a `ConfigAplicacao` podem ter responsabilidades semelhantes, e a unificação poderia simplificar a configuração. É preciso analisar o propósito de cada uma e avaliar se a fusão é viável.
- **Plano:**
    1. Analisar as responsabilidades de `Config` e `ConfigAplicacao`.
    2. Se a fusão for viável, mover as configurações de `Config` para `ConfigAplicacao`.
    3. Remover a classe `Config` e atualizar as referências, se houver.

### `sgc.comum.erros`

- **Arquivo:** `ErroNegocio.java`
- **TODO:** `// TODO em vez dessa classe geral demais, melhor criar erros mais específicos.`
- **Análise:** A exceção `ErroNegocio` é muito genérica. É preferível criar exceções mais específicas para cada regra de negócio, o que melhora a clareza do código e permite um tratamento de erros mais granular.
- **Plano:**
    1. Identificar todos os locais onde `ErroNegocio` é lançada.
    2. Para cada caso, criar uma exceção específica que descreva melhor o erro (ex: `MapaNaoEncontradoException`, `AtividadeSemCompetenciaException`).
    3. Substituir o lançamento de `ErroNegocio` pelas novas exceções.
    4. Atualizar o `RestExceptionHandler` para tratar as novas exceções, retornando os status HTTP apropriados.

- **Arquivo:** `ErroSubApi.java`
- **TODO:** `// TODO precisa mesmo esse erro? Se sim, documentar melhor.`
- **Análise:** A classe `ErroSubApi` não parece ser utilizada e sua finalidade não é clara. É preciso verificar se há algum uso para ela.
- **Plano:**
    1. Procurar por usos da classe `ErroSubApi` no projeto.
    2. Se não houver usos, remover a classe.
    3. Se houver usos, documentar detalhadamente sua finalidade no Javadoc da classe.

- **Arquivo:** `RestExceptionHandler.java`
- **TODO:** `// TODO essa classe me parece muito repetitiva. E os tratamentos não estão específicos o suficiente.`
- **Análise:** O `RestExceptionHandler` contém código repetitivo e tratamentos de erro genéricos. A refatoração pode simplificar a classe e melhorar a qualidade das respostas de erro da API.
- **Plano:**
    1. Criar um método privado para a construção do objeto `ApiError`, evitando a repetição de código.
    2. Revisar os tratamentos de exceção para garantir que cada exceção seja mapeada para um status HTTP apropriado e específico.
    3. Considerar a criação de exceções de negócio mais específicas para substituir os tratamentos genéricos.

### `sgc.painel`

- **Arquivo:** `PainelService.java`
- **TODO:** `// TODO usar exceção específica do sistema. Criar se precisar.`
- **Análise:** O serviço lança uma `RuntimeException` genérica. É melhor usar uma exceção de negócio específica.
- **Plano:**
    1. Criar uma exceção de negócio, como `PainelException`.
    2. Atualizar o `PainelService` para lançar `PainelException`.
    3. Adicionar um tratamento para `PainelException` no `RestExceptionHandler`.

### `sgc.unidade.model`

- **Arquivo:** `Unidade.java`
- **TODO:** `// TODO em vez de criar todos os esses construtores diferentes, fazer os clientes usarem sempre o builder.`
- **Análise:** A classe `Unidade` possui múltiplos construtores, o que pode ser confuso. O uso do padrão Builder pode tornar a criação de instâncias mais clara e flexível.
- **Plano:**
    1. Adicionar a anotação `@Builder` do Lombok à classe `Unidade`.
    2. Substituir o uso dos construtores pelo builder em todo o código.
    3. Remover os construtores antigos, se possível, ou torná-los privados.

### `sgc.util`

- **Arquivo:** `HtmlUtils.java`
- **TODO:** `// TODO me parece inutil essa classe.`
- **Análise:** A classe `HtmlUtils` parece não ter utilidade. É preciso confirmar se ela é usada em algum lugar.
- **Plano:**
    1. Pesquisar por usos da classe `HtmlUtils`.
    2. Se não for utilizada, removê-la.

- **Arquivo:** `E2eTestController.java`
- **TODO:** `// TODO verificar se precisamos mesmo desse controller` e `// TODO Esse trecho é duplicado a seguir`
- **Análise:** Este controlador parece ser usado apenas para testes end-to-end e contém código duplicado.
- **Plano:**
    1. Confirmar se o `E2eTestController` ainda é necessário para os testes E2E. Se não for, removê-lo.
    2. Se for necessário, refatorar o código duplicado para um método privado.

### `sgc.analise`

- **Arquivo:** `AnaliseController.java`
- **TODO:** `// TODO este tratamento está muito geral. E nem me parece bem um erro de negócio` e `// TODO este código repete quase igual no método 'criarAnaliseValidacao'`
- **Análise:** O controlador possui tratamentos de erro genéricos e código duplicado.
- **Plano:**
    1. Substituir `ErroNegocio` por exceções mais específicas.
    2. Refatorar o código duplicado dos métodos `criarAnaliseCadastro` e `criarAnaliseValidacao` para um método privado.

### `sgc.atividade`

- **Arquivo:** `AtividadeService.java`
- **TODO:** `// TODO isso realmente vai acontecer, se a segurança estiver configurada corretamemte?`
- **Análise:** O serviço verifica se o usuário autenticado existe, o que pode ser redundante se a segurança já garante isso.
- **Plano:**
    1. Analisar a configuração de segurança para confirmar se ela já garante que o usuário autenticado sempre existe.
    2. Se a verificação for redundante, removê-la.

- **Arquivo:** `AtividadeController.java`
- **TODO:** `// TODO remover essa sanitização. Está poluindo`
- **Análise:** A sanitização de HTML está sendo feita no controlador, o que pode não ser o local ideal.
- **Plano:**
    1. Mover a lógica de sanitização para a camada de serviço ou para um desserializador customizado do Jackson.
    2. Remover a sanitização do controlador.

- **Arquivo:** `AtividadeDto.java`
- **TODO:** `// TODO mudar para Builder e rever esse sanitizado aqui, parece poluição`
- **Análise:** O DTO pode ser melhorado com o uso do padrão Builder, e a sanitização deve ser removida.
- **Plano:**
    1. Adicionar a anotação `@Builder` ao DTO.
    2. Remover a lógica de sanitização.

- **Arquivo:** `ConhecimentoDto.java`
- **TODO:** `// TODO sanitizar aqui parece ruído!`
- **Análise:** A sanitização no DTO não é o ideal.
- **Plano:**
    1. Remover a lógica de sanitização do DTO.

### `sgc.sgrh.dto`

- **Arquivo:** `ServidorDto.java`
- **TODO:** `// TODO esse dto deve ser removido, sendo usado apenas o UsuarioDto`
- **Análise:** O `ServidorDto` é redundante e deve ser substituído pelo `UsuarioDto`.
- **Plano:**
    1. Substituir todas as ocorrências de `ServidorDto` por `UsuarioDto`.
    2. Remover a classe `ServidorDto`.

### `sgc.mapa`

- **Arquivo:** `TipoImpactoCompetencia.java`
- **TODO:** `// TODO as constantes reais nao estao sendo usadas. Parece indicar áreas nao implementadas. Investigar.` e `// TODO Não existe isso!`
- **Análise:** O enum contém valores que não parecem ser usados, indicando funcionalidade incompleta.
- **Plano:**
    1. Investigar a funcionalidade de impacto de competência.
    2. Implementar a lógica de negócio que utiliza os valores do enum ou remover os valores não utilizados.

- **Arquivo:** `MapaCompletoDto.java`
- **TODO:** `// TODO precisa mesmo de um MapaDto e de um MapaCompletoDto?`
- **Análise:** A existência de dois DTOs para mapa pode ser redundante.
- **Plano:**
    1. Analisar o uso de `MapaDto` e `MapaCompletoDto`.
    2. Se possível, unificar os dois DTOs em um só.

- **Arquivo:** `ImpactoMapaDto.java`
- **TODO:** `// TODO tentar maneira mais elegante de verificar se estao vazias?`
- **Análise:** O método `temImpactos` pode ser simplificado.
- **Plano:**
    1. Refatorar o método `temImpactos` para ser mais conciso, usando a sintaxe do Java moderno.

- **Arquivo:** `CompetenciaMapaDto.java`
- **TODO:** `// TODO verificar a necessidade disso:`
- **Análise:** A anotação `@JsonInclude(JsonInclude.Include.NON_NULL)` pode ser desnecessária.
- **Plano:**
    1. Verificar se a anotação é realmente necessária. Se não for, removê-la.

- **Arquivo:** `visualizacao/AtividadeDto.java`
- **TODO:** `// TODO essa classe e todo esse pacote estao me parecendo redundantes. Se nao for redundante, mude o nome e documente.`
- **Análise:** O pacote `visualizacao` parece redundante.
- **Plano:**
    1. Analisar se o DTO de visualização é realmente necessário.
    2. Se for, renomeá-lo para algo mais claro e documentar sua finalidade.
    3. Se não for, remover o pacote `visualizacao`.

- **Arquivo:** `MapaDto.java`
- **TODO:** `// TODO tem necessidade desses AccesslLevel aqui?`
- **Análise:** O uso de `AccessLevel` pode ser desnecessário.
- **Plano:**
    1. Verificar a necessidade do `AccessLevel` e removê-lo se não for essencial.

- **Arquivo:** `MapaIntegridadeService.java`
- **TODO:** `// TODO essa validação está me parecendo inócua. Parece indicar partes ainda nao implementadas!`
- **Análise:** A validação de integridade do mapa parece incompleta.
- **Plano:**
    1. Implementar as validações de integridade do mapa que estão faltando.

- **Arquivo:** `ImpactoCompetenciaService.java`
- **TODO:** `// TODO Não existe isso! Tem que ser algum dos tipos acima`
- **Análise:** O tratamento para o tipo de impacto `INCLUSAO_CONHECIMENTO` está ausente.
- **Plano:**
    1. Implementar o tratamento para o tipo de impacto `INCLUSAO_CONHECIMENTO`.

- **Arquivo:** `MapaVisualizacaoService.java`
- **TODO:** `// TODO nao é precipitadao lançar essa exceção aqui? Nem deveria acontecer se as camadas de cima fizerem sua parte.`
- **Análise:** A exceção lançada pode ser um sinal de que as camadas superiores não estão validando os dados corretamente.
- **Plano:**
    1. Adicionar validações nas camadas de serviço e controle para garantir que o mapa sempre seja encontrado.
    2. Remover o lançamento da exceção, se a validação for garantida.

### `sgc.subprocesso`

- **Arquivo:** `SubprocessoValidacaoController.java`
- **TODO:** `// TODO limpar a sanitização desse controlador`
- **Análise:** A sanitização de HTML deve ser removida do controlador.
- **Plano:**
    1. Mover a lógica de sanitização para a camada de serviço ou para um desserializador customizado.
    2. Remover a sanitização do controlador.

- **Arquivo:** `MapaAjusteDto.java`
- **TODO:** `// TODO Parametros demais! Mudar para @Builder`
- **Análise:** O construtor do DTO tem muitos parâmetros.
- **Plano:**
    1. Adicionar a anotação `@Builder` ao DTO.
    2. Usar o builder em vez do construtor.

- **Arquivo:** `SubprocessoNotificacaoService.java`
- **TODO:** `// TODO esta classe está usando muitos strings fixos. Mudar para usar templates do thymeleaf`, `// TODO em vez de IllegalArgumentException usar exceções de negócio específicas`, `// TODO usar builder par instanciar os alertas. Considerar criar método auxiliar: codigo esta repetitivo`
- **Análise:** O serviço de notificação tem vários pontos a serem melhorados.
- **Plano:**
    1. Substituir as strings fixas por templates do Thymeleaf para a geração de e-mails.
    2. Trocar `IllegalArgumentException` por exceções de negócio específicas.
    3. Usar o padrão Builder para criar alertas e refatorar o código repetitivo para métodos auxiliares.

- **Arquivo:** `SubprocessoMapaService.java`
- **TODO:** `// TODO usar exceções mais específicas nessa classe toda`, `// TODO Estranho passar o destino duas vezes nesse construtor. Bug?`
- **Análise:** O serviço de mapa de subprocesso precisa de melhorias no tratamento de erros e tem um possível bug.
- **Plano:**
    1. Substituir as exceções genéricas por exceções de negócio específicas.
    2. Investigar o construtor do `SubprocessoIniciadoEvent` e corrigir o possível bug, se confirmado.
