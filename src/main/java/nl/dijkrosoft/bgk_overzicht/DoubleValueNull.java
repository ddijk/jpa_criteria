package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.hoofdsom.HoofdsomInvoer;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class DoubleValueNull {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            long caseId = 25991L;

            final CriteriaBuilder cb = em.getCriteriaBuilder();

            final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();


            final Root<FinancialData> financialDataRoot = tupleQuery.from(FinancialData.class);
            tupleQuery.where(cb.equal(financialDataRoot.get(FinancialData_.projectCode), "19550493" ));
            tupleQuery.multiselect(
                    financialDataRoot.get(FinancialData_.projectCode).alias("projCode"),
                    financialDataRoot.get(FinancialData_.totaalBgkGedeclareerd).alias("totBgkDecl"));

            final List<Tuple> resultList = em.createQuery(tupleQuery).getResultList();

            System.out.println("aantal is "+ resultList.size());
            for ( Tuple t : resultList) {

                System.out.println("projCode:"+ t.get("projCode", String.class));
                final Double totBgkDecl = t.get("totBgkDecl", Double.class);
                System.out.println("totBgkDecl="+totBgkDecl);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }
}
