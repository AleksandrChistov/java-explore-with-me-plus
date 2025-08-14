import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsRequest {
    // Для получения GET-эндпоинта
    @NotNull
    @PastOrPresent
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    private List<String> uris;

    private Boolean unique;
}
