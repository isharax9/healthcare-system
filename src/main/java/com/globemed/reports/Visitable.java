package com.globemed.reports;

/**
 * Defines the accept method that allows a visitor to perform an operation on an object.
 * This is the "Element" interface in the Visitor pattern.
 */
public interface Visitable {
    void accept(ReportVisitor visitor);
}