package nl.dijkrosoft;

import nl.bytesoflife.clienten.data.*;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;

public class SortHelper {

    /*
      Join<Case, AccountviewProject> projectJoin = root.join(Case_.accountviewProject);
            Join<Case, Folder> folderJoin
     */
    public static Expression<?> getColumnForSortParam(String sort, Join<Case, AccountviewProject> projectJoin, Join<Case, Folder> folderJoin ) {

        switch (sort) {
            case "PRAKTIJK": return folderJoin.get(Folder_.shortName);
            case "PROJ_CODE": return projectJoin.get(AccountviewProject_.PROJ_CODE);
            case "PROJ_DESC": return projectJoin.get(AccountviewProject_.PROJ_DESC);

            default: throw new RuntimeException("Invalid sort column");
        }
    }
}