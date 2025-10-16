# Diretório de Constantes

Este diretório armazena valores constantes que são utilizados em toda a aplicação. O objetivo é centralizar "números mágicos" e strings literais em um único local, facilitando a manutenção e evitando duplicação.

## Conteúdo

Os arquivos aqui dentro devem exportar constantes nomeadas. Eles podem ser agrupados por domínio ou finalidade.

Exemplos de constantes que podem ser encontradas aqui:

- `API.ts`: URL base da API, _endpoints_ e chaves de API.
- `ROUTES.ts`: Nomes e caminhos das rotas da aplicação, para uso com o Vue Router.
- `DEFAULTS.ts`: Valores padrão, como o número de itens por página em uma tabela (`ITENS_POR_PAGINA = 10`).
- `ENUMS.ts`: Representações de enums do backend no frontend, como tipos de status ou perfis de usuário.

## Utilização

Para usar uma constante, basta importá-la diretamente do arquivo apropriado:

```typescript
import { API_BASE_URL } from '@/constants/API';
import { NOME_ROTA_HOME } from '@/constants/ROUTES';

// Exemplo de uso
axios.get(`${API_BASE_URL}/usuarios`);
router.push({ name: NOME_ROTA_HOME });
```