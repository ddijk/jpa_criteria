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
            final Query nativeQuery = em.createNamedQuery("GetLatestHoofdsomInvoer");
            nativeQuery.setParameter(1, caseId);

            final HoofdsomInvoer2 resultList = (HoofdsomInvoer2) nativeQuery.getSingleResult();

            if ( resultList==null) {
                System.out.println("leeg");
            } else {
                System.out.println(resultList);
            }
//            System.out.println("Aantal results is " + resultList.size());
//            for (HoofdsomInvoer2  hoofdsomInvoer : resultList) {
//
//
//                    System.out.println(hoofdsomInvoer);
//            }


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
