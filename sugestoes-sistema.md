# Relatório de Análise Sistêmica e Sugestões

## 1. Introdução

Este relatório apresenta uma análise técnica do sistema SGC, focando em consistência arquitetural, segurança de tipos e qualidade de testes. A análise baseou-se na documentação existente e na verificação do código-fonte real.

## 2. Consistência Arquitetural

### 2.1. Violação do Padrão Event-Driven
A documentação (AGENTS.md e READMEs) descreve uma arquitetura onde módulos de domínio (como `processo`) comunicam-se com módulos de suporte (como `notificacao`) exclusivamente via eventos de domínio para garantir desacoplamento.

**Problema Identificado:**
O serviço `sgc.processo.service.ProcessoNotificacaoService` injeta e invoca diretamente o `NotificacaoEmailService` do pacote `notificacao`.
```java
// ProcessoNotificacaoService.java
private final NotificacaoEmailService notificacaoEmailService; // Acoplamento direto
```

**Impacto:**
- Cria um acoplamento rígido entre o *Core Domain* e o mecanismo de entrega de e-mails.
- Dificulta testes unitários (necessidade de mockar serviços de infraestrutura).
- Viola a regra documentada de que "Módulos notificacao e alerta não são diretamente acoplados ao processo".

**Sugestão de Melhoria:**
Refatorar o fluxo para usar o `ApplicationEventPublisher`.
1. `ProcessoService` publica `EventoProcessoFinalizado`.
2. Criar/Atualizar um `EventoProcessoListener` no pacote `notificacao` (ou `integracao`) que escuta este evento e invoca o `NotificacaoEmailService`.

## 3. Segurança de Tipos (Frontend vs Backend)

### 3.1. Sincronização Manual de Tipos
O arquivo `frontend/src/types/tipos.ts` contém definições manuais de interfaces TypeScript (ex: `Processo`, `Unidade`) que espelham os DTOs Java.

**Problema Identificado:**
Não há evidência de geração automática. Se um campo for renomeado ou adicionado no Backend (`ProcessoDto.java`), o Frontend quebrará silenciosamente em tempo de execução ou exigirá atualização manual, sujeita a erro humano.

**Impacto:**
- Aumento de bugs de integração.
- Retrabalho constante para manter contratos de API sincronizados.

**Sugestão de Melhoria:**
Implementar a geração automática de clientes e tipos TypeScript.
- Utilizar o **OpenAPI Generator** (via plugin Gradle) para gerar interfaces TypeScript a partir da especificação Swagger/OpenAPI já exposta pelo Spring Boot (`/v3/api-docs`).
- Integrar isso ao build pipeline (`npm run generate-api`).

## 4. Infraestrutura de Testes

### 4.1. Dependência de Dados Globais (Monólito de Dados)
Os testes de integração (`BaseIntegrationTest`) não definem explicitamente seus dados via `@Sql`, dependendo implicitamente da execução global do `data-h2.sql` ou `import.sql` na inicialização do contexto Spring.

**Problema Identificado:**
Todos os testes compartilham o mesmo "cenário do mundo".
- Se um teste modifica o estado global (ex: deleta uma unidade padrão), outros testes podem falhar (flaky tests).
- `data-h2.sql` tende a crescer indefinidamente, tornando a inicialização dos testes lenta.

**Sugestão de Melhoria:**
Adotar uma estratégia de **Testes Isolados e Modulares**.
1. Desabilitar a execução automática do script de dados global no perfil de teste (`spring.sql.init.mode=never`).
2. Criar scripts SQL pequenos e focados (ex: `unidades-basicas.sql`, `processo-padrao.sql`).
3. Anotar classes de teste com `@Sql` para carregar apenas o necessário:
   ```java
   @Sql(scripts = {"/sql/unidades.sql", "/sql/processos.sql"})
   class ProcessoFluxoTest extends BaseIntegrationTest { ... }
   ```

## 5. Resumo das Ações Recomendadas

| Prioridade | Área | Ação | Benefício |
|------------|------|------|-----------|
| **Alta** | Arquitetura | Refatorar `ProcessoNotificacaoService` para usar Eventos | Desacoplamento e aderência à documentação |
| **Média** | Frontend | Automatizar geração de tipos via OpenAPI | Eliminação de erros de contrato API |
| **Média** | Testes | Modularizar scripts de dados de teste (`@Sql`) | Testes mais rápidos e confiáveis |
| **Baixa** | Documentação | Automatizar/Remover seção "Detalhamento técnico" | Limpeza e profissionalismo |
