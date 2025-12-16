package com.maze.internet_cafe.service;


import javafx.application.Application;
import javafx.stage.Stage;

public class AgentApplication extends Application {

    @Override
    public void start(Stage stage) {
        AgentService.register();
        HeartbeatTask.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
