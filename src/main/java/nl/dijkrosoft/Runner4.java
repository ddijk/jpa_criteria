package nl.dijkrosoft;

import nl.bytesoflife.clienten.CasesResponse;
import nl.bytesoflife.clienten.Zaken;
import nl.bytesoflife.clienten.cases.*;
import nl.bytesoflife.clienten.data.*;
import nl.bytesoflife.clienten.finance.praktijk.PageHelper;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;
import static nl.dijkrosoft.JPARunner.authPraktijken;
import static nl.dijkrosoft.JPARunner.selectedPraktijken;
import static nl.dijkrosoft.JPQL_Runner.archivedOnly;

public class Runner4 {
    private static String filters = "{ folderId:[1313], name: \"\"}";
    private static int page = 0;

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            Filters filterz = Filters.create(filters);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

            final Root<Case> caseRoot = countQuery.from(Case.class);
            Predicate praktijkenFilter = createPraktijkenFilter(cb, caseRoot.get(Case_.folder).get(Folder_.id), selectedPraktijken, authPraktijken);

            if (filterz.getIsArchived() != null && (boolean) filterz.getIsArchived()) {
                countQuery.where(cb.and(praktijkenFilter, archivedOnly(cb, caseRoot)));
            } else {
                countQuery.where(cb.and(praktijkenFilter, archivedOnly(cb, caseRoot).not()));
            }

            countQuery.select(cb.count(caseRoot));

            long totalElements = em.createQuery(countQuery).getSingleResult();

//            caseRoot.join("accountviewProject");


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
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
            return new CasesResponse(new Zaken(numPages - 1, numPages, totalElements, Collections.emptyList()), filters, null);
        }

        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        final Root<Case> root = tupleQuery.from(Case.class);

        Join<Case, AccountviewProject> projectJoin = root.join(Case_.accountviewProject, JoinType.LEFT);
        Join<Case, Folder> folderJoin = root.join(Case_.folder);
        Join<Case, ClientContactDetails> clientJoin = root.join(Case_.client, JoinType.LEFT);

        Join<Case, ClientContactDetails> contactClientJoin = root.join(Case_.contactClient, JoinType.LEFT);

        Join<Case, CaseArchiveCheck> archiveCheckJoin = root.join(Case_.caseArchiveCheck, JoinType.LEFT);
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
                root.get(Case_.lastPaymentDate).alias("lastPaymentDate"),

                folderJoin.get(Folder_.name).alias("folderName"),

                clientJoin.get(ClientContactDetails_.straatnaam).alias("straatnaam"),
                clientJoin.get(ClientContactDetails_.huisnummer).alias("huisnummer"),
                clientJoin.get(ClientContactDetails_.toevoeging).alias("toevoeging"),
                clientJoin.get(ClientContactDetails_.postcode).alias("postcode"),
                clientJoin.get(ClientContactDetails_.woonplaats).alias("woonplaats"),
                clientJoin.get(ClientContactDetails_.defaultContact).alias("defaultContact"),
                clientJoin.get(ClientContactDetails_.geslacht).alias("geslacht"),
                clientJoin.get(ClientContactDetails_.naam).alias("mainContactNaam"),

                contactClientJoin.get(ClientContactDetails_.id).alias("contactClientId"),

                projectJoin.get(AccountviewProject_.BLOK).alias("blok"),

                archiveCheckJoin.get(CaseArchiveCheck_.praktijkhoofdCheckedDate).alias("praktijkhoofdCheckedDate"),
                archiveCheckJoin.get(CaseArchiveCheck_.needsPraktijkhoofdChecked).alias("needsPraktijkhoofdChecked")

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
            System.out.println("****** id " + id);
            System.out.println(String.format("case id: '%d', dossiernummer:'%s' ", id, t.get("dossiernummer")));
            List<Contact> mainCaseEmailAddresses = getMainCaseList(em, id, ClientContactDetails::getEmail);
            System.out.println("mainCaseEmailAddresses:" + mainCaseEmailAddresses);
            List<Contact> mainCaseTelefoonNrs = getMainCaseList(em, id, ClientContactDetails::getTelefoon);
            System.out.println("mainCaseTelefoonNrs:" + mainCaseTelefoonNrs);
            final Long contactClientId = t.get("contactClientId", Long.class);

            List<Contact> contactCaseEmailAddresses = getContactCaseList(em, id, ClientContactDetails::getEmail);

            System.out.println("Contact case: Email addresses: " + contactCaseEmailAddresses);

            OtherCaseContactDetails otherCaseContactDetails = getOtherCaseContactDetails(id, em);

            List<OtherCaseContactDetails> otherCaseContactDetailsList = new ArrayList<>();
            otherCaseContactDetailsList.add(otherCaseContactDetails);
            //  public CaseListItem(Long id, String name, String dossiernummer, Long leadnummer, String bijzondereStatus, String datumToedracht, CaseFolder folder, MainCaseContactDetails mainCaseContactDetails, ContactCaseContactDetails contactCaseContactDetails, List<OtherCaseContactDetails> otherCaseContactDetails, LocalDate lastPaymentDate, Double saldo, Double saldoAV, Long unreadCount) {
            //
            CaseListItem item = new CaseListItem(1L,
                    t.get("name", String.class),
                    t.get("dossiernummer", String.class),
                    t.get("leadnummer", Long.class),
                    t.get("bijzondereStatus", String.class),
                    t.get("datumToedracht", String.class),
                    new CaseFolder(t.get("folderName", String.class), "dummy"),
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
                    otherCaseContactDetailsList,

                    t.get("lastPaymentDate", LocalDate.class),
                    t.get("saldo", Double.class),
                    t.get("saldoAV", Double.class),
                    new Date(), 0L
            );
            final Boolean blok = t.get("blok", Boolean.class);
            if (blok != null) {
                item.setAccountviewProject(new Project(blok, "type", "ref"));
            }
            final Date praktijkhoofdCheckedDate = t.get("praktijkhoofdCheckedDate", Date.class);
            final Boolean needsPraktijkhoofdChecked = t.get("needsPraktijkhoofdChecked", Boolean.class);

            if (praktijkhoofdCheckedDate != null && needsPraktijkhoofdChecked != null) {

                final CaseArchiveChk caseArchiveCheck = new CaseArchiveChk(praktijkhoofdCheckedDate, needsPraktijkhoofdChecked);
                item.setCaseArchiveCheck(caseArchiveCheck);
            }
            caseList.add(item);

        }

        return new CasesResponse(new Zaken<>(calculatedPageNr, numPages, totalElements, caseList), filters, null);

    }

    private static OtherCaseContactDetails getOtherCaseContactDetails(Long caseId, EntityManager em) {


        final TypedQuery<ClientContactDetails> query = em.createQuery("Select c from ClientContactDetails  c where c.otherCase.id=?1 and c.typeDerde='Wederpartij'", ClientContactDetails.class);
        query.setParameter(1, caseId);

        List<Contact> emailLijst = new ArrayList<>();
        List<Contact> telefoonLijst = new ArrayList<>();
        final List<ClientContactDetails> resultList = query.getResultList();
        if (resultList.size() > 0) {

            final ClientContactDetails ccd = resultList.get(0);
            System.out.println("here we go getOtherCaseList:" + ccd);
            emailLijst.addAll(ccd.getEmail().stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));
            telefoonLijst.addAll(ccd.getTelefoon().stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));

            return new OtherCaseContactDetails(ccd.getNaam(), ccd.getDefaultContact(), emailLijst, ccd.getInstantie(),
                    ccd.getPostbus(), ccd.getPostcodePostbus(), ccd.getPlaatsnaamPostbus(), telefoonLijst, ccd.getTypeDerde(), "ref");
        } else {
            return null;
        }


    }

    private static List<Contact> getContactCaseList(EntityManager em, Long caseId, Function<ClientContactDetails, List<ClientContactValueWithType>> valueRetriever) {
        final TypedQuery<ClientContactDetails> query = em.createQuery("Select c from ClientContactDetails  c where c.contactCase.id=?1 and c.typeDerde='Wederpartij'", ClientContactDetails.class);
        query.setParameter(1, caseId);

        List<Contact> result = new ArrayList<>();
        for (ClientContactDetails ccd : query.getResultList()) {
            System.out.println("here we go getContactCaseList:" + ccd);
            result.addAll(valueRetriever.apply(ccd).stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));
        }
        return result;
    }

    private static List<Contact> getMainCaseList(EntityManager em, Long caseId, Function<ClientContactDetails, List<ClientContactValueWithType>> valueRetriever) {
        final TypedQuery<ClientContactDetails> query = em.createQuery("Select c from ClientContactDetails  c where c.mainCase.id=?1", ClientContactDetails.class);
        query.setParameter(1, caseId);

        List<Contact> result = new ArrayList<>();
        for (ClientContactDetails ccd : query.getResultList()) {
            System.out.println("here we go getMainCaseList:" + ccd);
            result.addAll(valueRetriever.apply(ccd).stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));
        }
        return result;
    }

    private static List<Contact> getOtherCaseList(EntityManager em, Long caseId, Function<ClientContactDetails, List<ClientContactValueWithType>> valueRetriever) {
        final TypedQuery<ClientContactDetails> query = em.createQuery("Select c from ClientContactDetails  c where c.otherCase.id=?1 and c.typeDerde='Wederpartij'", ClientContactDetails.class);
        query.setParameter(1, caseId);

        List<Contact> result = new ArrayList<>();
        for (ClientContactDetails ccd : query.getResultList()) {
            System.out.println("here we go getOtherCaseList:" + ccd);
            result.addAll(valueRetriever.apply(ccd).stream().map(e -> new Contact(e.getValue(), e.getIsDefault())).collect(Collectors.toList()));
        }
        return result;
    }
}
