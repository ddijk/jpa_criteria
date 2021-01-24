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

            CriteriaBuilder cb = emf.getCriteriaBuilder();

            final CriteriaQuery<Tuple> query = cb.createTupleQuery();

            final Root<Case> root = query.from(Case.class);
            final Join<Case, AccountviewProject> joinProj = root.join("accountviewProject");
            query.multiselect(root.get("name").alias("name"), joinProj.get("EMP_NAME").alias("empl") );
            query.where(cb.equal(joinProj.get("PROJ_CODE"), "16550300"));

           for ( Tuple t : em.createQuery(query).getResultList()) {
               System.out.println(String.format("Case name is '%s', employee is '%s'",t.get("name"), t.get("empl")));
           }

        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
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
