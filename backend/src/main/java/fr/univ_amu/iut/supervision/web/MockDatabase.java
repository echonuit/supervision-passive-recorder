package fr.univ_amu.iut.supervision.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory stand-in for a real database. Data is lost when the server stops.
 * To use a real DB later, keep these two method signatures and swap the body for JDBC/JPA calls.
 */
public class MockDatabase {

    private final List<Submission> rows = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public Submission insert(NewSubmission input) {
        Submission row = new Submission(
                nextId.getAndIncrement(),
                input.name(),
                input.email(),
                input.message(),
                Instant.now().toString());
        rows.add(row);
        return row;
    }

    /** Newest first. */
    public List<Submission> findAll() {
        List<Submission> copy = new ArrayList<>(rows);
        Collections.reverse(copy);
        return copy;
    }
}
