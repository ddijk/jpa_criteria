package nl.dijkrosoft.derdengelden;

import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.data.Case_;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;
import static nl.dijkrosoft.JPARunner.authPraktijken;
import static nl.dijkrosoft.JPARunner.selectedPraktijken;

public class DerdengeldenDateRunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            final Query query = em.createQuery("select fd from FinancialData fd");
         for (Object o :   query.getResultList()) {

             FinancialData f = (FinancialData) o;
             System.out.println(String.format("'%s en first date is '%s'", f.getProjectCode(), f.getDerdengeldenFirstTxDate()));
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
