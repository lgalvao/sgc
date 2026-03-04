# Diagrama de Sequência: Processo de Mapeamento (Caminho Feliz)

Este diagrama ilustra a interação sequencial entre os atores do sistema (ADMIN, CHEFE e GESTOR) e o próprio Sistema de Gestão de Competências (SGC) durante o fluxo ideal (sem devoluções) de um Processo de Mapeamento.

```mermaid
sequenceDiagram
    autonumber
    
    actor Admin as ADMIN
    participant Sistema as SGC
    actor Chefe as CHEFE
    actor Gestor as GESTOR

    %% Fase 1: Início
    rect rgb(240, 248, 255)
        Admin->>Sistema: Cria Processo de Mapeamento
        Sistema-->>Admin: Processo "Criado"
        Admin->>Sistema: Inicia Processo
        Sistema->>Chefe: Notifica início (sit. Não iniciado)
        Sistema->>Gestor: Notifica início
    end

    %% Fase 2: Cadastro (Unidade Operacional)
    rect rgb(245, 255, 245)
        Chefe->>Sistema: Acessa cadastro
        Chefe->>Sistema: Cadastra atividades/conhecimentos
        Sistema-->>Chefe: Salva rascunho (sit. Cadastro em andamento)
        Chefe->>Sistema: Disponibiliza cadastro
        Sistema->>Gestor: Disponibiliza cadastro para hierarquia (sit. Cadastro disponibilizado)
    end

    %% Fase 3: Validação (Unidade Intermediária)
    rect rgb(255, 248, 240)
        Gestor->>Sistema: Acessa cadastro da unidade subordinada
        Gestor->>Sistema: Valida o cadastro
        Sistema->>Admin: Encaminha para homologação (sit. Cadastro validado)
    end

    %% Fase 4: Homologação e Criação do Mapa (Raiz)
    rect rgb(255, 240, 245)
        Admin->>Sistema: Analisa e homologa o cadastro
        Sistema-->>Admin: sit. Cadastro homologado
        Admin->>Sistema: Agrupa atividades em competências (Cria Mapa)
        Sistema-->>Admin: sit. Mapa criado
        Admin->>Sistema: Disponibiliza Mapa para hierarquia
        Sistema->>Chefe: Notifica disponibilização do Mapa (sit. Mapa disponibilizado)
    end

    %% Fase 5: Validação do Mapa e Finalização
    rect rgb(240, 255, 255)
        Chefe->>Sistema: Valida mapa (sem sugestões)
        Sistema->>Gestor: Encaminha para validação
        Gestor->>Sistema: Valida Mapa
        Sistema->>Admin: sit. Mapa validado
        Admin->>Sistema: Homologa Mapa
        Sistema-->>Todos: Notifica conclusão (sit. Mapa homologado)
        Sistema-->>Todos: Processo "Finalizado" (após todas unidades concluírem)
    end
```
