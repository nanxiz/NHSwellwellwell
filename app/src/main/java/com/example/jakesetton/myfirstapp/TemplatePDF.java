package com.example.jakesetton.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TemplatePDF {
    private static Context context;
    private String fileName;//default???
    private static File pdfFile;
    private PdfWriter pdfWriter;
    private Document document;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN,19,Font.BOLD);
    private Font ftext = new Font(Font.FontFamily.TIMES_ROMAN,13,Font.BOLD);
    //private Font fOtherTitle = new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);




    public TemplatePDF(Context context){
        this.context= context;
        this.fileName= fileName;
    }

    public static void viewPdf() {
       Intent intent = new Intent(context,ViewPDFActivity.class);
       intent.putExtra("path",pdfFile.getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void openDocument(){
        createFile();
        try{
            document=new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document,new FileOutputStream(pdfFile));
            document.open();
        }
        catch(Exception e){}

    }
    private void createFile(){
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(),"PDF");

        if (!folder.exists()){
            folder.mkdir();

        }
        fileName=new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(System.currentTimeMillis())+".pdf";

        pdfFile= new File(folder,fileName);
    }

    public void closeDocu(){
        document.close();
    }
    /*
    public void addTitle(String title){ //ADD TIME LATER
        document.addTitle(title);
    }
    */
    public void addSubTitle(String title){ //ADD TIME LATER
        try{document.add(new Paragraph(title, fTitle));}
        catch(Exception e){

        }
    }

    public void addEmphasizedSubject(String title, String text){
        try{
        paragraph = new Paragraph();
        addChild(new Paragraph(title, fTitle));
        addChild(new Paragraph(text));
        paragraph.setSpacingAfter(5);
        document.add(paragraph);}
        catch(Exception e){

        }

    }



    public void addSubject(String title, String text){
        try{
            paragraph = new Paragraph();
            addChild(new Paragraph(title +"\t\t"+ text, ftext));
            //paragraph.setAlignment(Element.ALIGN_LEFT);

            //addChild(new Paragraph(text));
            //paragraph.setSpacingAfter(30);
            document.add(paragraph);}
        catch(Exception e){

        }
    }




    public void addChild(Paragraph childparagraph){
        childparagraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.add(childparagraph);
    }


    public String getFileName(){
        return fileName;
    }
    public void sharePdf(){



        /*
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"PDF/"+fileName);
        Uri uri = Uri.fromFile(outputFile);
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM,uri);
        share.setPackage("com.whatsapp");
        //startActivity
       // activity.*/
    }
}
