import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class AbstractImportCheckerTest {
    private AbstractImportChecker check;
    private String lastPackageName, lastImportName;

    @Before
    public void setup() {
        check = new AbstractImportChecker() {
            @Override
            public void onImport(String packageName, String importName) {
                lastPackageName = packageName;
                lastImportName = importName;
            }
        };
    }

    @Test
    public void testThatOnlyImportsAndPackageStatementsAreChecked() {
        assertThat(check.getAcceptableTokens(), is(new int[] { TokenTypes.IMPORT, TokenTypes.PACKAGE_DEF }));
    }

    @Test
    public void whenPackageAstFollowedByImportThenOnImportIsCalledWithCorrectValues() {
        final String PACKAGE = "com.example";
        final DetailAST packageAst = parseToDetailAST(PACKAGE);
        packageAst.setType(TokenTypes.PACKAGE_DEF);

        final String IMPORT = "com.example.TestClass";
        final DetailAST importAst = parseToDetailAST(IMPORT);
        importAst.setType(TokenTypes.IMPORT);

        check.visitToken(packageAst);
        check.visitToken(importAst);

        assertThat(lastPackageName, is(PACKAGE));
        assertThat(lastImportName, is(IMPORT));
    }

    @Test(expected = NullPointerException.class)
    public void nullDottedPathCausesException() {
        new AbstractImportChecker.DottedPath(null);
    }

    public void sameDottedValuesAreEqual() {
        final AbstractImportChecker.DottedPath dp1 = new AbstractImportChecker.DottedPath("java.util.Date");
        final AbstractImportChecker.DottedPath dp2 = new AbstractImportChecker.DottedPath("java.util.Date");
        assertEquals(dp1, dp2);
    }

    public void sameDifferentDottedValuesAreNotEqual() {
        final AbstractImportChecker.DottedPath dp1 = new AbstractImportChecker.DottedPath("java.util.Date");
        final AbstractImportChecker.DottedPath dp2 = new AbstractImportChecker.DottedPath("java.util.Date2");
        assertNotEquals(dp1, dp2);
    }

    public void duplicatePathIsNotChild() {
        final AbstractImportChecker.DottedPath dp1 = new AbstractImportChecker.DottedPath("java.util.Date");
        final AbstractImportChecker.DottedPath dp2 = new AbstractImportChecker.DottedPath("java.util.Date");
        assertThat(dp1.isChildOf(dp2), is(false));
    }

    public void importIsChildOfPackage() {
        final AbstractImportChecker.DottedPath parent = new AbstractImportChecker.DottedPath("java.util");
        final AbstractImportChecker.DottedPath child = new AbstractImportChecker.DottedPath("java.util.Date");
        assertThat(child.isChildOf(parent), is(true));
    }

    public void subPackageIsChildOfPackage() {
        final AbstractImportChecker.DottedPath parentPackage = new AbstractImportChecker.DottedPath("java.util");
        final AbstractImportChecker.DottedPath childPackage = new AbstractImportChecker.DottedPath("java.util.concurrent");
        assertThat(childPackage.isChildOf(parentPackage), is(true));
    }

    private DetailAST parseToDetailAST(String name) {
        final DetailAST ast = new DetailAST();
        DetailAST dot = getDot();
        dot.setType(TokenTypes.DOT);
        ast.setFirstChild(dot);

        String[] parts = name.split("\\.");

        for (String part : Arrays.copyOfRange(parts, 0, parts.length - 1)) {
            final DetailAST text = getTextNode(part);
            dot.setFirstChild(text);
            final DetailAST newDot = getDot();
            text.setNextSibling(newDot);
            dot = newDot;
        }

        final DetailAST finalTextNode = getTextNode(parts[parts.length - 1]);
        dot.addChild(finalTextNode);

        ast.setType(TokenTypes.IMPORT);
        return ast;
    }

    private DetailAST getTextNode(String text) {
        DetailAST dot = new DetailAST();
        dot.setType(TokenTypes.STRING_LITERAL);
        dot.setText(text);
        return dot;
    }

    private DetailAST getDot() {
        DetailAST dot = new DetailAST();
        dot.setType(TokenTypes.DOT);
        return dot;
    }
}
