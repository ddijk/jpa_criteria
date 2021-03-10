package nl.dijkrosoft;

import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class FindDerdengeldenRunner {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            final CriteriaQuery<String> projCodeQuery = cb.createQuery(String.class);

            final Root<FinancialData> fdRoot = projCodeQuery.from(FinancialData.class);


            projCodeQuery.where(cb.gt(cb.abs(fdRoot.get(FinancialData_.derdengeldenSaldo)), 0.004d));

            projCodeQuery.select(fdRoot.get(FinancialData_.projectCode));

            final List<String> resultList = em.createQuery(projCodeQuery).getResultList();


            System.out.println("total:"+resultList);
            for ( String projCode : resultList) {
                System.out.println(projCode);
            }



        } finally {
            if (em != null)
            {
                em.close();
            }
            if (emf != null) emf.close();
        }
    }
}
