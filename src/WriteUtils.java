


import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;




public final class WriteUtils {

    public static double Compliant;

    public static double NonCompliant;

    public static void copyHSSFSheets(HSSFWorkbook sourceWB, HSSFWorkbook destinationWB) {
        for (Iterator<Sheet> it = sourceWB.sheetIterator(); it.hasNext(); ) {
            HSSFSheet sheet = (HSSFSheet) it.next();
            String sheetName = sheet.getSheetName();
            if (destinationWB.getSheetIndex(sheetName) != -1) {
                int index = 1;
                while (destinationWB.getSheetIndex(sheetName + "(" + index + ")") != -1) {
                    index++;
                }
                sheetName += "(" + index + ")";
            }
            HSSFSheet newSheet = destinationWB.createSheet(sheetName);
            copySheetSettings(newSheet, sheet);
            copyHSSFSheet(newSheet, sheet);
            copyPictures(newSheet, sheet);
        }
    }

    public static void copyHSSFSheet(HSSFSheet newSheet, HSSFSheet sheet) {
        int maxColumnNum = 0;
        Map<Integer, HSSFCellStyle> styleMap = new HashMap<>();
        // manage a list of merged zone in order to not insert two times a merged zone
        Set<String> mergedRegions = new TreeSet<>();
        List<CellRangeAddress> sheetMergedRegions = sheet.getMergedRegions();
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            HSSFRow srcRow = sheet.getRow(i);
            HSSFRow destRow = newSheet.createRow(i);
            if (srcRow != null) {
                copyHSSFRow(newSheet, srcRow, destRow, styleMap, sheetMergedRegions, mergedRegions);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
        }
    }

    public static void copyHSSFRow(HSSFSheet destSheet, HSSFRow srcRow, HSSFRow destRow, Map<Integer, HSSFCellStyle> styleMap, List<CellRangeAddress> sheetMergedRegions, Set<String> mergedRegions) {
        destRow.setHeight(srcRow.getHeight());
        // pour chaque row
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
            HSSFCell oldCell = srcRow.getCell(j);   // ancienne cell
            HSSFCell newCell = destRow.getCell(j);  // new cell
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                // copy chaque cell
                copyHSSFCell(oldCell, newCell, styleMap);
                // copy les informations de fusion entre les cellules
                CellRangeAddress mergedRegion = getMergedRegion(sheetMergedRegions, srcRow.getRowNum(), (short) oldCell.getColumnIndex());

                if (mergedRegion != null) {
                    CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow(), mergedRegion.getLastRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
                    if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
                        mergedRegions.add(newMergedRegion.formatAsString());
                        destSheet.addMergedRegion(newMergedRegion);
                    }
                }
            }
        }

    }

    public static void copyHSSFCell(HSSFCell oldCell, HSSFCell newCell, Map<Integer, HSSFCellStyle> styleMap) {
        if (styleMap != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                int stHashCode = oldCell.getCellStyle().hashCode();
                HSSFCellStyle newCellStyle = styleMap.get(stHashCode);
                if (newCellStyle == null) {
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }
        switch (oldCell.getCellTypeEnum()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BLANK:
                newCell.setCellType(CellType.BLANK);
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }

    }

    public static void copyXSSFSheets(XSSFWorkbook sourceWB, XSSFWorkbook destinationWB) {
        for (Iterator<Sheet> it = sourceWB.sheetIterator(); it.hasNext(); ) {
            XSSFSheet sheet = (XSSFSheet) it.next();
            String sheetName = sheet.getSheetName();
            System.out.println(sheetName);

            if (destinationWB.getSheetIndex(sheetName) != -1) {
                int index = 1;

                while (destinationWB.getSheetIndex(sheetName + "(" + index + ")") != -1) {
                    index++;

                }
                sheetName += "(" + index + ")";
            }
            XSSFSheet newSheet = destinationWB.createSheet(sheetName);


            copySheetSettings(newSheet, sheet);

            copyXSSFSheet(newSheet, sheet);

            copyPictures(newSheet, sheet);
        }
    }

    public static void copySheetSettings(Sheet newSheet, Sheet sheetToCopy) {

        newSheet.setAutobreaks(sheetToCopy.getAutobreaks());
        newSheet.setDefaultColumnWidth(sheetToCopy.getDefaultColumnWidth());
        newSheet.setDefaultRowHeight(sheetToCopy.getDefaultRowHeight());
        newSheet.setDefaultRowHeightInPoints(sheetToCopy.getDefaultRowHeightInPoints());
        newSheet.setDisplayGuts(sheetToCopy.getDisplayGuts());
        newSheet.setFitToPage(sheetToCopy.getFitToPage());

        newSheet.setForceFormulaRecalculation(sheetToCopy.getForceFormulaRecalculation());

        PrintSetup sheetToCopyPrintSetup = sheetToCopy.getPrintSetup();
        PrintSetup newSheetPrintSetup = newSheet.getPrintSetup();

        newSheetPrintSetup.setPaperSize(sheetToCopyPrintSetup.getPaperSize());
        newSheetPrintSetup.setScale(sheetToCopyPrintSetup.getScale());
        newSheetPrintSetup.setPageStart(sheetToCopyPrintSetup.getPageStart());
        newSheetPrintSetup.setFitWidth(sheetToCopyPrintSetup.getFitWidth());
        newSheetPrintSetup.setFitHeight(sheetToCopyPrintSetup.getFitHeight());
        newSheetPrintSetup.setLeftToRight(sheetToCopyPrintSetup.getLeftToRight());
        newSheetPrintSetup.setLandscape(sheetToCopyPrintSetup.getLandscape());
        newSheetPrintSetup.setValidSettings(sheetToCopyPrintSetup.getValidSettings());
        newSheetPrintSetup.setNoColor(sheetToCopyPrintSetup.getNoColor());
        newSheetPrintSetup.setDraft(sheetToCopyPrintSetup.getDraft());
        newSheetPrintSetup.setNotes(sheetToCopyPrintSetup.getNotes());
        newSheetPrintSetup.setNoOrientation(sheetToCopyPrintSetup.getNoOrientation());
        newSheetPrintSetup.setUsePage(sheetToCopyPrintSetup.getUsePage());
        newSheetPrintSetup.setHResolution(sheetToCopyPrintSetup.getHResolution());
        newSheetPrintSetup.setVResolution(sheetToCopyPrintSetup.getVResolution());
        newSheetPrintSetup.setHeaderMargin(sheetToCopyPrintSetup.getHeaderMargin());
        newSheetPrintSetup.setFooterMargin(sheetToCopyPrintSetup.getFooterMargin());
        newSheetPrintSetup.setCopies(sheetToCopyPrintSetup.getCopies());

        Header sheetToCopyHeader = sheetToCopy.getHeader();
        Header newSheetHeader = newSheet.getHeader();
        newSheetHeader.setCenter(sheetToCopyHeader.getCenter());
        newSheetHeader.setLeft(sheetToCopyHeader.getLeft());
        newSheetHeader.setRight(sheetToCopyHeader.getRight());

        Footer sheetToCopyFooter = sheetToCopy.getFooter();
        Footer newSheetFooter = newSheet.getFooter();
        newSheetFooter.setCenter(sheetToCopyFooter.getCenter());
        newSheetFooter.setLeft(sheetToCopyFooter.getLeft());
        newSheetFooter.setRight(sheetToCopyFooter.getRight());

        newSheet.setHorizontallyCenter(sheetToCopy.getHorizontallyCenter());
        newSheet.setMargin(Sheet.LeftMargin, sheetToCopy.getMargin(Sheet.LeftMargin));
        newSheet.setMargin(Sheet.RightMargin, sheetToCopy.getMargin(Sheet.RightMargin));
        newSheet.setMargin(Sheet.TopMargin, sheetToCopy.getMargin(Sheet.TopMargin));
        newSheet.setMargin(Sheet.BottomMargin, sheetToCopy.getMargin(Sheet.BottomMargin));

        newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
        newSheet.setRowSumsBelow(sheetToCopy.getRowSumsBelow());
        newSheet.setRowSumsRight(sheetToCopy.getRowSumsRight());
        newSheet.setVerticallyCenter(sheetToCopy.getVerticallyCenter());
        newSheet.setDisplayFormulas(sheetToCopy.isDisplayFormulas());
        newSheet.setDisplayGridlines(sheetToCopy.isDisplayGridlines());
        newSheet.setDisplayRowColHeadings(sheetToCopy.isDisplayRowColHeadings());
        newSheet.setDisplayZeros(sheetToCopy.isDisplayZeros());
        newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
        newSheet.setRightToLeft(sheetToCopy.isRightToLeft());
        newSheet.setZoom(100);
    }

    public static void copyXSSFSheet(XSSFSheet newSheet, XSSFSheet sheet) {
        int maxColumnNum = 0;
        Map<Integer, XSSFCellStyle> styleMap = new HashMap<>();
        // manage a list of merged zone in order to not insert two times a merged zone
        Set<String> mergedRegions = new TreeSet<>();
        List<CellRangeAddress> sheetMergedRegions = sheet.getMergedRegions();

        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            XSSFRow srcRow = sheet.getRow(i);
            XSSFRow destRow = newSheet.createRow(i);

            if (srcRow != null) {
                //BaseUtils.systemLogger.info("copy row " + i);

                WriteUtils.copyXSSFRow(newSheet, srcRow, destRow, styleMap, sheetMergedRegions, mergedRegions);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            if(newSheet.getColumnWidth(i) != sheet.getColumnWidth(i)) {
                newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
            }
        }
    }

    public static void copyXSSFRow(XSSFSheet destSheet, XSSFRow srcRow, XSSFRow destRow, Map<Integer, XSSFCellStyle> styleMap, List<CellRangeAddress> sheetMergedRegions, Set<String> mergedRegions) {
        destRow.setHeight(srcRow.getHeight());

        // pour chaque row
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
           // System.out.println("Index kolumny = "+j);


            XSSFCell oldCell = srcRow.getCell(j);   // ancienne cell
            XSSFCell newCell = destRow.getCell(j);  // new cell
            try{
            System.out.print(oldCell.getAddress().toString()+" ");}catch (NullPointerException NPE){
                System.out.print("\n");
            }
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                // copy chaque cell
                copyXSSFCell(oldCell, newCell, styleMap);
                // copy les informations de fusion entre les cellules
                CellRangeAddress mergedRegion = getMergedRegion(sheetMergedRegions, srcRow.getRowNum(), (short) oldCell.getColumnIndex());
                if (mergedRegion != null) {
                    CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow(), mergedRegion.getLastRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
                    if (isNewMergedRegion(newMergedRegion, mergedRegions)) {
                        mergedRegions.add(newMergedRegion.formatAsString());
                        destSheet.addMergedRegion(newMergedRegion);
                    }
                }
            }
        }
    }

    public static void copyXSSFCell(XSSFCell oldCell, XSSFCell newCell, Map<Integer, XSSFCellStyle> styleMap) {
        if (styleMap != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                int stHashCode = oldCell.getCellStyle().hashCode();
                XSSFCellStyle newCellStyle = styleMap.get(stHashCode);
                if (newCellStyle == null) {
                    newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    //по какой-то причине заливка не клонируется
                    newCellStyle.setFillBackgroundColor(oldCell.getCellStyle().getFillBackgroundColor());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }
        switch (oldCell.getCellTypeEnum()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BLANK:
                newCell.setCellType(CellType.BLANK);
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }

    }

    private static void copyPictures(HSSFSheet newSheet, HSSFSheet sheet) {
        Drawing drawingOld = sheet.createDrawingPatriarch();
        Drawing drawingNew = newSheet.createDrawingPatriarch();
        CreationHelper helper = newSheet.getWorkbook().getCreationHelper();

        List<HSSFShape> shapes = ((HSSFPatriarch) drawingOld).getChildren();
        for (HSSFShape shape : shapes) {
            if (shape instanceof HSSFPicture) {
                HSSFPicture pic = (HSSFPicture) shape;
                HSSFPictureData picdata = pic.getPictureData();
                int pictureIndex = newSheet.getWorkbook().addPicture(picdata.getData(), picdata.getFormat());
                ClientAnchor anchor = null;
                if (pic.getAnchor() != null) {
                    anchor = helper.createClientAnchor();
                    anchor.setDx1(pic.getAnchor().getDx1());
                    anchor.setDx2(pic.getAnchor().getDx2());
                    anchor.setDy1(pic.getAnchor().getDy1());
                    anchor.setDy2(pic.getAnchor().getDy2());
                    anchor.setCol1(((HSSFClientAnchor) pic.getAnchor()).getCol1());
                    anchor.setCol2(((HSSFClientAnchor) pic.getAnchor()).getCol2());
                    anchor.setRow1(((HSSFClientAnchor) pic.getAnchor()).getRow1());
                    anchor.setRow2(((HSSFClientAnchor) pic.getAnchor()).getRow2());
                    anchor.setAnchorType(((HSSFClientAnchor) pic.getAnchor()).getAnchorType());
                }
                drawingNew.createPicture(anchor, pictureIndex);
            }
        }
    }


    private static void copyPictures(XSSFSheet newSheet, XSSFSheet sheet) {
        Drawing drawingOld = sheet.createDrawingPatriarch();
        Drawing drawingNew = newSheet.createDrawingPatriarch();
        CreationHelper helper = newSheet.getWorkbook().getCreationHelper();

        List<XSSFShape> shapes = ((XSSFDrawing) drawingOld).getShapes();
        for (XSSFShape shape : shapes) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture pic = (XSSFPicture) shape;
                XSSFPictureData picdata = pic.getPictureData();
                int pictureIndex = newSheet.getWorkbook().addPicture(picdata.getData(), picdata.getPictureType());
                ClientAnchor anchor = null;
                if (pic.getAnchor() != null) {
                    anchor = helper.createClientAnchor();
                    anchor.setDx1(pic.getAnchor().getDx1());
                    anchor.setDx2(pic.getAnchor().getDx2());
                    anchor.setDy1(pic.getAnchor().getDy1());
                    anchor.setDy2(pic.getAnchor().getDy2());
                    anchor.setCol1(((XSSFClientAnchor) pic.getAnchor()).getCol1());
                    anchor.setCol2(((XSSFClientAnchor) pic.getAnchor()).getCol2());
                    anchor.setRow1(((XSSFClientAnchor) pic.getAnchor()).getRow1());
                    anchor.setRow2(((XSSFClientAnchor) pic.getAnchor()).getRow2());
                    anchor.setAnchorType(((XSSFClientAnchor) pic.getAnchor()).getAnchorType());
                }
                drawingNew.createPicture(anchor, pictureIndex);
            }
        }
    }

    public static CellRangeAddress getMergedRegion(List<CellRangeAddress> sheetMergedRegions, int rowNum, short cellNum) {
        for (CellRangeAddress merged : sheetMergedRegions) {
            if (merged.isInRange(rowNum, cellNum)) {
                return merged;
            }
        }
        return null;
    }

    private static boolean isNewMergedRegion(CellRangeAddress newMergedRegion, Set<String> mergedRegions) {
        return !mergedRegions.contains(newMergedRegion.formatAsString());
    }

    public static void checkCompliance(String cell, Double cellValue){



        if(cell.equals("Compliant") || cell.equals("Pending system restart") || cell.equals("Successfully installed update(s)")){

            Compliant = Compliant+cellValue;
            System.out.println(Compliant);

        }else if(cell.equals("Downloaded update(s)") ||cell.equals("Downloading update(s)") ||cell.equals("Failed to download update(s)") ||cell.equals("Failed to install update(s)") ||cell.equals("Non-compliant") ||cell.equals("Installing updates(s)") ||cell.equals("Waiting for another installation to complete")){

            NonCompliant = NonCompliant+cellValue;
            System.out.println(NonCompliant);
        }

    }

    public static String createStringFromCell(XSSFWorkbook workbook, int row, int column) {

        String cell ="";

        if (workbook.getSheetAt(0).getRow(row).getCell(column) != null) {

            cell = workbook.getSheetAt(0).getRow(row).getCell(column).getStringCellValue();

            System.out.println("Cellname > "+cell);

        }
        return cell;
    }

    public static Double createDoubleFromCell(XSSFWorkbook workbook, int row, int column) {

        Double cell =0.0;

        if (workbook.getSheetAt(0).getRow(row).getCell(column) != null) {

            cell = workbook.getSheetAt(0).getRow(row).getCell(column).getNumericCellValue();

        }return cell;
    }

    public static void showMenu() {

        System.out.println("\n" +
                "███████╗ ██████╗ ██████╗ ███████╗██╗   ██╗███████╗██████╗     ███╗   ███╗ ██████╗██████╗ \n" +
                "██╔════╝██╔═══██╗██╔══██╗██╔════╝██║   ██║██╔════╝██╔══██╗    ████╗ ████║██╔════╝██╔══██╗\n" +
                "█████╗  ██║   ██║██████╔╝█████╗  ██║   ██║█████╗  ██████╔╝    ██╔████╔██║██║     ██║  ██║\n" +
                "██╔══╝  ██║   ██║██╔══██╗██╔══╝  ╚██╗ ██╔╝██╔══╝  ██╔══██╗    ██║╚██╔╝██║██║     ██║  ██║\n" +
                "██║     ╚██████╔╝██║  ██║███████╗ ╚████╔╝ ███████╗██║  ██║    ██║ ╚═╝ ██║╚██████╗██████╔╝\n" +
                "╚═╝      ╚═════╝ ╚═╝  ╚═╝╚══════╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝    ╚═╝     ╚═╝ ╚═════╝╚═════╝ \n" +
                "                                                                                         \n");
        System.out.println("By Kamil Sobecki\n");
    }
    public static void main(String[] args) throws IOException  {

        showMenu();

//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Podaj lokalizacje plikow:\n");
//        String sciezka;

        /////////////////////Working Block//////////////////////////////////
//        sciezka =scanner.nextLine();
//        System.out.println(sciezka);
        File folder = new File(".//Pliki");
        ExcelFileFilter filter = new ExcelFileFilter();

        File[] listOfFiles = folder.listFiles();
        File[] justFileNames = folder.listFiles(filter);

        XSSFWorkbook dupadon = null;
        XSSFWorkbook dupatron = new XSSFWorkbook(new FileInputStream(".\\Work.xlsx"));


        //Loop going thru source Workbooks and saving in one destination Workbook
        for(int i = 0; i < listOfFiles.length ; i++){
            dupadon = new XSSFWorkbook(new FileInputStream(listOfFiles[i]));
            String nazwa = justFileNames[i].getName();

            dupadon.setSheetName(0,nazwa);
            XSSFSheet sheet = dupadon.getSheetAt(0);


            Row row4 = sheet.getRow(3);
            int hopefulyLast = sheet.getLastRowNum();
            Row lastrow = sheet.getRow(hopefulyLast);


            try{
            sheet.removeRow(row4);
            sheet.removeRow(lastrow);
           // sheet.removeRow(lastrow2);
            }catch (NullPointerException HWDP){
                System.out.println("HWDP");
            }

            copyXSSFSheets(dupadon,dupatron);


        }
        for(int i =0; i < dupatron.getNumberOfSheets();i++){
            System.out.println(dupatron.getSheetName(i));
        }

        dupatron.removeSheetAt(0);
        dupatron.setSheetOrder("Overall.xlsx",0);
        int lastRow = dupatron.getSheetAt(0).getLastRowNum();
        dupatron.getSheetAt(0).createRow(lastRow+1);
        dupatron.getSheetAt(0).createRow(lastRow+2);
        dupatron.getSheetAt(0).createRow(lastRow+3);
        dupatron.getSheetAt(0).createRow(lastRow+4);
        dupatron.getSheetAt(0).createRow(lastRow+5);
        dupatron.getSheetAt(0).createRow(lastRow+6);
        dupatron.getSheetAt(0).createRow(lastRow+7);
        dupatron.getSheetAt(0).createRow(lastRow+8);
        dupatron.getSheetAt(0).createRow(lastRow+9);
        dupatron.getSheetAt(0).createRow(lastRow+10);
        dupatron.getSheetAt(0).createRow(lastRow+11);
        dupatron.getSheetAt(0).createRow(lastRow+12);
        dupatron.getSheetAt(0).createRow(lastRow+13);
        dupatron.getSheetAt(0).createRow(lastRow+14);
        dupatron.getSheetAt(0).createRow(lastRow+15);



        String F7 = createStringFromCell(dupatron,6,5);
        String F8 = createStringFromCell(dupatron,7,5);
        String F9 = createStringFromCell(dupatron,8,5);
        String F10 = createStringFromCell(dupatron,9,5);
        String F11 = createStringFromCell(dupatron,10,5);
        String F12 = createStringFromCell(dupatron,11,5);
        String F13 = createStringFromCell(dupatron,12,5);
        String F14 = createStringFromCell(dupatron,13,5);
        String F15 = createStringFromCell(dupatron,14,5);
        String F16 = createStringFromCell(dupatron,15,5);
        String F17 = createStringFromCell(dupatron,16,5);




        double F7Value = createDoubleFromCell(dupatron,6,6);
        double F8Value = createDoubleFromCell(dupatron,7,6);
        double F9Value = createDoubleFromCell(dupatron,8,6);
        double F10Value = createDoubleFromCell(dupatron,9,6);
        double F11Value = createDoubleFromCell(dupatron,10,6);
        double F12Value = createDoubleFromCell(dupatron,11,6);
        double F13Value = createDoubleFromCell(dupatron,12,6);
        double F14Value = createDoubleFromCell(dupatron,13,6);
        double F15Value = createDoubleFromCell(dupatron,14,6);
        double F16Value = createDoubleFromCell(dupatron,15,6);
        double F17Value = createDoubleFromCell(dupatron,16,6);


        //System.out.println(F7 + F7Value + F8 + F8Value + F9 + F9Value + F10 + F10Value + F11 + F11Value + F12 + F12Value+F13+F14+F15+F16+F17);


            checkCompliance(F7, F7Value);
            checkCompliance(F8, F8Value);
            checkCompliance(F9, F9Value);
            checkCompliance(F10, F10Value);
            checkCompliance(F11, F11Value);
            checkCompliance(F12, F12Value);
            checkCompliance(F13, F13Value);
            checkCompliance(F14, F14Value);
            checkCompliance(F15, F15Value);
            checkCompliance(F16, F16Value);
           checkCompliance(F17, F17Value);

        System.out.println(Compliant + " " + NonCompliant);

        dupatron.getSheetAt(0).getRow(19).createCell(5);
        dupatron.getSheetAt(0).getRow(19).getCell(5).setCellValue("Compliant");
        dupatron.getSheetAt(0).getRow(20).createCell(5);
        dupatron.getSheetAt(0).getRow(20).getCell(5).setCellValue("Non-Compliant");
        dupatron.getSheetAt(0).getRow(21).createCell(5);
        dupatron.getSheetAt(0).getRow(21).getCell(5).setCellValue("Overall");
        dupatron.getSheetAt(0).getRow(19).createCell(6);
        dupatron.getSheetAt(0).getRow(19).getCell(6).setCellValue(Compliant);
        dupatron.getSheetAt(0).getRow(20).createCell(6);
        dupatron.getSheetAt(0).getRow(20).getCell(6).setCellValue(NonCompliant);
        dupatron.getSheetAt(0).getRow(21).createCell(6);
        dupatron.getSheetAt(0).getRow(21).getCell(6).setCellValue(Compliant/(Compliant+NonCompliant));


        CellStyle style = dupatron.createCellStyle();
        style.setDataFormat(dupatron.createDataFormat().getFormat("0.00%"));
        dupatron.getSheetAt(0).getRow(21).getCell(6).setCellStyle(style);
        dupatron.setActiveSheet(0);



        String zapisz = ".\\Raport.xlsx";
        FileOutputStream out = new FileOutputStream(zapisz);
        dupatron.write(out);

   }}
