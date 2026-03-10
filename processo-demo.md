# Criando um Processo de Mapeamento

*2026-03-10T16:55:47Z*

```pwsh
$COOKIE = curl.exe -i -s -X POST 'http://localhost:10000/api/usuarios/autenticar' -H 'Content-Type: application/json' -d '{"tituloEleitoral":"111111","senha":"senha"}' | Select-String -Pattern 'SGC_PRE_AUTH=([^;]+)' | %{$_.Matches.Groups[1].Value}; curl.exe -s -X POST 'http://localhost:10000/api/usuarios/autorizar' -H 'Content-Type: application/json' -H "Cookie: SGC_PRE_AUTH=$COOKIE" -d '{"tituloEleitoral":"111111"}' | Out-Null; $TOKEN = curl.exe -s -X POST 'http://localhost:10000/api/usuarios/entrar' -H 'Content-Type: application/json' -H "Cookie: SGC_PRE_AUTH=$COOKIE" -d '{"tituloEleitoral":"111111","perfil":"ADMIN","unidadeCodigo":1}' | ConvertFrom-Json | Select-Object -ExpandProperty token; curl.exe -s -X POST 'http://localhost:10000/api/processos' -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"descricao":"Processo Mapeamento Curl","tipo":"MAPEAMENTO","dataLimiteEtapa1":"2027-12-31T23:59:59","unidades":[2]}' | ConvertFrom-Json | Select-Object @{Name="codigo";Expression={"[NUM_ID]"}}, descricao, tipo, situacao | ConvertTo-Json -Compress
```

```output
{"codigo":221,"descricao":"Processo Mapeamento Curl","tipo":"MAPEAMENTO","situacao":"CRIADO"}
```




