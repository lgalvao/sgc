# Módulo de Atividade e Análise - SGC

## Visão Geral
Este pacote tem uma dupla responsabilidade: gerenciar as **Atividades** do sistema e as **Análises de Cadastro**.

1.  **Atividades**: Representam tarefas ou ações que podem ser associadas a outros contextos, como um mapa de competências. O pacote fornece um `AtividadeController` para realizar operações CRUD (Criar, Ler, Atualizar, Excluir) sobre as atividades.
2.  **Análise de Cadastro**: Refere-se a um registro de análise, geralmente com observações, vinculado a um `Subprocesso`. Isso é usado para documentar a revisão de cadastros ou outras etapas de um processo.

## Arquivos Principais

### Gestão de Atividades

#### 1. `Atividade.java`
**Localização:** `backend/src/main/java/sgc/atividade/Atividade.java`
- **Descrição:** Entidade JPA que representa uma atividade. Mapeia a tabela `TB_ATIVIDADE`.
- **Campos Importantes:**
  - `descricao`: O texto que descreve a atividade.
  - `mapa`: Associação com a entidade `Mapa`.

#### 2. `AtividadeController.java`
**Localização:** `backend/src/main/java/sgc/atividade/AtividadeController.java`
- **Descrição:** Controlador REST que expõe endpoints para gerenciar `Atividade`.
- **Endpoints:**
  - `GET /api/atividades`: Lista todas as atividades.
  - `GET /api/atividades/{id}`: Obtém uma atividade por ID.
  - `POST /api/atividades`: Cria uma nova atividade.
  - `PUT /api/atividades/{id}`: Atualiza uma atividade existente.
  - `DELETE /api/atividades/{id}`: Exclui uma atividade.

#### 3. `AtividadeDTO.java` e `AtividadeMapper.java`
**Localização:** `backend/src/main/java/sgc/atividade/`
- **Descrição:**
  - `AtividadeDTO`: Data Transfer Object para `Atividade`, usado para a comunicação via API.
  - `AtividadeMapper`: Utilitário para converter entre a entidade `Atividade` e `AtividadeDTO`.

#### 4. `AtividadeRepository.java`
**Localização:** `backend/src/main/java/sgc/atividade/AtividadeRepository.java`
- **Descrição:** Interface Spring Data JPA para acesso aos dados da entidade `Atividade`.

### Gestão de Análise de Cadastro

#### 5. `AnaliseCadastro.java`
**Localização:** `backend/src/main/java/sgc/atividade/AnaliseCadastro.java`
- **Descrição:** Entidade JPA para um registro de análise vinculado a um `Subprocesso`. Mapeia a tabela `TB_ANALISE_CADASTRO`.
- **Campos Importantes:**
  - `subprocesso`: O subprocesso ao qual a análise está associada.
  - `observacoes`: Texto com as observações da análise.

#### 6. `AnaliseCadastroService.java`
**Localização:** `backend/src/main/java/sgc/analise/AnaliseCadastroService.java`
- **Descrição:** Serviço que gerencia a lógica de negócio para `AnaliseCadastro`.
- **Métodos Principais:**
  - `listarPorSubprocesso(...)`: Lista todas as análises de um subprocesso.
  - `criarAnalise(...)`: Cria uma nova análise para um subprocesso.
  - `removerPorSubprocesso(...)`: Remove todas as análises de um subprocesso.

## Como Usar

### Gerenciando Atividades
Interaja com os endpoints do `AtividadeController` através de um cliente HTTP.

**Exemplo: Criar uma nova atividade**
```http
POST /api/atividades
Content-Type: application/json

{
  "descricao": "Revisar documentação do projeto X",
  "mapaId": 1
}
```

### Gerenciando Análises de Cadastro
Injete `AnaliseCadastroService` em outros serviços para gerenciar análises programaticamente.

**Exemplo:**
```java
@Service
public class MeuServico {

    @Autowired
    private AnaliseCadastroService analiseCadastroService;

    public void registrarAnalise(Long subprocessoId, String minhasObservacoes) {
        analiseCadastroService.criarAnalise(subprocessoId, minhasObservacoes);
    }
}
```

## Notas Importantes
- **Separação de Responsabilidades**: Embora no mesmo pacote, `Atividade` e `AnaliseCadastro` servem a propósitos distintos. A primeira é um recurso genérico, enquanto a segunda é específica para o fluxo de processos.
- **Uso de DTOs**: O `AtividadeController` utiliza DTOs (`AtividadeDTO`) para a comunicação com o frontend, o que é uma boa prática para desacoplar a API da estrutura do banco de dados.