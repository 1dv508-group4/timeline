package main.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import main.common.AlertMessage;
import javafx.stage.Stage;
import main.common.ScreenController;
import main.model.Event;
import main.model.Timeline;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static main.common.StageManager.getStage;
import static main.controller.HomeFragment.myTime;
import static main.controller.NewEventFragment.myEvent;


public class TimelineDetailsFragment {
	@FXML private Button ButtonBack;
	@FXML private AnchorPane myDisplay;
	@FXML private ImageView newEventButton;
	@FXML private ScrollPane scrollPane;
	@FXML private Separator separator;
	@FXML private AnchorPane PaneMain;
	@FXML private ImageView editButton;
	@FXML private Text title;
	@FXML private Label endDate;
	@FXML private Label startDate;
	@FXML private Label description;
	@FXML private ImageView removeTimeline;
	@FXML private ImageView AddImage;
	@FXML private AnchorPane LeftPane;
	@FXML private ImageView exportButton;

   private Timeline display = myTime;
   private double lineHeight;
   private double lineStart;
   private double distanceBetweenLines;
   private int timelinePeriodInDays;
    private Tooltip tooltip = new Tooltip();
    ArrayList<LocalDate> duplicates = new ArrayList<LocalDate>();


    public void initialize() throws SQLException {
        ButtonBack.setOnMouseEntered(e -> getStage().getScene().setCursor(Cursor.HAND));
        ButtonBack.setOnMouseExited(e -> getStage().getScene().setCursor(Cursor.DEFAULT));

        // The height of the actual line is calculated based on the height of the Scrollpane:
        lineHeight = scrollPane.getPrefHeight() / 2;
        lineStart = 15; // to account for space near the left border of the scrollpane
        timelinePeriodInDays = (int) ChronoUnit.DAYS.between(display.getStartDate(),display.getEndDate());

        title.setText("Title: " + display.getTitle());
        startDate.setText("Start date: " + display.getStartDate().toString());
        endDate.setText("End date: " + display.getEndDate().toString());
        description.setText("Description: " + display.getDescription());

        displayTimeline();
        displayEvents();

        // The PaneMain is the parent pane. It holds the Scrollpane inside it and therefore must be bounded to the
        // changing size of the stage.
        PaneMain.prefHeightProperty().bind(getStage().heightProperty());
        PaneMain.prefWidthProperty().bind(getStage().widthProperty());

        // The area in which the Timeline is shown is a Scrollpane. If the user makes the screen wider, the width of the
        // scrollpane is bound to the changing width of the stage.
        // The subtraction is needed to account for the width of the ListView.
        scrollPane.prefWidthProperty().bind(getStage().widthProperty().subtract(255));
    }

    /*
     * The length of the line is always fixed to an X of 1600.
     */

    private void displayTimeline() {
       Line lineTimeline = new Line(lineStart,lineHeight,1600,lineHeight);
        myDisplay.getChildren().add(lineTimeline);

        Line beginVertical = new Line(lineStart,lineHeight-15,lineStart,lineHeight+15);
        Line endVertical = new Line(1600,lineHeight-15,1600,lineHeight+15);
        myDisplay.getChildren().addAll(beginVertical,endVertical);

        distanceBetweenLines = (1600 - lineStart) / timelinePeriodInDays;

        if (timelinePeriodInDays < 60) {
            for (int i = 1; i < timelinePeriodInDays; i++) {
                Line verticalLine = new Line((i * distanceBetweenLines) + lineStart, lineHeight - 5, (i * distanceBetweenLines) + lineStart, lineHeight + 5);
                myDisplay.getChildren().add(verticalLine);
            }
        } else if (timelinePeriodInDays < 300) {
            for (int i = 0; i < timelinePeriodInDays; i += 7) {
                Line verticalLine = new Line((i * distanceBetweenLines) + lineStart, lineHeight - 5, (i * distanceBetweenLines) + lineStart, lineHeight + 5);
                myDisplay.getChildren().add(verticalLine);
            }
        }
    }

    private void displayEvents() {
        ArrayList<Event> events = myTime.getListOfEvents();
        ArrayList<Event> tempEvents = new ArrayList<>(events.size());
        HashMap<Integer,Integer> hm = new HashMap<>(8);
        HashMap<Integer,LocalDate[]> durationalMap = new HashMap<>();

        /**
         * For each event, I try to calculate the position of the event on the timeline.
         * I do this by getting the total length of the line, being the total period in days of the timeline.
         * I compare this with the days until the event takes place, given the date of the event and the start date of
         * the timeline.
         *
         * Dividing the days until the event with the total days of the timeline, I get the relative position of where to
         * put the event. This position is then used by creating a line on the timeline.
         *
         */

        ContextMenu conMenu = new ContextMenu();
        MenuItem modify = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");
        conMenu.getItems().addAll(modify, delete);for (Event e: events) {
            if (!e.isDurational()) {int key = e.getEvent_startDate().hashCode();

            if (hm.containsKey(key)) {

                hm.replace(key,hm.get(key) + 1);
            } else
                hm.put(e.getEvent_startDate().hashCode(),0);


                int daysUntilEvent = (int) ChronoUnit.DAYS.between(display.getStartDate(), e.getEvent_startDate());

                // Calculate position on line to put event.
                distanceBetweenLines = (1600 - lineStart) / timelinePeriodInDays;//1600 * relativePositionOfEvent / 100;

                // Some metro style colors:
                String[] colors = {"#00a300", "#9f00a7", "#7e3878", "#00aba9", "#ffc40d", "#da532c", "#ee1111"};

                Pane circlePane = new Pane();
                Circle circle = new Circle(10, Color.web(colors[e.hashCode() % 7]));
                circlePane.getChildren().add(circle);

                circle.setOnMouseExited(event -> getStage().getScene().setCursor(Cursor.DEFAULT));
                circle.setOnContextMenuRequested(event -> conMenu.show(circle, event.getScreenX(), event.getScreenY()));
                circle.setOnMouseEntered(event -> {
                    getStage().getScene().setCursor(Cursor.HAND);
                    tooltip.setText("Title: "+myEvent.getEvent_title()+"\n"+"Description: \n"+myEvent.getEvent_description());
                    Tooltip.install(circle, tooltip);
                });

                delete.setOnAction(event -> {
                    System.out.println("Event Deleted or at least pretend :)");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("This will delete event: " + e.getEvent_title());
                    alert.setContentText("Are you ok with this?");

                    // Styling of alert:
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getScene().getStylesheets().add("css/menu_items.css");
                    ButtonBar buttonBar = (ButtonBar)alert.getDialogPane().lookup(".button-bar");
                    buttonBar.getButtons().forEach(b-> {
                        b.getStyleClass().clear();
                        b.getStyleClass().add("btn");
                    });

                    // After 'OK' is clicked:
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && (result.get() == ButtonType.OK)) {
                        myTime.deleteEvent(e);
                        myDisplay.getChildren().removeAll(circlePane);
                    } else if (result.get() == ButtonType.CANCEL) {
                        alert.close();
                    }
                });

                //create a new fxml and controller for modifying events.
                modify.setOnAction(event -> {
                    System.out.println(e.getEvent_title());
                    myEvent = e;
                    try {
                        ScreenController.setScreen(ScreenController.Screen.EDIT_EVENT);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });


                // the method bellow is overriding and a quick fix was to add the edit as modify in the menu item.

               /* circle.setOnMouseClicked(event -> {
                    myEvent = e;
                    try {
                        ScreenController.setScreen(ScreenController.Screen.NEW_EVENT);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });*/

                AnchorPane.setLeftAnchor(circlePane, (daysUntilEvent * distanceBetweenLines) + lineStart);
                AnchorPane.setTopAnchor(circlePane, lineHeight - (hm.get(key) * 50));

                Label dateOfEvent = new Label(e.getEvent_startDate().toString());
                dateOfEvent.relocate(5, -21);
                dateOfEvent.setFont(Font.font(10));
                circlePane.getChildren().add(dateOfEvent);

                Label titleOfEvent = new Label(e.getEvent_title());
                titleOfEvent.relocate(5, -34);
                titleOfEvent.setFont(Font.font(12));
                circlePane.getChildren().add(titleOfEvent);

                myDisplay.getChildren().add(circlePane);
            } else {
                int i = 0;
                int max = 0;

                // Calculation for knowing which level to put a durational event on, in case of overlap.
                // Hashcodes are used to compare dates.
                for (Event tempEvent:tempEvents) {
                    if (e.getEvent_startDate().hashCode() <= tempEvent.getEvent_endDate().hashCode() &&
                            e.getEvent_endDate().hashCode() >= tempEvent.getEvent_startDate().hashCode()) {
                        if (tempEvent.getLevel() + 1 > i) {
                            i = tempEvent.getLevel() + 1;
                        }
                    }
                }
                e.setLevel(i);
                tempEvents.add(e);


                LocalDate eventStartDate = e.getEvent_startDate();
                LocalDate eventEndDate = e.getEvent_endDate();

                int daysUntilEvent = (int) ChronoUnit.DAYS.between(display.getStartDate(), eventStartDate);
                int eventDuration = (int) ChronoUnit.DAYS.between(eventStartDate,eventEndDate);

                // Calculate position on line to put event.
                distanceBetweenLines = (1600 - lineStart) / timelinePeriodInDays;

                Pane circlePane = new Pane();
                AnchorPane.setLeftAnchor(circlePane, (daysUntilEvent * distanceBetweenLines) + lineStart);
                AnchorPane.setTopAnchor(circlePane, lineHeight + (e.getLevel() * 30));

                Rectangle rect = new Rectangle(0,30,eventDuration * distanceBetweenLines,20);
                circlePane.getChildren().add(rect);

                String[] colors = {"#00a300", "#9f00a7", "#7e3878", "#00aba9", "#ffc40d", "#da532c", "#ee1111"};

                rect.setFill(Color.web(colors[e.hashCode() % 7]));
                rect.setOnMouseEntered(event ->{
                    getStage().getScene().setCursor(Cursor.HAND);
                    tooltip.setText("Title: "+myEvent.getEvent_title()+"\n"+"Description: \n"+myEvent.getEvent_description());
                    tooltip.install(rect, tooltip);
                });

                rect.setOnMouseExited(event -> getStage().getScene().setCursor(Cursor.DEFAULT));
                rect.setOnContextMenuRequested(event -> conMenu.show(rect, event.getScreenX(), event.getScreenY()));
                rect.setOnMouseEntered(event -> {
                    getStage().getScene().setCursor(Cursor.HAND);
                    tooltip.setText("Title: "+myEvent.getEvent_title()+"\n"+"Description: \n"+myEvent.getEvent_description());
                    Tooltip.install(rect, tooltip);
                });

                delete.setOnAction(event -> {
                    System.out.println("Event Deleted or at least pretend :)");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation Dialog");
                    alert.setHeaderText("This will delete event: " + e.getEvent_title());
                    alert.setContentText("Are you ok with this?");

                    // Styling of alert:
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getScene().getStylesheets().add("css/menu_items.css");
                    ButtonBar buttonBar = (ButtonBar)alert.getDialogPane().lookup(".button-bar");
                    buttonBar.getButtons().forEach(b-> {
                        b.getStyleClass().clear();
                        b.getStyleClass().add("btn");
                    });

                    // After 'OK' is clicked:
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && (result.get() == ButtonType.OK)) {
                        myTime.deleteEvent(e);
                        myDisplay.getChildren().removeAll(circlePane);
                    } else if (result.get() == ButtonType.CANCEL) {
                        alert.close();
                    }
                });

                //create a new fxml and controller for modifying events.
                modify.setOnAction(event -> {
                    System.out.println(e.getEvent_title());
                    myEvent = e;
                    try {
                        ScreenController.setScreen(ScreenController.Screen.EDIT_EVENT);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });

                Label titleOfEvent = new Label(e.getEvent_title());
                circlePane.getChildren().add(titleOfEvent);
                titleOfEvent.relocate(2, 31);
                titleOfEvent.setFont(Font.font(13));
                titleOfEvent.setTextFill(Color.WHITE);

                myDisplay.getChildren().add(circlePane);
            }
        }
    }

    void setRightClickMenu(Event event) {

    }

    @FXML
    public void back() throws IOException {ScreenController.setScreen(ScreenController.Screen.HOME);}

    @FXML
    public void addEvent() throws IOException {
        ScreenController.setScreen(ScreenController.Screen.NEW_EVENT);
    }

    @FXML
    void Addimage() {
    	 FileChooser fileChooser = new FileChooser();

         FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
         FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
         fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

         File file = fileChooser.showOpenDialog(null);

         try {
             BufferedImage bufferedImage = ImageIO.read(file);
             Image image = SwingFXUtils.toFXImage(bufferedImage, null);
         } catch (IOException ex) {

         }
    }
    @FXML
    void removeTimeline() throws IOException{
    	myDisplay.getChildren().clear();
    	LeftPane.getChildren().clear();

        ScreenController.setScreen(ScreenController.Screen.NEW_TIMELINE);
    }
    @FXML
    public void editTimeline() throws IOException{
    	 ScreenController.setScreen(ScreenController.Screen.EDIT_TIMELINE);
    }

	@FXML
	public void exportTimeline() throws IOException{
		try{
			JAXBContext context = JAXBContext.newInstance(Timeline.class);
			Marshaller m = context.createMarshaller();
			//for pretty-print XML in JAXB
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			// Write to System.out for debugging



			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Timeline");
			File file = fileChooser.showSaveDialog(new Stage());


			if (file != null) {
				try {
					m.marshal(display, file);
				} catch (JAXBException ex) {
					System.out.println(ex.getMessage());
				}

				finally{
					System.out.println("XML saved");
				}


				// Write to File
				//m.marshal(display, new File("C:\\Temp\\Timeline.xml"));

			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}