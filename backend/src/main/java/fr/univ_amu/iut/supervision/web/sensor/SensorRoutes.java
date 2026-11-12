package fr.univ_amu.iut.supervision.web.sensor;

import io.javalin.config.JavalinConfig;
import java.util.Map;

public class SensorRoutes {

    public static void register(JavalinConfig config, SensorService service) {

        config.routes.post("/api/sensors", ctx -> {
            String userId = ctx.sessionAttribute("UserId");
            if (userId == null) {
                ctx.status(401).json(Map.of("error", "Utilisateur non authentifié"));
                return;
            }
            NewSensor sensor = ctx.bodyAsClass(NewSensor.class);
            int id = service.addSensor(sensor, userId);
            ctx.status(201).json(Map.of("id", id));
        });

        config.routes.post("/api/groups", ctx -> {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            int id = service.createGroup((String) body.get("name"));
            ctx.status(201).json(Map.of("id", id));
        });

        config.routes.exception(
                IllegalArgumentException.class, (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
        config.routes.exception(
                DuplicateSensorException.class, (e, ctx) -> ctx.status(409).json(Map.of("error", e.getMessage())));
        config.routes.exception(
                GroupNotFoundException.class, (e, ctx) -> ctx.status(404).json(Map.of("error", e.getMessage())));
    }
}