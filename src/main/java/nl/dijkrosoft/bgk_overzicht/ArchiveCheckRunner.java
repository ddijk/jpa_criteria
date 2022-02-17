package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.data.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.List;

public class ArchiveCheckRunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            CriteriaBuilder cb = em.getCriteriaBuilder();

//            CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();

            CriteriaQuery<Tuple> query = cb.createTupleQuery();
            Root<Case> root = query.from(Case.class);
            Join<Case, CaseArchiveCheck> caseCaseArchiveCheckJoin = root.join(Case_.caseArchiveCheck, JoinType.LEFT);
            query.multiselect(root.get(Case_.id).alias("id"),
                    root.get(Case_.isArchived).alias("archived"),
                    caseCaseArchiveCheckJoin.get(CaseArchiveCheck_.id).alias("check"));
            query.where(cb.or(cb.equal(root.get(Case_.id), cb.literal(6L)), cb.equal(root.get(Case_.id), cb.literal(30L))));

            List<Tuple> resultList = em.createQuery(query).getResultList();

            for (Tuple t : resultList) {
                System.out.println("id=" + t.get("id", Long.class));
                System.out.println("archived=" + t.get("archived", Boolean.class));
                System.out.println("caseArchiveCheck=" + t.get("check", Long.class));
            }

            System.out.println("Aantal is " + resultList.get(0));

            System.out.println("Update done");

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
