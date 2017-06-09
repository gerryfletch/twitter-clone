package me.gerryfletcher.twitter.config;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.*;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.glassfish.jersey.internal.util.Base64;

/**
 * This filter verify the access permissions for a User
 * based on username and passowrd provided in request
 */
@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    @Override
    public void filter(ContainerRequestContext requestContext) {

        Response ACCESS_UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED)
                .entity("You cannot access this resource. Code #1").build();

        Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
                .entity("You cannot access this resource. Code #2").build();

        Method method = resourceInfo.getResourceMethod();

        if(method.isAnnotationPresent(PermitAll.class)) {
            // permit all, so no filtering necessary
            return;
        }

        if(method.isAnnotationPresent(DenyAll.class)) {
            // deny all, abort with forbidden
            requestContext.abortWith(ACCESS_FORBIDDEN);
            return;
        }

        //Get request headers
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            requestContext.abortWith(ACCESS_UNAUTHORIZED);
            return;
        }

        final String[] authorizationHeader = authorization.get(0).split(" ");
        final String authType = authorizationHeader[0].toLowerCase();

        /*
            These methods check the User role
         */
        boolean authorized = false;

        switch (authType) {
            // User is attempting to Login
            case ("basic"):
                System.out.println("Basic http auth");
                authorized = checkBasicAuth(authorizationHeader, method);
                break;
            case ("bearer"):
                System.out.println("Bearer http auth");
                authorized = checkBearerAuth(authorizationHeader, method);
                break;
        }

        if(! authorized) {
            requestContext.abortWith(ACCESS_UNAUTHORIZED);
            return;
        }
    }

    private boolean checkBearerAuth(String[] authorizationHeader, Method method) {
        String token = authorizationHeader[1];
        System.out.println("TOKEN: " + token);

        try {
            Algorithm algorithm = Algorithm.HMAC256(JWTSecret.getKey());
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("auth0")
                .build(); //Reusable verifier instance
            System.out.println("\n-\n");
            DecodedJWT jwt = verifier.verify(token);

            System.out.println("Valid token. Checking roles...");
            // Verify role access
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

                if (! rolesSet.contains("temp")) {
                    return false;
                }
            }

        } catch (UnsupportedEncodingException exception) {
            System.out.println("UTF-8 encoding not supported.");
            return false;
        } catch (JWTVerificationException exception) {
            System.out.println("Invalid signature/claims");
            return false;
        }
        return true;
    }

    private boolean checkBasicAuth(String[] authorizationHeader, Method method) {
        String userRole = "User";

        String token = decrypt64(authorizationHeader[1]);

        String[] usernameAndPassword = token.split(":");

        if(usernameAndPassword.length < 2) {
            return false;
        }

        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        // TODO
        /*
            1) Do database lookup
            2) Check User role
            3) Encrypt incoming password
            4) Check DB against username + password
            5) Allow/Deny
         */

        // Verify role access
        if (method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<String>(Arrays.asList(rolesAnnotation.value()));

            if (! rolesSet.contains(userRole)) {
                return false;
            }
        }

        if (!(username.equals("admin") && password.equals("password"))) {
            return false;
        }

        return true;
    }

    private String decrypt64(String s) {
        return new String(Base64.decode(s.getBytes()));
    }
}