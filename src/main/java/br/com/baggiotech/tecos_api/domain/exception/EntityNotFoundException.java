package br.com.baggiotech.tecos_api.domain.exception;

public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s com id %s não encontrado(a)", entityName, id));
    }
}
