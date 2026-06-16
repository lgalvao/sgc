# Como logar no SGC (Perfil HOM - STIC)

Este documento descreve as credenciais e o processo de login para o ambiente de homologação, com foco em usuários da
STIC.

## Comando para trocar de perfil

#### Perfil de desenvolvimento/teste:

```bash
 $env:SGC_PERFIL='e2e'; node e2e/lifecycle.js
```

#### Perfil de homologação (hom):

```bash
$env:SGC_PERFIL='hom'; node e2e/lifecycle.js
```

Após rodar um dos dois comandos acima, se não quiser trocar de perfil, pode usar:

```bash
node e2e/lifecycle.js
```

## 1) Credenciais de acesso

- Login: usar o campo `TITULO` do usuário.
- Senha padrão para todos os usuários de teste: `12345678`.

## 2) Usuários sugeridos para testes

### Perfil ADMIN

| TITULO       | NOME                                           | UNIDADE |
|--------------|------------------------------------------------|---------|
| 025545511252 | DANIEL LIMA BARBOSA                            | ADMIN   |
| 039703250884 | LEONARDO FERREIRA DA SILVA DE ARROXELAS GALVÃO | ADMIN   |

### Perfil SERVIDOR

| TITULO       | NOME                             | UNIDADE  |
|--------------|----------------------------------|----------|
| 002830130825 | ALECTOR DE ANDRADE PEREIRA       | SEMIC    |
| 006806770850 | BETTINA SOUTO MAIOR FONTES PINTO | SEAUTIC  |
| 072233370809 | BRUNO FONSECA LINS DE OLIVEIRA   | SEDESENV |
| 083582140825 | DAVYSON COSTA                    | SEDIA    |

### Perfil CHEFE

| TITULO       | NOME                              | UNIDADE   |
|--------------|-----------------------------------|-----------|
| 045098980809 | MLEXENER BEZERRA ROMEIRO          | COSIS     |
| 049391480884 | ANDRÉ RICARDO NEVES DE MORAES     | SEMIC     |
| 048217280809 | FLÁVIO ROBERTO GOMES DA COSTA     | SEAUTIC   |
| 040706770809 | GLAUCIA MARIA DOS SANTOS FERREIRA | SEPLANTIC |

### Perfil GESTOR

| TITULO       | NOME                           | UNIDADE   |
|--------------|--------------------------------|-----------|
| 050290130841 | GEORGE CAVALCANTI MACIEL FILHO | STIC      |
| 048634960841 | VALÉRIA FARIAS DE MIRANDA      | COSERVTIC |
| 048482410825 | JOSÉ FERREIRA DE LIMA JÚNIOR   | COSINF    |

## 3) Árvore de usuários e unidades

```text
▾ ☐ ADMIN
  ▾ ☐ ADMIN
      ☐ 025545511252 - DANIEL LIMA BARBOSA
      ☐ 039703250884 - LEONARDO FERREIRA DA SILVA DE ARROXELAS GALVÃO

▾ ☐ SERVIDOR 
  ▾ ☐ SEMIC
      ☐ 002830130825 - ALECTOR DE ANDRADE PEREIRA
  ▾ ☐ SEAUTIC
      ☐ 006806770850 - BETTINA SOUTO MAIOR FONTES PINTO
  ▾ ☐ SEDESENV
      ☐ 072233370809 - BRUNO FONSECA LINS DE OLIVEIRA
  ▾ ☐ SEDIA
      ☐ 083582140825 - DAVYSON COSTA

▾ ☐ CHEFE
  ▾ ☐ COSIS
      ☐ 045098980809 - MLEXENER BEZERRA ROMEIRO
  ▾ ☐ SEMIC
      ☐ 049391480884 - ANDRÉ RICARDO NEVES DE MORAES
  ▾ ☐ SEAUTIC
      ☐ 048217280809 - FLÁVIO ROBERTO GOMES DA COSTA
  ▾ ☐ SEPLANTIC
      ☐ 040706770809 - GLAUCIA MARIA DOS SANTOS FERREIRA

▾ ☐ GESTOR
  ▾ ☐ STIC
      ☐ 050290130841 - GEORGE CAVALCANTI MACIEL FILHO
  ▾ ☐ COSERVTIC
      ☐ 048634960841 - VALÉRIA FARIAS DE MIRANDA
  ▾ ☐ COSINF
      ☐ 048482410825 - JOSÉ FERREIRA DE LIMA JÚNIOR
```
