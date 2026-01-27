# Pacote Mapa

## Visão Geral

O pacote `mapa` é o coração do domínio do SGC. Ele encapsula toda a lógica relacionada à estruturação e gerenciamento
das competências organizacionais. Este pacote centraliza não apenas o conceito de "Mapa", mas também seus componentes
constituintes: **Competências**, **Atividades** e **Conhecimentos**.

## Estrutura de Domínio

O modelo de dados é hierárquico:

1. **Mapa**: Entidade raiz que agrupa as definições para uma unidade em um determinado ciclo.
2. **Competencia**: Habilidade ou capacidade necessária (ex: "Gestão de Projetos").
3. **Atividade**: Tarefas práticas que exigem a competência (ex: "Elaborar cronograma").
4. **Conhecimento**: Saberes específicos necessários para realizar a atividade (ex: "Ferramenta MS Project").

> **Nota:** Anteriormente, `conhecimento` e `atividade` eram módulos separados. Agora, eles foram consolidados neste
> pacote para garantir alta coesão, visto que são partes inseparáveis do domínio do Mapa.

## Serviços Principais

### Serviços de CRUD

| Serviço               | Responsabilidade                                   |
|-----------------------|----------------------------------------------------|
| `MapaService`         | Operações básicas de CRUD para a entidade `Mapa`.  |
| `AtividadeService`    | Gerencia o cadastro e manutenção de atividades.    |
| `ConhecimentoService` | Gerencia o cadastro e manutenção de conhecimentos. |
| `CompetenciaService`  | Centraliza a lógica de CRUD para competências.     |

### Serviços Especializados

| Serviço                   | Responsabilidade                                                                                         |
|---------------------------|----------------------------------------------------------------------------------------------------------|
| `AtividadeFacade`         | Orquestra operações entre Atividade, Conhecimento e Subprocesso. Remove lógica de negócio do Controller. |
| `MapaSalvamentoService`   | Processa salvamentos complexos do mapa completo com competências e associações.                          |
| `CopiaMapaService`        | Realiza cópias profundas de mapas e importação de atividades entre mapas.                                |
| `MapaVisualizacaoService` | Monta DTOs complexos para exibir a árvore de competências no frontend.                                   |

### Serviços de Análise de Impacto

| Serviço                             | Responsabilidade                                                            |
|-------------------------------------|-----------------------------------------------------------------------------|
| `ImpactoMapaService`                | Orquestra a verificação de impactos no mapa de competências (CDU-12).       |
| `DetectorMudancasAtividadeService`  | Detecta atividades inseridas, removidas ou alteradas entre versões de mapa. |
| `DetectorImpactoCompetenciaService` | Identifica quais competências foram afetadas por mudanças em atividades.    |

## Arquitetura

```
sgc/mapa/
├── AtividadeController.java     # REST endpoints para Atividades
├── MapaController.java          # REST endpoints para Mapas
├── dto/                         # Data Transfer Objects
│   ├── visualizacao/            # DTOs específicos para visualização
│   └── ...
├── evento/                      # Eventos de domínio
├── mapper/                      # MapStruct mappers
├── model/                       # Entidades JPA e Repositórios
└── service/                     # Lógica de negócios
    ├── AtividadeService.java
    ├── AtividadeFacade.java
    ├── CompetenciaService.java
    ├── ConhecimentoService.java
    ├── CopiaMapaService.java
    ├── DetectorImpactoCompetenciaService.java
    ├── DetectorMudancasAtividadeService.java
    ├── ImpactoMapaService.java
    ├── MapaSalvamentoService.java
    ├── MapaService.java
    └── MapaVisualizacaoService.java
```

## Padrões Utilizados

* **Facade Pattern:** `AtividadeFacade` simplifica a interface de uso e centraliza a coordenação entre múltiplos
  serviços.
* **Single Responsibility Principle:** Cada serviço tem uma responsabilidade bem definida.
* **DTOs de Visualização:** Separação clara entre entidades JPA e objetos retornados para a UI (pacote
  `dto.visualizacao`), evitando problemas de serialização cíclica.
* **Mapper:** Uso de MapStruct para conversão eficiente entre Entidades e DTOs.
* **Domain Events:** `EventoMapaAlterado` para comunicação desacoplada entre módulos.
