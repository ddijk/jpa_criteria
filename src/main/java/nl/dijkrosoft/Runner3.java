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
//            CasesResponse cr = testCaseListRefactoring(em, cb);

            ObjectMapper om = new ObjectMapper();
//            om.writerWithDefaultPrettyPrinter().writeValue(System.out, cr);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) emf.close();
        }

    }
}

