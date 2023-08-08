import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Vartotojas {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
    private static int kiekis = 0; // Ivestu vartotoju kiekis
    private final LocalDateTime regData = LocalDateTime.now();
    private final LocalDate gimimoData;
    private int id;
    private String vardas;
    private String slaptazodis;
    private String email;
    private Lytis lytis;


    public Vartotojas(String vardas, String slaptazodis, String email, Lytis lytis, LocalDate gimimoData) {
        this.id = kiekis++;
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
        this.gimimoData = gimimoData;
    }

    public static int getKiekis() {
        return kiekis;
    }

    @Override
    public String toString() {
        return String.format("""
                        Id: %d | Vardas: %s | Slaptazodis: %s | Email: %s | Lytis: %s
                        \tReg. data: %s | Gimimo data: %s""",
                id, vardas, slaptazodis, email, lytis, regData.format(DATE_TIME_FORMATTER), gimimoData);
    }

    //<editor-fold desc="Getters/Setters">
    public LocalDateTime getRegData() {
        return regData;
    }

    public LocalDate getGimimoData() {
        return gimimoData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVardas() {
        return vardas;
    }

    public void setVardas(String vardas) {
        this.vardas = vardas;
    }

    public String getSlaptazodis() {
        return slaptazodis;
    }

    public void setSlaptazodis(String slaptazodis) {
        this.slaptazodis = slaptazodis;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Lytis getLytis() {
        return lytis;
    }

    public void setLytis(Lytis lytis) {
        this.lytis = lytis;
    }
    //</editor-fold>
}
