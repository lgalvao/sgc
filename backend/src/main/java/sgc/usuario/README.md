# Pacote SGRH e Usuário


## Visão Geral

Este pacote tem uma **dupla responsabilidade** fundamental para o SGC:

1. **Gestão de Usuários e Autenticação:** Define e gerencia a entidade `Usuario` do próprio SGC. Esta entidade é usada
   pelo Spring Security para autenticação e para armazenar os perfis de acesso (`Perfil`) do usuário no sistema.
2. **Fachada para o Sistema de RH Externo:** Através do `SgrhService`, atua como uma camada de abstração (uma fachada)
   para buscar dados complementares de um sistema de RH externo, como a estrutura de unidades e os responsáveis por
   elas.

**Status da Integração:** A fachada (`SgrhService`) está implementada com **dados simulados (mock)**. Ela está pronta
para ser conectada a uma fonte de dados real, mas atualmente não realiza chamadas externas.

## Arquitetura Híbrida

O pacote gerencia uma entidade interna (`Usuario`) e, ao mesmo tempo, consulta um serviço externo, como ilustrado
abaixo.

```mermaid
graph TD
    subgraph "Aplicação SGC"
        direction LR
        SpringSecurity
        OutrosServicos
    end

    subgraph "Pacote SGRH (este pacote)"
        direction LR
        SgrhController
        SgrhService
        UsuarioRepo
    end

    subgraph "Fontes de Dados"
        direction LR
        DB_SGC(Banco de Dados SGC)
        SGRH_Externo(Sistema Externo de RH)
    end

    SpringSecurity -- Usa --> UsuarioRepo
    SgrhController -- Usa --> SgrhService
    SgrhService -- Usa --> UsuarioRepo
    UsuarioRepo -- Gerencia entidade Usuario em --> DB_SGC

    OutrosServicos -- Consultam --> SgrhService
    SgrhService -- Busca dados em --> SGRH_Externo

    subgraph "Estado Atual"
       SGRH_Externo(Atualmente simulado/mockado)
    end
```

## Componentes Principais

### Controladores e Serviços (`service`)

- **`SgrhService`**: Serviço unificado que gerencia autenticação/autorização de usuários e atua como fachada para o sistema de RH externo. Outros módulos do SGC utilizam este serviço para obter informações organizacionais.
- **`SgrhController`**: Expõe a API REST (`/api/usuarios`) para autenticação, autorização e finalização de login.

### Modelo de Dados (`model`)

- **`Usuario`**: Entidade JPA que representa o usuário do SGC. Implementa a interface `UserDetails` do Spring Security.
- **`Perfil`**: Enum que define os perfis de acesso (`ADMIN`, `CHEFE`, `GESTOR`, `SERVIDOR`).
- **`UsuarioRepo`**: Repositório para persistência de usuários.

### DTOs (`dto`)

- **`AutenticacaoReq`**, **`EntrarReq`**: Requisições de login.
- **`LoginResp`**: Resposta de login contendo token (simulado).

## Propósito e Uso

- **Para autenticação e autorização**, o Spring Security interage com o `UsuarioRepo` para carregar os dados do usuário.
- **Para obter dados de RH (unidades, responsáveis, etc.)**, outros serviços devem injetar e utilizar o `SgrhService`.


## Como Testar

Para executar apenas os testes deste módulo:
```bash
./gradlew :backend:test --tests "sgc.sgrh.*"
```
