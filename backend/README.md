# Backend do Sistema de Gestão de Competências (SGC)

## Visão Geral

Este diretório contém o código do backend do SGC. Ele fornece uma API REST para consumo pelo frontend. A arquitetura é organizada em pacotes representando domínios específicos.

## 🏗️ Arquitetura e Stack

A aplicação segue uma arquitetura **Modular Monolith** construída com:

* **Java 21**: Linguagem base.
* **Spring Boot 4.0.1**: Framework de aplicação (GA).
* **Hibernate/JPA**: Persistência de dados.
* **Oracle**: Banco de dados de produção.
* **H2 Database**: Banco de dados em memória para testes e desenvolvimento local rápido.
* **Gradle**: Ferramenta de build e gerenciamento de dependências.

## 📦 Módulos Principais

O código está organizado em `src/main/java/sgc/` com os seguintes módulos principais:

* **`processo`**: Orquestrador central de fluxos de alto nível.
* **`subprocesso`**: Máquina de estados para gerenciamento de tarefas por unidade.
* **`mapa`**: Núcleo do domínio (Mapas, Competências, Atividades, Conhecimentos).
* **`usuario`**: Gestão de usuários, perfis e autenticação.
* **`unidade`**: Modelagem da estrutura organizacional.
* **`alerta` / `notificacao`**: Módulos reativos para comunicação com o usuário.
* **`analise`**: Auditoria e histórico de revisões.

## 🚀 Como Executar

A partir da raiz do projeto (`/app`), execute:

```bash
./gradlew bootRun -Dspring.profiles.active=e2e
```

A API do backend estará disponível em `http://localhost:10000`.

### Perfis do Spring

O sistema utiliza perfis para configurar o comportamento do ambiente:

* `default`/`local`: Usa banco H2 em memória. Ideal para desenvolvimento.
* `prod`: Configurado para Oracle.
* `test`: Ativado durante a execução de testes unitários/integração.
* `e2e`: Ativa endpoints auxiliares para testes end-to-end (reset de banco, fixtures).

## 🧪 Testes

### Execução

Para rodar todos os testes de backend:

```bash
./gradlew :backend:test
```

### Estrutura de Testes

Os testes estão localizados em `src/test/java/sgc/`:

* **`integracao/`**: Testes de integração cobrindo os Casos de Uso (CDU-XX).
* **`[pacote]/`**: Testes unitários específicos de cada módulo.
* **`architecture/`**: Testes ArchUnit garantindo a integridade arquitetural.