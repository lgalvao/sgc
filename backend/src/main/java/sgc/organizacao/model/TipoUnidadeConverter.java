package sgc.organizacao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoUnidadeConverter implements AttributeConverter<TipoUnidade, String> {

    @Override
    public String convertToDatabaseColumn(TipoUnidade attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().replace("_", " ");
    }

    @Override
    public TipoUnidade convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return TipoUnidade.valueOf(dbData.replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown TipoUnidade: " + dbData, e);
        }
    }
}
