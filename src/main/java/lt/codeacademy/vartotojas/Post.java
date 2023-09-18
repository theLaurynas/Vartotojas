package lt.codeacademy.vartotojas;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "vartotojas", name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String pavadinimas;
    private String tekstas;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id")
    private User user;

    public Post(String pavadinimas, String tekstas, User user) {
        this.pavadinimas = pavadinimas;
        this.tekstas = tekstas;
        this.user = user;
    }
}





