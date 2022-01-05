package nl.dijkrosoft.bgk_overzicht;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.bytesoflife.clienten.data.AppUser;
import nl.bytesoflife.clienten.data.Case;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoofdsom_invoer")
@Data
@NoArgsConstructor
@SqlResultSetMapping(
        name="HoofdinvoerResult",
        classes={
                @ConstructorResult(
                        targetClass=nl.dijkrosoft.bgk_overzicht.HoofdsomInvoer2.class,
                        columns={
                                @ColumnResult(name="id", type=Integer.class),
                                @ColumnResult(name="nu", type=Float.class),
                                @ColumnResult(name="potentieel", type=Float.class),
                                @ColumnResult(name="posting_date", type=LocalDateTime.class)})})
@NamedNativeQuery(name="GetLatestHoofdsomInvoer", query="select id, nu,potentieel,posting_date from hoofdsom_invoer where \"case\"=?1 order by posting_date desc limit 1", resultSetMapping = "HoofdinvoerResult")
public class HoofdsomInvoer2 {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "\"user\"") // extra quoting is nodig omdat dit een SQL/Postgress reserved keyword is
    @ToString.Exclude
    private AppUser user;

    @Column(name="posting_date")
    @EqualsAndHashCode.Exclude
    private LocalDateTime timestamp;

    private float nu;

    private float potentieel;

    @ManyToOne
    @JoinColumn(name="\"case\"") // extra quoting is nodig omdat dit een SQL/Postgress reserved keyword is
    @ToString.Exclude
    private Case zaak;


    public HoofdsomInvoer2(AppUser user, float nu, float potentieel, Case zaak) {
        this.user = user;
        this.nu = nu;
        this.potentieel = potentieel;
        this.zaak = zaak;
    }

    public HoofdsomInvoer2(int id, float nu, float potentieel, LocalDateTime timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.nu = nu;
        this.potentieel = potentieel;
    }

}
