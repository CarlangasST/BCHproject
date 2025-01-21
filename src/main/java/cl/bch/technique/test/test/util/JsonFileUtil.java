package cl.bch.technique.test.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Component;
import cl.bch.technique.test.test.model.User;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonFileUtil {
    private final ObjectMapper objectMapper;
    private final String FILE_PATH = "src/main/resources/users.json";

    public JsonFileUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<User> readUsers() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(ArrayList.class, User.class);
        return objectMapper.readValue(file, listType);
    }

    public void writeUsers(List<User> users) throws IOException {
        objectMapper.writeValue(new File(FILE_PATH), users);
    }
}
