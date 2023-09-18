package lt.codeacademy.vartotojas;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static lt.codeacademy.vartotojas.Main.ISO_LOCAL_DATE_TIME_NO_MILIS;

@Data
@NoArgsConstructor
@Entity
@Table(schema = "vartotojas", name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private String vardas;
    private String slaptazodis;
    @Column(unique = true)
    private String email;
    private String lytis;

    @Column(name = "gimimo_data")
    private LocalDate gimimoData;

    @Column(name = "registracijos_data")
    private LocalDateTime registracijosData;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts;

    public User(String vardas, String slaptazodis, String email, String lytis, LocalDate gimimoData, LocalDateTime registracijosData) {
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
        this.gimimoData = gimimoData;
        this.registracijosData = registracijosData;
    }

    public String toSimpleString() {

        String gimimoData = this.gimimoData.format(DateTimeFormatter.ISO_DATE);

        String registracijosData = this.registracijosData.format(ISO_LOCAL_DATE_TIME_NO_MILIS);

        return String.format("%03d | %s | ****** | %s | %s | %s | %s",
                id, vardas, email, lytis,
                gimimoData, registracijosData
        );
    }
}
