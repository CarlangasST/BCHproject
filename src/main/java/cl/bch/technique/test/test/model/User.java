package cl.bch.technique.test.test.model;

import lombok.Data;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class User {
    private Long id;
    private String rut;
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "last_name")
    private String lastName;
    @JsonProperty(value = "date_birth")
    private LocalDate dateBirth;
    @JsonProperty(value = "mobile_phone")
    private String mobilePhone;
    private String email;
    private String address;
    @JsonProperty(value = "city_id")
    private Long cityId;
    @JsonProperty(value = "session_active")
    private boolean sessionActive;
    private String password;
}
