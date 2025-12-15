package sgc.alerta.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.alerta.model.Alerta;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring")
public abstract class AlertaMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    @Autowired
    private SubprocessoRepo subprocessoRepo;

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
        if (dataHora == null) {
            return "";
        }
        return dataHora.format(FORMATTER);
    }

    @Named("extractProcessoName")
    protected String extractProcessoName(String descricao) {
        if (descricao == null) {
            return "";
        }
        Pattern pattern = Pattern.compile(".*processo '(.*?)'.*");
        Matcher matcher = pattern.matcher(descricao);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
