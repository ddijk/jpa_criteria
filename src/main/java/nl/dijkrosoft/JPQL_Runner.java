package nl.dijkrosoft;

import nl.bytesoflife.clienten.CasesResponse;
import nl.bytesoflife.clienten.Zaken;
import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.data.CaseArchiveCheck;
import nl.bytesoflife.clienten.data.Case_;
import nl.bytesoflife.clienten.data.ClientContactDetails;
import nl.bytesoflife.clienten.finance.praktijk.PraktijkOverzichtCase;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Collections;
import java.util.List;

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

//        countQuery.where(cb.and(praktijkenFilter, archivedOnly(cb, caseRoot)));
//            countQuery.where(cb.and(praktijkenFilter));
            List<String> projectWithNonZeroDepotBedrag = getProjectsWithNonZeroDepotbedrag(em);
            if (projectWithNonZeroDepotBedrag.isEmpty()) {
                System.out.println("Geen data");
                return;
            }
            countQuery.where(cb.and(
                    praktijkenFilter,
                    caseRoot.get(Case_.dossiernummer).in(projectWithNonZeroDepotBedrag))
            );
            countQuery.select(cb.count(caseRoot));
            caseRoot.join(Case_.accountviewProject);

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

    static private List<String> getProjectsWithNonZeroDepotbedrag(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<String> projCodeQuery = cb.createQuery(String.class);

        final Root<FinancialData> fdRoot = projCodeQuery.from(FinancialData.class);

        // 0.004 is een drempel om floating point onnauwkeurigheden goed af te handelen
        projCodeQuery.where(cb.gt(cb.abs(fdRoot.get(FinancialData_.depotBedrag)), 0.004d));

        projCodeQuery.select(fdRoot.get(FinancialData_.projectCode));

        return em.createQuery(projCodeQuery).getResultList();
    }
}
