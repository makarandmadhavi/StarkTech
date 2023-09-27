package tech.stark.controller;

import io.micronaut.configuration.jdbc.hikari.HikariUrlDataSource;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Controller for the health endpoint
 * @since 1.0
 */
@Controller("/healthz")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    private HikariUrlDataSource dataSource;


    @Inject
    public HealthController(HikariUrlDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Endpoint to check the health of the application
     * @return 200 if the application is healthy, 503 if the database is down
     */
    @Get(uri="/", produces="text/plain")
    public HttpResponse<String> index(HttpRequest<?> request) {
        MutableHttpResponse<String> response = HttpResponse.ok();
        try {

            dataSource.getConnection()
                    .prepareStatement(dataSource.getConnectionTestQuery())
                    .executeQuery();

            LOGGER.info("Connection test successful!");

        } catch (SQLException e){
            response = HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE);
            LOGGER.error("Connection test unsuccessful! " + e.getMessage());
            LOGGER.error(Arrays.toString(e.getStackTrace()).replace(", ", "\n"));
        } finally {
            response.header(HttpHeaders.CACHE_CONTROL,"no-cache, no-store, must-revalidate");
        }
        return response;
    }


    @Error(status = HttpStatus.METHOD_NOT_ALLOWED, global = true)
    public HttpResponse<?> notFound(HttpRequest<?> request) {
        return HttpResponse.notAllowed(HttpMethod.GET)
                .header(HttpHeaders.CACHE_CONTROL,"no-cache, no-store, must-revalidate");
    }

}