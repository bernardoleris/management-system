package br.com.system.services;

public record EmailVerificationRequestedEvent(String email, String token) {}
