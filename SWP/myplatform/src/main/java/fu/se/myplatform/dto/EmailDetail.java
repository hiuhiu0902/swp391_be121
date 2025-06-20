package fu.se.myplatform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
public class EmailDetail {
    private String recipient;
    private String subject;
    private String link;
}
