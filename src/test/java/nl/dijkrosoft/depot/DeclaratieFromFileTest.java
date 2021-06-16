package nl.dijkrosoft.depot;

import nl.bytesoflife.clienten.service.accountview.finance.CompanyTransactions;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialOverview;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeclaratieFromFileTest {

    @Test
    public void verifyPayableDeclarations() throws IOException {

        FinancialOverview financialOverview = DeclarationsHelper.readFinancialOverviewFromFile("declarations_2.json");

        final List<CompanyTransactions> companyTransactionsList = financialOverview.getCompanyTransactionsList();
        assertEquals(2, companyTransactionsList.size());

        // add company to declaration:
        List<Declaration> declarations = DepotUtil.flatMapToDeclarations(companyTransactionsList);

        assertEquals(15, declarations.size());


        double depotAmount = 1000d;

        DepotUtil.calculatePayableDeclarations(depotAmount, declarations);
    }

    @Test
    public void verifyDuplicateInvoiceNrs() {
        Assert.fail("not implemented");
    }


}
