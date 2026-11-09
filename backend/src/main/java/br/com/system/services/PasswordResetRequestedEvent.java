package br.com.system.services;

public record PasswordResetRequestedEvent(String email, String token) {}
