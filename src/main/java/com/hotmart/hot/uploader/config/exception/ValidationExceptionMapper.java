/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hotmart.hot.uploader.config.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Mapper responsavel pelo envio dos erros de validação em JSON no lugar do padrão text
 * @author julio
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<javax.validation.ConstraintViolationException>{

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(getViolations(exception)).build();
    }
    
    
    private Map<String, List<String>> getViolations(ConstraintViolationException exception) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (ConstraintViolation violation : exception.getConstraintViolations()) {
            String path = "";
            if (null != violation.getPropertyPath()) {
                path = violation.getPropertyPath().toString();
            }
            List<String> msgs = result.get(path);
            if (null == msgs) {
                msgs = new ArrayList<String>();
                result.put(path, msgs);
            }
            msgs.add(violation.getMessage());
        }
        return result;
    }
}
