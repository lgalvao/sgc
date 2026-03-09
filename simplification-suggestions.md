# Estratégias de Simplificação e Anti-Overengineering (SGC)

Este documento descreve as estratégias arquiteturais e de código para evitar a complexidade excessiva, superengenharia e fragmentação no projeto SGC. Considerando que o sistema atenderá a um público restrito (5 a 10 usuários simultâneos em uma intranet), padrões comuns em aplicações de alta escalabilidade ou altamente distribuídas **não se aplicam** e devem ser evitados.

## 1. Backend: Simplificação de Camadas e Serviços

### 1.1. Remoção de Facades Redundantes
O projeto atualmente possui diversas classes `*Facade` (`UsuarioFacade`, `ProcessoFacade`, `AtividadeFacade`, `LoginFacade`, etc.) que frequentemente atuam apenas como intermediárias (pass-through) entre Controladores e Serviços.
* **Ação:** Remover as Facades. Controladores devem chamar os Serviços (`@Service`) diretamente. A lógica de negócio ou orquestração deve residir nos Serviços.

### 1.2. Consolidação de Serviços Fragmentados
Existe uma fragmentação excessiva de serviços (ex: `MapaVisualizacaoService`, `MapaSalvamentoService`, `ImpactoMapaService`, `MapaManutencaoService`, `CopiaMapaService`). Essa granularidade é desnecessária para o tamanho da aplicação.
* **Ação:** Consolidar serviços altamente granulares em serviços mais coesos por domínio (ex: consolidar as lógicas em um `MapaService` e/ou `AtividadeService`).
* Favorecer código procedural simples nos serviços em vez de padrões de projeto complexos (ex: evitar implementações de Strategy ou Factory onde um simples `switch` ou `if/else` seria suficiente).

### 1.3. Minimização de DTOs e Interfaces
* **Ação:** Para consultas e leituras simples, retorne as entidades JPA diretamente para o frontend, utilizando as anotações `@JsonView` já existentes para controlar a serialização.
* Evitar criar DTOs de leitura que mapeiam 1:1 para entidades apenas por purismo arquitetural.
* Utilizar `Records` do Java para os DTOs estritamente necessários (ex: de Request/Escrita), acessando as propriedades através de métodos nativos (`dto.nome()`) e não via getters (`dto.getNome()`).
* Não utilizar interfaces com apenas uma implementação (Single-Implementation Interfaces). Crie a classe concreta diretamente.

### 1.4. Centralização de Validações
* **Ação:** Mover validações de negócio e controle transacional inteiramente para os Serviços. Controladores devem focar apenas no roteamento HTTP e serialização.

## 2. Frontend: Gestão de Estado e Componentes

### 2.1. Remoção de Stores Pinia Intermediárias
Diversas stores do Pinia (ex: `mapas.ts`, `atividades.ts`, `subprocessos.ts`, `processos.ts`, `usuarios.ts`) atuam primariamente como wrappers de caching e chamadas de API, adicionando uma camada extra (`Componente -> Store Pinia -> Service API`) que aumenta a complexidade de manutenção.
* **Ação:** Remover as stores Pinia que funcionam apenas como pass-through de dados.
* Componentes devem consumir os dados via `refs` locais do Vue ou `composables` customizados, invocando as funções do serviço de API diretamente (ex: `import { buscarMapaCompleto } from '@/services/subprocessoService'`).
* **Regra:** O Pinia deve ser reservado EXCLUSIVAMENTE para verdadeiro estado global da aplicação (ex: Dados de Autenticação/Perfil do usuário, e UI global como Notifications/Toasts).

### 2.2. Simplificação de Componentes Vue
* **Ação:** Evitar "Wrapper Components" que servem apenas para repassar propriedades (props) e eventos para outro componente base (ex: abstraindo indevidamente componentes do `bootstrap-vue-next`). Use os componentes base diretamente caso não agreguem nova lógica.

## 3. Arquitetura Geral
* Manter um monólito coeso e simples. Evitar abordagens de microsserviços, múltiplos subprojetos Gradle fragmentados e arquiteturas multicamadas excessivas. O foco deve ser na facilidade de manutenção e velocidade de desenvolvimento.
