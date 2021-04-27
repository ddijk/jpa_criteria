package nl.dijkrosoft.depot;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DepotAccumulationContainer {
    List<DeclaratieBetaling> betalingList = new ArrayList<>();

    // money left after paying declaration. if moneyLeft > 0 then more declarations can be paid.
    double moneyLeft;

}
