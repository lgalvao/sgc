# Sistema de Envio de E-mails - SGC

## Visão Geral
Implementação completa do sistema de envio de e-mails usando Spring Mail, que substitui a implementação simulada (`ServicoNotificacaoSimulado`) em ambientes de produção.

## Arquivos Criados

### 1. ServicoNotificacaoEmail.java
**Localização:** `backend/src/main/java/sgc/notificacao/ServicoNotificacaoEmail.java`

Implementação real do `ServicoNotificacao` com:
- ✅ Envio de e-mails via SMTP.
- ✅ Persistência de notificações no banco de dados antes do envio.
- ✅ Execução assíncrona usando `@Async`.
- ✅ Retentativa automática (3 tentativas com backoff).
- ✅ Validação de formato de e-mail.
- ✅ Logs detalhados.
- ✅ Anotação `@Primary` para substituir `ServicoNotificacaoSimulado` automaticamente.

**Métodos principais:**
- `enviarEmail(String para, String assunto, String corpo)` - Interface padrão.
- `enviarEmailHtml(String para, String assunto, String corpoHtml)` - Para e-mails em HTML.
- `enviarEmailDto(DtoEmail emailDto)` - Usando um DTO.
- `enviarEmailAssincrono(DtoEmail emailDto)` - Execução assíncrona com retentativas.

### 2. ServicoDeTemplateDeEmail.java
**Localização:** `backend/src/main/java/sgc/notificacao/ServicoDeTemplateDeEmail.java`

Serviço para criação de templates HTML específicos para cada caso de uso:
- `criarEmailDeProcessoIniciado()`
- `criarEmailDeCadastroDisponibilizado()`
- `criarEmailDeCadastroDevolvido()`
- `criarEmailDeMapaDisponibilizado()`
- `criarEmailDeMapaValidado()`
- `criarEmailDeProcessoFinalizado()`
- `criarEmailDeProcessoFinalizadoPorUnidade()`

### 3. DtoEmail.java
**Localização:** `backend/src/main/java/sgc/notificacao/DtoEmail.java`

`Record` Java para transferência de dados de e-mail:
```java
public record DtoEmail(
    String destinatario,
    String assunto,
    String corpo,
    boolean html
){}
```

### 4. ConfiguracaoAssincrona.java
**Localização:** `backend/src/main/java/sgc/comum/config/ConfiguracaoAssincrona.java`

Configuração para execução assíncrona com um pool de threads dedicado para e-mails.

## Configurações

### application.yml
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
aplicacao:
  email:
    remetente: ${EMAIL_REMETENTE:sgc@tre-pe.jus.br}
    remetente-nome: "Sistema de Gestão de Competências"
    assunto-prefixo: "[SGC]"
```

## Uso

### Exemplo 1: E-mail simples
```java
@Autowired
private ServicoNotificacao servicoNotificacao;

servicoNotificacao.enviarEmail(
    "usuario@tre-pe.jus.br",
    "Teste",
    "Corpo do e-mail"
);
```

### Exemplo 2: E-mail HTML com template
```java
@Autowired
private ServicoNotificacaoEmail servicoNotificacaoEmail;

@Autowired
private ServicoDeTemplateDeEmail servicoDeTemplateDeEmail;

String corpoHtml = servicoDeTemplateDeEmail.criarEmailDeProcessoIniciado(/*...*/);

servicoNotificacaoEmail.enviarEmailHtml(
    "responsavel@tre-pe.jus.br",
    "Processo Iniciado",
    corpoHtml
);
```

## Notas Importantes
1.  **Prioridade**: `@Primary` garante que `ServicoNotificacaoEmail` seja usado em produção.
2.  **Mock**: `ServicoNotificacaoSimulado` ainda existe para ser usado em testes.
3.  **Erros**: Falhas no envio de e-mail são logadas mas não interrompem o fluxo principal da aplicação.
4.  **Logs**: Todas as operações de envio são registradas para fins de auditoria.
5.  **Banco**: As notificações são persistidas no banco antes da tentativa de envio.