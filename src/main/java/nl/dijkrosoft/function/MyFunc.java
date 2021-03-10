package nl.dijkrosoft.function;

import nl.bytesoflife.clienten.data.ClientContactDetails;
import nl.bytesoflife.clienten.data.ClientContactValueWithType;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.ListAttribute;
import java.util.Set;

@FunctionalInterface
public interface MyFunc {

    Set<Long> method(String searchValue, CriteriaBuilder cb, EntityManager em, ListAttribute<ClientContactDetails, ClientContactValueWithType> attribute);

}
