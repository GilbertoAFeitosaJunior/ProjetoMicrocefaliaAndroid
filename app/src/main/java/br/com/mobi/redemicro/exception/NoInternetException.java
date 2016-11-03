package br.com.mobi.redemicro.exception;

/**
 * Created by Gilberto on 24/10/2016.
 */

public class NoInternetException extends Exception {

    public NoInternetException() {
        super("Seu dispositivo não está online.");
    }

}
