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
 * Filters all requests made to the REST API and 
 * checks a custom header (X-API-KEY) for the authorization of the requests.
 * Depending on the API-KEY delivired by the request, 
 * returns the data or an UNAUTHORIZED (http-status code 401) response.
 * <p>
 * A custom header is being used because the authorization 
 * header is already being used for basic auth, by the server.
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
        
        String authorization = requestContext.getHeaderString(AUTHORIZATION_PROPERTY);
        
        // checking if there was an x-api-key header
        if(authorization == null){
            throw new WebApplicationException(ACCESS_DENIED.entity("Unauthorized. Authorization header is required to perform this request.").build());
        }
                
        // checking if the x-api-key header is valid
        if(!authorization.equals("6qKKfkX7u2lmJqxd8RrpLk7m")){
            throw new WebApplicationException(ACCESS_DENIED.entity(authorization + " is an invalid API-key. Use a valid API-key.").build());
        }         
    }
    
}
