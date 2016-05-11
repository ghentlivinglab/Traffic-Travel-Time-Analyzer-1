/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Handles all Exceptions thrown by the REST API, 
 * returns appropriate messages according to the mapped exceptions.
 * 
 * @author Piet
 */

@Provider
public class CustomWebApplicationExceptionMapper implements  ExceptionMapper<Exception>{

    /**
     * Handles all the known errors, returns a Response. 
     * If the errors are not mapped the returned text is the 
     * standard string of the exception.
     * 
     * @param exception the exception thrown in the REST API
     * @return Response with a proper http-error code and 
     * and an appropriate message for the client who
     * performed the request to the REST API.
     */
    @Override
    public Response toResponse(Exception exception) {
        if(exception instanceof WebApplicationException){
            if(exception instanceof ClientErrorException) { // errors made by the client (user)
                if(exception instanceof javax.ws.rs.NotFoundException) {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity("The requested resource does not exist. Change the url-path before trying again.").build();
                }
                else if(exception instanceof javax.ws.rs.NotAuthorizedException) { // thrown in NotAuthorizedFilter
                    return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(exception.getMessage()).build();
                }
                else if(exception instanceof javax.ws.rs.BadRequestException) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(exception.getMessage()).build();
                }
                else {
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity("Client-error occured." + exception.getMessage()).build();
                }
            }
            else if(exception instanceof ServerErrorException) { 
                if(exception instanceof InternalServerErrorException) {
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(exception.getMessage()).build();
                }
                else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(exception.getMessage()).build();
                }
            }
            else { // Oath1Exceptions, RedirectExceptions and ParamExceptions, first 2 are not used in this REST API. Only Params we use are QueryParam which means only QueryParamException gets thrown.
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("One of the given query-parameter values is invalid. Change the incorrect query-parameter before trying again.").build();
            }
        }
        else if(exception instanceof java.lang.IllegalArgumentException) { // wrong syntax of a query in the REST API.
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Syntax of the query which performs your request is wrong.").build();
        } 
        else { // catches all not impelemented errors yet (database errors etc.)
            return Response.status(Response.Status.NOT_FOUND).entity(exception.toString()).build();
        }
    }
    
}
