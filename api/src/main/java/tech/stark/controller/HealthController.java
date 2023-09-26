package tech.stark.controller;

import io.micronaut.configuration.jdbc.hikari.HikariUrlDataSource;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;

@Controller("/healthz")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    private HikariUrlDataSource dataSource;

    @Inject
    public HealthController(HikariUrlDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Get(uri="/", produces="text/plain")
    public HttpResponse<String> index() {
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
            response.header(HttpHeaders.CACHE_CONTROL,"no-cache");
        }
        return response;
    }

}