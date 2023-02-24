package io.zeebe.tasklist.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.zeebe.tasklist.HazelcastService;
import io.zeebe.tasklist.repository.HazelcastConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "server.allowedOriginsUrls: http://www.someurl.com",
      "logging.level.io.zeebe.tasklist: info",
    })
@AutoConfigureMockMvc
public class CorsSettingsControllerTest {

  @LocalServerPort protected int port;
  @Autowired protected MockMvc mockMvc;

  @MockBean protected HazelcastConfigRepository hazelcastConfigRepository;
  @MockBean protected HazelcastService zeebeHazelcastService;

  @Test
  @WithMockUser(username = "demo", password = "demo")
  public void access_control_origin_request_header_is_checked() throws Exception {
    mockMvc
        .perform(
            options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Host", "localhost")
                .header("Origin", "http://a.bad-person.internet")
        )
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "demo", password = "demo")
  public void access_control_allow_origin_response_header_is_send() throws Exception {
    mockMvc
        .perform(
            options("/")
                .header("Access-Control-Request-Method", "GET")
                .header("Host", "localhost")
                .header("Origin", "http://www.someurl.com"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://www.someurl.com"));
  }
}
