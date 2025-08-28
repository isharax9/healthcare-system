package com.globemed.auth;

/**
 * The abstract Decorator class. It holds a reference to a component object
 * and delegates all requests to it. Its main purpose is to define a
 * wrapping interface for all concrete decorators.
 */
public abstract class UserRoleDecorator implements IUser {
    protected final IUser wrappedUser;

    public UserRoleDecorator(IUser user) {
        this.wrappedUser = user;
    }

    @Override
    public String getUsername() {
        return wrappedUser.getUsername();
    }

    @Override
    public String getRole() {
        return wrappedUser.getRole();
    }

    @Override
    public boolean hasPermission(String permission) {
        // Delegate to the wrapped user by default.
        // Concrete decorators will override this.
        return wrappedUser.hasPermission(permission);
    }
}