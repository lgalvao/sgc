package sgc.comum.config;

import org.mapstruct.*;

/**
 * Configuração central para mappers MapStruct.
 * 
 * <p>Define comportamentos padrão rigorosos para garantir compatibilidade com @NullMarked:
 * <ul>
 *   <li>Retorna um valor "vazio" (instância padrão) se o input for nulo, em vez de retornar null.</li>
 *   <li>Ignora propriedades nulas durante atualizações para preservação de dados existentes.</li>
 *   <li>Exige mapeamento explícito de todos os campos (ReportingPolicy.ERROR).</li>
 * </ul>
 * </p>
 */
@MapperConfig(
    componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CentralMapperConfig {
}
