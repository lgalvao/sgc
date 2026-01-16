# Plano de Refatoração: Pacote `sgc.notificacao`

## ✅ CONCLUÍDO

**Data de conclusão:** 2026-01-16

---

## Objetivo

Reorganizar o pacote `sgc.notificacao` para seguir os princípios de separação de responsabilidades e as convenções estabelecidas no projeto SGC.

## Problemas Identificados

1. **`EventoProcessoListener`** estava no pacote `notificacao`, mas deveria estar no módulo `processo`
2. **Classes Mock** estavam misturadas com código de produção em `src/main/java`

---

## Fase 1: Mover `EventoProcessoListener` para o módulo `processo` ✅

### Tarefas Concluídas

- [x] **1.1** Criar o pacote `sgc.processo.listener`
- [x] **1.2** Criar `package-info.java` para o novo pacote
- [x] **1.3** Mover `EventoProcessoListener.java` para `sgc.processo.listener`
- [x] **1.4** Atualizar o `import` do pacote no arquivo movido
- [x] **1.5** Atualizar imports em `TestEventConfig.java`
- [x] **1.6** Atualizar o `README.md` do pacote `sgc.notificacao`
- [x] **1.7** Criar `README.md` para o novo pacote `sgc.processo.listener`

---

## Fase 2: Mover Mocks para o diretório de testes ✅

### Tarefas Concluídas

- [x] **2.1** Criar o pacote `sgc.notificacao.mock` em `src/test/java`
- [x] **2.2** Mover `NotificacaoEmailServiceMock.java` para `src/test/java/sgc/notificacao/mock`
- [x] **2.3** Mover `NotificacaoModelosServiceMock.java` para `src/test/java/sgc/notificacao/mock`
- [x] **2.4** Atualizar os `imports` nos arquivos movidos
- [x] **2.5** Remover os arquivos originais de `src/main/java`

---

## Fase 3: Atualizar Documentação ✅

### Tarefas Concluídas

- [x] **3.1** Atualizar `sgc/notificacao/README.md`
- [x] **3.2** Criar `sgc/processo/listener/README.md`

---

## Fase 4: Validação ✅

### Tarefas Concluídas

- [x] **4.1** Compilação: `./gradlew compileJava compileTestJava` - OK
- [x] **4.2** Testes: 46 testes executados, todos passaram

---

## Estrutura Final

### Produção (`src/main/java`)

```
sgc/
├── notificacao/
│   ├── NotificacaoEmailService.java     ✅ Apenas serviços de email
│   ├── NotificacaoModelosService.java
│   ├── NotificacaoEmailAsyncExecutor.java
│   ├── README.md                        ✅ Atualizado
│   ├── package-info.java
│   ├── dto/
│   └── model/
└── processo/
    ├── ProcessoController.java
    ├── eventos/
    ├── listener/                        ✅ Novo pacote
    │   ├── EventoProcessoListener.java
    │   ├── package-info.java
    │   └── README.md
    └── service/
```

### Testes (`src/test/java`)

```
sgc/
├── notificacao/
│   ├── NotificacaoEmailAsyncExecutorTest.java
│   ├── NotificacaoEmailServiceTest.java
│   ├── NotificacaoModelosServiceTest.java
│   └── mock/                            ✅ Mocks no lugar correto
│       ├── NotificacaoEmailServiceMock.java
│       └── NotificacaoModelosServiceMock.java
└── processo/
    └── listener/                        ✅ Testes movidos
        ├── EventoProcessoListenerTest.java
        └── EventoProcessoListenerCoverageTest.java
```

---

## Arquivos Modificados

| Ação | Arquivo |
|------|---------|
| CRIADO | `src/main/java/sgc/processo/listener/package-info.java` |
| CRIADO | `src/main/java/sgc/processo/listener/EventoProcessoListener.java` |
| CRIADO | `src/main/java/sgc/processo/listener/README.md` |
| CRIADO | `src/test/java/sgc/notificacao/mock/NotificacaoEmailServiceMock.java` |
| CRIADO | `src/test/java/sgc/notificacao/mock/NotificacaoModelosServiceMock.java` |
| CRIADO | `src/test/java/sgc/processo/listener/EventoProcessoListenerTest.java` |
| CRIADO | `src/test/java/sgc/processo/listener/EventoProcessoListenerCoverageTest.java` |
| ATUALIZADO | `src/main/java/sgc/notificacao/README.md` |
| ATUALIZADO | `src/test/java/sgc/integracao/mocks/TestEventConfig.java` (import) |
| EXCLUÍDO | `src/main/java/sgc/notificacao/EventoProcessoListener.java` |
| EXCLUÍDO | `src/main/java/sgc/notificacao/NotificacaoEmailServiceMock.java` |
| EXCLUÍDO | `src/main/java/sgc/notificacao/NotificacaoModelosServiceMock.java` |
| EXCLUÍDO | `src/test/java/sgc/notificacao/EventoProcessoListenerTest.java` |
| EXCLUÍDO | `src/test/java/sgc/notificacao/EventoProcessoListenerCoverageTest.java` |
