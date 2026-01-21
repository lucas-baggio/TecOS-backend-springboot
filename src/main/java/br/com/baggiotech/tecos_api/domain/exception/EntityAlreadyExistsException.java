package br.com.baggiotech.tecos_api.domain.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
    
    public EntityAlreadyExistsException(String entityName, String field, Object value) {
        super(String.format("%s com %s '%s' já existe", entityName, field, value));
    }
}
