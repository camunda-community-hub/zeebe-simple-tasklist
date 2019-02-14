package io.zeebe.tasklist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.tasklist.view.FormField;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TaskDataSerializer {

  public static final TypeReference<List<FormField>> FORM_FIELDS_TYPE =
      new TypeReference<List<FormField>>() {};

  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<FormField> readFormFields(String json) {
    try {
      return objectMapper.readValue(json, FORM_FIELDS_TYPE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> readVariables(String json) {
    try {
      return objectMapper.readValue(json, Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
