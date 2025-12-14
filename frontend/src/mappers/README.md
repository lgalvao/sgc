# Mappers (Mapeadores de Dados)

Última atualização: 2025-12-14

Este diretório contém funções puras responsáveis por transformar dados entre o formato da API (DTOs do Backend) e o
formato utilizado internamente pelo Frontend (Interfaces TypeScript e Stores).

## Por que usar Mappers?

1. **Desacoplamento:** Isola o frontend de mudanças na estrutura da API. Se o backend mudar o nome de um campo, basta
   ajustar o mapper, sem precisar caçar todas as referências nos componentes.
2. **Formatação:** Permite formatar dados (como datas e moedas) logo na entrada, garantindo que os componentes recebam
   dados prontos para exibição.
3. **Tipagem:** Garante que os objetos manipulados no frontend estejam estritamente tipados de acordo com as interfaces
   do TypeScript.

## Arquivos Disponíveis

- **`alertas.ts`**: Mapeia DTOs de alertas para o modelo de alerta da UI.
- **`atividades.ts`**: Transforma dados de atividades e conhecimentos.
- **`mapas.ts`**: Lida com a estrutura complexa dos mapas de competências.
- **`processos.ts`**: Mapeia listas e detalhes de processos.
- **`servidores.ts`**: Mapeia dados de servidores (usuários).
- **`sgrh.ts`**: Mapeia dados vindos da integração com o SGRH (unidades, perfis).
- **`unidades.ts`**: Transforma dados da estrutura organizacional.

## Detalhamento técnico (gerado em 2025-12-14)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
