package previewcode.backend.services;


import jregex.Matcher;
import jregex.Pattern;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for GitHub diffs
 */
public class DiffParser {

    /**
     * The diffparser for diffs
     * @param diff to parse
     * @return list of hunkChecksums
     */
    public List<String> parseDiff(String diff) {
       ArrayList<String> hunkChecksums = new ArrayList<>();

        if (diff != "") {
            String ANYTHING = "(?:.|\n)+?";
            String DIFF_DELIMETER = "diff --git";
            String DIFF_HEADER = DIFF_DELIMETER + ANYTHING;
            String FROM_FILE = "(a\\/(?:.+))";
            String TO_FILE = "(b\\/(?:.+)|\\/dev\\/null)";
            String DIFF_FILES = "\\-\\-\\- (a\\/(?:.+)|\\/dev\\/null)\n" +
                    "\\+\\+\\+ " + TO_FILE + "\n";
            String DIFF_EXPRESSION = "(?:" + DIFF_HEADER + FROM_FILE + " " + TO_FILE + ANYTHING + "(" + DIFF_FILES + "(" + ANYTHING + "))?(?:(?=\n" + DIFF_DELIMETER + ")|$))";

            String DIGITS = "(\\d+)(?:,(\\d+))?";
            String HUNK_DELIMETER = "@@ \\-" + DIGITS + " \\+" + DIGITS + " @@";
            String HUNK_EXPRESSION = "(" + HUNK_DELIMETER + ").*\n(" + ANYTHING + ")" + "?(?:(?=" + "\n" + HUNK_DELIMETER + ")|$)";

            Matcher matchDiff = new Pattern(DIFF_EXPRESSION).matcher(diff);

            while (matchDiff.find()) {
                String hunk = matchDiff.group(6);

                String fileName = getFileName(matchDiff.group(1), matchDiff.group(2));
                //There is no code in the hunk
                if (hunk == null) {
                    //Done to be compatible with the frontend
                    String toEncode = fileName + ",undefined,undefined";
                    hunkChecksums.add(new String(Base64.encodeBase64(toEncode.getBytes())));
                } else {

                    Matcher matchHunk = new Pattern(HUNK_EXPRESSION).matcher(hunk);
                    while (matchHunk.find()) {
                        ArrayList<String> hunkList = new ArrayList<>();
                        for (int i = 0; i < matchHunk.groupCount(); i++) {
                            String newFind = matchHunk.group(i);
                            hunkList.add(newFind);
                        }
                        String toEncode = fileName + "," + hunkList.get(2) + "," + hunkList.get(4);
                        hunkChecksums.add(new String(Base64.encodeBase64(toEncode.getBytes())));
                    }
                }
            }
        }
        return hunkChecksums;
    }

    /**
     * Gets the filename for file
     * @param fromFile the previous name of the file
     * @param toFile the current name of the file
     * @return the filename
     */
    private String getFileName(String fromFile, String toFile) {
        if (toFile.equals("/dev/null")) {
            return fromFile.substring(2);
        } else {
            return toFile.substring(2);
        }
    }
}
