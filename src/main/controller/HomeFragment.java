package main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.common.ScreenController;
import main.common.TimelineDB;
import main.model.Timeline;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static main.common.TimelineDB.myTime;

public class HomeFragment {

    @FXML private Button createBtn;
    @FXML private Button loadBtn;
    public static int numberOfTimelines; // holds a record of the number of created timelines

    public void initialize() throws SQLException {
    }

    @FXML
    public void createTimeline() throws IOException {
        ScreenController.setScreen(ScreenController.Screen.NEW_TIMELINE);
    }

    @FXML
    public void loadTimeline() throws JAXBException, IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        File file = chooser.showOpenDialog(new Stage());
        Timeline importedTimeline = importFromFile(file);
        TimelineDB.getCreatedTimelines().add(importedTimeline);
        myTime = importedTimeline;

        ScreenController.setScreen(ScreenController.Screen.TIMELINE_DETAILS);
    }

    private Timeline importFromFile(File file) throws JAXBException {
    	JAXBContext context = JAXBContext.newInstance(Timeline.class);
		javax.xml.bind.Unmarshaller unMarshaller = context.createUnmarshaller();
		return (Timeline) unMarshaller.unmarshal(file);
    }
}
