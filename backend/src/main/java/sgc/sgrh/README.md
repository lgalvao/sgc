# Integração com SGRH (Sistema de Gestão de RH)

## Visão Geral

Este pacote implementa a integração do SGC com o SGRH (Sistema de Gestão de RH) para consultar:
- Dados dos servidores (usuários)
- Estrutura organizacional (unidades)
- Responsabilidades (titulares e substitutos)
- Perfis por unidade

## Status da Implementação

✅ **Estrutura Completa Criada**
- Entidades JPA para views Oracle
- Repositories read-only
- DTOs para transferência de dados
- Service com interface e implementação
- Configuração de datasource separado
- Cache configurado

⚠️ **Dados MOCK Ativos**
- Todos os Metodos do `SgrhService` retornam dados MOCK
- A estrutura está pronta para conectar ao Oracle
- Marcadores `// TODO: Conectar ao banco SGRH real` indicam onde substituir

## Estrutura de Pacotes

```
sgc/sgrh/
├── entity/              # Entidades JPA (views Oracle)
│   ├── VwUsuario.java
│   ├── VwUnidade.java
│   ├── VwResponsabilidade.java
│   └── VwUsuarioPerfilUnidade.java
├── repository/          # Repositories read-only
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
    └── SgrhService.java
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

## Uso

### Injetar o Service

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

### Metodos Disponíveis

#### Usuários
- `buscarUsuarioPorTitulo(String titulo)` - Busca por CPF
- `buscarUsuarioPorEmail(String email)` - Busca por email
- `buscarUsuariosAtivos()` - Lista todos ativos

#### Unidades
- `buscarUnidadePorCodigo(Long codigo)` - Busca por código
- `buscarUnidadesAtivas()` - Lista todas ativas
- `buscarSubunidades(Long codigoPai)` - Lista filhos de uma unidade
- `construirArvoreHierarquica()` - Monta árvore completa

#### Responsabilidades
- `buscarResponsavelUnidade(Long unidadeCodigo)` - Busca titular/substituto
- `buscarUnidadesOndeEhResponsavel(String titulo)` - Lista unidades do servidor

#### Perfis
- `buscarPerfisUsuario(String titulo)` - Lista perfis com unidades
- `usuarioTemPerfil(String titulo, String perfil, Long unidadeCodigo)` - Verifica perfil
- `buscarUnidadesPorPerfil(String titulo, String perfil)` - Lista unidades por perfil

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
        // Converte para DTO do seguranca...
    }
}
```

## Migração de MOCK para Real

### Passo 1: Configurar Conexão Oracle

```properties
SGRH_DB_URL=jdbc:oracle:thin:@//servidor-oracle:1521/SGRH
SGRH_DB_USERNAME=usuario_leitura
SGRH_DB_PASSWORD=senha_segura
```

### Passo 2: Verificar Views no Oracle

Confirme que as views existem:
- `SGRH.VW_USUARIO`
- `SGRH.VW_UNIDADE`
- `SGRH.VW_RESPONSABILIDADE`
- `SGRH.VW_USUARIO_PERFIL_UNIDADE`

### Passo 3: Descomentar Código Real

No `SgrhService.java`, substitua cada Metodo MOCK:

**ANTES (MOCK):**
```java
public Optional<UsuarioDto> buscarUsuarioPorTitulo(String titulo) {
    // TODO: Conectar ao banco SGRH real
    log.warn("MOCK SGRH: Buscando usuário por título: {}", titulo);
    return Optional.of(new UsuarioDto(...)); // dados fake
}
```

**DEPOIS (REAL):**
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

### Passo 4: Testar Conexão

```java
@SpringBootTest
class SgrhServiceIntegrationTest {
    
    @Autowired
    private SgrhService sgrhService;
    
    @Test
    void testBuscarUsuario() {
        Optional<UsuarioDto> usuario = sgrhService.buscarUsuarioPorTitulo("12345678901");
        assertThat(usuario).isPresent();
    }
}
```

## Troubleshooting

### Erro de Conexão Oracle

```
Caused by: java.sql.SQLException: Listener refused the connection
```

**Solução**: Verificar URL, porta e SID/Service Name do Oracle.

### Erro de Permissão

```
ORA-00942: table or view does not exist
```

**Solução**: Garantir que o usuário tem `SELECT` nas views do schema SGRH.

### Cache não Funciona

**Solução**: Verificar se `@EnableCaching` está ativo na configuração principal.

### Performance Lenta

**Solução**: 
- Aumentar pool de conexões no `application.yml`
- Ajustar tempos de cache
- Criar índices nas views Oracle

## Logs

Para debug, habilite logs do SGRH:

```yaml
logging:
  level:
    sgc.sgrh: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Segurança

⚠️ **IMPORTANTE**:
- Datasource SGRH é **READ-ONLY**
- Pool de conexões limitado a 5
- Timeout de 30 segundos
- Todas transações são read-only
- NUNCA permitir INSERT/UPDATE/DELETE

## Próximos Passos

1. ✅ Estrutura criada
2. ⏳ Conectar ao Oracle SGRH real
3. ⏳ Remover dados MOCK
4. ⏳ Adicionar testes de integração
5. ⏳ Monitorar performance
6. ⏳ Documentar mapeamento de campos das views

## Contatos

Para dúvidas sobre as views Oracle do SGRH, contatar:
- Equipe de RH
- DBA responsável pelo Oracle

---

**Data de Criação**: 2025-01-06  
**Última Atualização**: 2025-01-06  
**Status**: ✅ Estrutura Completa | ⚠️ Usando MOCK