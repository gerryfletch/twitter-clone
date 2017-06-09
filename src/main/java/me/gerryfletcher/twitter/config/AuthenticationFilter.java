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

import org.glassfish.jersey.internal.util.Base64;

/**
 * This filter verify the access permissions for a user
 * based on username and passowrd provided in request
 */
@Provider
public class AuthenticationFilter implements javax.ws.rs.container.ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";

    private static List<String> JWTList = new ArrayList<>();

    @Override
    public void filter(ContainerRequestContext requestContext) {

        Response ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED)
                .entity("You cannot access this resource. Code #1").build();

        Response ACCESS_FORBIDDEN = Response.status(Response.Status.FORBIDDEN)
                .entity("You cannot access this resource. Code #2").build();

        Method method = resourceInfo.getResourceMethod();

        //Access allowed for all
        if (!method.isAnnotationPresent(PermitAll.class)) {
            //Access denied for all
            if (method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            //Get request headers
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();

            //Fetch authorization header
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

            //If no authorization information present; block access
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }

            final String[] authorizationHeader = authorization.get(0).split(" ");
            final String authType = authorizationHeader[0].toLowerCase();

            /*
                These methods check the user role
             */
            boolean authorized = false;

            switch (authType) {
                // User is attempting to login
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
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }
        }
    }

    private boolean checkBearerAuth(String[] authorizationHeader, Method method) {

        return true;
    }

    private boolean checkBasicAuth(String[] authorizationHeader, Method method) {
        String userRole = "User";

        String token = decrypt64(authorizationHeader[1]);

        String[] usernameAndPassword = token.split(":");

        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];

        // TODO
        /*
            1) Do database lookup
            2) Check user role
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

        /*
            This is where we will do database lookup
         */
        if (!(username.equals("admin") && password.equals("password"))) {
            return false;
        }

        return true;
    }

    private String decrypt64(String s) {
        return new String(Base64.decode(s.getBytes()));
    }
}