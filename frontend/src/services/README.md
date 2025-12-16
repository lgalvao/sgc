# Services (Camada de Serviço)


Este diretório contém os módulos responsáveis pela **comunicação com o Backend**.

Os serviços atuam como uma camada de abstração sobre o protocolo HTTP. Os componentes e stores nunca devem chamar
`axios.get` ou `axios.post` diretamente; eles devem invocar métodos semânticos nos serviços (ex:
`processoService.criar(...)`).

## Padrão de Implementação

Cada arquivo de serviço exporta um objeto ou classe contendo métodos que correspondem aos endpoints da API. Eles
utilizam a instância configurada do Axios (`apiClient` de `src/axios-setup.ts`) para fazer as requisições.

### Arquivos Disponíveis

- **`alertaService.ts`**: Gerencia alertas do usuário (`/api/alertas`).
- **`analiseService.ts`**: Busca e cria históricos de análise (`/api/subprocessos/.../analises`).
- **`atividadeService.ts`**: CRUD de atividades e conhecimentos (`/api/atividades`).
- **`atribuicaoTemporariaService.ts`**: Gerencia atribuições temporárias de chefia.
- **`cadastroService.ts`**: Ações de workflow da etapa de cadastro.
- **`mapaService.ts`**: Operações no mapa de competências (`/api/mapas`).
- **`painelService.ts`**: Dados para o dashboard (`/api/painel`).
- **`processoService.ts`**: Gestão de processos (`/api/processos`).
- **`subprocessoService.ts`**: Gestão de subprocessos e workflow (`/api/subprocessos`).
- **`unidadesService.ts`**: Consulta de unidades (`/api/unidades`).
- **`usuarioService.ts`**: Autenticação e gestão de usuários (`/api/usuarios`).

## Tratamento de Erros

Os serviços devem repassar os erros do Axios para que possam ser tratados pelas camadas superiores (Stores ou
Components), que decidirão como exibir a mensagem ao usuário (Toast, Modal, etc.).

## Referência Cruzada de API

| Frontend Service | Backend Controller | Prefixo URL |
|------------------|--------------------|-------------|
| `alertaService.ts` | `AlertaController` | `/api/alertas` |
| `analiseService.ts` | `AnaliseController` | `/api/subprocessos/{id}` |
| `atividadeService.ts` | `AtividadeController` | `/api/atividades` |
| `atribuicaoTemporariaService.ts` | `UnidadeController` | `/api/unidades` |
| `cadastroService.ts` | `SubprocessoCadastroController` | `/api/subprocessos` |
| `mapaService.ts` | `MapaController` | `/api/mapas` |
| `painelService.ts` | `PainelController` | `/api/painel` |
| `processoService.ts` | `ProcessoController` | `/api/processos` |
| `subprocessoService.ts` | `SubprocessoCrudController`, `SubprocessoMapaController`, `SubprocessoValidacaoController` | `/api/subprocessos` |
| `unidadesService.ts` | `UnidadeController` | `/api/unidades` |
| `usuarioService.ts` | `SgrhController` | `/api/usuarios` |

