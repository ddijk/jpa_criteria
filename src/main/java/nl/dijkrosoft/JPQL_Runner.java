package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.CaseArchiveCheck;
import nl.bytesoflife.clienten.data.ClientContactDetails;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;

public class JPQL_Runner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

//            listJoin(em, cb);

            final TypedQuery<ClientContactDetails> query = em.createQuery("Select a from ClientContactDetails a where a.id=3036", ClientContactDetails.class);

            for ( ClientContactDetails c:    query.getResultList()) {
                System.out.println(c);
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
