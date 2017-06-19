package previewcode.backend.DTO;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Diff {
    public final String diff;
    public List hunkChecksums;

    public Diff(String diff) {
        this.diff = diff;
        this.parseDiff(diff);
    }

    //        return btoa(hunk.file.name + "," +
    //          hunk.beforeStart + "," +
    //          hunk.afterStart);

    public void parseDiff(String diff) {
        if (diff == "") {
            hunkChecksums = new ArrayList<>();
        }
        else{

            String ANYTHING = "(?:.|\n)+?";
            String DIFF_DELIMETER = "diff --git";
            String DIFF_HEADER = DIFF_DELIMETER + ANYTHING;
            String FROM_FILE = "(a\\/(?:.+))";
            String TO_FILE = "(b\\/(?:.+))";
            String DIFF_FILES = "\\-\\-\\- (a\\/(?:.+)|\\/dev\\/null)\n" +
                    "\\+\\+\\+ (b\\/(?:.+)|\\/dev\\/null)\n";
            String DIFF_EXPRESSION = "(?:" + DIFF_HEADER + FROM_FILE + " " + TO_FILE + ANYTHING + "(" + DIFF_FILES + "(" + ANYTHING + "))?(?:(?=\n" + DIFF_DELIMETER + ")|$))";

//        List<String> allMatches = new ArrayList<String>();
//        Matcher m = Pattern.compile(DIFF_EXPRESSION)
//                .matcher(diff);
//        while (m.find()) {
//            allMatches.add(m.group());
//        }

            String DIGITS = "(\\d+)(?:,(\\d+))?";
            String HUNK_DELIMETER = "@@ \\-" + DIGITS + " \\+" + DIGITS + " @@";
            String HUNK_EXPRESSION = "(" + HUNK_DELIMETER + ").*\n(" + ANYTHING + ")" + "\n?(?:(?=" + HUNK_DELIMETER + ")|$)";



            List<String> allMatches = new ArrayList<String>();
            Matcher m = Pattern.compile(DIFF_EXPRESSION)
                    .matcher(diff);
            while (m.find()) {
                String newFind = m.group();
                allMatches.add(newFind);

//                String fileHeader = getFileHeader(newFind, newFind[1].substring(2), parsedDiff[2].substring(2));
            }

        }

    }

//
//    public Object getFileHeader(Array parsedDiff, String fromFile, String toFile) {
//        // File was newly created`
//        if (parsedDiff[4] === '/dev/null') {
//            return { name: toFile, created: true, fromTo: toFile };
//            // File was deleted
//        } else if (parsedDiff[5] === '/dev/null') {
//            return { name: fromFile, deleted: true, fromTo: fromFile };
//            // File was renamed
//        } else if (fromFile !== toFile) {
//            return { name: toFile, moved: true, from: fromFile, fromTo: fromFile + ' -> ' + toFile };
//        } return { name: toFile, fromTo: toFile};
//    }
}
