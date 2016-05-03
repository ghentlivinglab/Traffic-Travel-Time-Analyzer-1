/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.IOException;
import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Filter that checks the authorization header of all requests made to the REST API.
 * Depending on the API-KEY delivired by the request, 
 * returns the data or an UNAUTHORIZED (http-status code 401) response.
 * 
 * @author Piet
 */

@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    
    private static final String AUTHORIZATION_PROPERTY = "X-API-KEY";
    private static final Response.ResponseBuilder ACCESS_DENIED = Response.status(Response.Status.UNAUTHORIZED);
    
    /**
     * Filters all requests in the REST API.
     * 
     * @param requestContext context of the request to be filtered.
     * @throws IOException 
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        String authorization = requestContext.getHeaderString("X-API-KEY");
        
        // checking if there was an authorization header
        if(authorization == null){
            throw new WebApplicationException(ACCESS_DENIED.entity("Unauthorized. Authorization header is required to perform this request.").build());
        }
                
        // TODO : having multiple API-KEYS stored and checking them here
        if(!authorization.equals("6qKKfkX7u2lmJqxd8RrpLk7m")){
            throw new WebApplicationException(ACCESS_DENIED.entity(authorization + " is an invalid API-key. Use a valid API-key.").build());
        }         
    }
    
}
