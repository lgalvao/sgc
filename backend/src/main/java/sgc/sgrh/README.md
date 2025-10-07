# Integração com SGRH (Sistema de Gestão de RH)

## Visão Geral

Este pacote implementa a integração do SGC com o SGRH (Sistema de Gestão de RH) para consultar:
- Dados dos servidores (usuários)
- Estrutura organizacional (unidades)
- Responsabilidades (titulares e substitutos)
- Perfis por unidade

## Status da Implementação

✅ **Estrutura Completa Criada**
- Entidades JPA para as views do Oracle
- Repositórios somente leitura
- DTOs para transferência de dados
- Serviço com interface e implementação
- Configuração de fonte de dados (datasource) separada
- Cache configurado

⚠️ **Dados Simulados (Mock) Ativos**
- Todos os métodos do `SgrhServiceImpl` retornam dados simulados.
- A estrutura está pronta para se conectar ao Oracle.
- Marcadores `// TODO: Conectar ao banco SGRH real` indicam onde substituir os dados simulados.

## Estrutura de Pacotes

```
sgc/sgrh/
├── entity/              # Entidades JPA (views Oracle)
│   ├── VwUsuario.java
│   ├── VwUnidade.java
│   ├── VwResponsabilidade.java
│   └── VwUsuarioPerfilUnidade.java
├── repository/          # Repositórios somente leitura
│   ├── VwUsuarioRepository.java
│   ├── VwUnidadeRepository.java
│   ├── VwResponsabilidadeRepository.java
│   └── VwUsuarioPerfilUnidadeRepository.java
├── dto/                 # DTOs de transferência
│   ├── UsuarioDto.java
│   ├── UnidadeDto.java
│   ├── ResponsavelDto.java
│   └── PerfilDto.java
└── service/            # Serviços de negócio
    ├── SgrhService.java
    └── SgrhServiceImpl.java
```

## Configuração

### 1. Variáveis de Ambiente

Configure no `application.yml` ou via variáveis de ambiente:

```yaml
spring:
  sgrh:
    datasource:
      url: ${SGRH_DB_URL:jdbc:oracle:thin:@//localhost:1521/SGRH}
      username: ${SGRH_DB_USERNAME:sgrh_reader}
      password: ${SGRH_DB_PASSWORD:}
```

### 2. Dependências

Já adicionadas ao `build.gradle.kts`:
- `com.oracle.database.jdbc:ojdbc11:23.3.0.23.09`
- `com.github.ben-manes.caffeine:caffeine:3.1.8`
- `org.springframework.boot:spring-boot-starter-cache`

### 3. Cache

O cache está configurado com:
- **Tipo**: Caffeine
- **Tamanho máximo**: 500 entradas
- **Expiração**: 1 hora após a escrita

Caches disponíveis:
- `sgrh-usuarios`
- `sgrh-unidades`
- `sgrh-responsabilidades`
- `sgrh-perfis`

## Uso

### Injetar o Serviço

```java
@Service
@RequiredArgsConstructor
public class MinhaClasse {
    private final SgrhService sgrhService;
    
    public void meuMetodo() {
        // Buscar usuário
        Optional<UsuarioDto> usuario = sgrhService.buscarUsuarioPorTitulo("12345678901");
        
        // Buscar perfis
        List<PerfilDto> perfis = sgrhService.buscarPerfisUsuario("12345678901");
        
        // Buscar unidades
        List<UnidadeDto> unidades = sgrhService.buscarUnidadesAtivas();
        
        // Árvore hierárquica
        List<UnidadeDto> arvore = sgrhService.construirArvoreHierarquica();
    }
}
```

### Métodos Disponíveis

#### Usuários
- `buscarUsuarioPorTitulo(String titulo)` - Busca por CPF
- `buscarUsuarioPorEmail(String email)` - Busca por e-mail
- `buscarUsuariosAtivos()` - Lista todos os usuários ativos

#### Unidades
- `buscarUnidadePorCodigo(Long codigo)` - Busca por código
- `buscarUnidadesAtivas()` - Lista todas as unidades ativas
- `buscarSubunidades(Long codigoPai)` - Lista as unidades filhas de uma unidade
- `construirArvoreHierarquica()` - Monta a árvore completa

#### Responsabilidades
- `buscarResponsavelUnidade(Long unidadeCodigo)` - Busca titular/substituto
- `buscarUnidadesOndeEhResponsavel(String titulo)` - Lista as unidades de responsabilidade de um servidor

#### Perfis
- `buscarPerfisUsuario(String titulo)` - Lista os perfis com suas respectivas unidades
- `usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo)` - Verifica se o usuário tem um perfil em uma unidade
- `buscarUnidadesPorPerfil(String titulo, String perfil)` - Lista as unidades associadas a um perfil do usuário

## Integração com AuthService

O `AuthService` já foi atualizado para usar o `SgrhService`:

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SgrhService sgrhService;
    
    private List<PerfilDto> buscarPerfisUsuario(String titulo) {
        // Busca perfis via SGRH
        List<sgc.sgrh.dto.PerfilDto> perfisSgrh = sgrhService.buscarPerfisUsuario(titulo);
        // Converte para o DTO de segurança...
    }
}
```

## Migração de Dados Simulados (Mock) para Conexão Real

### Passo 1: Configurar Conexão Oracle

```properties
SGRH_DB_URL=jdbc:oracle:thin:@//servidor-oracle:1521/SGRH
SGRH_DB_USERNAME=usuario_leitura
SGRH_DB_PASSWORD=senha_segura
```

### Passo 2: Verificar Views no Oracle

Confirme que as views existem no schema `SGRH`:
- `SGRH.VW_USUARIO`
- `SGRH.VW_UNIDADE`
- `SGRH.VW_RESPONSABILIDADE`
- `SGRH.VW_USUARIO_PERFIL_UNIDADE`

### Passo 3: Substituir o Código Simulado

No `SgrhServiceImpl.java`, substitua cada método simulado:

**ANTES (Simulado):**
```java
public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
    // TODO: Conectar ao banco SGRH real
    log.warn("SGRH SIMULADO: Buscando usuário por título: {}", titulo);
    return Optional.of(new UsuarioDto(...)); // dados fictícios
}
```

**DEPOIS (Real):**
```java
public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
    log.debug("Buscando usuário por título no SGRH: {}", titulo);
    return usuarioRepository.findByTitulo(titulo)
        .map(u -> new UsuarioDto(
            u.getTitulo(),
            u.getNome(),
            u.getEmail(),
            u.getMatricula(),
            u.getCargo()
        ));
}
```

### Passo 4: Testar a Conexão

```java
@SpringBootTest
class SgrhServiceIntegrationTest {
    
    @Autowired
    private SgrhService sgrhService;
    
    @Test
    void testarBuscaDeUsuario() {
        Optional<UsuarioDto> usuario = sgrhService.buscarUsuarioPorTitulo("12345678901");
        assertThat(usuario).isPresent();
    }
}
```

## Solução de Problemas

### Erro de Conexão Oracle

```
Caused by: java.sql.SQLException: Listener refused the connection
```

**Solução**: Verifique a URL, porta e SID/Service Name do Oracle.

### Erro de Permissão

```
ORA-00942: table or view does not exist
```

**Solução**: Garanta que o usuário de banco de dados tenha permissão de `SELECT` nas views do schema SGRH.

### Cache Não Funciona

**Solução**: Verifique se a anotação `@EnableCaching` está ativa na classe de configuração principal da aplicação.

### Baixo Desempenho

**Solução**: 
- Aumentar o `pool` de conexões no `application.yml`.
- Ajustar os tempos de expiração do cache.
- Criar índices nas colunas consultadas das views Oracle.

## Logs

Para depuração (debug), habilite os seguintes logs no `application.yml`:

```yaml
logging:
  level:
    sgc.sgrh: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Segurança

⚠️ **IMPORTANTE**:
- A fonte de dados (datasource) do SGRH deve ser **somente leitura (read-only)**.
- O `pool` de conexões deve ser limitado (ex: 5 conexões).
- O `timeout` de conexão deve ser configurado (ex: 30 segundos).
- Todas as transações devem ser marcadas como somente leitura.
- **NUNCA** permita operações de `INSERT`, `UPDATE` ou `DELETE`.

## Próximos Passos

1. ✅ Estrutura criada
2. ⏳ Conectar ao banco de dados Oracle SGRH real
3. ⏳ Remover os dados simulados (mock)
4. ⏳ Adicionar testes de integração
5. ⏳ Monitorar o desempenho
6. ⏳ Documentar o mapeamento de campos das views

## Contatos

Para dúvidas sobre as views do SGRH no Oracle, contate:
- Equipe de RH
- DBA responsável pelo Oracle

---

**Data de Criação**: 2025-01-06  
**Última Atualização**: 2025-01-06  
**Status**: ✅ Estrutura Completa | ⚠️ Usando Dados Simulados (Mock)