package com.h4j4x.expenses.api.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTests {
    @Test
    public void whenGetHello_Then_ShouldGetHelloMessage() {
        given()
            .when().get("hello")
            .then()
            .statusCode(200)
            .body(is(GreetingResource.HELLO_MESSAGE));
    }
}
