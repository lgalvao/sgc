# View Validations Audit

Validations found on fields guaranteed by Database Views:

### PainelFacade.java
`backend/src/main/java/sgc/painel/PainelFacade.java`

| Line | Field | Source View | Code |
|------|-------|-------------|------|
| 173 | `getSigla()` | VW_UNIDADE | `.filter(unidade -> unidade != null && unidade.getSigla() != null)` |
