# Módulo de Conhecimentos - SGC

## Visão Geral
O pacote `conhecimento` é responsável pelo gerenciamento de **Conhecimentos**. No contexto do sistema, um "Conhecimento" representa uma habilidade, um saber ou uma informação necessária para executar uma determinada `Atividade`.

Este pacote fornece a estrutura completa para o CRUD (Criar, Ler, Atualizar, Excluir) de conhecimentos, incluindo a entidade, o repositório, o controlador REST, o DTO e o mapper.

## Arquivos Principais

### 1. `Conhecimento.java`
**Localização:** `backend/src/main/java/sgc/conhecimento/Conhecimento.java`
- **Descrição:** Entidade JPA que representa um conhecimento. Mapeia a tabela `TB_CONHECIMENTO`.
- **Campos Importantes:**
  - `descricao`: O texto que descreve o conhecimento.
  - `atividade`: A atividade à qual este conhecimento está associado.

### 2. `ConhecimentoController.java`
**Localização:** `backend/src/main/java/sgc/conhecimento/ConhecimentoController.java`
- **Descrição:** Controlador REST que expõe os endpoints para gerenciar `Conhecimento`.
- **Endpoints:**
  - `GET /api/conhecimentos`: Lista todos os conhecimentos.
  - `GET /api/conhecimentos/{id}`: Obtém um conhecimento específico por seu ID.
  - `POST /api/conhecimentos`: Cria um novo conhecimento.
  - `PUT /api/conhecimentos/{id}`: Atualiza um conhecimento existente.
  - `DELETE /api/conhecimentos/{id}`: Exclui um conhecimento.

### 3. `ConhecimentoDTO.java`
**Localização:** `backend/src/main/java/sgc/conhecimento/ConhecimentoDTO.java`
- **Descrição:** Data Transfer Object (DTO) para a entidade `Conhecimento`. É usado para transferir dados entre o cliente (frontend) e o servidor (backend) de forma segura e desacoplada da entidade do banco de dados.

### 4. `ConhecimentoMapper.java`
**Localização:** `backend/src/main/java/sgc/conhecimento/ConhecimentoMapper.java`
- **Descrição:** Um componente (geralmente implementado com MapStruct ou manualmente) responsável por converter a entidade `Conhecimento` para `ConhecimentoDTO` e vice-versa.

### 5. `ConhecimentoRepository.java`
**Localização:** `backend/src/main/java/sgc/conhecimento/ConhecimentoRepository.java`
- **Descrição:** Interface Spring Data JPA que fornece os métodos de acesso a dados para a entidade `Conhecimento` (ex: `findAll()`, `findById()`, `save()`).

## Fluxo de uma Requisição

1.  **Requisição HTTP**: O cliente envia uma requisição para um dos endpoints do `ConhecimentoController` (ex: `POST /api/conhecimentos`).
2.  **Controlador**: O controller recebe a requisição. O corpo da requisição, contendo um `ConhecimentoDTO`, é validado.
3.  **Mapper**: O `ConhecimentoMapper` é usado para converter o `ConhecimentoDTO` em uma entidade `Conhecimento`.
4.  **Repositório**: O controller chama o `ConhecimentoRepository` para persistir a entidade no banco de dados.
5.  **Resposta HTTP**: O controller retorna uma resposta HTTP (ex: `201 Created`) com o DTO do conhecimento recém-criado no corpo da resposta.

## Como Usar
Para interagir com este módulo, utilize um cliente HTTP para fazer requisições aos endpoints expostos pelo `ConhecimentoController`.

**Exemplo: Criar um novo conhecimento associado a uma atividade**
```http
POST /api/conhecimentos
Content-Type: application/json

{
  "descricao": "Conhecimento em Spring Boot",
  "atividadeId": 42
}
```

## Notas Importantes
- **Padrão DTO**: O uso do padrão DTO (Data Transfer Object) é uma boa prática que evita expor a estrutura interna do banco de dados (entidades JPA) diretamente na API. Isso aumenta a segurança e a flexibilidade da aplicação.
- **Simplicidade**: Este pacote é um exemplo clássico de uma implementação de CRUD bem estruturada em uma aplicação Spring Boot, com responsabilidades claramente divididas entre o controlador, o serviço (implícito no controlador neste caso), o repositório e o mapper.