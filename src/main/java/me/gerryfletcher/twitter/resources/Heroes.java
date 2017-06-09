package me.gerryfletcher.twitter.resources;

import com.google.gson.*;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gerry on 05/06/2017.
 */
@Path("v1/heroes")
public class Heroes {

    private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private static List<Hero> heroes = new ArrayList<>();

    private static boolean isPopulated = false;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public String getHeroes() {
        if (!isPopulated) {
            populateList();
        }
        System.out.println("GET called.");
        System.out.println(this.heroes);
        return gson.toJson(this.heroes);
    }

    private void populateList() {
        this.heroes.add(new Hero("Istannen", "Coding Boi", 1));
        this.heroes.add(new Hero("Gerry", "Can eat 10 dogs", 2));
        this.heroes.add(new Hero("Bosco", "Will burn your house down", 3));

        this.isPopulated = true;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addHero(String json) {
        System.out.println("\n----POST called----");
        System.out.println(json);

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        String name = jsonObject.getAsJsonPrimitive("name").getAsString();
        String power = jsonObject.getAsJsonPrimitive("superpower").getAsString();
        int id = this.heroes.size() + 1;

        this.heroes.add(new Hero(name, power, id));

        return Response.status(201).build();
    }
}
