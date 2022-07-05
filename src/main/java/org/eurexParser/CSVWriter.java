package org.eurexParser;

import java.io.FileOutputStream;
import java.io.IOException;


public class CSVWriter {
    private String fileName;
    private FileOutputStream fos;

    public boolean isOpen(){
        if (fos == null)
            return false;
        return true;
    }

    public void open() throws IOException{
        if (!isOpen())
            fos = new FileOutputStream(fileName);
    }

    public void close() throws IOException{
        if (isOpen()){
            fos.close();
            fos = null;
        }
    }

    public CSVWriter(String fileName){
        this.fileName = fileName;
    }

    public CSVWriter(){
        this.fileName = "data.csv";
    }

    public void writeLine(String[] values) throws IOException {
        if (!isOpen())
            throw new IOException("Writer isn't opened");

        String line = String.join(";",values);
        fos.write(line.getBytes());
        fos.write("\n".getBytes());
    }

}
