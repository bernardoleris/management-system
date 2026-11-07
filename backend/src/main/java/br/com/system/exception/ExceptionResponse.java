package br.com.system.exception;

import java.util.Date;

public record ExceptionResponse(Date timestamp, int status, String message, String details) {}
