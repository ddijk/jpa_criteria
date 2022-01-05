package nl.dijkrosoft.bgk_overzicht;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class RunnerBgkNativeSQL {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            long caseId = 63746L;
            final Query nativeQuery = em.createNativeQuery("select nu,potentieel,posting_date from hoofdsom_invoer where \"case\"=?1 order by posting_date desc limit 1");
            nativeQuery.setParameter(1, caseId);

            final List<Object[]> resultList = nativeQuery.getResultList();
            System.out.println("Aantal results is " + resultList.size());
            for (Object[]  hoofdsomInvoer : resultList) {

                for ( Object o : hoofdsomInvoer) {

                    System.out.println(o);
                }
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
