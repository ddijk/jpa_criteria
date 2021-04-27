package nl.dijkrosoft.depot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Declaration {
    double amount;

    String datum;

    String invoice;

    String company;

}
