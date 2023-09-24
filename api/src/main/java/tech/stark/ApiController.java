package tech.stark;

import io.micronaut.http.annotation.*;

@Controller("/api")
public class ApiController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}