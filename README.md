# checkstyle-import-checker

[AbstractImportChecker.java](https://github.com/codebox/checkstyle-import-checker) simplifies the process of writing [custom CheckStyle rules](http://checkstyle.sourceforge.net/writingchecks.html) that examine class imports.

Note that Checkstyle contains a number of quite flexible [built-in rules for managing imports](http://checkstyle.sourceforge.net/config_imports.html), you should obviously use these if they meet your needs.

To implement your own custom import checker just sub-class `AbstractImportChecker` and provide an implementation for `onImport()`:

<pre>public class ImportChecker extends AbstractImportChecker {
    @Override
    public void onImport(String packageName, String importName) {
        if (! isImportAllowedInPackage(packageName, importName)) {
            logError(String.format("Import %s not allowed in package %s", importName, packageName));
        }
    }

    private boolean isImportAllowedInPackage(String packageName, String importName) {
        return true; // custom logic here
    }
}</pre>

The `DottedPath` helper class can be used to check for sub-packages and package membership:

<pre>private boolean isImportAllowedInPackage(String packageName, String importName){
    DottedPath packagePath = new DottedPath(packageName);
    DottedPath importPath = new DottedPath(importName);

    return importPath.isChildOf(packagePath);
}</pre>
