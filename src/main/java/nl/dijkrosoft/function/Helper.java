package nl.dijkrosoft.function;

import nl.bytesoflife.clienten.data.*;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.ListAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Helper {
    static final String[] linksToCase = new String[]{"mainCase", "contactCase", "otherCase"};

    public static Set<Long> findCases(String searchValue, ListAttribute<Long, String> attr) {

        System.out.println("Helper.findCases()");
        return new HashSet<>();
    }

    public static Set<Long> findCases(String searchValue, CriteriaBuilder cb, EntityManager em, ListAttribute<ClientContactDetails, ClientContactValueWithType> attribute) {
        final CriteriaQuery<Tuple> ccdQuery = cb.createTupleQuery();

        final Root<ClientContactDetails> ccdRoot = ccdQuery.from(ClientContactDetails.class);

        final ListJoin<ClientContactDetails, ClientContactValueWithType> valueJoin = ccdRoot.join(attribute);

        String value = prepareSearchValue(searchValue);
        ccdQuery.where(cb.like(prepareDatabaseValue(cb, valueJoin.get(ClientContactValueWithType_.value)), value));

        final Join<ClientContactDetails, Case> mainCaseJoin = ccdRoot.join(ClientContactDetails_.mainCase, JoinType.LEFT);
        final Join<ClientContactDetails, Case> contactCaseJoin = ccdRoot.join(ClientContactDetails_.contactCase, JoinType.LEFT);
        final Join<ClientContactDetails, Case> otherCaseJoin = ccdRoot.join(ClientContactDetails_.otherCase, JoinType.LEFT);

        ccdQuery.multiselect(
                mainCaseJoin.get(Case_.id).alias("mainCase"),
                contactCaseJoin.get(Case_.id).alias("contactCase"),
                otherCaseJoin.get(Case_.id).alias("otherCase")
        ).distinct(true);

        // collect all cases that are linked to the matched contacts:
        Set<Long> caseIds = new HashSet<>();
        for (Tuple t : em.createQuery(ccdQuery).getResultList()) {

            for (String linkToCase : linksToCase) {
                final Long caseId = t.get(linkToCase, Long.class);
                if (caseId != null) {
                    caseIds.add(caseId);
                }
            }

        }
        return caseIds;
    }

    public static Set<Long> findCaseByPostCode(String searchVal, CriteriaBuilder cb, EntityManager em, Object o) {

        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Case> root = query.from(Case.class);

        final Join<Case, ClientContactDetails> clientJoin = root.join(Case_.client);

        query.where(cb.equal(clientJoin.get(ClientContactDetails_.postcode), "1191 CK"));

        query.select(root.get(Case_.id));

        final List<Long> resultList = em.createQuery(query).getResultList();

        Set<Long> result = new HashSet<>();
        result.addAll(resultList);

        return result;
    }

    //    public static Set<Long> findCases(String searchValue, CriteriaBuilder cb, EntityManager em, ListAttribute<ClientContactDetails, ClientContactValueWithType> attribute) {
//
//
//        static String prepareSearchValue(String value) {
//        String searchValue = "%" + value.trim().replace(" ", "").replace("-", "") + "%";
//        return searchValue.toLowerCase();
//    }
    static String prepareSearchValue(String value) {
        String searchValue = "%" + value.trim().replace(" ", "").replace("-", "") + "%";
        return searchValue.toLowerCase();
    }

    // remove space and hyphen characters
    private static Expression<String> prepareDatabaseValue(CriteriaBuilder criteriaBuilder, Expression<String> fieldValue) {
        return criteriaBuilder.lower(
                criteriaBuilder.function("replace", String.class,
                        criteriaBuilder.function("replace", String.class,
                                fieldValue, criteriaBuilder.literal("-"), criteriaBuilder.literal("")
                        ), criteriaBuilder.literal(" "), criteriaBuilder.literal("")
                ));
    }
}
