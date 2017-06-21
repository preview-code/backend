package previewcode.backend.DTO;



import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class DiffTest {

    @Test
    public void Diff1() {
        Diff diff = new Diff("diff --git a/a b/a\n" +
                "new file mode 100644\n" +
                "index 0000000..e3c0674\n" +
                "--- /dev/null\n" +
                "+++ b/a\n" +
                "@@ -0,0 +1 @@\n" +
                "+one line");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "YSwwLDE=");
    }

    @Test
    public void Diff2() {
        Diff diff = new Diff("diff --git a/b b/b\n" +
                "index e3c0674..ca5d643 100644\n" +
                "--- a/b\n" +
                "+++ b/b\n" +
                "@@ -1 +1,2 @@\n" +
                " one line\n" +
                "+line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "YiwxLDE=");
    }

    @Test
    public void Diff3() {
        Diff diff = new Diff("diff --git a/c b/c\n" +
                "index ca5d643..bf475b0 100644\n" +
                "--- a/c\n" +
                "+++ b/c\n" +
                "@@ -1,2 +1,3 @@\n" +
                " one line\n" +
                "+line inbetween\n" +
                " line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "YywxLDE=");
    }

    @Test
    public void Diff4() {
        Diff diff = new Diff("diff --git a/d b/d\n" +
                "index bf475b0..88afc4f 100644\n" +
                "--- a/d\n" +
                "+++ b/d\n" +
                "@@ -1,3 +1,5 @@\n" +
                " one line\n" +
                " line inbetween\n" +
                "+another line\n" +
                "+hello world\n" +
                " line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "ZCwxLDE=");
    }
    @Test
    public void Diff5() {
        Diff diff = new Diff("diff --git a/e b/e\n" +
                "index 88afc4f..0a96631 100644\n" +
                "--- a/e\n" +
                "+++ b/e\n" +
                "@@ -1,5 +1,5 @@\n" +
                " one line\n" +
                " line inbetween\n" +
                " another line\n" +
                "-hello world\n" +
                "+replace a line\n" +
                " line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "ZSwxLDE=");
    }
    @Test
    public void Diff6() {
        Diff diff = new Diff("diff --git a/f b/f\n" +
                "index 0a96631..c8f047a 100644\n" +
                "--- a/f\n" +
                "+++ b/f\n" +
                "@@ -1,5 +1,5 @@\n" +
                " one line\n" +
                " line inbetween\n" +
                "-another line\n" +
                " replace a line\n" +
                "+another line\n" +
                " line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "ZiwxLDE=");
    }
    @Test
    public void Diff7() {
        Diff diff = new Diff("diff --git a/g b/g\n" +
                "index c8f047a..35cced2 100644\n" +
                "--- a/g\n" +
                "+++ b/g\n" +
                "@@ -1,5 +1,4 @@\n" +
                " one line\n" +
                " line inbetween\n" +
                "-replace a line\n" +
                " another line\n" +
                " line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "ZywxLDE=");
    }
    @Test
    public void Diff8() {
        Diff diff = new Diff("diff --git a/h b/h\n" +
                "index 35cced2..b77e78a 100644\n" +
                "--- a/h\n" +
                "+++ b/h\n" +
                "@@ -1,4 +1,3 @@\n" +
                " one line\n" +
                " line inbetween\n" +
                " another line\n" +
                "-line two");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "aCwxLDE=");
    }
    @Test
    public void Diff9() {
        Diff diff = new Diff("diff --git a/i b/i\n" +
                "index b77e78a..ba817a3 100644\n" +
                "--- a/i\n" +
                "+++ b/i\n" +
                "@@ -1,3 +1,3 @@\n" +
                "-one line\n" +
                " line inbetween\n" +
                " another line\n" +
                "+add last");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "aSwxLDE=");
    }
    @Test
    public void Diff10() {
        Diff diff = new Diff("diff --git a/j b/j\n" +
                "index ba817a3..56ed89c 100644\n" +
                "--- a/j\n" +
                "+++ b/j\n" +
                "@@ -1,3 +1,2 @@\n" +
                "-line inbetween\n" +
                " another line\n" +
                " add last");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "aiwxLDE=");
    }
    @Test
    public void Diff11() {
      Diff diff = new Diff("diff --git a/k b/k\n" +
              "index 56ed89c..e69de29 100644\n" +
              "--- a/k\n" +
              "+++ b/k\n" +
              "@@ -1,2 +0,0 @@\n" +
              "-another line\n" +
              "-add last");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "aywxLDA=");
    }
    @Test
    public void Diff12() {
        Diff diff = new Diff("diff --git a/l b/l\n" +
                "index 4dbe2d7..4dc0b35 100644\n" +
                "--- a/l\n" +
                "+++ b/l\n" +
                "@@ -3,8 +3,6 @@ Fusce laoreet dui in lectus tempus, ut vulputate nisi porttitor.\n" +
                " Pellentesque a nulla a libero molestie blandit vitae id eros.\n" +
                " Maecenas sit amet turpis condimentum enim volutpat imperdiet.\n" +
                " Vestibulum at sem convallis, congue erat porttitor, mattis dui.\n" +
                "-Donec scelerisque massa in dignissim egestas.\n" +
                "-Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Integer non turpis eu quam bibendum pulvinar vel non ante.\n" +
                " Etiam vel nibh aliquam, dignissim nunc vel, ultrices sapien.\n" +
                " Nulla ac justo non tellus convallis suscipit.\n" +
                "@@ -340,6 +338,8 @@ Vestibulum sed sem fermentum, tristique diam ut, aliquam mauris.\n" +
                " Sed malesuada orci non pulvinar dictum.\n" +
                " Etiam convallis augue nec posuere convallis.\n" +
                " Proin molestie turpis a orci ultricies, nec porta urna fringilla.\n" +
                "+Donec scelerisque massa in dignissim egestas.\n" +
                "+Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Cras aliquet lacus eget urna sagittis hendrerit.\n" +
                " Curabitur suscipit ipsum non mollis vestibulum.\n" +
                " Suspendisse tempus purus ac tellus pharetra fringilla.");

        assertEquals(diff.hunkChecksums.size(), 2);
        assertEquals(diff.hunkChecksums.get(0), "bCwzLDM=");
        assertEquals(diff.hunkChecksums.get(1), "bCwzNDAsMzM4");
    }
    @Test
    public void Diff13() {
        Diff diff = new Diff("diff --git a/m b/m\n" +
                "index efe6276..6114b7a 100644\n" +
                "--- a/m\n" +
                "+++ b/m\n" +
                "@@ -343,3 +341,5 @@ Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Pellentesque a nulla a libero molestie blandit vitae id eros.\n" +
                " Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Integer non turpis eu quam bibendum pulvinar vel non ante.\n" +
                "+Etiam vel nibh aliquam, dignissim nunc vel, ultrices sapien.\n" +
                "+Nulla ac justo non tellus convallis suscipit.");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "bSwzNDMsMzQx");
    }
    @Test
    public void Diff14() {
        Diff diff = new Diff("diff --git a/n b/n\n" +
                "index efe6276..6114b7a 100644\n" +
                "--- a/n\n" +
                "+++ b/n\n" +
                "@@ -341,5 +343,3 @@ Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Pellentesque a nulla a libero molestie blandit vitae id eros.\n" +
                " Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Integer non turpis eu quam bibendum pulvinar vel non ante.\n" +
                "-Etiam vel nibh aliquam, dignissim nunc vel, ultrices sapien.\n" +
                "-Nulla ac justo non tellus convallis suscipit.");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "biwzNDEsMzQz");
    }
    @Test
    public void Diff15() {
        Diff diff = new Diff("diff --git a/a b/o\n" +
                "index efe6276..6114b7a 100644\n" +
                "--- a/a\n" +
                "+++ b/o\n" +
                "@@ -341,5 +343,3 @@ Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Pellentesque a nulla a libero molestie blandit vitae id eros.\n" +
                " Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                " Integer non turpis eu quam bibendum pulvinar vel non ante.\n" +
                "-Etiam vel nibh aliquam, dignissim nunc vel, ultrices sapien.\n" +
                "-Nulla ac justo non tellus convallis suscipit.");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "bywzNDEsMzQz");
    }
    @Test
    public void Diff16() {
        Diff diff = new Diff("diff --git a/q b/q\n" +
                "index efe6276..6114b7a 100644\n" +
                "--- a/q\n" +
                "+++ b/q\n" +
                "@@ -3,2 +3,1 @@ \n" +
                "First hunk .\n" +
                "-first hunk line two.\n" +
                "@@ -340,1 +338,3 @@ \n" +
                "second hunk line 1\n" +
                "+two two\n" +
                "+two three.\n" +
                "@@ -512,1 +514,3 @@ \n" +
                "Third hunk first line.\n" +
                "+ Third second.\n" +
                "+Last line in last hunk.");

        assertEquals(diff.hunkChecksums.size(), 3);
        assertEquals(diff.hunkChecksums.get(0), "cSwzLDM=");
        assertEquals(diff.hunkChecksums.get(1), "cSwzNDAsMzM4");
        assertEquals(diff.hunkChecksums.get(2), "cSw1MTIsNTE0");
    }
    @Test
    public void Diff17() {
        Diff diff = new Diff("diff --git a/r b/s\n" +
                "similarity index 100% \n" +
                "rename from new \n" +
                "rename to renamed.");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "cyx1bmRlZmluZWQsdW5kZWZpbmVk");

    }

    @Test
    public void Diff18() {
        Diff diff = new Diff("diff --git a/r b/r\n" +
                "index efe6276..6114b7a 100644\n" +
                "--- a/r\n" +
                "+++ b/r\n" +
                "@@ -3,2 +3,1 @@ \n" +
                "First hunk .\n" +
                "+ @@ -3,2 +3,1 @@");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "ciwzLDM=");

    }

    @Test
    public void Diff19() {
        Diff diff = new Diff("diff --git a/p /dev/null\n" +
                "deleted file mode 100644\n" +
                "index efe6276..6114b7a 100644\n" +
                        "--- a/p\n" +
                        "+++ /dev/null\n" +
                        "@@ -341,5 +343,3 @@ Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                        "-Pellentesque a nulla a libero molestie blandit vitae id eros.\n" +
                        "-Morbi fermentum neque sit amet ante eleifend, non molestie nulla pretium.\n" +
                        "-Integer non turpis eu quam bibendum pulvinar vel non ante.\n" +
                        "-Etiam vel nibh aliquam, dignissim nunc vel, ultrices sapien.\n" +
                        "-Nulla ac justo non tellus convallis suscipit.");

        assertEquals(diff.hunkChecksums.size(), 1);
        assertEquals(diff.hunkChecksums.get(0), "cCwzNDEsMzQz");
    }
}
