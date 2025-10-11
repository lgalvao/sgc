# Relatório de Revisão de Código - SGC

Este relatório detalha os resultados da revisão de código realizada no backend do sistema SGC, com foco em segurança, arquitetura, performance, e conformidade com as convenções do projeto.

## Seção 1: Issues Críticas

### 1.1. [Segurança] Exposição de Perfil de Usuário na API

**Localização:** `sgc.processo.ProcessoControle.java`

**Endpoint:** `GET /api/processos/{id}/detalhes`

**Descrição:**
O endpoint recebe o perfil do usuário (`perfil`) como um `RequestParam`. Isso representa uma falha de segurança crítica, pois permite que um usuário mal-intencionado eleve seus próprios privilégios simplesmente alterando o valor do parâmetro na URL (ex: `?perfil=ADMIN`). A autorização nunca deve depender de dados fornecidos pelo cliente.

**Recomendação:**
Remover o parâmetro `perfil` da requisição. A lógica de autorização no `ProcessoService.obterDetalhes` deve ser refatorada para obter o perfil do usuário diretamente do contexto de segurança do Spring (`SecurityContextHolder`), que é um repositório seguro para as informações do usuário autenticado.

## Seção 2: Issues de Alta Prioridade

### 2.1. [Performance] Múltiplas Chamadas ao Banco de Dados em Loop (N+1 Query)

**Localização:** `sgc.processo.ProcessoService.java`

**Métodos:**
1.  `validarUnidadesComMapasVigentes(List<Long> codigosUnidades)`
2.  `enviarNotificacoesDeFinalizacao(Processo processo)`

**Descrição:**
- No método `validarUnidadesComMapasVigentes`, o código itera sobre as unidades para verificar a existência de mapas, resultando em múltiplas consultas ao banco.
- No método `enviarNotificacoesDeFinalizacao`, o código itera sobre a lista de `subprocessos` e, dentro do loop, faz chamadas individuais ao `sgrhService` para buscar responsáveis e usuários. Isso resulta em N+1 chamadas a um serviço externo, degradando severamente a performance, especialmente em processos com muitas unidades.

**Recomendação:**
Refatorar ambos os métodos para utilizar uma abordagem de "bulk fetching":
- **`validarUnidadesComMapasVigentes`**: Modificar a consulta no `UnidadeMapaRepo` para verificar todas as unidades de uma só vez e comparar a lista de resultados com a lista de entrada.
- **`enviarNotificacoesDeFinalizacao`**: Coletar todos os IDs de unidades e títulos de responsáveis em listas e fazer chamadas únicas ao `sgrhService` (`buscarResponsaveisUnidades` e `buscarUsuariosPorTitulos`) antes de iniciar o loop de envio de e-mails.

### 2.2. [Arquitetura] Tratamento de Exceção Inadequado com Risco de Inconsistência

**Localização:** `sgc.processo.ProcessoService.java`

**Método:** `enviarNotificacoesDeFinalizacao(Processo processo)`

**Descrição:**
O método possui um bloco `try-catch (Exception ex)` que captura qualquer exceção durante o envio de notificação para uma unidade, registra um log de erro e continua a execução. Embora impeça que uma falha de notificação bloqueie as outras, isso mascara o problema. Mais criticamente, se uma ou mais notificações falharem, o processo geral é considerado `FINALIZADO` com sucesso, mas o sistema fica em um estado inconsistente (processo finalizado, mas notificações pendentes).

**Recomendação:**
A falha no envio de notificações é um efeito colateral e não deve reverter a transação principal de finalização do processo. A abordagem atual de log e continuação é aceitável, mas deve ser aprimorada. Uma solução robusta seria registrar as falhas em uma tabela de "notificações pendentes" ou enfileirar as notificações em um sistema de mensageria (como RabbitMQ ou Kafka) com políticas de `retry` e `dead-letter queue`. Para uma solução imediata e mais simples, o log de erro deve ser mais detalhado, incluindo o ID do processo e da unidade, para facilitar a re-execução manual ou por um job futuro.

## Seção 3: Issues de Média Prioridade

### 3.1. [Melhores Práticas] Lógica de Negócio Complexa em Método Transacional

**Localização:** `sgc.processo.ProcessoService.java`

**Método:** `finalizar(Long id)`

**Descrição:**
O método `finalizar` orquestra várias etapas: validação, atualização de mapas, mudança de estado e envio de notificações. Embora a lógica esteja correta, o método é longo e mistura diferentes responsabilidades, o que dificulta a leitura e a manutenção.

**Recomendação:**
Extrair a lógica de validação (`validarFinalizacaoProcesso`) e a de envio de notificações (`enviarNotificacoesDeFinalizacao`) para métodos privados e mais coesos, como já foi feito. Aplicar o mesmo princípio a outras seções, como `tornarMapasVigentes`. Isso está alinhado com o Princípio da Responsabilidade Única (SRP) e torna o fluxo principal do método `finalizar` mais claro.

### 3.2. [Convenção de Projeto] Nomes de Classes e Métodos em Inglês

**Observação:**
Embora o projeto siga majoritariamente a convenção de usar o português, foram encontradas algumas inconsistências.

**Exemplos:**
- `ProcessoDetalheMapperCustom.java` (deveria ser `ProcessoDetalheMapperCustomizado.java` ou similar)
- `ProcessoMapper.java` (deveria ser `ProcessoConversor.java` ou `MapeadorProcesso.java`)

**Recomendação:**
Padronizar todos os novos artefatos de código para o português. Para o código existente, criar uma tarefa de débito técnico para renomear gradualmente esses arquivos, garantindo que as alterações sejam propagadas para todas as referências.

## Seção 4: Observações Positivas

- **Uso de Eventos de Domínio:** O uso de `ApplicationEventPublisher` para publicar eventos como `ProcessoCriadoEvento` e `ProcessoFinalizadoEvento` é uma excelente prática de design. Isso desacopla os componentes, permitindo que outros módulos (como `notificacao` ou `alerta`) reajam a mudanças no processo sem criar um acoplamento direto com o `ProcessoService`.
- **Validações Claras de Domínio:** O serviço utiliza exceções de domínio customizadas (`ErroProcesso`, `ErroDominioAccessoNegado`), o que torna as regras de negócio explícitas e o tratamento de erros no `Controller` mais limpo.
- **Boa Estrutura de Pacotes:** A divisão de responsabilidades em pacotes (`processo`, `subprocesso`, `mapa`, `unidade`) é clara e segue os princípios de uma arquitetura modular.
- **Uso de DTOs:** O uso de Data Transfer Objects (DTOs) na camada de `Controller` (`CriarProcessoReq`, `ProcessoDto`) é uma boa prática que separa a representação da API do modelo de domínio interno.