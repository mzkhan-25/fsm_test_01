package com.fsm.task.infrastructure.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for role-based authorization.
 * Used to specify which roles are allowed to access a method or class.
 * ADMIN role always has access to all endpoints.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * Array of roles that are allowed to access the annotated method/class.
     * ADMIN role is automatically granted access regardless of this list.
     */
    Role[] value();
}
