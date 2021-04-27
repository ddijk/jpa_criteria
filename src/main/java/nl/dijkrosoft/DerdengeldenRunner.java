package nl.dijkrosoft;

import nl.bytesoflife.clienten.service.accountview.finance.DerdengeldenHelper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class DerdengeldenRunner {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            List<Long> folderId = Arrays.asList(1313L, 1616L);
            Query q = em.createNativeQuery("select mc.id from mcase mc join financial_data fd on fd.proj_code=mc.dossiernummer where fd.derdengelden_saldo > 0.004 and mc.folder_id in ?1 ");
            q.setParameter(1, folderId);
            Set<Long> matchedCases =   q.getResultList().stream().mapToLong(o-> ((BigInteger) o).longValue()).boxed().collect(toSet());


            System.out.println("Size is "+ matchedCases.size());
            for ( Object o :matchedCases) {
                System.out.println(o);
            }


//            Long number = getNumberOfItemsWithDerdengelden(em);

//            System.out.println("number:"+ number);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }

    private static Long getNumberOfItemsWithDerdengelden(EntityManager em ) {
        List<Long> folderId = Arrays.asList(1313L, 1616L);
        final Query nativeQuery = em.createNativeQuery("select  count(mc.id) from mcase mc join financial_data fd on mc.dossiernummer = fd.proj_code where folder_id in (?1) and abs(fd.derdengelden_saldo) > 0.004");
        nativeQuery.setParameter(1, folderId);

        final List resultList = nativeQuery.getResultList();

        if (resultList.size() > 0) {
            return  ((BigInteger) resultList.get(0)).longValue();
        } else {
            return 0L;
        }
    }

    private static void getDerdengelden(EntityManager em) {
        final Double derdengelden = DerdengeldenHelper.getDerdengelden(em, "18113137");

        System.out.println("Derdengelden: "+ derdengelden);
    }

}
