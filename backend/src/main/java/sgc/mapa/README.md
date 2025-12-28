# Pacote Mapa

## Visão Geral

O pacote `mapa` é o coração do domínio do SGC. Ele encapsula toda a lógica relacionada à estruturação e gerenciamento das competências organizacionais. Este pacote centraliza não apenas o conceito de "Mapa", mas também seus componentes constituintes: **Competências**, **Atividades** e **Conhecimentos**.

## Estrutura de Domínio

O modelo de dados é hierárquico:

1.  **Mapa**: Entidade raiz que agrupa as definições para uma unidade em um determinado ciclo.
2.  **Competencia**: Habilidade ou capacidade necessária (ex: "Gestão de Projetos").
3.  **Atividade**: Tarefas práticas que exigem a competência (ex: "Elaborar cronograma").
4.  **Conhecimento**: Saberes específicos necessários para realizar a atividade (ex: "Ferramenta MS Project").

> **Nota:** Anteriormente, `conhecimento` e `atividade` eram módulos separados. Agora, eles foram consolidados neste pacote para garantir alta coesão, visto que são partes inseparáveis do domínio do Mapa.

## Serviços Principais

### `MapaService`
Gerencia o ciclo de vida da entidade `Mapa`. Responsável por criar, buscar e associar mapas a subprocessos.

### `AtividadeService`
Gerencia o cadastro e manutenção de atividades e seus conhecimentos associados.

### `CompetenciaService`
Centraliza a lógica de CRUD para competências.

### `ImpactoMapaService`
Realiza a análise de impacto quando há alterações no mapa (ex: remoção de uma competência utilizada em múltiplos subprocessos). Calcula e retorna o `ImpactoMapaDto`.

### `CopiaMapaService`
Serviço especializado responsável por realizar a "deep copy" (cópia profunda) de um mapa. Essencial para iniciar novos ciclos de revisão baseados em mapas anteriores, duplicando toda a estrutura de competências, atividades e conhecimentos.

### `MapaVisualizacaoService`
Otimizado para leitura, monta DTOs complexos (`MapaVisualizacaoDto`) para exibir a árvore completa de competências no frontend de forma performática.

## Padrões Utilizados

*   **DTOs de Visualização:** Separação clara entre entidades JPA e objetos retornados para a UI (pacote `dto.visualizacao`), evitando problemas de serialização cíclica e carregamento desnecessário de dados.
*   **Mapper:** Uso de MapStruct para conversão eficiente entre Entidades e DTOs.
