package nl.dijkrosoft;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;

public class Runner3 {

    public static void main(String[] args) throws IOException {

        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
//            CasesResponse cr = testCaseListRefactoring(em, cb);

//            ObjectMapper om = new ObjectMapper();
//            om.writerWithDefaultPrettyPrinter().writeValue(System.out, cr);

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

