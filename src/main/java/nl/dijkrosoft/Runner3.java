package nl.dijkrosoft;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bytesoflife.clienten.CasesResponse;
import nl.bytesoflife.clienten.Zaken;
import nl.bytesoflife.clienten.cases.*;
import nl.bytesoflife.clienten.data.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static nl.dijkrosoft.JPARunner.*;

public class Runner3 {

    public static void main(String[] args) throws IOException {

        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CasesResponse cr = testCaseListRefactoring(em, cb);

            ObjectMapper om = new ObjectMapper();
            om.writerWithDefaultPrettyPrinter().writeValue(System.out, cr);

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

    private static CasesResponse testCaseListRefactoring(EntityManager em, CriteriaBuilder cb) {

        final CriteriaQuery<Tuple> tupleQuery = cb.createTupleQuery();
        final Root<Case> root = tupleQuery.from(Case.class);

        Join<Case, AccountviewProject> projectJoin = root.join(Case_.accountviewProject, JoinType.LEFT);
        Join<Case, Folder> folderJoin = root.join(Case_.folder);
        Join<Case, ClientContactDetails> clientJoin = root.join(Case_.client, JoinType.LEFT);


        // contactClient
        Join<Case, ClientContactDetails> contactClientJoin = root.join(Case_.contactClient, JoinType.LEFT);
//        final ListJoin<ClientContactDetails, ClientContactValueWithType> emailJoinContactClient = contactClientJoin.join(ClientContactDetails_.email, JoinType.LEFT);

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

                folderJoin.get(Folder_.name).alias("folderName"),

                clientJoin.get(ClientContactDetails_.straatnaam).alias("straatnaam"),
                clientJoin.get(ClientContactDetails_.huisnummer).alias("huisnummer"),
                clientJoin.get(ClientContactDetails_.toevoeging).alias("toevoeging"),
                clientJoin.get(ClientContactDetails_.postcode).alias("postcode"),
                clientJoin.get(ClientContactDetails_.woonplaats).alias("woonplaats"),
                clientJoin.get(ClientContactDetails_.defaultContact ).alias("defaultContact"),
                clientJoin.get(ClientContactDetails_.geslacht ).alias("geslacht"),
                clientJoin.get(ClientContactDetails_.naam ).alias("mainContactNaam"),
//                emailJoinClient.get(ClientContactValueWithType_.value).alias("mainContactEmail"),
//                telefoonJoinClient.get(ClientContactValueWithType_.value).alias("mainContactTelefoon"),
//
//                emailJoinContactClient.get(ClientContactValueWithType_.value),
                contactClientJoin.get(ClientContactDetails_.id).alias("contactClientId"),
                projectJoin.get(AccountviewProject_.BLOK).alias("blok"),

                // tpa
                root.get(Case_.lastPaymentDate).alias("tpaLastPaymentDate"),
//                otherClientsJoin.get(ClientContactDetails_.naam).alias("tpaNaam"),
//                otherClientsJoin.get(ClientContactDetails_.defaultContact).alias("tpaDefaultContact"),
//                otherClientsJoin.get(ClientContactDetails_.instantie).alias("tpaInstantie"),
//                otherClientsJoin.get(ClientContactDetails_.postbus).alias("tpaPostbus"),
//                otherClientsJoin.get(ClientContactDetails_.postcodePostbus).alias("tpaPostcodePostbus"),
//                otherClientsJoin.get(ClientContactDetails_.plaatsnaamPostbus).alias("tpaPlaatsnaamPostbus"),
//                emailJoinOtherContact.get(ClientContactValueWithType_.value).alias("tpaContactEmail"),
//                telefoonJoinOtherContact.get(ClientContactValueWithType_.value).alias("tpaContactTelefoon"),

                archiveCheckJoin.get(CaseArchiveCheck_.praktijkhoofdCheckedDate).alias("praktijkhoofdCheckedDate"),
                archiveCheckJoin.get(CaseArchiveCheck_.needsPraktijkhoofdChecked).alias("needsPraktijkhoofdChecked")
//                folderJoin.get("accountviewCompany").alias("company")

        );

        Predicate whereClause = getPred(cb, root.get("folder").get("id"), selectedPraktijken, authPraktijken);
        final String searchTerm = "";// filterz.getName();
        if (searchTerm != null && searchTerm.length() > 0) {
//            .info(String.format("Searchterm '%s'", searchTerm));
            final String likeSearchTerm = String.format("%%%s%%", searchTerm);
            final Predicate searchTermPredicate = cb.or(
                    cb.like(root.get("dossiernummer"), likeSearchTerm));

            whereClause = cb.and(whereClause, searchTermPredicate);
        }

        // alleen otherClientsJoin van typeDerde=wederpartij => filter in conversie naar JSON


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
        int pageSize = 2000;
        final TypedQuery<Tuple> emQuery = em.createQuery(tupleQuery);
        emQuery.setMaxResults(pageSize);
        emQuery.setFirstResult(0);


        List<Tuple> result = emQuery.getResultList();

        System.out.println("Aantal: "+ result.size());
        List<CaseListItem> caseList = new ArrayList<>();
        for ( Tuple t : result) {
            //System.out.println(String.format("case id: '%d', dossiernummer:'%s' telefoon: '%s', email: '%s'",t.get("id"),t.get("dossiernummer"),t.get("mainContactTelefoon"),t.get("mainContactEmail")));
            final Long id = t.get("id", Long.class);
            System.out.println(String.format("case id: '%d', dossiernummer:'%s' ", id,t.get("dossiernummer")));

            final Long contactClientId = t.get("contactClientId",Long.class);
            // zoek de otherContacts ( tpa ) erbij:
            List<Contact> emailList= Collections.emptyList();
            List<Contact> telefoonList= Collections.emptyList();
            List<OtherClient> otherClients = Collections.emptyList();
            CaseListItem item = new CaseListItem(t.get("name", String.class),
                    t.get("dossiernummer", String.class),
                    t.get("leadnummer", Long.class),
                    t.get("bijzondereStatus", String.class),
                    t.get("datumToedracht", String.class),
                     new CaseFolder( t.get("folderName", String.class)),
                        new Client(t.get("defaultContact", DefaultContact.class),
                                t.get("geslacht", Geslacht.class),
                                t.get("mainContactNaam", String.class ),
                                emailList,
                                telefoonList,
                                t.get("straatnaam", String.class),
                                t.get("huisnummer", String.class),
                                t.get("toevoeging", String.class),
                                t.get("postcode", String.class),
                                t.get("woonplaats", String.class)),
                                new ContactClient(emailList),
                                otherClients
                                );
            final Boolean blok = t.get("blok", Boolean.class);
            if ( blok != null) {
                item.setAccountviewProject(new Project(blok));
            }
            final Date praktijkhoofdCheckedDate = t.get("praktijkhoofdCheckedDate", Date.class);
            final Boolean needsPraktijkhoofdChecked = t.get("needsPraktijkhoofdChecked", Boolean.class);

            if ( praktijkhoofdCheckedDate != null && needsPraktijkhoofdChecked != null ) {

                final CaseArchiveChk caseArchiveCheck = new CaseArchiveChk(praktijkhoofdCheckedDate, needsPraktijkhoofdChecked);
                item.setCaseArchiveCheck(caseArchiveCheck);
            }
            caseList.add(item);

        }

        //List<CaseListItem> caseList =  result.stream().map(t -> convert(t)).collect(Collectors.toList());
        String filters = "{blah}";
        return new CasesResponse(new Zaken<>(0, 1, 5, caseList), filters);

    }

    private static CaseListItem convert(Tuple t) {

        /*
          List<Contact> emailList = new ArrayList<>();
        emailList.add(new Contact("d@d", true));
        List<Contact> telelfoonList = new ArrayList<>();
        OtherClient otherClient1 = new OtherClient("naam", "def", emailList, "instantie", "postbus", "pcPostbus", "plaats",  telelfoonList, "blah");
        OtherClient otherClient2 = new OtherClient("naam2", "def2", emailList, "instantie", "postbus", "pcPostbus", "plaats",  telelfoonList, "wederpartij");

        List<CaseListItem> caseListItems = new ArrayList<>();
        CaseListItem caseListItem =createCaseListItem(Arrays.asList(otherClient1, otherClient2));

        CaseArchiveChk caseArchiveChk = new CaseArchiveChk(new Date(), true);
        caseListItem.setCaseArchiveCheck(caseArchiveChk);

        Project proj=new Project(true);
        caseListItem.setAccountviewProject(proj);

        Client client = new Client("def_contact", "man", "name", emailList, telelfoonList, "straat", "nr", "toev", "pc", "plaats");
        caseListItem.setClient(client);

        ContactClient contactClient = new ContactClient(emailList);
        caseListItem.setContactClient(contactClient);
         */

//        CaseListItem caseListItem = new CaseListItem(t.get("name"),
//                t.get("dossiernummer"),
//                t.get("leadnummer"),
//                t.get("bijzondereStatus"),
//                t.get("datumToedracht"),
//                new CaseFolder(t.get("folderName", String.class)),
//                new Client(t.get("defaultContact"), t.get("geslacht"), t.get("mainContactNaam"), ),
//                )
        return null;
    }
}
