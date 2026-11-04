package ec.edu.monster.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Reads src/test/resources/config.properties, which wins over the main one in tests. */
class WsConfigTest {

  @Test
  void buildsBaseUrlFromProperties() {
    assertEquals("http://localhost:18090/eurekabank-test", WsConfig.getBaseUrl());
  }

  @Test
  void buildsEndpointOfOneRoute() {
    assertEquals("http://localhost:18090/eurekabank-test/api/Login/loginEmpleado",
        WsConfig.getEndpoint("api/Login/loginEmpleado"));
  }
}
