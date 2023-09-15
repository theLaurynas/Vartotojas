package lt.codeacademy.vartotojas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static lt.codeacademy.vartotojas.Main.ISO_LOCAL_DATE_TIME_NO_MILIS;

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


    public String toSimpleString() {

        String gimimoData = this.gimimoData.format(DateTimeFormatter.ISO_DATE);

        String registracijosData = this.registracijosData.format(ISO_LOCAL_DATE_TIME_NO_MILIS);

        return String.format("%03d | %s | ****** | %s | %s | %s | %s",
                id, vardas, email, lytis,
                gimimoData, registracijosData
        );
    }
}
