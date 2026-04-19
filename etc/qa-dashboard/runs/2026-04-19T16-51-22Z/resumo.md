# Resumo do Dashboard de QA

- Gerado em: 2026-04-19T16:52:20.215Z
- Perfil: rapido
- Branch: main
- Commit: 542398442
- Status geral: vermelho
- Indice de saude: 80

## Verificacoes

| Verificacao | Status | Duracao (s) | Sumario |
| --- | --- | ---: | --- |
| Backend unitario | sucesso | 0.42 | 1183/1183 testes aprovados no backend unitario. |
| Backend cobertura | sucesso | 0.63 | Cobertura backend: 99.98% de linhas e 97.54% de branches. |
| Frontend cobertura | falha | 49.29 | Cobertura frontend: 94.58% de linhas com 1283 testes aprovados. |
| Frontend lint | sucesso | 5.66 | Lint frontend sem problemas. |
| Frontend typecheck | sucesso | 2.02 | Typecheck frontend sem erros. |

## Hotspots

- sgc.organizacao.model.Responsabilidade: risco 100
- sgc.organizacao.model.Administrador$AdministradorBuilderImpl: risco 100
- sgc.organizacao.model.UsuarioPerfil$UsuarioPerfilBuilder: risco 100
- sgc.organizacao.model.Responsabilidade$ResponsabilidadeBuilder: risco 100
- sgc.organizacao.model.Responsabilidade$ResponsabilidadeBuilderImpl: risco 100
- sgc.organizacao.model.UsuarioConsultaLeitura$UsuarioConsultaLeituraBuilder: risco 100
- sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura$UsuarioPerfilAutorizacaoLeituraBuilder: risco 100
- sgc.organizacao.model.Usuario$UsuarioBuilderImpl: risco 100
- sgc.organizacao.model.ResponsabilidadeUnidadeLeitura$ResponsabilidadeUnidadeLeituraBuilder: risco 100
- sgc.organizacao.model.ResponsabilidadeUnidadeResumoLeitura$ResponsabilidadeUnidadeResumoLeituraBuilder: risco 100
