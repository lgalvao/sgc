# Resumo do Dashboard de QA

- Gerado em: 2026-04-26T23:16:42.406Z
- Perfil: rapido
- Branch: main
- Commit: 9dfc377bc
- Status geral: vermelho
- Indice de saude: 20

## Verificacoes

| Verificacao | Status | Duracao (s) | Sumario |
| --- | --- | ---: | --- |
| Backend unitario | falha | 29.48 | 1208/1213 testes aprovados no backend unitario. |
| Backend cobertura | sucesso | 2.71 | Cobertura backend: 99.98% de linhas e 97.54% de branches. |
| Frontend cobertura | falha | 83.24 | Cobertura frontend: 90.03% de linhas com 1281 testes aprovados. |
| Frontend lint | falha | 6.53 | Lint frontend encontrou 14 problemas. |
| Frontend typecheck | falha | 4.52 | Typecheck frontend encontrou 2 erros. |

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
