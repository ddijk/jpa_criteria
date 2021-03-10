package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.data.CaseArchiveCheck;
import nl.bytesoflife.clienten.data.Case_;
import nl.bytesoflife.clienten.data.ClientContactDetails;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;
import static nl.dijkrosoft.JPARunner.authPraktijken;
import static nl.dijkrosoft.JPARunner.selectedPraktijken;

public class JPQL_Runner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        final Root<Case> caseRoot = countQuery.from(Case.class);
        Predicate praktijkenFilter = createPraktijkenFilter(cb, caseRoot.get(Case_.folder).get("id"), selectedPraktijken, authPraktijken);
        countQuery.where(cb.and(praktijkenFilter, archivedOnly(cb, caseRoot)));
//            countQuery.where(cb.and(praktijkenFilter));
        countQuery.select(cb.count(caseRoot));

        long totalElements = em.createQuery(countQuery).getSingleResult();


            System.out.println("total:"+totalElements);

        } finally {
            if (em != null)
            {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }


    static Predicate archivedOnly(CriteriaBuilder cb, Root<Case> root) {
        return cb.isTrue(root.get(Case_.isArchived));
    }

}
