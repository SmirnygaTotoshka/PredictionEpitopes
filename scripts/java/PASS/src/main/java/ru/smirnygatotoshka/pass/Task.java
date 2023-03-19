package ru.smirnygatotoshka.pass;

public class Task extends javafx.concurrent.Task<Void> {

    private Model[] models;

    public Task(Model[] models) {
        this.models = models;
    }

    @Override
    protected Void call() throws Exception {
        for (Model m: models) {
            m.call();
            Thread.sleep(1000);
        }
        return null;
    }
}
