package nl.dijkrosoft.filter;

import nl.bytesoflife.clienten.data.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

public class ErkendStandaardFilter {
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            final CriteriaQuery<Tuple> caseQuery = cb.createQuery(Tuple.class);
            final Root<Case> caseRoot = caseQuery.from(Case.class);

            final Join<Case, AccountviewProject> accountviewProjectJoin = caseRoot.join(Case_.accountviewProject);

            ParameterExpression<String> erkendExpression = cb.parameter(String.class);
//            caseQuery.where(cb.isNotNull(cb.function("REGEXP_MATCHES", String.class,accountviewProjectJoin.get(AccountviewProject_.REFE),  erkendExpression )));
//            EscapingLikeExpression escapingLikeExpression = new EscapingLikeExpression(accountviewProjectJoin.get(AccountviewProject_.REFE).toString(), "%_ERKEND_%");
//            caseQuery.where(cb.like( escapingLikeExpression));

//            caseQuery.where(caseRoot.get(Case_.id).in(36, 2911,52,43));
//            caseQuery.where(accountviewProjectJoin.get(AccountviewProject_.REFE).in("_(1)_@_(2)_LC_(3)_GCS_(4)_ERKEND_///_(a)_080_(b)_@_(c)_@_(d)_@_(e)_@_(f)_@"));

            caseQuery.where(cb.like(accountviewProjectJoin.get(AccountviewProject_.REFE),"%\\_ERKEND\\_%", '\\' ));
            caseQuery.multiselect(
                    caseRoot.get(Case_.id).alias("id"),
                    accountviewProjectJoin.get(AccountviewProject_.REFE).alias("ref"),
                    accountviewProjectJoin.get(AccountviewProject_.TYPE).alias("type"));


            final TypedQuery<Tuple> query = em.createQuery(caseQuery);
//            query.setParameter(erkendExpression, "\\_ERKEND\\_");
            final List<Tuple> resultList = query.getResultList();
            System.out.println("Aantal results: "+ resultList.size());
            for ( Tuple tuple : resultList) {

                System.out.println(String.format("%s,%s,%s", tuple.get("id"), tuple.get("ref"), tuple.get("type")));

            }

        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }
}