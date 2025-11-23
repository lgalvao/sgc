package sgc.alerta.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import sgc.alerta.model.Alerta;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring")
public abstract class AlertaMapper {

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Mapping(source = "processo.codigo", target = "codProcesso")
    @Mapping(source = "unidadeOrigem.sigla", target = "unidadeOrigem")
    @Mapping(source = "unidadeDestino.sigla", target = "unidadeDestino")

    // TODO 'alerta' is no being recognized
    @Mapping(source = "alerta", target = "linkDestino", qualifiedByName = "buildLinkDestino")
    @Mapping(source = "descricao", target = "mensagem")
    @Mapping(source = "dataHora", target = "dataHoraFormatada", qualifiedByName = "formatDataHora")
    @Mapping(source = "descricao", target = "processo", qualifiedByName = "extractProcessoName")
    @Mapping(source = "unidadeOrigem.sigla", target = "origem")
    @Mapping(target = "dataHoraLeitura", ignore = true)
    public abstract AlertaDto toDto(Alerta alerta);

    @Named("buildLinkDestino")
    protected String buildLinkDestino(Alerta alerta) {
        if (alerta.getProcesso() == null || alerta.getUnidadeDestino() == null) {
            return null;
        }

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long codUnidade = usuario.getUnidade().getCodigo();

        Optional<Subprocesso> subprocessoOpt = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(
            alerta.getProcesso().getCodigo(),
            codUnidade
        );

        return subprocessoOpt
            .map(sp -> String.format("/subprocessos/%d", sp.getCodigo()))
            .orElse(null);
    }

    @Named("formatDataHora")
    protected String formatDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "";
        }
        return dataHora.format(formatter);
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
