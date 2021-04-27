package nl.dijkrosoft.depot;

import nl.bytesoflife.clienten.service.accountview.finance.CompanyTransactions;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialOverview;
import nl.bytesoflife.clienten.service.accountview.finance.SerializationHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class DeclaratiesTest {

    EntityManagerFactory emf = null;
    EntityManager em = null;

    @Before
    public void setUp() throws Exception {
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();
        } catch ( Exception ex ) {
            System.err.println(ex);
        }
    }

    @Test
    public void bepaalDeclaratiesDieUitDepotBetaaldKunnenWorden() {
        final FinancialData financialData = em.find(FinancialData.class, Integer.valueOf(90));

        assertEquals(Double.valueOf(1100d), financialData.getDepotBedrag());


        SerializationHelper<FinancialOverview>  serializationHelper = new SerializationHelper();
        final FinancialOverview fo = serializationHelper.deserialize(financialData.getData());


//        assertEquals("1100,00", fo.getDepotAmount());

        final List<CompanyTransactions> companyTransactionsList = fo.getCompanyTransactionsList();

        assertEquals(2, companyTransactionsList.size());

        // add company to declaration:
        List<Declaration> declarations = DepotUtil.flatMapToDeclarations(companyTransactionsList);

        Collections.sort(declarations, Comparator.comparing(d->d.getDatum()));
        assertEquals(15, declarations.size());


        for (Declaration declaration : declarations) {
            System.out.println(declaration);
        }
    }



    @After
    public void tearDown() throws Exception {
        if (em != null)
        {
            em.close();
        }
        if (emf != null) emf.close();
    }
}