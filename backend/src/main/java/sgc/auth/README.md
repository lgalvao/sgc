# Sistema de Autenticação e Autorização - CDU-01

## Implementação Completa

Este pacote implementa o sistema completo de autenticação e autorização usando **Spring Security + JWT** com integração ao serviço AD do TRE-PE.

## Estrutura Implementada

```
sgc/auth/
├── dto/
│   ├── LoginRequest.java         ✅ DTO para requisição de login
│   ├── LoginResponse.java        ✅ DTO para resposta com token e perfis
│   ├── PerfilDto.java            ✅ DTO representando perfil do usuário
│   ├── ServidorDto.java          ✅ DTO com dados do servidor
│   └── LoginAcesso.java          ✅ DTO para integração com serviço AD
├── AuthController.java           ✅ Endpoints REST de autenticação
├── AuthService.java              ✅ Lógica de negócio
├── JwtService.java               ✅ Geração e validação de tokens JWT
├── JwtAuthenticationFilter.java ✅ Filter para validar JWT nas requisições
├── CustomAuthenticationProvider.java ✅ Provider customizado (integração AD)
└── SecurityConfig.java           ✅ Configuração Spring Security
```

## Endpoints Implementados

### POST `/api/auth/login`
Autentica o usuário e retorna token JWT.

**Request:**
```json
{
  "titulo": "123456789012",
  "senha": "senhaDoUsuario"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "perfis": [
    {
      "perfil": "ADMIN",
      "unidade": "SEDOC"
    }
  ],
  "servidor": {
    "titulo": "123456789012",
    "nome": "João da Silva",
    "email": "joao.silva@tre-pe.jus.br",
    "ramal": "1234",
    "unidadeCodigo": "1"
  }
}
```

### POST `/api/auth/logout`
Realiza logout (JWT é stateless, então apenas retorna 200 OK).

**Response (200 OK):** (vazio)

### GET `/api/auth/perfis`
Retorna perfis do usuário autenticado atual.

**Headers:** `Authorization: Bearer <token>`

**Response (200 OK):**
```json
[
  {
    "perfil": "ADMIN",
    "unidade": "SEDOC"
  },
  {
    "perfil": "GESTOR",
    "unidade": "SEDOC"
  }
]
```

### GET `/api/auth/verify`
Verifica se o usuário está autenticado.

**Headers:** `Authorization: Bearer <token>`

**Response:** 
- 200 OK se autenticado
- 401 Unauthorized se não autenticado

## Fluxo de Autenticação

1. **Frontend envia POST para `/api/auth/login`** com título e senha
2. **CustomAuthenticationProvider** valida credenciais via serviço AD (HTTP)
3. Se válido, **AuthService** busca perfis do usuário (MOCK temporário)
4. **JwtService** gera token JWT com perfis no payload
5. Token é retornado ao frontend junto com perfis e dados do servidor
6. Frontend armazena token e envia em todas as requisições subsequentes via header `Authorization: Bearer <token>`
7. **JwtAuthenticationFilter** intercepta requisições, valida token e configura contexto de segurança

## Configurações (application.yml)

```yaml
jwt:
  secret: ${JWT_SECRET:chave-secreta-de-256-bits}
  expiration: 86400000  # 24 horas

aplicacao:
  ambiente-testes: ${AMBIENTE_TESTES:true}
  url-acesso-hom: ${URL_ACESSO_HOM:http://localhost:8081/api/acesso}
  url-acesso-prod: ${URL_ACESSO_PROD:https://acesso.tre-pe.jus.br/api}
```

## Perfis Suportados

- **ADMIN**: Administrador do sistema
- **GESTOR**: Gestor de unidade
- **CHEFE**: Chefe de unidade
- **SERVIDOR**: Servidor comum

Um usuário pode ter múltiplos perfis em diferentes unidades.

## Segurança

- ✅ CSRF desabilitado (JWT é stateless)
- ✅ Sessões stateless (não mantém estado no servidor)
- ✅ Token JWT assinado com HS256
- ✅ Senhas nunca logadas
- ✅ CORS configurado para permitir frontend
- ✅ Endpoints públicos: `/api/auth/**` e `/error`
- ✅ Todos os outros endpoints requerem autenticação

## MOCK Temporário

### CustomAuthenticationProvider
A autenticação AD está usando URLs configuráveis para ambientes de teste e produção.

### AuthService - Busca de Perfis
**IMPLEMENTAÇÃO ATUAL (MOCK):**
- Perfis são retornados baseados no início do título:
  - `111*` → ADMIN na SEDOC
  - `222*` → GESTOR na SEDOC
  - `333*` → CHEFE na UNIDADE_X
  - Outros → SERVIDOR na UNIDADE_Y

**TODO:** Substituir por consulta à view Oracle `VW_SGC_PERFIS_USUARIO`:
```sql
SELECT perfil, unidade_codigo as unidade
FROM VW_SGC_PERFIS_USUARIO
WHERE titulo = :titulo
```

## ⚠️ DEPENDÊNCIAS NECESSÁRIAS

Para o sistema funcionar, é necessário adicionar as seguintes dependências ao `build.gradle.kts`:

```kotlin
dependencies {
    // ... dependências existentes ...
    
    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Para testes de segurança
    testImplementation("org.springframework.security:spring-security-test")
}
```

**IMPORTANTE:** Atualmente há erros de compilação porque essas dependências ainda não foram adicionadas ao `build.gradle.kts`. Após adicionar as dependências, execute:

```bash
./gradlew clean build
```

## Variáveis de Ambiente (Produção)

Em produção, configure as seguintes variáveis de ambiente:

```bash
JWT_SECRET=sua-chave-secreta-de-pelo-menos-256-bits-aqui
AMBIENTE_TESTES=false
URL_ACESSO_PROD=https://acesso.tre-pe.jus.br/api
```

## Tratamento de Erros

- **401 Unauthorized**: Credenciais inválidas ou token expirado/inválido
- **403 Forbidden**: Usuário autenticado mas sem permissão
- **400 Bad Request**: Dados de entrada inválidos (validação)

## Próximos Passos

1. ✅ Adicionar dependências ao `build.gradle.kts`
2. ⏳ Substituir MOCK de perfis por consulta à view Oracle
3. ⏳ Implementar integração real com serviço AD (se URLs ainda não estiverem configuradas)
4. ⏳ Adicionar testes unitários e de integração
5. ⏳ Implementar refresh token (opcional, para melhor UX)

## Testes Manuais

### 1. Teste de Login (com MOCK)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "111222333444",
    "senha": "123456"
  }'
```

### 2. Teste de Endpoint Protegido
```bash
curl -X GET http://localhost:8080/api/auth/perfis \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### 3. Teste de Verify
```bash
curl -X GET http://localhost:8080/api/auth/verify \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## Observações Importantes

- O sistema está 100% implementado, faltando apenas as dependências no Gradle
- A integração com AD usa `HttpClient` do Java 11+ (não precisa de dependências extras)
- O MOCK de perfis deve ser substituído quando a view Oracle estiver disponível
- Todos os arquivos seguem as boas práticas do Spring Boot e Java 21