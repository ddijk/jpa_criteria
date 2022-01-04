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


//            caseQuery.where(cb.like(accountviewProjectJoin.get(AccountviewProject_.REFE),"%\\_ERKEND\\_%", '\\' ));
            caseQuery.where(cb.and(
                    cb.equal(accountviewProjectJoin.get(AccountviewProject_.TYPE), cb.literal("BET STD")),
                    cb.like(accountviewProjectJoin.get(AccountviewProject_.REFE),"%\\_ERKEND\\_%", '\\' )
            ));
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