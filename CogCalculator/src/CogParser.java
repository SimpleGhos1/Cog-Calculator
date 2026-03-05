import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.List;

public class CogParser {

    public static List<CogInfo> loadCogs(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            List<CogInfo> cogs = gson.fromJson(reader, new TypeToken<List<CogInfo>>() {}.getType());
            return cogs;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // return empty list if error
        }
    }
}