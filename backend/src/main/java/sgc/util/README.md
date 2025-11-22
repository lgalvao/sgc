# Pacote Util

## Visão Geral
O pacote `util` agrupa classes utilitárias que fornecem funcionalidades auxiliares e transversais para a aplicação.

Estas classes são projetadas para serem estáticas e reutilizáveis, sem dependências de estado ou contexto de negócio específico.

## Componentes

### `HtmlUtils`

- **Responsabilidade:** Fornece métodos estáticos para manipulação e sanitização de HTML.
- **Uso Principal:** É utilizado principalmente na geração de e-mails e na limpeza de inputs de usuário para prevenir ataques XSS, embora a sanitização principal seja feita por filtros de segurança.
- **Métodos Típicos:**
    - Remoção de tags HTML inseguras.
    - Formatação de texto para exibição segura em templates HTML.
