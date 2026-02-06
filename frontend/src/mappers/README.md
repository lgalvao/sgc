# Diretório de Mappers

Responsáveis pela transformação de dados entre a camada de API (DTOs) e a camada de Visualização (Modelos do Frontend).

## Mappers Disponíveis

* **`processos.ts`**: Conversão de cronogramas e dados de processo.
* **`atividades.ts`**: Formatação de itens de atividade e taxonomias.
* **`mapas.ts`**: Transformação de competências e revisões de mapa.
* **`unidades.ts`**: Mapeamento da árvore e detalhes de unidades.
* **`usuarios.ts`**: Dados de perfil e permissões de usuário.
* **`alertas.ts`**: Transformação de notificações e alertas.
* **`sgrh.ts`**: Integração de dados provenientes do sistema de RH.

## Por que usar Mappers?

1. **Desacoplamento**: Protege o frontend contra mudanças na estrutura do JSON da API.
2. **Sanitização**: Garante que campos opcionais tenham valores padrão seguros.
3. **Formatação**: Centraliza lógica de exibição (ex: converter `STATUS_CONCLUIDO` para "Concluído").
