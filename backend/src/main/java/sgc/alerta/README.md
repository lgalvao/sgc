# Módulo de Alertas - SGC

## Visão Geral
Este pacote é responsável pela gestão e criação de alertas dentro do sistema. Os alertas são notificações direcionadas a usuários específicos, geralmente relacionadas a eventos importantes em processos e subprocessos, como o início de um processo, a disponibilização de um cadastro ou a devolução de um documento.

O sistema de alertas é projetado para garantir que os usuários relevantes sejam notificados sobre ações que exigem sua atenção, diferenciando as mensagens com base no tipo de unidade organizacional (Operacional, Intermediária, etc.).

## Arquivos Principais

### 1. `Alerta.java`
**Localização:** `backend/src/main/java/sgc/alerta/Alerta.java`
- **Descrição:** Entidade JPA que representa um alerta no sistema. Mapeia a tabela `TB_ALERTA` no banco de dados.
- **Campos Importantes:**
  - `processo`: O processo ao qual o alerta está associado.
  - `tipoAlerta`: Uma string que categoriza o alerta (ex: `PROCESSO_INICIADO_OPERACIONAL`).
  - `descricao`: O texto do alerta a ser exibido ao usuário.
  - `dataLimite`: Uma data opcional para a conclusão da ação relacionada ao alerta.
  - `dataCiencia`: A data em que o usuário marcou o alerta como lido.

### 2. `AlertaUsuario.java`
**Localização:** `backend/src/main/java/sgc/alerta/AlertaUsuario.java`
- **Descrição:** Entidade que representa a associação entre um `Alerta` e um `Usuario`. Um alerta pode ser direcionado a múltiplos usuários.
- **Relacionamentos:**
  - `ManyToOne` com `Alerta`.
  - `ManyToOne` com `Usuario`.

### 3. `AlertaService.java` (Interface)
**Localização:** `backend/src/main/java/sgc/alerta/AlertaService.java`
- **Descrição:** Define o contrato para os serviços que gerenciam alertas.
- **Métodos Principais:**
  - `criarAlerta(...)`: Cria um alerta genérico para uma unidade de destino.
  - `criarAlertasProcessoIniciado(...)`: Lógica de negócio para criar alertas específicos quando um novo processo é iniciado.
  - `criarAlertaCadastroDisponibilizado(...)`: Cria um alerta para notificar sobre um cadastro que foi disponibilizado.
  - `criarAlertaCadastroDevolvido(...)`: Cria um alerta para notificar sobre um cadastro que foi devolvido com um motivo.

### 4. `AlertaServiceImpl.java` (Implementação)
**Localização:** `backend/src/main/java/sgc/alerta/AlertaServiceImpl.java`
- **Descrição:** Implementação concreta da interface `AlertaService`. Contém a lógica de negócio para criar e salvar os alertas e suas associações com os usuários.
- **Funcionalidades:**
  - Determina os destinatários de um alerta (responsável da unidade e superiores).
  - Customiza as mensagens de alerta com base no tipo de unidade (Operacional, Intermediária).
  - Interage com `AlertaRepository` e `AlertaUsuarioRepository` para persistir os dados.

### 5. `AlertaRepository.java` e `AlertaUsuarioRepository.java`
**Localização:** `backend/src/main/java/sgc/alerta/`
- **Descrição:** Interfaces Spring Data JPA para acesso aos dados das entidades `Alerta` and `AlertaUsuario`.

### 6. `AlertaDto.java`
**Localização:** `backend/src/main/java/sgc/alerta/AlertaDto.java`
- **Descrição:** Um Data Transfer Object (DTO) para transportar informações de alerta entre as camadas da aplicação, geralmente para a API REST.

## Fluxo de Criação de Alerta

1.  **Invocação do Serviço**: Um serviço de nível superior (ex: `ProcessoService`) chama um método em `AlertaService` (ex: `criarAlertasProcessoIniciado`).
2.  **Lógica de Negócio**: `AlertaServiceImpl` processa a requisição, determina a mensagem, os destinatários e outras propriedades do alerta.
3.  **Criação da Entidade**: Uma nova instância de `Alerta` é criada.
4.  **Identificação dos Destinatários**: O serviço busca os usuários associados à unidade de destino.
5.  **Persistência**: O `Alerta` e as associações `AlertaUsuario` são salvos no banco de dados através dos respectivos repositórios.

## Como Usar

Para criar um alerta, injete `AlertaService` em seu componente e chame o método apropriado.

**Exemplo:**
```java
@Service
public class GestaoDeProcessosService {

    @Autowired
    private AlertaService alertaService;

    @Autowired
    private ProcessoRepository processoRepository;

    public void iniciarNovoProcesso(Long processoId) {
        Processo processo = processoRepository.findById(processoId)
            .orElseThrow(() -> new RuntimeException("Processo não encontrado"));

        // ... lógica para criar subprocessos ...
        List<Subprocesso> subprocessos = ...;

        // Cria os alertas para o início do processo
        alertaService.criarAlertasProcessoIniciado(processo, subprocessos);
    }
}
```

## Notas Importantes
- **Diferenciação por Tipo de Unidade**: A lógica em `criarAlertasProcessoIniciado` é um bom exemplo de como o sistema adapta a comunicação com base no papel da unidade no processo.
- **Associação com Usuários**: Alertas não são ligados diretamente a unidades, mas aos usuários responsáveis por elas, garantindo que a notificação chegue à pessoa certa.