package com.techforb.app.shared.auth;

import com.techforb.app.shared.security.CurrentUser;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Usuarios de prueba EN MEMORIA (todavía no hay base): salen de src/main/resources/demo-users.json, que genera el
 * paso base con un usuario por rol del sistema [{ id, name, email, role }]. Sin ese archivo hay un único usuario
 * demo@demo.com sin rol. Todos entran con app.security.demo-password (por defecto Demo1234!, la misma del front).
 */
@Component
public class DemoUsers {

    static final String RESOURCE = "demo-users.json";
    private static final Logger log = LoggerFactory.getLogger(DemoUsers.class);
    private static final CurrentUser FALLBACK = new CurrentUser("demo", "Usuario de prueba", "demo@demo.com", null);

    private final List<CurrentUser> users;

    public DemoUsers(ObjectMapper objectMapper) {
        this.users = load(objectMapper);
    }

    public Optional<CurrentUser> findByEmail(String email) {
        return users.stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
    }

    private static List<CurrentUser> load(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource(RESOURCE);
        if (!resource.exists()) {
            log.warn("No hay {}: solo existe el usuario {}", RESOURCE, FALLBACK.email());
            return List.of(FALLBACK);
        }
        try (InputStream input = resource.getInputStream()) {
            List<CurrentUser> loaded = objectMapper.readValue(input, new TypeReference<List<CurrentUser>>() { });
            List<CurrentUser> valid = loaded.stream().filter(user -> user.id() != null && user.email() != null && !user.email().isBlank()).toList();
            return valid.isEmpty() ? List.of(FALLBACK) : valid;
        } catch (IOException exception) {
            throw new IllegalStateException(RESOURCE + " no es un JSON válido: [{ id, name, email, role }]", exception);
        }
    }
}
