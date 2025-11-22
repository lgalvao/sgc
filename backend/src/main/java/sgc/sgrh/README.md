# Pacote SGRH e Usuário

## Visão Geral
Este pacote tem uma **dupla responsabilidade** fundamental para o SGC:

1.  **Gestão de Usuários e Autenticação:** Define e gerencia a entidade `Usuario` do próprio SGC. Esta entidade é usada pelo Spring Security para autenticação e para armazenar os perfis de acesso (`Perfil`) do usuário no sistema.
2.  **Fachada para o Sistema de RH Externo:** Através do `SgrhService`, atua como uma camada de abstração (uma fachada) para buscar dados complementares de um sistema de RH externo, como a estrutura de unidades e os responsáveis por elas.

**Status da Integração:** A fachada (`SgrhService`) está implementada com **dados simulados (mock)**. Ela está pronta para ser conectada a uma fonte de dados real, mas atualmente não realiza chamadas externas.

## Arquitetura Híbrida
O pacote gerencia uma entidade interna (`Usuario`) e, ao mesmo tempo, consulta um serviço externo, como ilustrado abaixo.

```mermaid
graph TD
    subgraph "Aplicação SGC"
        direction LR
        SpringSecurity
        OutrosServicos
        SgrhController
        UsuarioService
    end

    subgraph "Pacote SGRH (este pacote)"
        direction LR
        UsuarioRepo
        SgrhServiceFacade(SgrhService - Fachada)
    end

    subgraph "Fontes de Dados"
        direction LR
        DB_SGC(Banco de Dados SGC)
        SGRH_Externo(Sistema Externo de RH)
    end

    SpringSecurity & UsuarioService -- Usa --> UsuarioRepo
    UsuarioRepo -- Gerencia entidade Usuario em --> DB_SGC
    SgrhController -- Usa --> UsuarioService

    OutrosServicos -- Consultam --> SgrhServiceFacade
    SgrhServiceFacade -- Busca dados em --> SGRH_Externo

    subgraph "Estado Atual"
       SGRH_Externo(Atualmente simulado/mockado)
    end
```

## Componentes Principais

### Gestão de Usuários (Lógica Interna)
- **`Usuario` / `Perfil`**: Entidades JPA que modelam o usuário do SGC e seus perfis de acesso (`ADMIN`, `CHEFE`, etc.). A entidade `Usuario` implementa a interface `UserDetails` do Spring Security.
- **`UsuarioRepo`**: Repositório para persistir e buscar usuários no banco de dados do SGC.
- **`UsuarioService`**: Camada de serviço para operações relacionadas ao usuário, como o fluxo de login/autenticação.
- **`SgrhController`**: Expõe a API REST (`/api/usuarios`) para autenticação, autorização e finalização de login.

### Fachada de Integração (Lógica Externa)
- **`SgrhService`**: O serviço que atua como cliente do sistema de RH externo. Outros módulos do SGC (como `alerta` ou `processo`) utilizam este serviço para obter informações sobre unidades e seus responsáveis, sem precisar conhecer os detalhes da integração.

## Propósito e Uso
- **Para autenticação e autorização**, o Spring Security interage diretamente com o `UsuarioRepo` para carregar os dados do usuário.
- **Para obter dados de RH (unidades, responsáveis, etc.)**, outros serviços devem injetar e utilizar o `SgrhService`.

Esta separação é crucial: a autenticação depende de dados internos e controlados (`Usuario`), enquanto os dados organizacionais são buscados de uma fonte de verdade externa.
