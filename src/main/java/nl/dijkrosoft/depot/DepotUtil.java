package nl.dijkrosoft.depot;

import nl.bytesoflife.clienten.service.accountview.finance.CompanyTransactions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DepotUtil {

    public static List<Declaration> flatMapToDeclarations(List<CompanyTransactions> companyTransactionsList) {

        final List<Declaration> declarations = companyTransactionsList.stream().flatMap(tx -> addCompany(tx)).collect(toList());
        Collections.sort(declarations, Comparator.comparing(d->d.getDatum())); // sort on oldest first (asc)
        return declarations;
    }

    private static Stream<Declaration> addCompany(CompanyTransactions tx) {

        String company = tx.getCompanyName();

        return tx.getDeclarations().stream().map(d -> new Declaration(d.getAMOUNT(), d.getTRN_DATE(), d.getINV_NR(), company));
    }


    public static List<DeclaratieBetaling> calculatePayableDeclarations(double depotAmount, List<Declaration> declarations) {

        DepotAccumulationContainer initial = new DepotAccumulationContainer();
        initial.setMoneyLeft(depotAmount);
        BiFunction<DepotAccumulationContainer, Declaration, DepotAccumulationContainer> accumulator = ( container, decl) -> {

            final double moneyLeft = container.getMoneyLeft();
            if ( Math.abs(moneyLeft) < 0.004) {
                System.out.println("'t is op");
                return container;
            } else {
                if ( moneyLeft >= decl.getAmount() ) {

                    System.out.println(String.format("If tak: MoneyLeft=%f  decl amount:%f", moneyLeft, decl.getAmount()));
                    // pay the whole declaration
                    container.getBetalingList().add(new DeclaratieBetaling(decl, true, decl.getAmount()));
                    container.setMoneyLeft(container.getMoneyLeft()-decl.getAmount());
                } else {

                    System.out.println(String.format("Else tak: MoneyLeft=%f  decl amount:%f", moneyLeft, decl.getAmount()));
                    // pay part of the declaration
                    container.getBetalingList().add(new DeclaratieBetaling(decl, false, moneyLeft));
                    container.setMoneyLeft(0.0d);
                }
                return container;

            }

        };
        BinaryOperator<DepotAccumulationContainer> combiner = ( (DepotAccumulationContainer a, DepotAccumulationContainer b) -> { a.getBetalingList().addAll(b.getBetalingList()); a.setMoneyLeft(a.getMoneyLeft()+b.getMoneyLeft()); return a;});
        DepotAccumulationContainer result = declarations.stream().reduce(initial, accumulator, combiner);

        return result.getBetalingList();
    }
}
