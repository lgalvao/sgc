# Diretório de Types

Este diretório é o repositório central para todas as definições de tipos e interfaces TypeScript da aplicação. A utilização de um sistema de tipos robusto é fundamental para a qualidade, manutenibilidade e escalabilidade do código.

## Objetivo

O principal objetivo é fornecer um "contrato" claro sobre a estrutura dos dados que circulam pela aplicação. Isso resulta em:

- **Segurança de Tipos (_Type Safety_)**: Evita erros comuns em tempo de execução, como `undefined is not a function`, ao garantir que as propriedades e métodos esperados existam em um objeto.
- **IntelliSense e Autocompletar**: Melhora drasticamente a experiência de desenvolvimento, fornecendo autocompletar e documentação contextual no editor de código.
- **Refatoração Segura**: Permite refatorar o código com mais confiança, pois o compilador TypeScript apontará os locais que precisam ser atualizados após uma mudança em uma definição de tipo.
- **Documentação Viva**: As próprias definições de tipo servem como uma documentação precisa das estruturas de dados do projeto.

## Estrutura e Organização

Os tipos são organizados em arquivos e subdiretórios com base em seu domínio e propósito:

- **`dto/`**: Um subdiretório para as interfaces que representam os **Data Transfer Objects (DTOs)**. Essas interfaces devem espelhar exatamente a estrutura dos dados JSON retornados pela API do _backend_.
- **Modelos de Visão (_View Models_)**: Arquivos que definem a forma dos dados como são usados pelos componentes e _stores_. Frequentemente, são o resultado da transformação de um DTO por um `mapper`. Por exemplo, `UsuarioViewModel.ts`.
- **Tipos de Domínio**: Tipos que representam conceitos centrais da aplicação, como `StatusProcesso.ts` ou `PerfilUsuario.ts`.

## Convenção

- Utilize `interface` para definir a forma de objetos e `type` para uniões, tuplas ou tipos mais complexos.
- Os nomes dos tipos e interfaces devem seguir a convenção `PascalCase`.
- Exporte cada tipo ou interface para que possam ser importados em outras partes da aplicação.