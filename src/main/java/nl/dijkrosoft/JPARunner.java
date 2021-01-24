package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.AccountviewProject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class JPARunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = emf.getCriteriaBuilder();

            final CriteriaQuery<AccountviewProject> query = cb.createQuery(AccountviewProject.class);
            final Root<AccountviewProject> ap = query.from(AccountviewProject.class);


//            final CriteriaQuery<AccountviewProject> select = query.select(AccountviewProject_.);

            final TypedQuery<AccountviewProject> accountviewProjectTypedQuery = em.createQuery(query);


            System.out.println("Aantal projects is " + accountviewProjectTypedQuery.getResultList().size());

        } finally {
            if ( em != null) {
                em.close();
            }
            if ( emf != null) emf.close();
        }
    }
}
