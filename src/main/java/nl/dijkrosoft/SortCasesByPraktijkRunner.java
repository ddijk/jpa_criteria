package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.*;
import nl.bytesoflife.clienten.finance.praktijk.PageHelper;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;
import static nl.dijkrosoft.SortHelper.getColumnForSortParam;

public class SortCasesByPraktijkRunner {
    private static List<String> sortableColumns = Arrays.asList("PROJ_CODE", "PROJ_DESC", "REFE", "SUB_NR", "case:debtorBalanceAcountView", "PRAKTIJK");

    private static final Logger LOGGER = Logger.getLogger("SortCasesByPraktijkRunner");
    static List<Integer> selectedPraktijken = Arrays.asList(1313, 1, 2);

    public static void main(String[] args) {

        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            int pageSize = 20;
            int page = 0;

//            String sort = "PRAKTIJK";
            String sort = "PROJ_CODE";
            Filters filterz = Filters.create("{\"folderId\":[1313, 1,2]}");
            final List<Long> allowedPraktijken = Arrays.asList(1313L, 1L, 2L);
            String direction = "ASC";

            final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

            final Root<Case> caseRoot = countQuery.from(Case.class);
            Predicate praktijkenFilter = createPraktijkenFilter(cb, caseRoot.get(Case_.folder).get(Folder_.id), selectedPraktijken, allowedPraktijken);
            List<String> projectWithNonZeroDerdengelden = getProjectsWithNonZeroDerdengelden(em);
            if (projectWithNonZeroDerdengelden.isEmpty()) {

                System.out.println("Geen projecten met derdengelden");
                return;
            }
            countQuery.where(cb.and(
                    praktijkenFilter,
                    caseRoot.get(Case_.dossiernummer).in(projectWithNonZeroDerdengelden))
            );
            countQuery.select(cb.count(caseRoot));
            caseRoot.join(Case_.accountviewProject);

            long totalElements = em.createQuery(countQuery).getSingleResult();

            // pageNr is 0 based
            int calculatedPageNr = PageHelper.calculatePageIndex(page, totalElements, pageSize);
            long numPages = PageHelper.calculateNumberOfPages(totalElements, pageSize);
            if (calculatedPageNr == -1) {
                System.out.println("calculatedPageNr is -1");
                return;
            }

            final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
            final Root<Case> root = tupleQuery.from(Case.class);

            Join<Case, AccountviewProject> projectJoin = root.join(Case_.accountviewProject);
            Join<Case, Folder> folderJoin = root.join(Case_.folder);
//            Join<AccountviewProject, FinancialData> fdJoin = projectJoin.join("", )
            tupleQuery.multiselect(
                    root.get(Case_.debtorBalanceAcountView).alias("saldo"),
                    projectJoin.get(AccountviewProject_.PROJ_CODE).alias("projCode"),
                    projectJoin.get(AccountviewProject_.PROJ_DESC).alias("projDesc"),
                    projectJoin.get(AccountviewProject_.REFE).alias("projRef"),
                    projectJoin.get(AccountviewProject_.SUB_NR).alias("debiteurNr"),
                    folderJoin.get(Folder_.accountviewCompany).alias("company"),
                    folderJoin.get(Folder_.shortName).alias("praktijk")
            );

            Predicate whereClause = praktijkenFilter;
            final String searchTerm = filterz.getName();
            if (searchTerm != null && searchTerm.length() > 0) {
                LOGGER.info(String.format("Searchterm '%s'", searchTerm));
                final String likeSearchTerm = String.format("%%%s%%", searchTerm);
                final Predicate searchTermPredicate = cb.or(
                        cb.like(projectJoin.get(AccountviewProject_.PROJ_CODE), likeSearchTerm),
                        cb.like(projectJoin.get(AccountviewProject_.SUB_NR), likeSearchTerm),
                        cb.like(cb.lower(projectJoin.get(AccountviewProject_.PROJ_DESC)), likeSearchTerm.toLowerCase()));

                whereClause = cb.and(whereClause, searchTermPredicate);
            }

            whereClause = cb.and(whereClause, caseRoot.get(Case_.dossiernummer).in(projectWithNonZeroDerdengelden));
            tupleQuery.where(whereClause);

            if (sortableColumns.contains(sort)) {

                if (sort.indexOf(":") == -1) {
                    if ("ASC".equalsIgnoreCase(direction)) {

                        tupleQuery.orderBy(cb.asc(getColumnForSortParam(sort, projectJoin, folderJoin)));
                    } else {
                        tupleQuery.orderBy(cb.desc(projectJoin.get(sort)));

                    }
                } else {
                    if ("ASC".equalsIgnoreCase(direction)) {

                        tupleQuery.orderBy(cb.asc(root.get(sort.split(":", 2)[1])));
                    } else {
                        tupleQuery.orderBy(cb.desc(root.get(sort.split(":", 2)[1])));

                    }
                }

            }

            final TypedQuery<Tuple> emQuery = em.createQuery(tupleQuery);
            emQuery.setMaxResults(pageSize);
            emQuery.setFirstResult(calculatedPageNr * pageSize);

            final List<Tuple> resultList = emQuery.getResultList();
            System.out.println("Aantal results is: " + resultList.size());

            resultList.forEach(r -> System.out.println(String.format("projCode='%s', desc='%s', praktijk='%s'", r.get("projCode"), r.get("projDesc"), r.get("praktijk"))));


        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }


    private static List<String> getProjectsWithNonZeroDerdengelden(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<String> projCodeQuery = cb.createQuery(String.class);

        final Root<FinancialData> fdRoot = projCodeQuery.from(FinancialData.class);

        // 0.004 is een drempel om floating point onnauwkeurigheden goed af te handelen
        projCodeQuery.where(cb.gt(cb.abs(fdRoot.get(FinancialData_.derdengeldenSaldo)), 0.004d));

        projCodeQuery.select(fdRoot.get(FinancialData_.projectCode));

        return em.createQuery(projCodeQuery).getResultList();
    }
}
