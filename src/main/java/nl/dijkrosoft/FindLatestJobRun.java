package nl.dijkrosoft;

import nl.bytesoflife.clienten.service.accountview.finance.FinancialData;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialData_;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class FindLatestJobRun {
    // select jr.end_time from job_run jr join job j on jr.job_id = j.id join job_type jt on j.job_type_id = jt.id where jt.name= 'Actualiseer alle Financiële Overzichten' and jr.status='COMPLETED' order by jr.start_time desc limit 1;
    //
    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            final Query nativeQuery = em.createNativeQuery("select jr.end_time from job_run jr join job j on jr.job_id = j.id join job_type jt on j.job_type_id = jt.id where jt.name= 'Actualiseer alle Financiële Overzichten' and jr.status='COMPLETED' order by jr.start_time desc limit 1");

            final Object singleResult = nativeQuery.getSingleResult();

            Timestamp time = (Timestamp) singleResult;

            System.out.println("total:"+time.toLocalDateTime());




        } finally {
            if (em != null)
            {
                em.close();
            }
            if (emf != null) emf.close();
        }
    }
}
