# Diretório de Serviços (Services)

Este diretório contém a camada de abstração de rede. Os serviços são responsáveis por realizar as comunicações HTTP com
o backend.

## Padrão de Implementação

Os serviços utilizam a instância centralizada do Axios configurada em `@/axios-setup`.

```typescript
import api from '@/axios-setup';

export const exemploService = {
  listar: () => api.get('/exemplo').then(r => r.data),
};
```

## Serviços Disponíveis

* **`usuarioService`**: Login, perfis e dados de usuários.
* **`processoService`**: Gestão de processos e cronogramas.
* **`subprocessoService`**: Operações de fluxo de unidades.
* **`atividadeService`**: Cadastro e listagem de atividades.
* **`mapaService`**: Gestão de competências e revisões de mapa.
* **`diagnosticoService`**: Autoavaliação e monitoramento.
* **`unidadeService`**: Busca e hierarquia de unidades.
* **`alertaService`**: Recuperação de alertas do sistema.
* **`analiseService`**: Logs e histórico de auditoria.
* **`atribuicaoTemporariaService`**: Gestão de substitutos.
* **`configuracaoService`**: Parâmetros do sistema.
* **`administradorService`**: Gestão de administradores do sistema.
* **`painelService`**: Dados agregados para o dashboard.
* **`cadastroService`**: Operações auxiliares de cadastro.

## Configuração do Axios (`axios-setup.ts`)

O arquivo central de configuração trata:

1. **Base URL**: Definida via variáveis de ambiente.
2. **Interceptors**: Injeção de token Bearer e tratamento global de erros (401, 403, 500).
