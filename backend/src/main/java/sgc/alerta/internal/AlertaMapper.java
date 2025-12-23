package sgc.alerta.internal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.alerta.api.AlertaDto;
import sgc.alerta.internal.model.Alerta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring")
public abstract class AlertaMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigem")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestino")
    @Mapping(source = "descricao", target = "mensagem")
    @Mapping(source = "dataHora", target = "dataHoraFormatada", qualifiedByName = "formatDataHora")
    @Mapping(source = "descricao", target = "processo", qualifiedByName = "extractProcessoName")
    @Mapping(source = "unidadeOrigem.sigla", target = "origem")
    @Mapping(target = "dataHoraLeitura", ignore = true)
    public abstract AlertaDto toDto(Alerta alerta);

    @Named("formatDataHora")
    protected String formatDataHora(LocalDateTime dataHora) {
        return dataHora == null ? "" : dataHora.format(FORMATTER);
    }

    @Named("extractProcessoName")
    protected String extractProcessoName(String descricao) {
        if (descricao == null) return "";

        Pattern pattern = Pattern.compile(".*processo '(.*?)'.*");
        Matcher matcher = pattern.matcher(descricao);
        return matcher.find() ? matcher.group(1) : "";
    }
}
