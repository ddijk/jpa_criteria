package nl.dijkrosoft.bgk_overzicht;

import javax.persistence.NamedNativeQuery;

public class MedewerkerWhoBookedLast {

    public static void main(String[] args) {
//        @NamedNativeQuery(name="MedewerkerWhoBookedLast", query = "select au.display_name from app_user au join uren_regel ur on au.id = ur.app_user_id where ur.dossier=?1 order by ur.date_added desc limit 1")

    }
}
