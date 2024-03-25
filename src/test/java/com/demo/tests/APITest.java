package com.demo.tests;

import com.demo.model.User;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* Mockoon mock server: http://localhost:3001*/
public class APITest {
    @Test
    public void ageIs22() {
        RestAssured.get("http://localhost:3001/users").then().body("user.age", Matchers.equalTo(22));
    }

    @Test
    public void hasValueNodejsAndSpringboot() {
        RestAssured.get("http://localhost:3001/users").then()
                .body("user.skills.backend", Matchers.hasItems("nodejs", "springboot"));
    }

    @Test
    public void jsonSchemaTest() {
        RestAssured.get("http://localhost:3001/users").then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("user-schema.json"));
    }

    @Test
    public void anonymousJsonRootValidation() {
        RestAssured.get("http://localhost:3001/").then()
                .body("$", Matchers.hasItems(1, 2, 3));
    }

    @Test
    public void checkSalaryLessThan100() {
        RestAssured.get("http://localhost:3001/users/search").then()
                .body("users.user.findAll { it.salary < 100 }.name", Matchers.hasItems("Nigel Rees"));

        RestAssured.get("http://localhost:3001/users/search").then()
                .body("users.user.collect { it.salary }.sum()", Matchers.equalTo(210));
    }

    @Test
    public void deserializationWithGenerics() {
        Map<String, Map<String, List<User>>> users = RestAssured.get("http://localhost:3001/users/search")
                .as(new TypeRef<Map<String, Map<String, List<User>>>>() {
        });
        Assert.assertEquals(users.get("users").get("user").size(), 2);
        Assert.assertEquals(users.get("users").get("user").get(0).getName(), "Nigel Rees");
        Assert.assertEquals(users.get("users").get("user").get(0).getSalary(), 90);
        Assert.assertEquals(users.get("users").get("user").get(0).getRole(), "QA");
        Assert.assertEquals(users.get("users").get("user").get(1).getName(), "Sheldon");
        Assert.assertEquals(users.get("users").get("user").get(1).getSalary(), 120);
        Assert.assertEquals(users.get("users").get("user").get(1).getRole(), "SDE");
    }

    @Test
    public void responseTimeIsLessThan2Seconds() {
        RestAssured.get("http://localhost:3001/users/search").then()
                .time(Matchers.lessThan(2L), TimeUnit.SECONDS);
    }

    @Test
    public void authenticateUser(){
        RestAssured.given().auth().preemptive().basic("saloni", "123456")
                .when().get("http://localhost:3001/secured/users").then().statusCode(200);
    }


}

