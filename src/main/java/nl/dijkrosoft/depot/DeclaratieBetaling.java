package nl.dijkrosoft.depot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.math3.util.Precision;

import java.util.Objects;

@Data
@ToString
@AllArgsConstructor
public class DeclaratieBetaling {

    Declaration declaration;

    boolean fullyPaid;

    double amountPaid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeclaratieBetaling that = (DeclaratieBetaling) o;
        return fullyPaid == that.fullyPaid && Precision.equals(that.amountPaid, amountPaid, 0.004) && declaration.equals(that.declaration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaration, fullyPaid );
    }
}
