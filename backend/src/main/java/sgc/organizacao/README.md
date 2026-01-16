# Pacote Organizacao

## Visão Geral

O pacote `organizacao` é responsável pelo gerenciamento de **usuários** e **unidades organizacionais** dentro do SGC. Ele consolida o que antes eram dois pacotes separados (`usuario` e `unidade`), mantendo uma interface coesa para informações de identidade e estrutura organizacional.

## Responsabilidades Principais

1. **Gestão de Usuários:**
   - Autenticação e autorização via `UsuarioFacade`
   - Gerenciamento de perfis (`ADMIN`, `GESTOR`, `CHEFE`, `SERVIDOR`)
   - Administração de usuários com papel de administrador

2. **Gestão de Unidades:**
   - Hierarquia organizacional via `UnidadeService`
   - Mapa vigente por unidade
   - Atribuições temporárias de servidores

## Componentes Principais

### Controladores

- **`UsuarioController`**: Endpoints de autenticação, autorização e administradores (`/api/usuarios`)
- **`UnidadeController`**: Endpoints de hierarquia e atribuições (`/api/unidades`)

### Serviços

- **`UsuarioFacade`**: Lógica de autenticação, autorização e gestão de administradores
- **`UnidadeService`**: Hierarquia de unidades, elegibilidade e atribuições temporárias

### Modelo (`model/`)

Entidades de usuário:
- `Usuario`, `UsuarioPerfil`, `Perfil`, `Administrador`

Entidades de unidade:
- `Unidade`, `UnidadeMapa`, `AtribuicaoTemporaria`, `VinculacaoUnidade`

## Integração com Segurança

O módulo trabalha em conjunto com o pacote `seguranca` para:
- Gerar tokens JWT via `GerenciadorJwt`
- Validar requisições via filtros de segurança
- Recuperar dados de usuário autenticado

## Como Testar

Para executar apenas os testes deste módulo (a partir do diretório `backend`):
```bash
./gradlew test --tests "sgc.organizacao.*"
```
