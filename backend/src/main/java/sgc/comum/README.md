# Módulo Comum - SGC

## Visão Geral
O pacote `comum` é o coração da aplicação, contendo classes e configurações transversais que são utilizadas por diversos outros módulos. Ele serve como uma base compartilhada, definindo entidades centrais, configurações globais, tratamento de erros e outros componentes de utilidade geral.

## Arquivos e Diretórios Principais

### Entidades Comuns

#### 1. `BaseEntity.java`
**Localização:** `backend/src/main/java/sgc/comum/BaseEntity.java`
- **Descrição:** Uma `@MappedSuperclass` que fornece um campo de ID (`codigo`) autoincrementado para a maioria das entidades do sistema. Garante a padronização das chaves primárias.

#### 2. `Usuario.java`
**Localização:** `backend/src/main/java/sgc/comum/Usuario.java`
- **Descrição:** Entidade que representa um usuário do sistema, com informações como nome, e-mail e a unidade à qual pertence.

#### 3. `Administrador.java`
**Localização:** `backend/src/main/java/sgc/comum/Administrador.java`
- **Descrição:** Entidade que representa um administrador do sistema, provavelmente com permissões elevadas.

#### 4. `Parametro.java`
**Localização:** `backend/src/main/java/sgc/comum/Parametro.java`
- **Descrição:** Entidade para armazenar parâmetros de configuração do sistema no banco de dados, permitindo ajustes dinâmicos sem a necessidade de reimplantar a aplicação.

### Sub-pacotes

#### 5. `config/`
**Localização:** `backend/src/main/java/sgc/comum/config/`
- **Descrição:** Contém classes de configuração do Spring.
- **Arquivos Notáveis:**
  - `AsyncConfig.java`: Configura o pool de threads para operações assíncronas (como envio de e-mails).
  - `SgrhDataSourceConfig.java`: Configura uma fonte de dados secundária para integração com outro sistema (SGRH).
  - `WebConfig.java`: Define configurações globais da aplicação web, como CORS (Cross-Origin Resource Sharing).

#### 6. `erros/`
**Localização:** `backend/src/main/java/sgc/comum/erros/`
- **Descrição:** Define uma hierarquia de exceções customizadas e não checadas (`RuntimeException`) para padronizar o tratamento de erros na aplicação.
- **Exceções Notáveis:**
  - `ErroEntidadeNaoEncontrada.java`: Lançada quando uma busca por uma entidade no banco de dados não retorna resultados.
  - `ErroDominioAccessoNegado.java`: Lançada quando um usuário tenta executar uma ação para a qual não tem permissão.
  - `ErroServicoExterno.java`: Para erros de comunicação com serviços externos.

### Outros Componentes

#### 7. `PainelController.java` e `PainelService.java`
**Localização:** `backend/src/main/java/sgc/comum/`
- **Descrição:** Componentes que provavelmente servem para alimentar um painel de controle (dashboard) com dados agregados e estatísticas do sistema.

## Como Usar
As classes deste pacote são, em sua maioria, utilizadas implicitamente por outras partes do sistema.
- **Entidades**: São estendidas (`BaseEntity`) ou referenciadas (`Usuario`) por outras entidades.
- **Configurações**: São carregadas automaticamente pelo Spring no momento da inicialização.
- **Exceções**: São lançadas por serviços em outros pacotes para sinalizar erros de forma consistente.

**Exemplo de uso de uma exceção customizada:**
```java
@Service
public class MeuServico {

    @Autowired
    private RecursoRepository recursoRepository;

    public Recurso buscarRecurso(Long id) {
        return recursoRepository.findById(id)
            .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Recurso com ID " + id + " não encontrado."));
    }
}
```

## Notas Importantes
- **Centralização**: Manter componentes compartilhados neste pacote evita a duplicação de código e promove a consistência em toda a aplicação.
- **Configuração de Múltiplos DataSources**: A presença de `SgrhDataSourceConfig` indica que a aplicação se conecta a mais de um banco de dados, uma configuração avançada gerenciada pelo Spring.
- **Tratamento de Erros Padronizado**: O uso das exceções customizadas do pacote `erros` permite que um `ControllerAdvice` global (geralmente localizado no mesmo pacote) capture e traduza essas exceções em respostas HTTP padronizadas (ex: 404 Not Found, 403 Forbidden).