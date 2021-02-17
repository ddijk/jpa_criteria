package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class SearchByEmailRunner {
    public static void main(String[] args) {


        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<Tuple> ccdQuery = cb.createTupleQuery();

            final Root<ClientContactDetails> ccdRoot = ccdQuery.from(ClientContactDetails.class);

            final ListJoin<ClientContactDetails, ClientContactValueWithType> valueJoin = ccdRoot.join(ClientContactDetails_.email);

            String searchValue = prepareSearchValue(" test@example.com ");
            ccdQuery.where(cb.like(prepareDatabaseValue(cb, valueJoin.get(ClientContactValueWithType_.value)), searchValue));

            final Join<ClientContactDetails, Case> mainCaseJoin = ccdRoot.join(ClientContactDetails_.mainCase, JoinType.LEFT);
            final Join<ClientContactDetails, Case> contactCaseJoin = ccdRoot.join(ClientContactDetails_.contactCase, JoinType.LEFT);
            final Join<ClientContactDetails, Case> otherCaseJoin = ccdRoot.join(ClientContactDetails_.otherCase, JoinType.LEFT);

            ccdQuery.multiselect(
                    mainCaseJoin.get(Case_.id).alias("mainCase"),
                    contactCaseJoin.get(Case_.id).alias("contactCase"),
                    otherCaseJoin.get(Case_.id).alias("otherCase")
                    ).distinct(true);

            Set<Long> caseIds = new HashSet<>();
            for ( Tuple t : em.createQuery(ccdQuery).getResultList() ) {
                final Long mainCase = t.get("mainCase", Long.class);
                caseIds.add(mainCase);
                final Long contactCase = t.get("contactCase", Long.class);
                caseIds.add(contactCase);
                final Long otherCase = t.get("otherCase", Long.class);
                caseIds.add(otherCase);
                System.out.println(String.format("main='%d', contact='%d', other='%d'",mainCase, contactCase, otherCase));
            }

            System.out.println("-----------");
            System.out.println(caseIds.size());
            System.out.println(caseIds);


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }

    private static String prepareSearchValue(String value) {
        String searchValue= "%" + value.replace(" ", "").replace("-", "") + "%";
        return searchValue.toLowerCase();
    }

    // remove space and hyphen characters
    private static Expression<String> prepareDatabaseValue(CriteriaBuilder criteriaBuilder, Expression<String> fieldValue) {
              return  criteriaBuilder.lower(
                        criteriaBuilder.function("replace", String.class,
                                criteriaBuilder.function("replace", String.class,
                                        fieldValue, criteriaBuilder.literal("-"), criteriaBuilder.literal("")
                                ), criteriaBuilder.literal(" "), criteriaBuilder.literal("")
                        ));
    }
}
