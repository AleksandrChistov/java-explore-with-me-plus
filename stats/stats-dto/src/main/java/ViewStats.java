import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {
    // для ответа на GET-эндпоинт
    private String app;
    private String uri;
    private Long hits;
}
