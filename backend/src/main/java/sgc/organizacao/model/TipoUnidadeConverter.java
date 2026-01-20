package sgc.organizacao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class TipoUnidadeConverter implements AttributeConverter<TipoUnidade, String> {

    @Override
    public String convertToDatabaseColumn(TipoUnidade attribute) {
        if (attribute == null) {
            return null;
        }
        if (attribute == TipoUnidade.SEM_EQUIPE) {
            return "SEM EQUIPE";
        }
        return attribute.name();
    }

    @Override
    public TipoUnidade convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        if ("SEM EQUIPE".equals(dbData)) {
            return TipoUnidade.SEM_EQUIPE;
        }

        return Stream.of(TipoUnidade.values())
                .filter(c -> c.name().equals(dbData))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
