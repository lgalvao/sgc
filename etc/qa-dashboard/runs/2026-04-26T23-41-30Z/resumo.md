# Resumo do Dashboard de QA

- Gerado em: 2026-04-26T23:42:32.901Z
- Perfil: rapido
- Branch: main
- Commit: 9dfc377bc
- Status geral: vermelho
- Indice de saude: 60

## Verificacoes

| Verificacao | Status | Duracao (s) | Sumario |
| --- | --- | ---: | --- |
| Backend unitario | sucesso | 1.65 | 1213/1213 testes aprovados no backend unitario. |
| Backend cobertura | sucesso | 0.41 | Cobertura backend: 99.98% de linhas e 97.54% de branches. |
| Frontend cobertura | falha | 49.79 | Cobertura frontend: 90.04% de linhas com 1281 testes aprovados. |
| Frontend lint | sucesso | 6.10 | Lint frontend sem problemas. |
| Frontend typecheck | falha | 4.31 | Typecheck frontend encontrou 3 erros. |

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
