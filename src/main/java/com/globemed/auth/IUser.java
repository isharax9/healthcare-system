package com.globemed.auth;

/**
 * The Component interface for the Decorator pattern.
 * Defines the operations that can be altered by decorators.
 */
public interface IUser {
    String getUsername();
    String getRole();
    String getDoctorId();
    boolean hasPermission(String permission);
}