import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;

public class ConverterConfig {

    public ConverterConfig(JSONObject jo) throws IOException {
        this.input = (String) jo.get("input");
        this.output = (String) jo.get("output");
        this.column = (String) jo.get("column");
        if (!(jo.containsKey("input") || jo.containsKey("output") || jo.containsKey("column")))
            throw new IOException("Not found obligatory parameters for converter.");
        if (jo.containsKey("charged")){
            this.charged = (boolean) jo.get("charged");
        }
        else{
            this.charged = true;
        }
        if (jo.containsKey("alphabet")){
            this.alphabet = Alphabet.valueOf((String) jo.get("alphabet"));
        }
        else{
            this.alphabet = Alphabet.protein;
        }
        if (jo.containsKey("threads")){
            this.threads = (long) jo.get("threads");
        }
        else{
            this.threads = 1;
        }
        if (jo.containsKey("separator")){
            this.separator = (char) jo.get("separator");
        }
        else{
            this.separator = ';';
        }
        if (jo.containsKey("filename")){
            this.filename = (String) jo.get("filename");
        }
        else{
            String name = String.valueOf(Paths.get(input).getFileName());
            this.filename = name.substring(0,name.indexOf('.'));
        }
        if (jo.containsKey("delete_tmp")){
            this.delete_tmp = (boolean) jo.get("delete_tmp");
        }
        else{
            this.delete_tmp = true;
        }
    }

    public enum Alphabet{
        DNA,
        RNA,
        protein
    }
    private String input;
    private String output;
    private String column;
    private boolean charged;
    private Alphabet alphabet;
    private long threads;
    private char separator;
    private String filename;
    private boolean delete_tmp;

    public String getInput() {
        return input;
    }

    public String getOuput() {
        return output;
    }

    public String getColumn() {
        return column;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public String getAlphabet() {
        return alphabet.toString();
    }

    public void setAlphabet(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public long getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isDelete_tmp() {
        return delete_tmp;
    }

    public void setDelete_tmp(boolean delete_tmp) {
        this.delete_tmp = delete_tmp;
    }

    @Override
    public String toString() {
        return "ConverterConfig{" +
                "input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", column='" + column + '\'' +
                ", charged=" + charged +
                ", alphabet=" + alphabet +
                ", threads=" + threads +
                ", separator=" + separator +
                ", filename='" + filename + '\'' +
                ", delete_tmp=" + delete_tmp +
                '}';
    }
}
