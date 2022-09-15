package io.zeebe.tasklist.view;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController extends AbstractViewController {

  @GetMapping("/login")
  public String login(Map<String, Object> model) {

    addDefaultAttributesToModel(model);
    return "login";
  }

  @GetMapping("/login-error")
  public String loginError(Map<String, Object> model) {

    model.put("error", "Username or password is invalid.");
    addDefaultAttributesToModel(model);
    return "login";
  }
}
