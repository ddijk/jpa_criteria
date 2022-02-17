package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.data.Case_;
import nl.bytesoflife.clienten.data.DefaultContact;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
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

            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            Root<Case> root = query.from(Case.class);
            root.join(Case_.caseArchiveCheck, JoinType.LEFT);
            query.select(cb.count(root.get(Case_.id )));
            query.where(cb.equal(root.get(Case_.id), cb.literal(6L)));

            List<Long> resultList = em.createQuery(query).getResultList();

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
