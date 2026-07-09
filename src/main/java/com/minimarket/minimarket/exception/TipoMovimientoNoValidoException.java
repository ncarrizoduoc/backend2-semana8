package com.minimarket.minimarket.exception;

public class TipoMovimientoNoValidoException extends IllegalArgumentException{
    public TipoMovimientoNoValidoException(String message){
        super(message);
    }

}
