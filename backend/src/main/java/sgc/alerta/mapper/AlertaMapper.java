package sgc.alerta.mapper;

import sgc.comum.config.CentralMapperConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper para conversão entre Alerta (entidade) e AlertaDto.
 */
@Mapper(componentModel = "spring", config = CentralMapperConfig.class)
public abstract class AlertaMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigem")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestino")
    @Mapping(source = "descricao", target = "mensagem")
    @Mapping(source = "dataHora", target = "dataHoraFormatada", qualifiedByName = "formatDataHora")
    @Mapping(source = "processo.descricao", target = "processo")
    @Mapping(source = "unidadeOrigem.sigla", target = "origem")
    @Mapping(target = "dataHoraLeitura", ignore = true)
    public abstract AlertaDto toDto(Alerta alerta);

    /**
     * Converte Alerta para DTO incluindo a data/hora de leitura do usuário.
     */
    public AlertaDto toDto(Alerta alerta, LocalDateTime dataHoraLeitura) {
        AlertaDto dto = toDto(alerta);
        return AlertaDto.builder()
                .codigo(dto.getCodigo())
                .codProcesso(dto.getCodProcesso())
                .unidadeOrigem(dto.getUnidadeOrigem())
                .unidadeDestino(dto.getUnidadeDestino())
                .descricao(dto.getDescricao())
                .dataHora(dto.getDataHora())
                .dataHoraLeitura(dataHoraLeitura)
                .mensagem(dto.getMensagem())
                .dataHoraFormatada(dto.getDataHoraFormatada())
                .processo(dto.getProcesso())
                .origem(dto.getOrigem())
                .build();
    }

    @Named("formatDataHora")
    protected String formatDataHora(LocalDateTime dataHora) {
        return dataHora == null ? "" : dataHora.format(FORMATTER);
    }
}
