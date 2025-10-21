# Pacote Comum

## Visão Geral
O pacote `comum` é uma das fundações da aplicação SGC. Ele contém código transversal, essencial para o funcionamento de outros módulos. Seu objetivo é centralizar componentes compartilhados para evitar a duplicação de código e garantir consistência.

Este pacote abriga exclusivamente código de suporte sem lógica de negócio.

## Arquitetura e Subpacotes
O `comum` fornece infraestrutura básica, como o tratamento de erros e modelos de dados compartilhados.

```mermaid
graph TD
    subgraph "Módulos de Negócio (ex: processo, mapa, etc.)"
        direction LR
        Controllers
        Services
        Models
    end

    subgraph "Pacote Comum"
        direction LR
        Erros(erros)
        ModeloBase(modelo)
        BeanUtil
    end

    Services -- Lançam --> Erros
    Controllers -- Capturam exceções via --> Erros
    Models -- Herdam de --> ModeloBase
```

### 1. `erros`
- **Responsabilidade:** Define a hierarquia de exceções customizadas e o tratador global de erros.
- **Componentes Notáveis:**
  - `RestExceptionHandler`: Um `@ControllerAdvice` que intercepta exceções lançadas pela aplicação e as converte em respostas JSON padronizadas para a API.
  - `ErroDominioNaoEncontrado`: Exceção padrão para ser lançada quando uma entidade não é encontrada (resulta em HTTP 404).
  - `ApiError`: Classe que modela a resposta de erro JSON padrão.

### 2. `modelo`
- **Responsabilidade:** Contém modelos de dados compartilhados.
- **Componentes Notáveis:**
  - `EntidadeBase`: Uma superclasse (`@MappedSuperclass`) que fornece um campo de ID (`codigo`) padronizado para a maioria das entidades JPA do sistema.

### 3. Utilitários
- **Responsabilidade:** Fornece classes de utilidade diversas.
- **Componentes Notáveis:**
  - `BeanUtil`: Permite o acesso a beans gerenciados pelo Spring em contextos não gerenciados.

## Propósito e Uso
- **Exceções (`erros`)**: Lançadas pelos serviços para sinalizar um erro de negócio ou técnico. O `RestExceptionHandler` cuida do resto.
- **Modelo (`modelo`)**: A `EntidadeBase` é estendida por outras entidades para padronizar a chave primária.

**Exemplo de uso de uma exceção:**
```java
// Em um serviço de outro módulo
public Recurso buscar(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ErroDominioNaoEncontrado("Recurso", id));
}
```
