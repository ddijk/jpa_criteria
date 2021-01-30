package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.AccountviewProject;
import nl.bytesoflife.clienten.data.Case;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

public class JPARunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            final CriteriaQuery<Tuple> query = cb.createTupleQuery();

            final Root<Case> root = query.from(Case.class);
            final Join<Case, AccountviewProject> accountviewProject = root.join("accountviewProject");

            query.multiselect(accountviewProject.get("EMP_NAME").alias("emp"), root.get("id").alias("case_id"));
            query.where(cb.equal(root.get("dossiernummer"), "16550300"));
            query.orderBy(cb.asc(root.get("id")));

            for ( Tuple tuple : em.createQuery(query).getResultList()) {
                System.out.println(String.format("Emp is '%s' id='%d'", tuple.get("emp"), tuple.get("case_id")));
            }




        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }
    }

    private static void jpqlQuery(EntityManager em) {
        final TypedQuery<Double> query = em.createQuery("Select f.derdengeldenSaldo from FinancialData f where f.projectCode=?1", Double.class);
        query.setParameter(1, "16550300" );

        System.out.println("Saldo is : "+query.getSingleResult());
    }

    private static void selectDebiteurenNaam(EntityManager em) {
        final TypedQuery<String> query = em.createQuery("SELECT a.ACCT_NAME FROM AccountviewContact  a WHERE a.SUB_NR=?1 AND a.company=?2", String.class);
        query.setParameter(1, "29040");
        query.setParameter(2, "COLUMBUS");
        String zoekCode = query.getSingleResult();

        System.out.println("Zoekcode="+zoekCode);
    }

    private static void testTuple(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();

        final Root<Case> root = tupleQuery.from(Case.class);
        root.join("accountviewProject");

        tupleQuery.where(cb.equal(root.get("folder").get("id"), 1313));
        tupleQuery.multiselect(root.get("dossiernummer").alias("dossiernr"));

        final List<Tuple> resultList = em.createQuery(tupleQuery).getResultList();
        int n = resultList.size();
        System.out.println("Aantal records: "+ n);
        for ( Tuple t : resultList) {
            System.out.println("dossiernr is :"+t.get("dossiernr"));
        }
    }

    private static void testJoinToProj(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<String> query = cb.createQuery(String.class);

        final Root<Case> root = query.from(Case.class);

        final Join<Object, Object> accountviewProject = root.join("accountviewProject");

        query.select(accountviewProject.get("EMP_NAME"));
        query.where(cb.equal(accountviewProject.get("PROJ_CODE"), "16550300"));

        for ( String n :    em.createQuery(query).getResultList()) {
            System.out.println(n);
        }
    }

    private static void multiSelect(EntityManager em, CriteriaBuilder cb) {
        final CriteriaQuery<Tuple> query = cb.createTupleQuery();

        final Root<Case> root = query.from(Case.class);
        final Join<Case, AccountviewProject> joinProj = root.join("accountviewProject");
        query.multiselect(root.get("name").alias("name"), joinProj.get("EMP_NAME").alias("empl") );
        query.where(cb.equal(joinProj.get("PROJ_CODE"), "16550300"));

        for ( Tuple t : em.createQuery(query).getResultList()) {
            System.out.println(String.format("Case name is '%s', employee is '%s'",t.get("name"), t.get("empl")));
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
