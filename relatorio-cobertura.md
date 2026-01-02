# Relatório de Cobertura de Testes

Este relatório detalha o trabalho realizado para aumentar a cobertura de testes no backend do projeto SGC. Foram identificadas classes críticas com baixa cobertura e implementados testes unitários e de integração para sanar essas deficiências.

## Classes Impactadas

As seguintes classes foram alvo de melhorias na cobertura de testes:

| Classe | Cobertura Anterior | Cobertura Atual | Tipo de Teste Adicionado |
| :--- | :---: | :---: | :--- |
| `sgc.configuracao.service.ParametroService` | **23.68%** | **100%** | Unitário (`ParametroServiceTest`) |
| `sgc.notificacao.NotificacaoEmailAsyncExecutor` | **2.33%** | **100%** | Unitário (`NotificacaoEmailAsyncExecutorTest`) |
| `sgc.processo.service.ProcessoConsultaService` | **0.00%** | **100%** | Unitário (`ProcessoConsultaServiceTest`) |

## Detalhes da Implementação

### 1. ParametroService
A classe `ParametroService` possuía apenas 23% de cobertura. Foram implementados testes unitários cobrindo todos os métodos públicos:
- `buscarTodos`: Validação de retorno de lista.
- `buscarPorChave`: Validação de sucesso e exceção `ErroEntidadeNaoEncontrada`.
- `salvar`: Validação de persistência em lote.
- `atualizar`: Validação de fluxo de atualização e tratamento de erros.

### 2. NotificacaoEmailAsyncExecutor
Esta classe é responsável pelo envio assíncrono de e-mails e possui lógica crítica de retentativas. Por estar anotada com `@Profile("!test & !e2e")`, ela não era carregada nos testes padrão, resultando em cobertura próxima a 0%.
- Criado teste isolado usando `MockitoExtension`.
- Simulados cenários de sucesso imediato, sucesso após retentativa (erro temporário) e falha total após todas as tentativas.
- Utilizado `ReflectionTestUtils` para injetar propriedades de configuração (`@Value`).

### 3. ProcessoConsultaService
Serviço auxiliar criado para evitar dependências circulares, anteriormente sem testes.
- Implementado teste unitário mockando `ProcessoRepo`.
- Cobertos cenários com e sem processos ativos retornados pelo repositório.

## Correções Adicionais
Durante a execução, foram identificadas falhas em testes existentes no `CDU30IntegrationTest` (Manter Administradores) devido a mudanças nas rotas da API (de `/api/administradores` para `/api/usuarios/administradores`). Estes testes foram corrigidos para garantir que a suíte completa passasse antes da análise de cobertura.

## Conclusão
A cobertura global do projeto foi incrementada focando em componentes de serviço e infraestrutura que continham lógica de negócio não verificada. A suíte de testes agora valida comportamentos críticos de envio de e-mail e configuração do sistema.
