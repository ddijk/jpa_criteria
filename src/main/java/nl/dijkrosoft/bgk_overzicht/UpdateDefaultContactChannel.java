package nl.dijkrosoft.bgk_overzicht;

import nl.bytesoflife.clienten.data.Case;
import nl.bytesoflife.clienten.data.DefaultContact;
import nl.bytesoflife.clienten.hoofdsom.HoofdsomInvoer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class UpdateDefaultContactChannel {

    public static void main(String[] args) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {

            emf = Persistence.createEntityManagerFactory("myPU2");
            em = emf.createEntityManager();

            Case aCase = em.find(Case.class, Long.valueOf(6));
            aCase.getClient().setDefaultContact(DefaultContact.EMAIL);

            em.getTransaction().begin();
            em.persist(aCase);
            em.getTransaction().commit();

            System.out.println("Update done");

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
