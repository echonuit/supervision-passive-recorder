/*
Class used to test the building of an auth system
 */
package fr.univ_amu.iut.supervision.web;

import io.javalin.security.RouteRole;

public enum UserRoleTEST implements RouteRole {
    ANYONE,
    USER_READ,
    USER_WRITE
}
