import java.time.LocalDate;
import java.time.LocalDateTime;

public class Vartotojas {
    private static int idCounter = 0; // Ivestu vartotoju kiekis
    private final LocalDateTime regData;
    private final LocalDate gimimoData;
    private int id;
    private String vardas;
    private String slaptazodis;
    private String email;
    private Lytis lytis;


    public Vartotojas(String vardas, String slaptazodis, String email, Lytis lytis, LocalDate gimimoData) {
        this.id = idCounter++;
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
        this.gimimoData = gimimoData;
        this.regData = LocalDateTime.now();
    }

    public Vartotojas(int id, String vardas, String slaptazodis, String email, Lytis lytis, LocalDate gimimoData, LocalDateTime regData) {
        this.id = id;
        this.vardas = vardas;
        this.slaptazodis = slaptazodis;
        this.email = email;
        this.lytis = lytis;
        this.gimimoData = gimimoData;
        this.regData = regData;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public static void setIdCounter(int idCounter) {
        Vartotojas.idCounter = idCounter;
    }

    @Override
    public String toString() {
        return String.format("""
                        Id: %d | Vardas: %s | Slaptazodis: %s | Email: %s | Lytis: %s
                        \tReg. data: %s | Gimimo data: %s""",
                id, vardas, slaptazodis, email, lytis, regData.format(Main.DATE_TIME_FORMATTER), gimimoData);
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
