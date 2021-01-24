package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.AccountviewProject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class JPARunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            final TypedQuery<AccountviewProject> query = em.createQuery("select p from AccountviewProject  p", AccountviewProject.class);

            int n = query.getResultList().size();

            System.out.println("Aantal projects is " + n);

        } finally {
            if ( em != null) {
                em.close();
            }
            if ( emf != null) emf.close();
        }
    }
}
