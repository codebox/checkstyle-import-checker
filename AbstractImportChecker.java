

import java.util.Arrays;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * This class simplifies the process of writing custom CheckStyle rules that examine class imports. 
 * 
 * @see <a href="http://checkstyle.sourceforge.net/writingchecks.html">Writing custom Checkstyle rules</a>
 * @see <a href="http://checkstyle.sourceforge.net/config_imports.html">Built-in Checkstyle rules concerning imports</a>
 */
public abstract class AbstractImportChecker extends Check {
    private String currentPackage;
    private DetailAST currentAst;

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.IMPORT, TokenTypes.PACKAGE_DEF };
    }

    @Override
    public void beginTree(final DetailAST aRootAST) {
        currentPackage = null;
    }

    @Override
    public void visitToken(final DetailAST ast) {
        currentAst = ast;
        final DetailAST detail = ast.findFirstToken(TokenTypes.DOT);
        final String name = unparse(detail);

        if (ast.getType() == TokenTypes.PACKAGE_DEF) {
            currentPackage = name;
        } else {
            final String importName = name;
            onImport(currentPackage, importName);
        }
    }

    public abstract void onImport(String packageName, String importName);

    protected void logError(String errorMsg) {
        log(currentAst.getLineNo(), errorMsg);
    }

    private String unparse(final DetailAST node) {
        final String result;
        if (node.getType() == TokenTypes.DOT) {
            final DetailAST firstChild = node.getFirstChild();
            final DetailAST lastChild = node.getLastChild();

            if (firstChild == lastChild) {
                result = unparse(firstChild);
            } else {
                result = unparse(firstChild) + "." + unparse(lastChild);
            }

        } else {
            result = node.getText();
        }
        return result;
    }

    /**
     * This is a helper class to a parse import/package names and determine
     * whether one is a child of another. Applications include testing whether
     * one package is a sub-package of another, or checking if an import is from
     * a particular package.
     */
    public static class DottedPath {
        private final String path;
        private final String[] pathParts;

        public DottedPath(final String path) {
            if (path == null) {
                throw new NullPointerException();
            }
            this.path = path;
            pathParts = path.split("\\.");
        }

        public boolean isChildOf(final DottedPath otherPath) {
            final int otherLength = otherPath.pathParts.length;
            if (otherLength < pathParts.length) {
                return Arrays.equals(Arrays.copyOf(pathParts, otherLength), otherPath.pathParts);
            }
            return false;
        }

        @Override
        public boolean equals(final Object that) {
            if (that instanceof DottedPath) {
                return ((DottedPath) that).path.equals(path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
