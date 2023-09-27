package tech.stark.health;

import io.micronaut.configuration.jdbc.hikari.HikariUrlDataSource;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import tech.stark.controller.HealthController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HealthControllerTest {

    @Inject
    @Client("/")
    private HttpClient httpClient;

    @Mock
    private HikariUrlDataSource dataSource;

    private HealthController healthController;

    /**
     * Setup the mock objects
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        healthController = new HealthController(dataSource);
    }

    /**
     * Test that the health endpoint returns a 200 when the database is up
     * @throws SQLException
     */
    @Test
    public void testValidConnectionToDatabaseReturns200() throws SQLException {
        Connection connection = Mockito.spy(Connection.class);
        PreparedStatement preparedStatement = Mockito.spy(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.any())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);

        io.micronaut.http.HttpResponse<String> response = healthController.index(HttpRequest.GET("/healthz"));
        Assertions.assertEquals(HttpStatus.OK, response.status(), "Response code should be 200");
        Assertions.assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().get(HttpHeaders.CACHE_CONTROL));
    }

    /**
     * Test that the health endpoint returns a 503 when the database is down
     * @throws SQLException
     */
    @Test
    public void testHealthEndpointWhenDatabaseIsDown() throws SQLException {
        Mockito.when(dataSource.getConnection()).thenThrow(SQLException.class);

        io.micronaut.http.HttpResponse<String> response = healthController.index(HttpRequest.GET("/healthz"));
        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.status(), "Response code should be 503");
        Assertions.assertEquals("no-cache, no-store, must-revalidate", response.getHeaders().get(HttpHeaders.CACHE_CONTROL));
    }
}
