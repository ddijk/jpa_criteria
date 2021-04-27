package nl.dijkrosoft.depot;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.bytesoflife.clienten.service.accountview.finance.FinancialOverview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DeclarationsHelper {

    public static FinancialOverview readFinancialOverviewFromFile(String filename) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(Files.readAllBytes(Paths.get("src", "test", "resources",filename)), FinancialOverview.class);

    }
}
