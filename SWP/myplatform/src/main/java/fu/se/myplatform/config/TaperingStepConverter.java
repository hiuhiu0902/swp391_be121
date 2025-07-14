package fu.se.myplatform.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fu.se.myplatform.dto.TaperingStep;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class TaperingStepConverter implements AttributeConverter<List<TaperingStep>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<TaperingStep> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting tapering steps to JSON", e);
        }
    }

    @Override
    public List<TaperingStep> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(dbData, new TypeReference<List<TaperingStep>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to tapering steps", e);
        }
    }
}
