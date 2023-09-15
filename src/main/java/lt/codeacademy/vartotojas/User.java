package lt.codeacademy.vartotojas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "vartotojas", name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private String vardas;
    private String slaptazodis;
    private String email;
    private String lytis;

    @Column(name = "gimimo_data")
    private LocalDate gimimoData;

    @Column(name = "registracijos_data")
    private LocalDateTime registracijosData;
}
