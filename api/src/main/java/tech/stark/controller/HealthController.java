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

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Controller("/healthz")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);
    @Inject
    public HikariUrlDataSource dataSource;

    @Get(uri="/", produces="text/plain")
    public HttpResponse<String> index() {
        MutableHttpResponse<String> response = HttpResponse.ok();
        try {

            PreparedStatement statement = dataSource.getConnection().prepareStatement(dataSource.getConnectionTestQuery());
            statement.executeQuery().close();
            LOGGER.info("Connection test successful!");

        } catch (SQLException e){
            response = HttpResponse.status(HttpStatus.SERVICE_UNAVAILABLE);
            LOGGER.error("Connection test unsuccessful!\n" +e.getMessage()+"\n"+e.getStackTrace());
        } finally {
            response.header(HttpHeaders.CACHE_CONTROL,"no-cache");
        }
        return response;
    }

}