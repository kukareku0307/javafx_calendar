package com.example.kursach;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CalendarController implements Initializable {
    protected static final Logger logger = LogManager.getLogger();
    ZonedDateTime dateFocus;
    ZonedDateTime today;
    @FXML
    private TextField textField;
    @FXML
    private Text year;

    @FXML
    private Text month;

    @FXML
    private FlowPane calendar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Hello World!");
        dateFocus = ZonedDateTime.now();
        today = ZonedDateTime.now();
        textField.textProperty().addListener((observable, oldValue, newValue) -> {

            if(newValue.length() == 10 &&newValue.charAt(4) == '-'&& newValue.charAt(7) == '-'){

               var activities = FileStorage.findDate(newValue);
//                if(activities.isEmpty())return;
                try{
                showContextMenu(null, activities, Integer.parseInt(newValue.substring(8)), textField);} catch (
                        NumberFormatException e) {
                    logger.error("Ошибка в получении числа");
                }

            }else{
                contextMenuTextField.hide();
            }
        });

        drawCalendar();
    }

    private ContextMenu contextMenuTextField;

    @FXML
    void backOneMonth(ActionEvent event) {
        dateFocus = dateFocus.minusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    @FXML
    void forwardOneMonth(ActionEvent event) {
        dateFocus = dateFocus.plusMonths(1);
        calendar.getChildren().clear();
        drawCalendar();
    }

    @FXML
    void search(InputMethodEvent event) {
    }

    private void drawCalendar() {
        year.setText(String.valueOf(dateFocus.getYear()));
        month.setText(String.valueOf(dateFocus.getMonth()));

        double calendarWidth = calendar.getPrefWidth();
        double calendarHeight = calendar.getPrefHeight() * 1.4;
        double strokeWidth = 1;
        double spacingH = calendar.getHgap();
        double spacingV = calendar.getVgap();

        //List of activities for a given month
//        Map<Integer, List<CalendarActivity>> calendarActivityMap = getCalendarActivitiesMonth(dateFocus);

        int monthMaxDate = dateFocus.getMonth().maxLength();
        //Check for leap year
        if (dateFocus.getYear() % 4 != 0 && monthMaxDate == 29) {
            monthMaxDate = 28;
        }
        int dateOffset = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1, 0, 0, 0, 0, dateFocus.getZone()).getDayOfWeek().getValue();

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                StackPane stackPane = new StackPane();

                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setStrokeWidth(strokeWidth);
                double rectangleWidth = (calendarWidth / 7) - strokeWidth - spacingH;
                rectangle.setWidth(rectangleWidth);
                double rectangleHeight = (calendarHeight / 6) - strokeWidth - spacingV;
                rectangle.setHeight(rectangleHeight);
                rectangle.setArcHeight(10);
                rectangle.setArcWidth(10);
                stackPane.getChildren().add(rectangle);

                int calculatedDate = (j + 2) + (7 * i);
                if (calculatedDate > dateOffset) {
                    int currentDate = calculatedDate - dateOffset;
                    if (currentDate <= monthMaxDate) {
                        Text date = new Text(String.valueOf(currentDate));
                        double textTranslationY = -(rectangleHeight / 2) * 0.75;
                        date.setTranslateY(textTranslationY);
                        stackPane.getChildren().add(date);

//                        List<CalendarActivity> calendarActivities = calendarActivityMap.get(currentDate);
                        var list = FileStorage.findDate(String.format("%s-%s-%s", dateFocus.toLocalDate().getYear(), String.format("%02d", dateFocus.getMonth().getValue()), String.format("%02d", currentDate)));

                        if (!list.isEmpty()) {
                            createCalendarActivity(list.reversed(), rectangleHeight, rectangleWidth, stackPane, currentDate);
                        }
                        rectangle.setStroke(Color.BLACK);
                        createPlusButton(rectangleHeight, rectangleWidth, stackPane, currentDate);
                    }
                    if (today.getYear() == dateFocus.getYear() && today.getMonth() == dateFocus.getMonth() && today.getDayOfMonth() == currentDate) {
                        rectangle.setStroke(Color.DARKORANGE);
                        rectangle.setStrokeType(StrokeType.INSIDE);
                        rectangle.setStrokeWidth(2);
                    }

                }

                calendar.getChildren().add(stackPane);
            }
        }
    }

    private void createCalendarActivity(List<AbstractMap.SimpleEntry<Integer, String>> calendarActivities, double rectangleHeight, double rectangleWidth, StackPane stackPane, int currentDate) {
        VBox calendarActivityBox = new VBox();
        for (int k = 0; k < calendarActivities.size(); k++) {
            if (k >= 2) {
                Text moreActivities = new Text("и ещё " + String.valueOf(calendarActivities.size() - 2));
                moreActivities.setFill(Color.WHITE);
                calendarActivityBox.getChildren().add(moreActivities);

                break;
            }
            Label text = new Label(calendarActivities.get(k).getValue());
            calendarActivityBox.getChildren().add(text);

            text.setTextOverrun(OverrunStyle.ELLIPSIS);
//            text.wrappingWidthProperty().set(rectangleHeight * 0.6);
            text.maxWidth(rectangleWidth * 0.8);
        }
//        calendarActivityBox.setTranslateY((rectangleHeight / 3) * 0.01);
        calendarActivityBox.setMaxWidth(rectangleWidth * 0.8);
        calendarActivityBox.setMaxHeight(rectangleHeight * 0.45);
        calendarActivityBox.setStyle("-fx-background-color:GRAY");

        calendarActivityBox.setOnMouseClicked(mouseEvent -> {
            showContextMenu(calendarActivityBox, calendarActivities, currentDate, null);
        });

        stackPane.getChildren().add(calendarActivityBox);
    }

    private void createPlusButton(double rectangleHeight, double rectangleWidth, StackPane stackPane, int currentDate) {
        Button plusButton = new Button("+");
        plusButton.setStyle("-fx-font-size: 12; -fx-min-width: 40; -fx-min-height: 20;");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.centerOnScreen();

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        Label label = new Label("Добавить заметку");
        Separator divider = new Separator(); // Горизонтальная линия
        TextField textField = new TextField();
        Button addButton = new Button("Добавить");

        vbox.getChildren().addAll(label, divider, textField, addButton);
        CustomMenuItem contextMenuItem = new CustomMenuItem(vbox, false);
        contextMenuItem.setStyle("-fx-background-color: transparent;");
        contextMenuItem.setOnMenuValidation(e -> {
            contextMenuItem.setStyle("-fx-background-color: transparent;");
        });

        contextMenu.getItems().add(contextMenuItem);

        addButton.setOnAction(e -> {
            String note = textField.getText();
            if (!note.isEmpty()) {
                FileStorage.saveNote(String.format("%s-%s-%s", dateFocus.toLocalDate().getYear(), String.format("%02d", dateFocus.getMonth().getValue()), String.format("%02d", currentDate)), note);
                logger.info("Заметка добавлена{}", note);

                calendar.getChildren().clear();
                drawCalendar();
                // Запускаем задержку
//                pause.play();

            }
            contextMenu.hide(); // Закрыть контекстное меню
        });

        plusButton.setOnAction(e -> {
            contextMenu.show(plusButton, javafx.geometry.Side.BOTTOM, 0, 0);
//            textField.requestFocus();

        });

        plusButton.setTranslateY(rectangleHeight / 2.8);
        stackPane.getChildren().add(plusButton);
    }

    private void showContextMenu(VBox parent, List<AbstractMap.SimpleEntry<Integer, String>> calendarActivities, int currentDate, TextField field) {
        ContextMenu contextMenu = new ContextMenu();
        if(field!=null)contextMenuTextField = contextMenu;

        VBox container = new VBox(10);
        CompletableFuture<Integer> futureValue = sendAsyncGetRequest("https://isdayoff.ru/" + String.format("%s-%s-%s", dateFocus.toLocalDate().getYear(), String.format("%02d", dateFocus.getMonth().getValue()), String.format("%02d", currentDate)));

        // Обработка результата после завершения запроса

        Text loadingText = new Text("Загрузка...");
        container.getChildren().add(loadingText);


        futureValue.thenAccept(value -> {

            logger.info("Полученное значение: {}", value);

            if(value == 1){loadingText.setText("Выходной");}
            else if(value == 0){loadingText.setText("Рабочий");}
            else{loadingText.setText("Ошибка при получении даты");}
        }).exceptionally(ex -> {
            logger.error("Ошибка при выполнении запроса: {}", ex.getMessage());

            loadingText.setText("Ошибка при вычислении праздника");
            return null;
        });

        VBox scrollContent = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setPrefSize(200, 200);
        container.getChildren().add(scrollPane);
        container.getChildren().add(getButtonDelDate(contextMenu, currentDate));
        updateList(scrollContent, calendarActivities, contextMenu, currentDate);


        MenuItem item = new MenuItem();
        item.setGraphic(container);

        contextMenu.getItems().add(item);
        contextMenu.centerOnScreen();
        contextMenu.show( field != null ? field : parent, javafx.geometry.Side.BOTTOM, 0, 0);
        // Очистка списка на скрытие
    }

    private void updateList(VBox scrollContent, List<AbstractMap.SimpleEntry<Integer, String>> itemList, ContextMenu contextMenu, int currentDate) {
        scrollContent.getChildren().clear();
        int i = 1;
        for (AbstractMap.SimpleEntry<Integer, String> item : itemList) {
            VBox itemBox = new VBox();
            HBox horizontal = new HBox(5);
            Text itemText = new Text(i + "(id:" + item.getKey() + ")" + ": " + item.getValue());
            Button deleteButton = getButton(contextMenu, currentDate, item);

            horizontal.getChildren().addAll(itemText, deleteButton);
            scrollContent.getChildren().add(horizontal);
            i++;
        }
    }

    private Button getButton(ContextMenu contextMenu, int currentDate, AbstractMap.SimpleEntry<Integer, String> item) {
        Button deleteButton = new Button("Удалить");
        deleteButton.setStyle("-fx-margin: 10px 15px 20px 5px;");
        deleteButton.setOnAction(e -> {
//                itemList.remove(item);
//                showContextMenu(parent, contextMenu.getX(), contextMenu.getY(), itemList);
            FileStorage.deleteNote(String.format("%s-%s-%s", dateFocus.toLocalDate().getYear(), String.format("%02d", dateFocus.getMonth().getValue()), String.format("%02d", currentDate)), item.getKey());

            calendar.getChildren().clear();
            drawCalendar();
            contextMenu.hide();
        });
        return deleteButton;
    }

    private Button getButtonDelDate(ContextMenu contextMenu, int currentDate) {
        Button deleteButton = new Button("Удалить все");

        deleteButton.setOnAction(e -> {
//                itemList.remove(item);
//                showContextMenu(parent, contextMenu.getX(), contextMenu.getY(), itemList);
            FileStorage.deleteDate(String.format("%s-%s-%s", dateFocus.toLocalDate().getYear(), String.format("%02d", dateFocus.getMonth().getValue()), String.format("%02d", currentDate)));

            calendar.getChildren().clear();
            drawCalendar();
            contextMenu.hide();
        });
        return deleteButton;
    }

    public static CompletableFuture<Integer> sendAsyncGetRequest(String url) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body) // Получаем ответ как строку
                    .thenApply(Integer::parseInt); // Парсим строку в Integer
        }
    }


}