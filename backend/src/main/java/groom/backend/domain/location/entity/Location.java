package groom.backend.domain.location.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Location {

    @Id
    UUID id;
    String name;
}
