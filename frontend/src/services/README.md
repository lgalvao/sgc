# Diretório de Serviços (Services)

Este diretório contém a camada de abstração de API. Os arquivos aqui são responsáveis por realizar as chamadas HTTP para o backend.

## Padrão de Implementação

Os serviços exportam objetos ou funções que utilizam a instância configurada do Axios (`@/axios-setup`).

```typescript
import api from '@/axios-setup';
import type { Processo } from '@/types';

export default {
  async listar(): Promise<Processo[]> {
    const { data } = await api.get('/processos');
    return data;
  },

  async criar(payload: CriarProcessoDto): Promise<Processo> {
    const { data } = await api.post('/processos', payload);
    return data;
  }
};
```

## Configuração do Axios (`axios-setup.ts`)

O cliente HTTP centralizado configura:
1.  **Base URL**: Aponta para a API (ex: `/api` via proxy ou `http://localhost:10000`).
2.  **Request Interceptor**: Injeta o cabeçalho `Authorization: Bearer <token>` se o usuário estiver logado.
3.  **Response Interceptor**: Trata erros globais, como `401 Unauthorized` (redirecionando para login) ou `403 Forbidden`.

## Serviços Principais

*   **`authService`**: Login e troca de perfil.
*   **`processoService`**: Operações de Processo.
*   **`subprocessoService`**: Operações de Subprocesso e Workflow.
*   **`mapaService`**: Operações específicas do Mapa (atividades, competências).
