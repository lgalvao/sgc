# Plano de Refatoração: Eliminar @Data Completamente

> **Objetivo:** Remover TODOS os usos de `@Data` do projeto.
> 
> **Regra:** `@Data` está **PROIBIDO** em todo o projeto, sem exceções.

---

## Resumo

| Categoria | Arquivos | Ação |
|-----------|----------|------|
| 🔄 Configurações → record | 3 | Converter para record |
| 🔄 Eventos → @Getter | 12 | Substituir @Data por @Getter |
| 🔄 DTOs → @Getter + @Builder | 18 | Substituir |
| 🔄 Requests → @Getter + @Builder | 18 | Substituir |
| 🔄 Responses → @Getter + @Builder | 2 | Substituir |
| **Total** | **53** | |

---

## Fase 1: Configurações Spring (Prioridade Alta) 🔴

Converter classes `@ConfigurationProperties` de `@Data` para `record`:

| Arquivo | Antes | Depois |
|---------|-------|--------|
| `seguranca/config/ConfigCors.java` | `@Data` class | record |
| `seguranca/config/JwtProperties.java` | `@Data` class | record |
| `seguranca/login/PropriedadesAcessoAd.java` | `@Data` class | record |

### Exemplo de conversão:

```java
// ANTES
@ConfigurationProperties(prefix = "aplicacao.jwt")
@Component
@Data
public class JwtProperties {
    private String secret;
    private int expiracaoMinutos = 120;
}

// DEPOIS
@ConfigurationProperties(prefix = "aplicacao.jwt")
public record JwtProperties(
    String secret,
    int expiracaoMinutos
) {
    public JwtProperties {
        expiracaoMinutos = expiracaoMinutos > 0 ? expiracaoMinutos : 120;
    }
}
```

**Nota:** Remover `@Component` ao usar record - o binding é feito via `@EnableConfigurationProperties` ou `@ConfigurationPropertiesScan`.

---

## Fase 2: Eventos (Prioridade Alta) 🔴

Eventos **nunca** devem ter setters. Substituir `@Data` por `@Getter`:

### Módulo Subprocesso:
| Arquivo | Mudança |
|---------|---------|
| `subprocesso/eventos/EventoSubprocessoAtualizado.java` | `@Data` → `@Getter` |
| `subprocesso/eventos/EventoSubprocessoCriado.java` | `@Data` → `@Getter` |
| `subprocesso/eventos/EventoSubprocessoExcluido.java` | `@Data` → `@Getter` |
| `subprocesso/eventos/EventoTransicaoSubprocesso.java` | `@Data` → `@Getter` |

### Módulo Processo:
| Arquivo | Mudança |
|---------|---------|
| `processo/eventos/EventoProcessoAtualizado.java` | `@Data` → `@Getter` |
| `processo/eventos/EventoProcessoExcluido.java` | `@Data` → `@Getter` |
| `processo/eventos/EventoProcessoFinalizado.java` | `@Data` → `@Getter` |
| `processo/eventos/EventoProcessoIniciado.java` | `@Data` → `@Getter`, remover `@NoArgsConstructor` |

### Módulo Mapa:
| Arquivo | Mudança |
|---------|---------|
| `mapa/evento/EventoAtividadeAtualizada.java` | `@Data` → `@Getter` |
| `mapa/evento/EventoAtividadeCriada.java` | `@Data` → `@Getter` |
| `mapa/evento/EventoAtividadeExcluida.java` | `@Data` → `@Getter` |
| `mapa/evento/EventoMapaAlterado.java` | `@Data` → `@Getter` |

---

## Fase 3: DTOs Internos (Prioridade Média) 🟡

Substituir `@Data` por `@Getter @Builder @AllArgsConstructor`:

### Módulo Organização:
| Arquivo |
|---------|
| `organizacao/dto/AdministradorDto.java` |
| `organizacao/dto/AtribuicaoTemporariaDto.java` |
| `organizacao/dto/PerfilDto.java` |
| `organizacao/dto/ResponsavelDto.java` |
| `organizacao/dto/UnidadeDto.java` |
| `organizacao/dto/UsuarioDto.java` |

### Módulo Processo:
| Arquivo |
|---------|
| `processo/dto/ProcessoDto.java` |

### Módulo Subprocesso:
| Arquivo |
|---------|
| `subprocesso/dto/AnaliseValidacaoDto.java` |
| `subprocesso/dto/AtividadeOperacaoResponse.java` |
| `subprocesso/dto/AtividadeVisualizacaoDto.java` |
| `subprocesso/dto/ConhecimentoVisualizacaoDto.java` |
| `subprocesso/dto/MensagemResponse.java` |
| `subprocesso/dto/SubprocessoDto.java` |
| `subprocesso/dto/SugestoesDto.java` |

### Módulo Segurança:
| Arquivo |
|---------|
| `seguranca/login/dto/EntrarResponse.java` |
| `seguranca/login/dto/PerfilUnidadeDto.java` |
| `seguranca/login/dto/UsuarioAcessoAd.java` (2 ocorrências) |

### Módulo Notificação:
| Arquivo |
|---------|
| `notificacao/dto/EmailDto.java` |

---

## Fase 4: Requests (Prioridade Média) 🟡

Substituir `@Data` por `@Getter @Builder @AllArgsConstructor`:

### Módulo Subprocesso:
| Arquivo |
|---------|
| `subprocesso/dto/AceitarCadastroRequest.java` |
| `subprocesso/dto/AlterarDataLimiteRequest.java` |
| `subprocesso/dto/ApresentarSugestoesRequest.java` |
| `subprocesso/dto/AtualizarSubprocessoRequest.java` |
| `subprocesso/dto/CompetenciaRequest.java` |
| `subprocesso/dto/CriarSubprocessoRequest.java` |
| `subprocesso/dto/DevolverCadastroRequest.java` |
| `subprocesso/dto/DevolverValidacaoRequest.java` |
| `subprocesso/dto/DisponibilizarMapaRequest.java` |
| `subprocesso/dto/HomologarCadastroRequest.java` |
| `subprocesso/dto/ImportarAtividadesRequest.java` |
| `subprocesso/dto/ProcessarEmBlocoRequest.java` |
| `subprocesso/dto/ReabrirProcessoRequest.java` |
| `subprocesso/dto/SalvarAjustesRequest.java` |
| `subprocesso/dto/SubmeterMapaAjustadoRequest.java` |

### Módulo Processo:
| Arquivo |
|---------|
| `processo/dto/AtualizarProcessoRequest.java` |
| `processo/dto/CriarProcessoRequest.java` |
| `processo/dto/EnviarLembreteRequest.java` |

---

## Padrão de Refatoração

### Para Classes com @Builder:

```java
// ANTES
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExemploDto {
    private String campo;
}

// DEPOIS
@Getter
@Builder
@AllArgsConstructor
public class ExemploDto {
    private final String campo;
}
```

### Para Eventos:

```java
// ANTES
@Data
@Builder
public class EventoExemplo {
    private Long codigo;
}

// DEPOIS
@Getter
@Builder
public class EventoExemplo {
    private final Long codigo;
}
```

### Para Configurações:

```java
// ANTES
@ConfigurationProperties(prefix = "app.config")
@Data
public class ConfigExemplo {
    private String valor = "default";
}

// DEPOIS
@ConfigurationProperties(prefix = "app.config")
public record ConfigExemplo(String valor) {
    public ConfigExemplo {
        valor = valor != null ? valor : "default";
    }
}
```

---

## Checklist de Execução

- [ ] Fase 1: Refatorar configurações (3 arquivos)
- [ ] Fase 2: Refatorar eventos (12 arquivos)
- [ ] Fase 3: Refatorar DTOs (18 arquivos)
- [ ] Fase 4: Refatorar Requests (18 arquivos)
- [ ] Executar testes: `./gradlew :backend:test`
- [ ] Verificar compilação: `./gradlew :backend:compileJava`

---

## Riscos e Mitigações

| Risco | Mitigação |
|-------|-----------|
| **MapStruct espera setters** | Usar `@AllArgsConstructor` - MapStruct pode usar construtor |
| **Jackson desserialização** | `@AllArgsConstructor` + `@Builder` funciona com Jackson |
| **Testes usam setters** | Atualizar para usar builders |
| **ConfigurationProperties binding** | Records funcionam nativamente desde Spring Boot 2.2+ |

---

## Verificação Final

Após a refatoração, executar:

```bash
# Buscar @Data residuais (deve retornar 0 resultados)
grep -r "@Data" backend/src/main/java --include="*.java"

# Compilar
./gradlew :backend:compileJava

# Testes
./gradlew :backend:test
```
