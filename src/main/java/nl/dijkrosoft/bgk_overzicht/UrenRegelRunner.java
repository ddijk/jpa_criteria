package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.cases.overzicht.bgk.LatestBookingsRequest;
import nl.bytesoflife.clienten.cases.overzicht.bgk.LatestDate;
import nl.bytesoflife.clienten.data.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nl.bytesoflife.clienten.data.Filters.createPraktijkenFilter;

public class UrenRegelRunner {

    public static final Integer FOLDER_ID = 1;
    private static int chunkSize = 100;

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();


            LatestBookingsRequest request = new LatestBookingsRequest();
            request.setChunkIndex(0);
            request.setWerkCodes(Arrays.asList("018"));
            request.setFolders(Arrays.asList(FOLDER_ID));
            List<LatestDate> dates = doit(em, request);

            System.out.println("Aantal dates is " + dates.size());
            System.out.println(dates);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
                System.out.println("About to close em");
                em.close();
            }
            if (emf != null) {
                System.out.println("About to close emf");
                emf.close();
            }
        }

        System.out.println("Done...");
    }

    private static List<LatestDate> doit(EntityManager em, LatestBookingsRequest request) {
        int chunkIndex = request.getChunkIndex();
        final List<Long> allowedPraktijken = getAllowedPraktijken();

        final List<Integer> selectedPraktijken = request.getFolders();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Tuple> cbQuery = cb.createTupleQuery();
        final Root<Case> caseRoot = cbQuery.from(Case.class);
        caseRoot.join(Case_.accountviewProject);

//        final Root<UrenRegel> urenRegelRoot = cbQuery.from(UrenRegel.class);


        Predicate praktijkenFilter = createPraktijkenFilter(cb, caseRoot.get(Case_.folder).get(Folder_.id), selectedPraktijken, allowedPraktijken);
//        Predicate dossierJoin = cb.equal(caseRoot.get(Case_.dossiernummer), urenRegelRoot.get(UrenRegel_.dossier));
//        Predicate codeClause = urenRegelRoot.get(UrenRegel_.code).in(request.getWerkCodes());
        cbQuery.where(praktijkenFilter);
        // select ur.date_added from uren_regel ur join mcase mc on mc.dossiernummer= ur.dossier and mc.id=51231 and ur.code in ('014','023') order by ur.date_added desc

        cbQuery.multiselect(caseRoot.get(Case_.id).alias("caseId"), caseRoot.get(Case_.dossiernummer).alias("dossier"));
//        cbQuery.groupBy(caseRoot.get(Case_.id));
        cbQuery.orderBy(cb.asc(caseRoot.get(Case_.id)));
        final TypedQuery<Tuple> query = em.createQuery(cbQuery);
        query.setMaxResults(chunkSize);
        query.setFirstResult(chunkIndex * chunkSize);


        List<LatestDate> latestDates = new ArrayList<>();
        List<Tuple> resultList = query.getResultList();
        System.out.println("Aantal results is " + resultList.size());
        for (Tuple tuple : resultList) {

            Query datumQuery = createQueryForLatestWerkCodeBookings(em, tuple.get("dossier", String.class), request.getWerkCodes());

//            System.out.println( datumQuery.getResultList());

            latestDates.add(new LatestDate(tuple.get("caseId", Long.class), datumQuery.getResultList().size() == 0 ? null : (LocalDateTime) datumQuery.getResultList().get(0)));

        }
        return latestDates;
    }

//    private static LocalDateTime getLatestDate(EntityManager em, String dossier, List<String> werkCodes) {
//
//        TypedQuery<LocalDateTime> query = em.createQuery("select max(ur.date_added) from UrenRegel  ur where ur.dossier=?1 and code in ?2 ", LocalDateTime.class);
//        query.setParameter(1, dossier);
//        query.setParameter(2, werkCodes);
//
//        List<LocalDateTime> resultList = query.getResultList();
//
//        return resultList.size()==0?null:resultList.get(0);
//
//
//    }

    private static List<Long> getAllowedPraktijken() {
        return Arrays.asList(FOLDER_ID.longValue());
    }


//    private static TypedQuery<LocalDateTime> createQueryForLatestWerkCodeBookings2(EntityManager em, String dossier, List<String> werkcodes) {
////        final TypedQuery<LocalDateTime> nativeQuery = em.createNamedQuery("LastDateForUrenCodes", LocalDateTime.class);
//        final Query nativeQuery = em.createNamedQuery("LastDateForUrenCodes");
//        nativeQuery.setParameter(1, dossier);
//        nativeQuery.setParameter(2, werkcodes);
//        return nativeQuery;
//    }

    private static Query createQueryForLatestWerkCodeBookings(EntityManager em, String dossier, List<String> werkcodes) {
//        final TypedQuery<LocalDateTime> nativeQuery = em.createNamedQuery("LastDateForUrenCodes", LocalDateTime.class);
        final Query nativeQuery = em.createNamedQuery("LastDateForUrenCodes");
        nativeQuery.setParameter(1, dossier);
        nativeQuery.setParameter(2, werkcodes);
        return nativeQuery;
    }
}
