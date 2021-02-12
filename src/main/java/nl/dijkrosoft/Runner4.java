package nl.dijkrosoft;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bytesoflife.clienten.CasesResponse;
import nl.bytesoflife.clienten.Zaken;
import nl.bytesoflife.clienten.cases.*;
import nl.bytesoflife.clienten.data.*;
import nl.bytesoflife.clienten.finance.praktijk.PageHelper;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;

public class Runner4 {
    private static String filters = "{ folderId:[1313], name: \"\"}";
    private static int page = 0;
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

//            CasesResponse cr = runQuery(em);
            long caseId = 36;
            getOtherCaseContactDetails(caseId, em);
//            ObjectMapper om = new ObjectMapper();
//            om.writerWithDefaultPrettyPrinter().writeValue(System.out, cr);

        } catch (Exception ex ) {
            ex.printStackTrace();
        } finally {
            if (em != null)
            {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }

    private static CasesResponse runQuery(EntityManager em) {
        int pageSize = 200;
        Filters filterz = Filters.create(filters);
        final List<Integer> selectedPraktijken = filterz.getFolderId();
        final List<Long> allowedPraktijken = Arrays.asList(1313L, 1L);// ControllerHelper.getAllowedFolderIds(folderRepository.findAll(), user);

        final CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        final Root<Case> caseRoot = countQuery.from(Case.class);
        Predicate praktijkenFilter = createPraktijkenFilter(cb, caseRoot.get(Case_.folder).get("id"), selectedPraktijken, allowedPraktijken);
        countQuery.where(praktijkenFilter);
        countQuery.select(cb.count(caseRoot));

        long totalElements = em.createQuery(countQuery).getSingleResult();

        caseRoot.join("accountviewProject");
        // pageNr is 0 based
        int calculatedPageNr = PageHelper.calculatePageIndex(page, totalElements, pageSize);
        long numPages = PageHelper.calculateNumberOfPages(totalElements, pageSize);
        if (calculatedPageNr == -1) {
            return new CasesResponse(new Zaken(numPages - 1, numPages, totalElements, Collections.emptyList()), filters);
        }

        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        final Root<Case> root = tupleQuery.from(Case.class);

        Join<Case, AccountviewProject> projectJoin = root.join(Case_.accountviewProject, JoinType.LEFT);
        Join<Case, Folder> folderJoin = root.join(Case_.folder);
        Join<Case, ClientContactDetails> clientJoin = root.join(Case_.client, JoinType.LEFT);


        // contact_case
        Join<Case, ClientContactDetails> contactClientJoin = root.join(Case_.contactClient, JoinType.LEFT);

        // other_case
//        Join<Case, ClientContactDetails> otherContactJoin = root.join(Case_.otherClients, JoinType.LEFT);




        Join<Case,CaseArchiveCheck> archiveCheckJoin = root.join(Case_.caseArchiveCheck, JoinType.LEFT);
        tupleQuery.multiselect(
                root.get(Case_.id).alias("id"),
                root.get(Case_.name).alias("name"),
                root.get(Case_.dossiernummer).alias("dossiernummer"),
                root.get(Case_.leadnummer).alias("leadnummer"),
                root.get(Case_.bijzondereStatus).alias("bijzondereStatus"),
                root.get(Case_.datumToedracht).alias("datumToedracht"),
//                root.get("unreadCount").alias("unreadCount"), => from ChatService
                root.get(Case_.debtorBalanceAcountView).alias("saldoAV"),
                root.get(Case_.debtorBalance).alias("saldo"),

                folderJoin.get(Folder_.name).alias("folderName"),

                clientJoin.get(ClientContactDetails_.straatnaam).alias("straatnaam"),
                clientJoin.get(ClientContactDetails_.huisnummer).alias("huisnummer"),
                clientJoin.get(ClientContactDetails_.toevoeging).alias("toevoeging"),
                clientJoin.get(ClientContactDetails_.postcode).alias("postcode"),
                clientJoin.get(ClientContactDetails_.woonplaats).alias("woonplaats"),
                clientJoin.get(ClientContactDetails_.defaultContact).alias("defaultContact"),
                clientJoin.get(ClientContactDetails_.geslacht).alias("geslacht"),
                clientJoin.get(ClientContactDetails_.naam).alias("mainContactNaam"),
//                emailJoinClient.get(ClientContactValueWithType_.value).alias("mainContactEmail"),
//                telefoonJoinClient.get(ClientContactValueWithType_.value).alias("mainContactTelefoon"),
//
//                emailJoinContactClient.get(ClientContactValueWithType_.value),
                contactClientJoin.get(ClientContactDetails_.id).alias("contactClientId"),
                projectJoin.get(AccountviewProject_.BLOK).alias("blok"),

                // tpa
                root.get(Case_.lastPaymentDate).alias("tpaLastPaymentDate"),
//                otherContactJoin.get(ClientContactDetails_.id).alias("tpaId"),
//                otherContactJoin.get(ClientContactDetails_.naam).alias("tpaNaam"),
//                otherContactJoin.get(ClientContactDetails_.defaultContact).alias("tpaDefaultContact"),
//                otherContactJoin.get(ClientContactDetails_.instantie).alias("tpaInstantie"),
//                otherContactJoin.get(ClientContactDetails_.postbus).alias("tpaPostbus"),
//                otherContactJoin.get(ClientContactDetails_.postcodePostbus).alias("tpaPostcodePostbus"),
//                otherContactJoin.get(ClientContactDetails_.plaatsnaamPostbus).alias("tpaPlaatsnaamPostbus"),
//                otherContactJoin.get(ClientContactDetails_.typeDerde).alias("tpaTypeDerde"),
//                emailJoinOtherContact.get(ClientContactValueWithType_.value).alias("tpaContactEmail"),
//                telefoonJoinOtherContact.get(ClientContactValueWithType_.value).alias("tpaContactTelefoon"),

                archiveCheckJoin.get(CaseArchiveCheck_.praktijkhoofdCheckedDate).alias("praktijkhoofdCheckedDate"),
                archiveCheckJoin.get(CaseArchiveCheck_.needsPraktijkhoofdChecked).alias("needsPraktijkhoofdChecked")
//                folderJoin.get("accountviewCompany").alias("company")

        );

        Predicate whereClause = praktijkenFilter;
        final String searchTerm = filterz.getName();
        if (searchTerm != null && searchTerm.length() > 0) {
//            logger.info(String.format("Searchterm '%s'", searchTerm));
            final String likeSearchTerm = String.format("%%%s%%", searchTerm);
            final Predicate searchTermPredicate = cb.or(
                    cb.like(root.get(Case_.dossiernummer), likeSearchTerm),
                    cb.like(cb.lower(root.get(Case_.name)), likeSearchTerm.toLowerCase())
            );

            whereClause = cb.and(whereClause, searchTermPredicate);
        }

        // alleen otherClientsJoin van typeDerde=wederpartij => filter in conversie naar JSON
//        whereClause = cb.and(whereClause, cb.equal(otherContactJoin.get(ClientContactDetails_.typeDerde), "Wederpartij"));

//        whereClause = cb.and(whereClause, cb.equal(root.get(Case_.id), cb.any(subquery)));

//        whereClause = cb.and(whereClause, cb.equal(root.get(Case_.id), 61037));
        tupleQuery.where(whereClause);

//        if (sortableColumns.contains(sort)) {
//
//            if ( sort.indexOf(":") == -1) {
//                if ("ASC".equalsIgnoreCase(direction)) {
//
//                    tupleQuery.orderBy(cb.asc(projectJoin.get(sort)));
//                } else {
//                    tupleQuery.orderBy(cb.desc(projectJoin.get(sort)));
//
//                }
//            } else {
//                if ("ASC".equalsIgnoreCase(direction)) {
//
//                    tupleQuery.orderBy(cb.asc(root.get(sort.split(":", 2)[1])));
//                } else {
//                    tupleQuery.orderBy(cb.desc(root.get(sort.split(":", 2)[1])));
//
//                }
//            }
//
//        }
        tupleQuery.orderBy(cb.asc(root.get(Case_.id)));
        final TypedQuery<Tuple> emQuery = em.createQuery(tupleQuery);
        emQuery.setMaxResults(pageSize);
        emQuery.setFirstResult(calculatedPageNr * pageSize);

        List<Tuple> result = emQuery.getResultList();

        System.out.println("Aantal: " + result.size());
        List<CaseListItem> caseList = new ArrayList<>();
        for (Tuple t : result) {
            //System.out.println(String.format("case id: '%d', dossiernummer:'%s' telefoon: '%s', email: '%s'",t.get("id"),t.get("dossiernummer"),t.get("mainContactTelefoon"),t.get("mainContactEmail")));
            final Long id = t.get("id", Long.class);
            System.out.println(String.format("case id: '%d', dossiernummer:'%s' ", id, t.get("dossiernummer")));
            List<Contact> mainCaseEmailAddresses = getContactList(em, id, ClientContactDetails::getEmail);
            List<Contact> mainCaseTelefoonNrs = getContactList(em, id, ClientContactDetails::getTelefoon);
            final Long contactClientId = t.get("contactClientId", Long.class);

            List<Contact> contactCaseEmailAddresses = getContactList(em, contactClientId, ClientContactDetails::getEmail);

            System.out.println("Email addresses: "+ contactCaseEmailAddresses);

            // zoek de otherContacts ( tpa ) erbij:
//            Long tpaId = t.get("tpaId", Long.class );

//            List<Contact> emailList = getContactList(em, tpaId, ClientContactDetails::getEmail);
//            List<Contact> telefoonList = getContactList(em, tpaId, ClientContactDetails::getTelefoon);

            Long caseId = t.get("id", Long.class);

            OtherCaseContactDetails otherCaseContactDetails = getOtherCaseContactDetails(caseId, em);

            List<OtherCaseContactDetails> otherCaseContactDetailsList = new ArrayList<>();
            otherCaseContactDetailsList.add(otherCaseContactDetails);
            CaseListItem item = new CaseListItem(t.get("name", String.class),
                    t.get("dossiernummer", String.class),
                    t.get("leadnummer", Long.class),
                    t.get("bijzondereStatus", String.class),
                    t.get("datumToedracht", String.class),
                    new CaseFolder(t.get("folderName", String.class)),
                    new MainCaseContactDetails(t.get("defaultContact", DefaultContact.class),
                            t.get("geslacht", Geslacht.class),
                            t.get("mainContactNaam", String.class),
                            mainCaseEmailAddresses,
                            mainCaseTelefoonNrs,
                            t.get("straatnaam", String.class),
                            t.get("huisnummer", String.class),
                            t.get("toevoeging", String.class),
                            t.get("postcode", String.class),
                            t.get("woonplaats", String.class)),
                    new ContactCaseContactDetails(contactCaseEmailAddresses),
                    otherCaseContactDetailsList
            );
            final Boolean blok = t.get("blok", Boolean.class);
            if (blok != null) {
                item.setAccountviewProject(new Project(blok));
            }
            final Date praktijkhoofdCheckedDate = t.get("praktijkhoofdCheckedDate", Date.class);
            final Boolean needsPraktijkhoofdChecked = t.get("needsPraktijkhoofdChecked", Boolean.class);

            if (praktijkhoofdCheckedDate != null && needsPraktijkhoofdChecked != null) {

                final CaseArchiveChk caseArchiveCheck = new CaseArchiveChk(praktijkhoofdCheckedDate, needsPraktijkhoofdChecked);
                item.setCaseArchiveCheck(caseArchiveCheck);
            }
            caseList.add(item);

        }

        return new CasesResponse(new Zaken<>(calculatedPageNr, numPages, totalElements, caseList), filters);

    }

    private static OtherCaseContactDetails getOtherCaseContactDetails(Long caseId, EntityManager em) {


        CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();

        final Root<Case> caseRoot = tupleQuery.from(Case.class);

        final ListJoin<Case, ClientContactDetails> contactCaseJoin = caseRoot.join(Case_.otherClients);
        final ListJoin<ClientContactDetails, ClientContactValueWithType> valueJoin = contactCaseJoin.join(ClientContactDetails_.email);

        tupleQuery.where(cb.and(
                cb.equal(caseRoot.get(Case_.id), caseId),
                cb.equal(contactCaseJoin.get(ClientContactDetails_.typeDerde), "Wederpartij")
                )
        );
        tupleQuery.multiselect(valueJoin.get(ClientContactValueWithType_.value).alias("val"), valueJoin.get(ClientContactValueWithType_.isDefault).alias("isDef"));


//
//        final Query query = em.createQuery("Select ccd.naam, ccd.defaultContact, ccd.instantie from ClientContactDetails ccd where ccd.otherCase = ?1 and ccd.typeDerde = ?2");
//        query.setParameter(1, caseId);
//        query.setParameter(2, "Wederpartij");

        OtherCaseContactDetails otherCaseContactDetails = new OtherCaseContactDetails();
        for ( Tuple t : em.createQuery(tupleQuery).getResultList()) {
            System.out.println(String.format("value='%s', isDefault='%s'", t.get("val"), t.get("isDef")));
//                    otherCaseContactDetails.setNaam(o.);
        }
//                t.get("tpaNaam", String.class),
//                t.get("tpaDefaultContact", DefaultContact.class),
//                Collections.emptyList(),
//                t.get("tpaInstantie", String.class),
//                t.get("tpaPostbus", String.class),
//                t.get("tpaPostcodePostbus", String.class),
//                t.get("tpaPlaatsnaamPostbus", String.class),
//                Collections.emptyList(),
//                t.get("tpaTypeDerde", String.class)
//        );
        return otherCaseContactDetails;
    }

    private static List<Contact> getContactList(EntityManager em, Long contactClientId, Function<ClientContactDetails, List<ClientContactValueWithType>> valueRetriever) {
        final TypedQuery<ClientContactDetails> query = em.createQuery("Select c from ClientContactDetails  c where c.id=?1", ClientContactDetails.class);
        query.setParameter(1, contactClientId);

        List<Contact> result = new ArrayList<>();
        for (ClientContactDetails ccd : query.getResultList()) {
            System.out.println("here we go:" + ccd);
            result.addAll(valueRetriever.apply(ccd).stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));
        }
        return result;
    }
}
