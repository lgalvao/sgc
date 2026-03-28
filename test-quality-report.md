# Pendências remanescentes — Test Quality Check (backend/src/test/)

## Pendências priorizadas

### Alta prioridade
1. **`SubprocessoControllerCoverageTest#validarCadastro`**: adicionar cenário inválido com inconsistências reais de cadastro (não apenas caso válido).  
2. **`PainelSecurityReproductionTest`**: complementar com fixture própria para remover dependência residual de alerta fixo (`70002`) no cenário de acesso indevido.  

### Média prioridade
3. **`SubprocessoValidacaoServiceTest#erroSituacaoNula`**: evitar cenário de entidade persistida com `situacao = null`; cobrir nulo via DTO/entrada de API.  
4. **`SubprocessoControllerCoverageTest` x `SubprocessoControllerCoverageExtraTest`**: consolidar testes por endpoint para reduzir sobreposição e melhorar coesão da suíte.  
5. **`ProcessoServiceTest` + `ProcessoServiceCoverageTest` + `ProcessoServiceExtraCoverageTest`**: reorganizar por comportamento de negócio (criação/atualização/transição/validação).  
6. **Rastreabilidade**: incluir referência explícita de RN/CDU por método nos testes de manutenção de mapa e validações de subprocesso (além do `@DisplayName` da classe).  

### Baixa prioridade
7. Revisar testes de baixo valor restante (ex.: asserções de formato e utilitários triviais) para reduzir padding de cobertura.  
8. Padronizar todos os `@DisplayName` legados em formato comportamental consistente (Dado/Quando/Então).  

---

## Checklist de verificação para próximos ciclos

- [ ] Todo teste novo evita dependência de seed fixa (`data.sql`) quando o objetivo é regra de negócio/autorização.  
- [ ] Cada endpoint crítico possui cenário de sucesso **e** cenário de falha de regra de negócio.  
- [ ] Suítes de controller verificam payload relevante e interação com serviço (não apenas status HTTP).  
- [ ] Toda suíte de integração possui rastreabilidade explícita para CDU/RN em `etc/reqs/`.  
- [ ] Arquivos de teste mantêm descrições em português e focadas em comportamento observável.  
- [ ] Evitar testes de getters/setters/toString sem requisito funcional explícito.  
- [ ] Rodar `./gradlew :backend:test` (ou recorte equivalente) após mudanças e atualizar este arquivo com apenas pendências ativas.  
