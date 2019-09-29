import java.io.File;

public class ExcelFileFilter implements java.io.FileFilter {
    @Override
    public boolean accept(File file) {
        return file != null &&
                file.isFile() &&
                file.canRead() &&
                (file.getName().endsWith("xls")
                        || file.getName().endsWith("xlsx"));
    }
}