package com.minimarket.minimarket.exception;

public class StockInsuficienteException extends IllegalArgumentException{
    public StockInsuficienteException(String message){
        super(message);
    }

}
