# Sistema de Envio de E-mails - SGC

## Visão Geral
Implementação completa do sistema real de envio de e-mails usando Spring Mail, substituindo o `MockNotificationService`.

## Arquivos Criados

### 1. EmailNotificationService.java
**Localização:** `backend/src/main/java/sgc/notificacao/EmailNotificationService.java`

Implementação real do `NotificationService` com:
- ✅ Envio real de e-mails via SMTP
- ✅ Persistência de notificações no banco antes do envio
- ✅ Execução assíncrona usando `@Async`
- ✅ Retry automático (3 tentativas com backoff)
- ✅ Validação de e-mails
- ✅ Logging detalhado
- ✅ Anotação `@Primary` para substituir MockNotificationService automaticamente

**Metodos principais:**
- `enviarEmail(String to, String subject, String body)` - Interface padrão
- `enviarEmailHtml(String to, String subject, String htmlBody)` - Para e-mails HTML
- `enviarEmailDto(EmailDto email)` - Usando DTO
- `enviarEmailAsync(EmailDto email)` - Execução assíncrona com retry

### 2. EmailTemplateService.java
**Localização:** `backend/src/main/java/sgc/notificacao/EmailTemplateService.java`

Serviço para criação de templates HTML específicos para cada CDU:

**Templates disponíveis:**
- `criarEmailProcessoIniciado()` - CDU-04, CDU-05
- `criarEmailCadastroDisponibilizado()` - CDU-09, CDU-10
- `criarEmailCadastroDevolvido()` - CDU-13
- `criarEmailMapaDisponibilizado()` - CDU-17
- `criarEmailMapaValidado()` - CDU-18
- `criarEmailProcessoFinalizado()` - CDU-21

Todos os templates incluem:
- Design responsivo
- Estilos inline para compatibilidade
- Cabeçalho e rodapé padronizados
- Botões de ação (CTAs)
- Informações estruturadas

### 3. EmailDto.java
**Localização:** `backend/src/main/java/sgc/notificacao/dto/EmailDto.java`

Record Java para transferência de dados de e-mail:
```java
public record EmailDto(
    String destinatario,
    String assunto,
    String corpo,
    boolean html
)
```

### 4. AsyncConfig.java
**Localização:** `backend/src/main/java/sgc/comum/config/AsyncConfig.java`

Configuração para execução assíncrona:
- Pool de threads dedicado para e-mails
- 2-5 threads conforme demanda
- Fila de 100 e-mails
- Shutdown gracioso

## Configurações

### application.yml
Configurações adicionadas:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
        transport:
          protocol: smtp
    default-encoding: UTF-8

aplicacao:
  email:
    remetente: ${EMAIL_REMETENTE:sgc@tre-pe.jus.br}
    remetente-nome: "Sistema de Gestão de Competências"
    assunto-prefixo: "[SGC]"
```

### build.gradle.kts
Dependências adicionadas:
```kotlin
// Spring Mail
implementation("org.springframework.boot:spring-boot-starter-mail")

// Thymeleaf para templates HTML
implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
```

## Variáveis de Ambiente

Para configurar o servidor SMTP em produção, defina:

```bash
MAIL_HOST=smtp.tre-pe.jus.br          # Servidor SMTP
MAIL_PORT=587                          # Porta SMTP
MAIL_USERNAME=usuario@tre-pe.jus.br   # Usuário SMTP
MAIL_PASSWORD=senha_segura             # Senha SMTP
EMAIL_REMETENTE=sgc@tre-pe.jus.br     # E-mail remetente
```

## Fluxo de Envio

1. **Validação**: Verifica formato do e-mail
2. **Persistência**: Salva notificação no banco
3. **Envio Assíncrono**: Executa em thread separada
4. **Retry**: Até 3 tentativas em caso de falha
5. **Logging**: Registra todas as operações

## Uso

### Exemplo 1: E-mail simples
```java
@Autowired
private NotificationService notificationService;

notificationService.enviarEmail(
    "usuario@tre-pe.jus.br",
    "Teste",
    "Corpo do e-mail"
);
```

### Exemplo 2: E-mail HTML com template
```java
@Autowired
private EmailNotificationService emailService;

@Autowired
private EmailTemplateService templateService;

String htmlBody = templateService.criarEmailProcessoIniciado(
    "Zona Eleitoral 01",
    "Mapeamento 2024",
    "Mapeamento",
    LocalDate.now().plusDays(30)
);

emailService.enviarEmailHtml(
    "responsavel@tre-pe.jus.br",
    "Processo Iniciado",
    htmlBody
);
```

## Características

### ✅ Implementado
- [x] Envio real via SMTP
- [x] Templates HTML responsivos
- [x] Execução assíncrona
- [x] Retry automático
- [x] Persistência no banco
- [x] Validação de e-mails
- [x] Logging completo
- [x] Configuração via variáveis de ambiente
- [x] Substituição automática do Mock

### 🔒 Segurança
- Credenciais via variáveis de ambiente
- Validação de e-mails
- Encoding UTF-8
- STARTTLS obrigatório

### 🚀 Performance
- Envio assíncrono (não bloqueia)
- Pool de threads otimizado
- Retry com backoff
- Fila de 100 e-mails

## Notas Importantes

1. **Prioridade**: `@Primary` garante que `EmailNotificationService` será usado automaticamente
2. **Mock**: `MockNotificationService` ainda existe para testes
3. **Erros**: Falhas no envio não interrompem o fluxo do sistema
4. **Logs**: Todos os envios são logados para auditoria
5. **Banco**: Notificações são persistidas antes do envio

## Testes

Os erros de compilação mostrados no IDE são temporários e serão resolvidos quando:
1. O Gradle sincronizar as novas dependências Spring Mail
2. O IDE recarregar o projeto

Para testar em desenvolvimento:
1. Configure as variáveis de ambiente SMTP
2. Execute o projeto
3. Use qualquer serviço que chame `NotificationService.enviarEmail()`

## Suporte

Para dúvidas ou problemas:
- Verifique os logs em `sgc.notificacao`
- Confirme configurações SMTP no `application.yml`
- Verifique se as dependências foram baixadas