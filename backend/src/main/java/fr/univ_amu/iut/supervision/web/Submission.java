package fr.univ_amu.iut.supervision.web;

/** A stored row. */
public record Submission(int id, String name, String email, String message, String createdAt) {}
