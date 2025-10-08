# Módulo de Análise - SGC

## Visão Geral
Este pacote é responsável por gerenciar os registros de **Análise** que ocorrem durante o ciclo de vida de um `Subprocesso`. Ele captura as observações e decisões tomadas durante as etapas de revisão e validação dos cadastros e mapas.

Existem dois tipos principais de análise gerenciados aqui:

1.  **Análise de Cadastro**: Registros criados quando um cadastro de atividades é aceito ou devolvido por um gestor ou administrador.
2.  **Análise de Validação**: Registros criados durante o fluxo de validação de um mapa de competências.

## Arquivos Principais

### 1. `AnaliseControle.java`
**Localização:** `backend/src/main/java/sgc/analise/AnaliseControle.java`
- **Descrição:** Controlador REST que expõe endpoints para criar e listar os diferentes tipos de análise, sempre no contexto de um subprocesso.
- **Endpoints:**
  - `GET /api/subprocessos/{id}/analises-cadastro`: Lista as análises de cadastro de um subprocesso.
  - `POST /api/subprocessos/{id}/analises-cadastro`: Cria uma nova análise de cadastro.
  - `GET /api/subprocessos/{id}/analises-validacao`: Lista as análises de validação de um subprocesso.
  - `POST /api/subprocessos/{id}/analises-validacao`: Cria uma nova análise de validação.

### 2. Entidades de Análise (`modelo/`)
**Localização:** `backend/src/main/java/sgc/analise/modelo/`
- **`AnaliseCadastro.java`**: Entidade JPA que representa um registro de análise sobre o cadastro de atividades. Está vinculada a um `Subprocesso`.
- **`AnaliseValidacao.java`**: Entidade JPA para um registro de análise sobre a validação de um mapa.

### 3. Serviços de Análise
- **`AnaliseCadastroService.java` (e Impl)**: Define e implementa o contrato para as operações de negócio com `AnaliseCadastro` (criar, listar, remover).
- **`AnaliseValidacaoService.java` (e Impl)**: Define e implementa o contrato para as operações de negócio com `AnaliseValidacao`.

### 4. Repositórios (`modelo/`)
- **`AnaliseCadastroRepo.java`**: Interface Spring Data JPA para acesso aos dados de `AnaliseCadastro`.
- **`AnaliseValidacaoRepo.java`**: Interface Spring Data JPA para acesso aos dados de `AnaliseValidacao`.

## Como Usar

A criação de análises é normalmente orquestrada pelo `SubprocessoService` quando uma ação de fluxo de trabalho (como "devolver cadastro" ou "aceitar validação") é executada. Os endpoints do `AnaliseControle` também podem ser usados para registrar observações de forma ad-hoc.

**Exemplo de uso pelo serviço:**
```java
// Dentro de SubprocessoService.java

@Autowired
private AnaliseCadastroService analiseCadastroService;

public void devolverCadastro(Long subprocessoId, String motivo) {
    // ... lógica de negócio ...

    // Cria um registro da análise que motivou a devolução
    analiseCadastroService.criarAnalise(subprocessoId, motivo);

    // ... resto da lógica ...
}
```

## Notas Importantes
- **Trilha de Auditoria**: As entidades de análise são uma parte crucial da trilha de auditoria do sistema, pois armazenam as justificativas para as decisões tomadas durante o fluxo de trabalho.
- **Vínculo com Subprocesso**: Todas as análises estão intrinsecamente ligadas a um `Subprocesso`, que fornece o contexto para a análise.
