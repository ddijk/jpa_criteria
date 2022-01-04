package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.hoofdsom.HoofdsomInvoer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class RunnerBgk {
    private static String filters = "{ folderId:[1313], name: \"\"}";
    private static int page = 0;

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            long caseId = 63746L;
            final TypedQuery<HoofdsomInvoer> hoofdsomTypedQuery = em.createQuery("select h from HoofdsomInvoer h where h.zaak.id=?1 order by h.timestamp desc", HoofdsomInvoer.class);
            hoofdsomTypedQuery.setFirstResult(0);
            hoofdsomTypedQuery.setMaxResults(1);
            hoofdsomTypedQuery.setParameter(1, caseId);

            final List<HoofdsomInvoer> resultList = hoofdsomTypedQuery.getResultList();
            System.out.println("Aantal results is " + resultList.size());
//            for (HoofdsomInvoer hoofdsomInvoer : resultList) {
//                System.out.println(hoofdsomInvoer);
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
