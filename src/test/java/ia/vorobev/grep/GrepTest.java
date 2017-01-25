package ia.vorobev.grep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.print.PrintException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by ia.vorobev on 24.01.2017.
 */
public class GrepTest implements SysoutCaptureAndAssertionAbility {

    public static final String SEP = System.lineSeparator();

    @Before
    public void setUpSystemOut() throws PrintException, IOException {
        resetOut();
        captureSysout();
    }

    @Test
    public void grepOneFile() throws Exception {
        int i = Grep.grep("bod", "src/test/resources/bob.txt");
        assertSysoutEquals(
                "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/bob.txt: 1bod" + SEP +
                        "src/test/resources/bob.txt: bod2" + SEP +
                        "src/test/resources/bob.txt: boddob" + SEP +
                        "src/test/resources/bob.txt: bodbod" + SEP +
                        "src/test/resources/bob.txt: bod" + SEP

        );
        assertEquals(6, i);
    }

    @Test
    public void grepTwoFile() throws Exception {
        int i = Grep.grep("bod", "src/test/resources/bob.txt",
                "src/test/resources/pop.txt");
        assertSysoutEquals(
                "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/bob.txt: 1bod" + SEP +
                        "src/test/resources/bob.txt: bod2" + SEP +
                        "src/test/resources/bob.txt: boddob" + SEP +
                        "src/test/resources/bob.txt: bodbod" + SEP +
                        "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/pop.txt:  bod" + SEP);
        assertEquals(7, i);
    }

    @Test
    public void grepThreeFile() throws Exception {
        int i = Grep.grep("bod", "src/test/resources/bob.txt",
                "src/test/resources/pop.txt",
                "src/test/resources/three.txt");
        assertSysoutEquals(
                "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/bob.txt: 1bod" + SEP +
                        "src/test/resources/bob.txt: bod2" + SEP +
                        "src/test/resources/bob.txt: boddob" + SEP +
                        "src/test/resources/bob.txt: bodbod" + SEP +
                        "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/pop.txt:  bod" + SEP);
        assertEquals(7, i);
    }

    @Test
    public void grepThreeFile2() throws Exception {
        int i = Grep.grep("bod", "src/test/resources/bob.txt",
                "src/test/resources/three.txt",
                "src/test/resources/pop.txt");
        assertSysoutEquals(
                "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/bob.txt: 1bod" + SEP +
                        "src/test/resources/bob.txt: bod2" + SEP +
                        "src/test/resources/bob.txt: boddob" + SEP +
                        "src/test/resources/bob.txt: bodbod" + SEP +
                        "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/pop.txt:  bod" + SEP);
        assertEquals(7, i);
    }

    @Test
    public void grepThreeFile3() throws Exception {
        int i = Grep.grep("bod", "src/test/resources/three.txt",
                "src/test/resources/bob.txt",
                "src/test/resources/pop.txt");
        assertSysoutEquals(
                "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/bob.txt: 1bod" + SEP +
                        "src/test/resources/bob.txt: bod2" + SEP +
                        "src/test/resources/bob.txt: boddob" + SEP +
                        "src/test/resources/bob.txt: bodbod" + SEP +
                        "src/test/resources/bob.txt: bod" + SEP +
                        "src/test/resources/pop.txt:  bod" + SEP);
        assertEquals(7, i);
    }

    @Test
    public void grepEmptyFile() {
        int i = Grep.grep("bod",
                "src/test/resources/three.txt");
        assertSysoutEquals("");
        assertEquals(0, i);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgument() {
        Grep.main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgument2() {
        Grep.main(new String[]{""});
    }

    @After
    public void tearDown() {
        resetOut();
    }

}