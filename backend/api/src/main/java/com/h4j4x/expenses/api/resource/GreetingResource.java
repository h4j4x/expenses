package com.h4j4x.expenses.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {
    public static final String HELLO_MESSAGE = "Hello from Expenses API";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return HELLO_MESSAGE;
    }
}
