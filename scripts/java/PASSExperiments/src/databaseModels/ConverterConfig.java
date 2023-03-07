package databaseModels;

import java.sql.Connection;
import java.sql.SQLException;

public class ConverterConfig implements DatabaseManagement{
    private int id;
    private String input;
    private String output;
    private String column;
    private boolean charged;

    private String alphabet;//in db foreign key!

    private byte threads;

    private char separator;

    private String filename;

    private boolean delete_tmp;

    public ConverterConfig(String input, String output, String column, boolean charged, String alphabet, byte threads, char separator, String filename, boolean delete_tmp) {
        this.input = input;
        this.output = output;
        this.column = column;
        this.charged = charged;
        this.alphabet = alphabet;
        this.threads = threads;
        this.separator = separator;
        this.filename = filename;
        this.delete_tmp = delete_tmp;
    }

    public int getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public byte getThreads() {
        return threads;
    }

    public void setThreads(byte threads) {
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
    public void insert(Connection con) throws SQLException {

    }

    @Override
    public int getID(Connection con) {
        return 0;
    }
}
