package me.gerryfletcher.twitter.config;

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

import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import org.glassfish.jersey.internal.util.Base64;

/**
 * This filter will allow or deny access to a resource depending on
 * resource annotation.
 *
 * If a resource requires a user type, authentication is required.
 * Currently JWT authentication is fully implemented. If the token
 * is valid, the role is extracted and checked against the resource.
 */
@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    private Method method;

    /**
     * The filter checks all incoming HTTP requests.
     * If the resource has a PermitAll or DenyAll annotation, the request will be
     * passed or rejected with no further checks.
     *
     * If there is no authorization present at all, it is rejected unauthorized.
     *
     * The filter then checks for basic and bearer authentication.
     * Basic auth is no longer used, but the helper methods are still available for
     * future expansion.
     *
     * If the user does not have the correct permissions, they are aborted unauthorized.
     *
     * @param requestContext    The HTTP request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {

        Response ACCESS_UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED)
                .entity("You cannot access this resource. Code #1").build();

        Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
                .entity("You cannot access this resource. Code #2").build();

        this.method = resourceInfo.getResourceMethod();

        if(this.method.isAnnotationPresent(PermitAll.class)) {
            return;
        }

        if(this.method.isAnnotationPresent(DenyAll.class)) {
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
        final String authType = authorizationHeader[0].toLowerCase(); // "Bearer" or "Basic"
        final String token = authorizationHeader[1]; // Token - currently only used for Bearer.
        boolean authorized = false;

        switch (authType) {
            case ("basic"):
                System.out.println("Basic http auth");
                authorized = checkBasicAuth(authorizationHeader);
                break;
            case ("bearer"):
                System.out.println("Bearer http auth");
                authorized = checkBearerAuth(token);
                break;
        }

        if(! authorized) {
            requestContext.abortWith(ACCESS_UNAUTHORIZED);
        }
    }

    /**
     * Checks a Json Web Token for validity.
     * The role claim is extracted from the JWT, and checked against the role set for
     * the resource.
     * @param token The Json Web Token supplied.
     * @return  True or False if the user is verified.
     */
    private boolean checkBearerAuth(String token) {
        JWTSecret jwt = new JWTSecret();
        System.out.println("TOKEN: " + token);

        if(! jwt.validateToken(token)) {
            return false;
        }

        // Verify role access
        if (this.method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = this.method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
            String role = jwt.getClaim(token,"role");

            if (! rolesSet.contains(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <b>This method is no longer used as bearer auth is used instead.</b>
     * Checks the username and password against the DB.
     * DB functions have been removed, and checks if username is 'admin' and
     * password is 'password'.
     * @param authorizationHeader   The authorization header to be split.
     * @return  True or False if the user is verified.
     */
    private boolean checkBasicAuth(String[] authorizationHeader) {
        String userRole = "UserResource";

        String token = decode64(authorizationHeader[1]);

        String[] usernameAndPassword = token.split(":");

        if(usernameAndPassword.length < 2) {
            return false;
        }

        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        // Verify role access
        if (this.method.isAnnotationPresent(RolesAllowed.class)) {
            RolesAllowed rolesAnnotation = this.method.getAnnotation(RolesAllowed.class);
            Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

            if (! rolesSet.contains(userRole)) {
                return false;
            }
        }

        return username.equals("admin") && password.equals("password");
    }

    /**
     * Helper function to decode base 64 strings.
     * @param str   The base 64 encoded string
     * @return      The decoded string.
     */
    private String decode64(String str) {
        return new String(Base64.decode(str.getBytes()));
    }
}