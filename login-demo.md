# 2-Step Login no SGC

*2026-03-10T16:21:13Z*

Para iniciar o processo de login em duas etapas no SGC, primeiro fazemos uma requisição POST na rota de /autenticar com o título de eleitor e a senha. A resposta será um valor booleano indicando que a primeira etapa passou e um cookie `SGC_PRE_AUTH` é retornado na resposta.

```pwsh
curl.exe -s -k -c ./cookies.txt -X POST http://localhost:10000/api/usuarios/autenticar -H 'Content-Type: application/json' -d '{"tituloEleitoral":"202020","senha":"senha"}'
```

```output
true
```

Com o cookie `SGC_PRE_AUTH` configurado e armazenado, na segunda etapa, enviamos o título para descobrir quais os perfis os quais o usuário pode escolher para logar (/autorizar)

```pwsh
curl.exe -s -k -b ./cookies.txt -X POST http://localhost:10000/api/usuarios/autorizar -H 'Content-Type: application/json' -d '{"tituloEleitoral":"202020"}'
```

```output
[{"perfil":"CHEFE","unidade":{"codigo":2,"nome":"Secretaria 1","sigla":"SECRETARIA_1","codigoPai":1,"tipo":"INTEROPERACIONAL","subunidades":[],"tituloTitular":"202020","isElegivel":false,"responsavel":{"accountNonExpired":true,"accountNonLocked":true,"credentialsNonExpired":true,"email":"john.lennon@tre-pe.jus.br","enabled":true,"matricula":"00202020","nome":"John Lennon","password":null,"ramal":"2020","tituloEleitoral":"202020","unidadeCodigo":2,"username":"202020"},"elegivel":false}},{"perfil":"GESTOR","unidade":{"codigo":2,"nome":"Secretaria 1","sigla":"SECRETARIA_1","codigoPai":1,"tipo":"INTEROPERACIONAL","subunidades":[],"tituloTitular":"202020","isElegivel":false,"responsavel":{"accountNonExpired":true,"accountNonLocked":true,"credentialsNonExpired":true,"email":"john.lennon@tre-pe.jus.br","enabled":true,"matricula":"00202020","nome":"John Lennon","password":null,"ramal":"2020","tituloEleitoral":"202020","unidadeCodigo":2,"username":"202020"},"elegivel":false}}]
```

Por fim, o usuário deve enviar novamente os dados contendo o perfil selecionado e também o código da unidade. A rota /entrar finaliza a negociação e retorna um objeto contendo o Token JWT na propriedade `token` para ser usado no header `Authorization`.
