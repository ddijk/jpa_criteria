package nl.dijkrosoft.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bytesoflife.clienten.CasesResponse;
import nl.bytesoflife.clienten.data.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.*;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.dijkrosoft.function.Helper.findCaseByPostCode;
import static nl.dijkrosoft.function.Helper.findCases;

public class MyController {

    EntityManager em;

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            doit(em, Helper::findCases);

            doit2(em, Helper::findCaseByPostCode);

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

    private static void doit(EntityManager em, MyFunc myFunc) {

        System.out.println("Stap 1");

        CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<ClientContactDetails> root = query.from(ClientContactDetails.class);
        Set<Long> caseIds =  myFunc.method("test@example.com", cb, em, ClientContactDetails_.email);
        System.out.println("Stap 2: "+ caseIds.size());


    }

    private static void doit2(EntityManager em , MyFunc myFunc) {
        System.out.println("Stap 1");

        CriteriaBuilder cb = em.getCriteriaBuilder();

        final CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Set<Long> resultList = myFunc.method("2411", cb, em, null);

        System.out.println("Aantal hits: "+ resultList.size());
        for ( Long c : resultList) {
            System.out.println("case met id "+ c);
        }

//        Set<Long> caseIds = myFunc.method("2411LN", cb, em, root.get(ClientContactDetails_.postcode));
//        System.out.println("Stap 2: "+ caseIds);
    }

//    private static Set<Long> findCases(String s, /*Path<ChatDetails>*/ Object chatDetailsPath) {
//
//        return new HashSet<>();
//    }
}
