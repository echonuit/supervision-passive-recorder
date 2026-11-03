package fr.univ_amu.iut.supervision.web;

/** What the browser sends: no id or timestamp yet, the "database" assigns those. */
public record NewSubmission(String name, String email, String message) {}
