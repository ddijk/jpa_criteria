package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.*;
import nl.bytesoflife.clienten.finance.praktijk.PageHelper;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JPARunner {
    static List<Integer> selectedPraktijken = Arrays.asList(1313, 1, 43);
    static final List<Integer> authPraktijken = Arrays.asList(1313, 1);

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            testQueryForProjects(em, cb);

        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }
    }

    private static void createCase(EntityManager em) {
        Case c = new Case();
        c.setDossiernaam("test 3 feb b");

        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();


        System.out.println("Case created");
    }

    private static void testQueryForProjects(EntityManager em, CriteriaBuilder cb) {
        int page = 0;
        int pageSize = 5;
        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        final Root<Case> caseRoot = countQuery.from(Case.class);
        Predicate praktijkenFilter = getPred(cb, caseRoot.get("folder").get("id"), selectedPraktijken, authPraktijken);
        countQuery.where(praktijkenFilter);
        countQuery.select(cb.count(caseRoot));
        caseRoot.join("accountviewProject");

        long totalElements = em.createQuery(countQuery).getSingleResult();

        // pageNr is 0 based
        int calculatedPageNr = PageHelper.calculatePageIndex(page, totalElements, pageSize);

        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        final Root<Case> root = tupleQuery.from(Case.class);
        Join<Case, AccountviewProject> projectJoin = root.join("accountviewProject");

        Join<Case, ClientContactDetails> tpaJoin = root.join("contactClient");

        long numPages = PageHelper.calculateNumberOfPages(totalElements, pageSize);

        System.out.println(String.format("Aantal pages '%d', Aantal elementen: '%d' ", numPages, totalElements));

        Join<Case, Folder> folderJoin = root.join("folder");
        tupleQuery.multiselect(
                root.get("debtorBalanceAcountView").alias("saldo"),
                projectJoin.get("PROJ_CODE").alias("projCode"),
                projectJoin.get("PROJ_DESC").alias("projDesc"),
                projectJoin.get("REFE").alias("projRef"),
                projectJoin.get("SUB_NR").alias("debiteurNr"),
                folderJoin.get("accountviewCompany").alias("company"),
                tpaJoin.get("id").alias("tpaId")

        );
        tupleQuery.where(praktijkenFilter);


        final TypedQuery<Tuple> emQuery = em.createQuery(tupleQuery);
        emQuery.setMaxResults(pageSize);
        emQuery.setFirstResult(calculatedPageNr * pageSize);

        final List<Tuple> resultList = emQuery.getResultList();
        System.out.println("Aantal records:" + resultList.size());

        for ( Tuple tuple : resultList) {
            System.out.println(String.format("projCode is '%s' en tpaId is '%s'",tuple.get("projCode"), tuple.get("tpaId")));
        }
    }

    public static <T> Long findCountByCriteria(EntityManager em, CriteriaQuery<T> cqEntity, CriteriaBuilder qb) {
        CriteriaBuilder builder = qb;
        CriteriaQuery<Long> cqCount = builder.createQuery(Long.class);
        Root<?> entityRoot = cqCount.from(cqEntity.getResultType());
        cqCount.select(builder.count(entityRoot));
//        cqCount.where(cqEntity.getRestriction());
        return em.createQuery(cqCount).getSingleResult();
    }

    static void testCount(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        final Root<Case> caseRoot = countQuery.from(Case.class);
        final Join<Case, Folder> folder = caseRoot.join("folder");
        final Join<Case, Folder> proj = caseRoot.join("accountviewProject");
        Predicate praktijkenFilter = createPraktijkFilter(cb, folder.get("id"));
        countQuery.where(praktijkenFilter);
        final Expression<Long> count = cb.count(caseRoot);
        countQuery.select(count);

        long n = em.createQuery(countQuery).getSingleResult();
        System.out.println("res:" + n);
    }

    private static Predicate createPraktijkFilter(CriteriaBuilder cb, Path<Object> id) {

        return getPred(cb, id, selectedPraktijken, authPraktijken);
    }

    private static void selectAllowedCases(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Tuple> query = cb.createTupleQuery();

        final Root<Case> root = query.from(Case.class);
        final Join<Case, AccountviewProject> accountviewProject = root.join("accountviewProject");

        query.multiselect(accountviewProject.get("EMP_NAME").alias("emp"), root.get("id").alias("case_id"));
//            query.where(cb.equal(root.get("dossiernummer"), "16550300"));
        Predicate caseIsSelectedAndAuthorized = createPraktijkFilter(cb, root.get("folder").get("id"));
        query.where(caseIsSelectedAndAuthorized);
        query.orderBy(cb.asc(root.get("id")));


        int pageSize = 5;
        int pageNo = 1;
        final TypedQuery<Tuple> emQuery = em.createQuery(query);
        emQuery.setFirstResult(pageNo * pageSize);
        emQuery.setMaxResults(pageSize);
        for (Tuple tuple : emQuery.getResultList()) {
            System.out.println(String.format("id='%d', Emp is '%s' ", tuple.get("case_id"), tuple.get("emp")));
        }
    }


    static Predicate getPred(CriteriaBuilder cb, Path<Object> folderId, List<Integer> selectedPraktijken, List<Integer> authPraktijken) {

        List<Integer> effectivePraktijken = selectedPraktijken.stream().filter(e -> authPraktijken.contains(e)).collect(Collectors.toList());
        System.out.println("EffectivePraktijken: " + effectivePraktijken);

        List<Predicate> folderPredicates = new ArrayList<>();
        for (Integer praktijk : effectivePraktijken) {

            folderPredicates.add(cb.equal(folderId, praktijk));
        }
        return cb.or(folderPredicates.toArray(new Predicate[]{}));
    }

    private static void jpqlQuery(EntityManager em) {
        final TypedQuery<Double> query = em.createQuery("Select f.derdengeldenSaldo from FinancialData f where f.projectCode=?1", Double.class);
        query.setParameter(1, "16550300");

        System.out.println("Saldo is : " + query.getSingleResult());
    }

    private static void selectDebiteurenNaam(EntityManager em) {
        final TypedQuery<String> query = em.createQuery("SELECT a.ACCT_NAME FROM AccountviewContact  a WHERE a.SUB_NR=?1 AND a.company=?2", String.class);
        query.setParameter(1, "29040");
        query.setParameter(2, "COLUMBUS");
        String zoekCode = query.getSingleResult();

        System.out.println("Zoekcode=" + zoekCode);
    }

    private static void testTuple(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();

        final Root<Case> root = tupleQuery.from(Case.class);
        root.join("accountviewProject");

        tupleQuery.where(cb.equal(root.get("folder").get("id"), 1313));
        tupleQuery.multiselect(root.get("dossiernummer").alias("dossiernr"));

        final List<Tuple> resultList = em.createQuery(tupleQuery).getResultList();
        int n = resultList.size();
        System.out.println("Aantal records: " + n);
        for (Tuple t : resultList) {
            System.out.println("dossiernr is :" + t.get("dossiernr"));
        }
    }

    private static void testJoinToProj(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<String> query = cb.createQuery(String.class);

        final Root<Case> root = query.from(Case.class);

        final Join<Object, Object> accountviewProject = root.join("accountviewProject");

        query.select(accountviewProject.get("EMP_NAME"));
        query.where(cb.equal(accountviewProject.get("PROJ_CODE"), "16550300"));

        for (String n : em.createQuery(query).getResultList()) {
            System.out.println(n);
        }
    }

    private static void multiSelect(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Tuple> query = cb.createTupleQuery();

        final Root<Case> root = query.from(Case.class);
        final Join<Case, AccountviewProject> joinProj = root.join("accountviewProject");
        query.multiselect(root.get("name").alias("name"), joinProj.get("EMP_NAME").alias("empl"));
        query.where(cb.equal(joinProj.get("PROJ_CODE"), "16550300"));

        for (Tuple t : em.createQuery(query).getResultList()) {
            System.out.println(String.format("Case name is '%s', employee is '%s'", t.get("name"), t.get("empl")));
        }
    }

    private static void selectProjInfo(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<String> query = cb.createQuery(String.class);
        final Root<Case> root = query.from(Case.class);

        Join<Case, AccountviewProject> proj = root.join("accountviewProject");

        query.where(cb.equal(proj.get("PROJ_CODE"), "16550300"));
        query.select(proj.get("EMP_NAME"));

        final TypedQuery<String> accountviewProjectTypedQuery = em.createQuery(query);


        final List<String> resultList = accountviewProjectTypedQuery.getResultList();
        System.out.println("Aantal projects is " + resultList.size());


        for (String c : resultList) {
            System.out.println(c);
        }
    }
}
