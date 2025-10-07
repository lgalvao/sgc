# Módulo de Mapa de Competências - SGC

## Visão Geral
O pacote `mapa` é um dos módulos mais críticos do sistema, responsável por toda a gestão do **Mapa de Competências**. Um "Mapa" é um artefato complexo que representa o conjunto de competências e atividades de uma unidade organizacional em um determinado período.

Este pacote vai muito além de um simples CRUD. Ele orquestra operações de negócio complexas, como a criação e salvamento transacional de mapas, a validação de sua integridade, a cópia de mapas entre unidades e a análise de impacto de mudanças.

## Arquivos e Componentes Principais

### Entidades Core

#### 1. `Mapa.java`
**Localização:** `backend/src/main/java/sgc/mapa/Mapa.java`
- **Descrição:** A entidade JPA central que representa o Mapa de Competências.
- **Ciclo de Vida:** Possui um ciclo de vida que inclui status como "disponibilizado" e "homologado", com timestamps para cada etapa.
- **Associações:** Está diretamente associado a uma `Unidade`.

#### 2. `UnidadeMapa.java`
**Localização:** `backend/src/main/java/sgc/mapa/UnidadeMapa.java`
- **Descrição:** Entidade que representa a associação entre um mapa e uma unidade, possivelmente para registrar o histórico ou o status do mapa para aquela unidade (ex: `MAPEAMENTO_CONCLUIDO`).

### Serviços de Negócio

Este pacote se destaca pela sua arquitetura orientada a serviços, onde cada serviço tem uma responsabilidade de negócio bem definida.

#### 3. `MapaService.java` (e `MapaServiceImpl.java`)
**Localização:** `backend/src/main/java/sgc/mapa/`
- **Descrição:** O principal serviço para a manipulação de mapas.
- **Funcionalidades Chave:**
  - `obterMapaCompleto(...)`: Carrega um mapa com toda a sua árvore de objetos (competências, atividades, etc.).
  - `salvarMapaCompleto(...)`: Executa o salvamento **atômico** de um mapa. Esta operação transacional garante que o mapa, suas competências e os vínculos com atividades sejam salvos de forma consistente.
  - `validarMapaCompleto(...)`: Aplica regras de negócio para garantir que um mapa está íntegro antes de ser disponibilizado (ex: verifica se não há competências ou atividades órfãs).

#### 4. `CopiaMapaService.java` (e `CopiaMapaServiceImpl.java`)
**Localização:** `backend/src/main/java/sgc/mapa/`
- **Descrição:** Serviço especializado na clonagem de mapas.
- **Funcionalidade Chave:**
  - `copiarMapaParaUnidade(...)`: Cria uma cópia exata de um mapa existente (incluindo suas competências e atividades) e a associa a uma nova unidade.

#### 5. `ImpactoMapaService.java` (e `ImpactoMapaServiceImpl.java`)
**Localização:** `backend/src/main/java/sgc/mapa/`
- **Descrição:** Implementa o Caso de Uso CDU-12, que é a análise de impacto.
- **Funcionalidade Chave:**
  - `verificarImpactos(...)`: Compara o mapa atual de uma unidade com uma nova versão (vindo de um subprocesso) e identifica todas as diferenças: atividades adicionadas, removidas ou alteradas, e as competências que são afetadas por essas mudanças.

### DTOs (Data Transfer Objects)

#### 6. `dto/`
**Localização:** `backend/src/main/java/sgc/mapa/dto/`
- **Descrição:** Este sub-pacote contém DTOs complexos para lidar com as operações do serviço.
- **DTOs Notáveis:**
  - `MapaCompletoDto.java`: Uma estrutura de dados aninhada que representa o mapa completo, usado para transferir o mapa e suas relações para o frontend de uma só vez.
  - `SalvarMapaRequest.java`: DTO que encapsula todos os dados necessários para a operação de `salvarMapaCompleto`.
  - `ImpactoMapaDto.java`: DTO que estrutura o resultado da análise de impacto, separando as atividades por tipo de mudança (inserida, removida, alterada).

## Fluxos de Operação

### Salvando um Mapa
1.  O frontend envia uma requisição complexa (`SalvarMapaRequest`) para o `MapaController`.
2.  O controller invoca `MapaService.salvarMapaCompleto()`.
3.  Dentro de uma única transação (`@Transactional`), o serviço:
    a. Atualiza os dados do mapa principal.
    b. Exclui competências que foram removidas.
    c. Atualiza competências existentes.
    d. Cria novas competências.
    e. Limpa e recria todos os vínculos entre competências e atividades.
4.  Se qualquer etapa falhar, a transação inteira é revertida, garantindo a consistência dos dados.

### Verificando o Impacto de Mudanças
1.  Um serviço de processo invoca `ImpactoMapaService.verificarImpactos()` passando o ID de um subprocesso de revisão.
2.  O serviço carrega o mapa vigente da unidade e o cadastro de atividades do subprocesso.
3.  Ele compara as duas listas de atividades e monta o `ImpactoMapaDto` com as diferenças.
4.  O DTO resultante é retornado para ser exibido ao usuário, que pode então tomar decisões sobre como atualizar o mapa vigente.

## Notas Importantes
- **Complexidade de Negócio**: Este pacote encapsula uma lógica de negócio significativa. A separação em múltiplos serviços (`MapaService`, `CopiaMapaService`, `ImpactoMapaService`) é uma excelente prática de design que mantém o código organizado e coeso.
- **Transacionalidade**: A natureza atômica da operação de salvamento (`salvarMapaCompleto`) é crucial para a integridade dos dados e é um ponto central da arquitetura deste pacote.
- **DTOs Ricos**: O uso de DTOs complexos e específicos para cada operação (`SalvarMapaRequest`, `ImpactoMapaDto`) é fundamental para gerenciar a complexidade das interações com este módulo.