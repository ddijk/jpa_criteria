package nl.dijkrosoft.depot;

import nl.bytesoflife.clienten.service.accountview.finance.FinancialOverview;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.bytesoflife.clienten.Constants.COLUMBUS;
import static nl.bytesoflife.clienten.Constants.VERKRUISEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class DepotUtilTest {

    @Test
    public void testFlatMapToDeclarations() throws IOException {
        FinancialOverview financialOverview = DeclarationsHelper.readFinancialOverviewFromFile("declarations_1.json");


        final List<Declaration> actualDeclarations = DepotUtil.flatMapToDeclarations(financialOverview.getCompanyTransactionsList());

        final List<Declaration> expectedDeclarations = new ArrayList<>();
        expectedDeclarations.add(new Declaration(123.45, "2018", "inv1", VERKRUISEN));
        expectedDeclarations.add(new Declaration(723.45, "2019", "inv1", VERKRUISEN));
        expectedDeclarations.add(new Declaration(300.10, "2020", "inv2", VERKRUISEN));
        expectedDeclarations.add(new Declaration(200.10, "2021", "inv3", COLUMBUS));

        assertTrue(CollectionUtils.isEqualCollection(actualDeclarations, expectedDeclarations));
    }

    @Test
    public void testCalculatePayableDeclarations() throws IOException {

        FinancialOverview financialOverview = DeclarationsHelper.readFinancialOverviewFromFile("declarations_1.json");
        final List<Declaration> actualDeclarations = DepotUtil.flatMapToDeclarations(financialOverview.getCompanyTransactionsList());



        final List<DeclaratieBetaling> actualDeclaratieBetalingen = DepotUtil.calculatePayableDeclarations(1000, actualDeclarations);

        List<DeclaratieBetaling> expectedDeclaratieBetalingen = new ArrayList<>();
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(123.45, "2018", "inv1", VERKRUISEN), true, 123.45));
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(723.45, "2019", "inv1", VERKRUISEN), true, 723.45));
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(300.10, "2020", "inv2", VERKRUISEN), false, 153.10));

        assertThat(actualDeclaratieBetalingen).hasSameElementsAs(expectedDeclaratieBetalingen);
    }

    @Test
    public void testCalculatePayableDeclarationsWithLargeDepotValue() throws IOException {

        FinancialOverview financialOverview = DeclarationsHelper.readFinancialOverviewFromFile("declarations_1.json");
        final List<Declaration> actualDeclarations = DepotUtil.flatMapToDeclarations(financialOverview.getCompanyTransactionsList());



        final List<DeclaratieBetaling> actualDeclaratieBetalingen = DepotUtil.calculatePayableDeclarations(10_000, actualDeclarations);

        List<DeclaratieBetaling> expectedDeclaratieBetalingen = new ArrayList<>();
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(123.45, "2018", "inv1", VERKRUISEN), true, 123.45));
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(723.45, "2019", "inv1", VERKRUISEN), true, 723.45));
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(300.10, "2020", "inv2", VERKRUISEN), true, 300.10));
        expectedDeclaratieBetalingen.add(new DeclaratieBetaling(new Declaration(200.10, "2021", "inv3", COLUMBUS), true, 200.10));

        assertThat(actualDeclaratieBetalingen).hasSameElementsAs(expectedDeclaratieBetalingen);
    }

}