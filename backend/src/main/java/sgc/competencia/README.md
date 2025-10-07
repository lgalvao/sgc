# Módulo de Competências - SGC

## Visão Geral
Este pacote é responsável por gerenciar as **Competências** e a sua associação com **Atividades**. Ele permite não apenas a criação, leitura, atualização e exclusão de competências, mas também o gerenciamento dos vínculos que conectam uma competência a uma ou mais atividades.

## Arquivos Principais

### Gestão de Competências

#### 1. `Competencia.java`
**Localização:** `backend/src/main/java/sgc/competencia/Competencia.java`
- **Descrição:** Entidade JPA que representa uma competência. Mapeia a tabela `TB_COMPETENCIA`.
- **Campos Importantes:**
  - `descricao`: O texto que descreve a competência.
  - `mapa`: Associação com a entidade `Mapa`.

#### 2. `CompetenciaController.java`
**Localização:** `backend/src/main/java/sgc/competencia/CompetenciaController.java`
- **Descrição:** Controlador REST que expõe endpoints para as operações CRUD de `Competencia`.
- **Endpoints:**
  - `GET /api/competencias`: Lista todas as competências.
  - `GET /api/competencias/{id}`: Obtém uma competência por ID.
  - `POST /api/competencias`: Cria uma nova competência.
  - `PUT /api/competencias/{id}`: Atualiza uma competência existente.
  - `DELETE /api/competencias/{id}`: Exclui uma competência.

#### 3. `CompetenciaDTO.java` e `CompetenciaMapper.java`
**Localização:** `backend/src/main/java/sgc/competencia/`
- **Descrição:**
  - `CompetenciaDTO`: Data Transfer Object para `Competencia`, usado para a comunicação via API.
  - `CompetenciaMapper`: Utilitário para converter entre a entidade `Competencia` e `CompetenciaDTO`.

#### 4. `CompetenciaRepository.java`
**Localização:** `backend/src/main/java/sgc/competencia/CompetenciaRepository.java`
- **Descrição:** Interface Spring Data JPA para acesso aos dados da entidade `Competencia`.

### Gestão do Vínculo Competência-Atividade

#### 5. `CompetenciaAtividade.java`
**Localização:** `backend/src/main/java/sgc/competencia/CompetenciaAtividade.java`
- **Descrição:** Entidade que representa a tabela de associação (join table) entre `Competencia` e `Atividade`. Utiliza uma chave primária composta (`Id`).
- **Relacionamentos:**
  - `ManyToOne` com `Competencia`.
  - `ManyToOne` com `Atividade`.

#### 6. `CompetenciaAtividadeController.java`
**Localização:** `backend/src/main/java/sgc/competencia/CompetenciaAtividadeController.java`
- **Descrição:** Controlador REST para gerenciar o vínculo entre `Competencia` e `Atividade`.
- **Endpoints:**
  - `GET /api/competencia-atividades`: Lista todos os vínculos existentes.
  - `POST /api/competencia-atividades`: Cria um novo vínculo entre uma competência e uma atividade.
  - `DELETE /api/competencia-atividades`: Remove um vínculo existente.

#### 7. `CompetenciaAtividadeRepository.java`
**Localização:** `backend/src/main/java/sgc/competencia/CompetenciaAtividadeRepository.java`
- **Descrição:** Interface Spring Data JPA para acesso aos dados da entidade `CompetenciaAtividade`.

## Como Usar

### Gerenciando Competências
Interaja com os endpoints do `CompetenciaController` através de um cliente HTTP.

**Exemplo: Criar uma nova competência**
```http
POST /api/competencias
Content-Type: application/json

{
  "descricao": "Trabalho em equipe",
  "mapaCodigo": 1
}
```

### Vinculando Competência e Atividade
Interaja com os endpoints do `CompetenciaAtividadeController`.

**Exemplo: Vincular uma competência a uma atividade**
```http
POST /api/competencia-atividades
Content-Type: application/json

{
  "competenciaCodigo": 10,
  "atividadeCodigo": 25
}
```

## Notas Importantes
- **Chave Primária Composta**: A entidade `CompetenciaAtividade` usa uma classe aninhada `Id` como chave primária composta (`@EmbeddedId`), que é uma abordagem padrão para gerenciar tabelas de associação em JPA.
- **DTOs para Vínculo**: O `CompetenciaAtividadeController` usa um DTO interno (`VinculoRequest`) para simplificar o corpo da requisição de criação de vínculo.
- **Desacoplamento**: A separação dos controladores (`CompetenciaController` e `CompetenciaAtividadeController`) mantém as responsabilidades bem definidas: um para o recurso principal e outro para o seu relacionamento.