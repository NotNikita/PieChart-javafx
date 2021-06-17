package classes;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.apache.commons.math3.util.Precision;

import java.util.concurrent.atomic.AtomicReference;

public class PieChartNoPath {
    private Pane container;
    private Group group;
    private Circle chartCircle;
    private Label totalNumberLabel;
    private Label peopleLabel;

    // If you want to change direction of chart - just make this false
    private boolean paintChartClockwise;
    private double animationDurationMs = 900;
    private int chartStartAngleDeg = 360 + 90; // 360 is 3 o-clock, 450 is 12 o'clock

    private int slicesCount;     // The number of data for the chart.
    private ObservableList<Data> dataList;
    private ObservableList<Path> middleLinesList;
    private ObservableList<Arc> arcsList;
    private ObservableList<Text> textsList;

    // if parent is apMain -> (355,300, radius = centerY - 110)
    // if parent is pane   -> (268,258, radius = 190)
    private int centerX = 268;
    private int centerY = 258;
    private int radius = 190;
    private int[] angles;
    // An array to hold the angles that divide
    // the wedges.  For convenience, this array
    // is of size dataCt + 1, and it starts with
    // 0 and ends with 360.

    private final static Color[] palette = {
            // 1st
            Color.rgb(250, 98, 187),
            Color.rgb(100, 239, 199),
            Color.rgb(255, 170, 0),
            Color.rgb(255, 208, 0),
            Color.rgb(42, 195, 203),
            Color.rgb(154, 139, 231),
    };
    public PieChartNoPath(ObservableList<Data> data, Pane container, boolean paintChartClockwise) {
        // Set data list and Group
        this.dataList = data;
        this.container = container;
        this.group = new Group();
        this.totalNumberLabel = new Label();
        this.peopleLabel = new Label("people");
        this.paintChartClockwise = paintChartClockwise;
        setData();
    }

    private void refreshData() {
        // Delete any class-local variables to draw the chart.
        slicesCount = 0;
        angles = null;
        // removing old lines,labels from group
        middleLinesList = FXCollections.observableArrayList();
        arcsList = FXCollections.observableArrayList();
        textsList = FXCollections.observableArrayList();
    }
    private void setData() {
        refreshData();
        // The data needed to draw the pie chart is computed and stored
        // in the angles array.  Note that the number of degrees
        // in the i-th wedge is 360*data[i]/dataSum, where dataSum
        // is the total of all the data values.  The cumulative angles
        // are computed, converted to ints, and stored in angles.
        calculateAngles();
    }
    private void calculateAngles(){
        slicesCount = dataList.size();
        angles = new int[slicesCount + 1];
        angles[0] = 90;
        angles[slicesCount] = chartStartAngleDeg;
        double totalDataSum = updateTotalAmountLabel();
        double sum = 0;
        for (int i = 1; i < slicesCount; i++) {
            sum += dataList.get(i-1).getPieValue();
            angles[i] = (int)( 90 + 360*sum/totalDataSum + 0.5);
        }
    }

    public void paint() {
        if (slicesCount == 0) {
            System.out.println("No data available in method Paint.");
            return;
        }

        for (int i = 0; i < slicesCount; i++) {
            createSliceElements(i);
        }
        createLabelsInsideChart();
    }

    private void createSliceElements(int iteration){
        Path middleLinePath = createMiddleLine(iteration);
        Text sliceText = createTextForSlice(iteration);
        Arc createdArc = createArc(iteration);

        animateArcByAngle(createdArc, angles[iteration], createdArc.getLength());
        double targetAngleDeg = calculateMiddleLineAngle(angles, iteration);
        animateTextAndLineMoving(middleLinePath, sliceText, chartStartAngleDeg, targetAngleDeg, true);

        group.getChildren().add(middleLinePath);
        group.getChildren().add(createdArc);
        group.getChildren().add(sliceText);
    }
    private Path createMiddleLine(int currentIteration){
        double[] coordinates = calculateXYofArcsMiddle(currentIteration, 'L');
        Path middleLinePath = new Path( // line from center to middle point of arc
                new MoveTo(centerX, centerY),
                new LineTo(coordinates[0], coordinates[1])
        );
        middleLinePath.setStroke(palette[currentIteration % palette.length]);
        middleLinePath.setStrokeWidth(5);

        try {
            middleLinesList.set( currentIteration,middleLinePath );
        } catch (IndexOutOfBoundsException e){
            middleLinesList.add( middleLinePath );
        }
        return middleLinePath;
    }
    private Arc createArc(int currIteration){
        // Draw the next wedge. The start angle for the wedge
        // is angles[i].  The ending angle is angles[i+1}, so
        // the number of degrees in the wedge is
        // angles[i+1] - angles[i].
        Arc arc = new Arc(centerX,centerY,radius,radius, 0, angles[currIteration+1] - angles[currIteration]);
        arc.setType(ArcType.ROUND);
        arc.setFill(palette[currIteration % palette.length]);
        if (paintChartClockwise) arc.setStartAngle(chartStartAngleDeg);

        Text textForCurrentArc = textsList.get(currIteration);
        arc.lengthProperty().addListener(e->{
            String newText = calculateNewPercentage(arc.getLength());
            textForCurrentArc.setText(newText);
        });
        arc.hoverProperty().addListener( e -> {
            Data d = dataList.get(currIteration);
            Label label = new Label(d.getName() + " is " + d.getPieValue());
            label.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));
            label.setTextFill(Color.SILVER);

        });

        arcsList.add(arc);
        return arc;
    }
    private Text createTextForSlice(int iteration) {
        double[] coordinates = calculateXYofArcsMiddle(iteration, 'T');
        double arcLength = angles[iteration+1] - angles[iteration];

        Text text = new Text(coordinates[0], coordinates[1], calculateNewPercentage(arcLength));
        text.setFill(palette[iteration % palette.length]);
        text.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));
        text.setWrappingWidth(90);
        text.setTextAlignment(TextAlignment.LEFT);
        text.setTranslateY(5);

        // Выравнивание относительно четверти на графике
        text.xProperty().addListener(e->{
            if (text.getX() < centerX)
                text.setTranslateX(text.getWrappingWidth() / -2.0);
            else if (text.getX() > centerX)
                text.setTranslateX(-5);
        });
        textsList.add(text);
        return text;
    }
    private void createLabelsInsideChart(){
        chartCircle = new Circle(centerX, centerY, 145);
        chartCircle.getStyleClass().add("circle_above_chart");

        totalNumberLabel = new Label("100,000");
        totalNumberLabel.setLayoutX(182);
        totalNumberLabel.setLayoutY(193);
        totalNumberLabel.setPrefWidth(178);
        totalNumberLabel.getStyleClass().add("total_amount");

        peopleLabel.setLayoutX(223);
        peopleLabel.setLayoutY(246);
        peopleLabel.getStyleClass().add("people_label");

        group.getChildren().add(chartCircle);
        container.getChildren().add(group);
        container.getChildren().add(chartCircle);
        container.getChildren().add(peopleLabel);
        container.getChildren().add(totalNumberLabel);

        chartCircle.toFront();
        peopleLabel.toFront();
        totalNumberLabel.toFront();
    }

    //Calculators
    private String calculateNewPercentage(double arcLength){
        double currentPercentage = arcLength*100 / 360;
        return  String.valueOf(Precision.round(currentPercentage,1)) + '%';
    }
    private double[] calculateXYofArcsMiddle(int iteration, char type){
        // char type: 'L' stands for LINE, 'T' for TEXT
        double overLength = 10;
        if (type == 'T') overLength = 30;

        double[] result = new double[2];
        double angle = calculateMiddleLineAngle(angles, iteration);
        double currentAngleRadians = Math.toRadians(-angle);
        result[0] = centerX + (radius + overLength)*Math.cos(currentAngleRadians);
        result[1] = centerY + (radius + overLength)*Math.sin(currentAngleRadians);

        return result;
    }
    private double calculateMiddleLineAngle(int[] angles, int iteration){
        return (angles[iteration+1]-angles[iteration])/2.0 + angles[iteration];
    }
    private double calculateIncrementerPerPulse(double startAngleRad, double endAngleRad){
        double animationDurationSec = animationDurationMs / 1000.0;
        double updateFrequency = 1 / 60.0; // 60 fps
        return Math.abs(endAngleRad - startAngleRad) / (animationDurationSec / updateFrequency);
    }
    public double updateTotalAmountLabel(){
        int totalDataSum = 0;
        for (PieChart.Data node: dataList) {
            totalDataSum += node.getPieValue();
        }
        totalNumberLabel.setText(String.valueOf(totalDataSum));
        return totalDataSum;
    }

    public void addChartSlice(Data data) {
        int[] oldAngles = new int[this.angles.length];
        System.arraycopy(angles, 0, oldAngles, 0, oldAngles.length);
        for (Data node: dataList) {
            if (node.getName().equals(data.getName()))
            {
                node.setPieValue( node.getPieValue() + data.getPieValue() );
                calculateAngles();
                moveAndAnimateElements(oldAngles, slicesCount);
                return;
            }
        }
        dataList.add(data);

        calculateAngles();
        createSliceElements(dataList.size() - 1);
        moveAndAnimateElements(oldAngles, slicesCount - 1);
        updateTotalAmountLabel();
    }
    public void editChartSlice(Data data) {
        // 1. Array of old arc's angles
        int[] oldAngles = new int[this.angles.length];
        System.arraycopy(angles, 0, oldAngles, 0, oldAngles.length);
        // 2. Editing dataList's one selected arc (MAIN LOGIC)
        boolean nameFoundInList = false;
        for (Data node: dataList) {
            if (node.getName().equals(data.getName()))
            {
                node.setPieValue( data.getPieValue() );
                nameFoundInList = true;
            }
        }
        if (!nameFoundInList) return;
        // 3. calculating new angles for arcs from dataList
        calculateAngles();
        // 4. Animating text,arc,line
        moveAndAnimateElements(oldAngles, slicesCount);
        updateTotalAmountLabel();
    }
    public void deleteChartSlice(String nodeName) {
        // 1. Delete dataList's one selected arc (MAIN LOGIC)
        boolean nameFoundInList = false;
        int indexOfDeletedNode = 0;
        for (Data node: dataList) {
            if (node.getName().equals(nodeName)){
                dataList.remove(node);
                this.slicesCount = dataList.size();
                nameFoundInList = true;
                break;
            }
            indexOfDeletedNode++;
        }
        if (!nameFoundInList) return;
        deleteSliceElements(indexOfDeletedNode);
        updateTotalAmountLabel();
    }

    private void animateArcByAngle(Arc element, double targetAngle, double targetLength){
        element.setLength(0);
        // We want to animateArcByAngle pieSlice from 360* to another degree
        // and length to scale from (0 to its size)  as well
        KeyValue angleValue = new KeyValue(element.startAngleProperty(), targetAngle);
        KeyValue lengthValue = new KeyValue(element.lengthProperty(), targetLength);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(animationDurationMs), angleValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(animationDurationMs), lengthValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }

    // Edit arc functionality
    private void moveAndAnimateElements(int[] oldAngles, int amount){
        // We use amount because in Add\Delete operations we dont want
        // to animate last element in this func, we have others for this
        for (int j = 0; j < amount; j++) {
            // TODO: Может все таки Listener
            double targetArcLength = angles[j+1] - angles[j];
            if (targetArcLength == 0.0)
                animateFadeDisappear(middleLinesList.get(j), textsList.get(j));
            else if (arcsList.get(j).getLength() == 0.0 && targetArcLength > 0.0)
                animateFadeAppear(middleLinesList.get(j), textsList.get(j));

            moveArcByAngle(arcsList.get(j), angles[j], targetArcLength);

            double startMiddleAngle = calculateMiddleLineAngle(oldAngles, j);
            double endMiddleAngle = calculateMiddleLineAngle(angles, j);
            // Эта условие отлавливает направление движения текста и линии - по часовой или против
            if (startMiddleAngle > endMiddleAngle){
                //clockwise
                animateTextAndLineMoving(middleLinesList.get(j), textsList.get(j), startMiddleAngle, endMiddleAngle, true);
            } else {
                //counterclock
                animateTextAndLineMoving(middleLinesList.get(j), textsList.get(j), startMiddleAngle, endMiddleAngle, false);
            }
        }
    }
    private void moveArcByAngle(Arc element, double targetAngle, double targetLength){
        //Arc already has its old length, but angle = 0
        //element.setStartAngle(startAngle);
        // animating
        KeyValue angleValue = new KeyValue(element.startAngleProperty(), targetAngle);
        KeyValue lengthValue = new KeyValue(element.lengthProperty(), targetLength);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(animationDurationMs), angleValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(animationDurationMs), lengthValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }
    private void animateTextAndLineMoving(Path middleLine, Text text, double startAngleDegrees,double targetAngleDegrees, boolean clockwise){

        double startAngleRadians = Math.toRadians(-startAngleDegrees);
        double targetAngleRadians = Math.toRadians(-targetAngleDegrees);
        double newIncrementer = calculateIncrementerPerPulse(startAngleRadians, targetAngleRadians);
        // Getting our target  line
        LineTo line = (LineTo) middleLine.getElements().get(1);

        Thread th = new Thread(()-> new AnimationTimer()
        {
            private double prevIncrement = 1 / 60.0; // 60 FPS view updating
            @Override
            public void handle(long currentNanoTime)
            {
                // T is for angle and speed at the same time
                //В зависимости от увеличения или уменьшения угла, надо менять знак
                // + по часовой, - против
                prevIncrement += newIncrementer;
                double currAngleRad = clockwise? startAngleRadians + prevIncrement: startAngleRadians - prevIncrement;

                double xLine = centerX + (radius+10) * Math.cos(currAngleRad);
                double yLine = centerY + (radius+10) * Math.sin(currAngleRad);
                double xText = centerX + (radius+30) * Math.cos(currAngleRad);
                double yText = centerY + (radius+30) * Math.sin(currAngleRad);

                text.setX(xText);
                text.setY(yText);
                line.setX(xLine);
                line.setY(yLine);
                if ((Math.abs(currAngleRad - targetAngleRadians) < 0.02)) this.stop();
            }

        }.start());
        th.start();
    }

    // Delete
    private void deleteSliceElements(int iteration){
        // 1. Array of old arc's angles
        int[] oldAngles = new int[angles.length];
        System.arraycopy(angles, 0, oldAngles, 0, oldAngles.length);
        calculateAngles();
        // 2. DELETE ANIMATION
        Path lineToDelete = middleLinesList.get(iteration);
        Arc arcToDelete = arcsList.get(iteration);
        Text textToDelete = textsList.get(iteration);

        middleLinesList.remove(iteration);
        arcsList.remove(iteration);
        textsList.remove(iteration);
        animateDeleteElements(arcToDelete, lineToDelete, textToDelete, oldAngles).setOnFinished(event -> {
            group.getChildren().remove(arcToDelete);
            group.getChildren().remove(lineToDelete);
            group.getChildren().remove(textToDelete);
        });
    }
    private Timeline animateDeleteElements(Arc arcToDelete, Path linePath, Text text, int[] oldAngles){
        // Here, i want to animate radius and length to 0, so the slice will become tiny at the end
        KeyValue lengthValue = new KeyValue(arcToDelete.lengthProperty(), 0);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(animationDurationMs), lengthValue);

        Timeline tl = new Timeline();
        // if we are deleting first element in List(the only one starts in 12 o'clock) - we dont need to move it
        if (arcToDelete.getStartAngle() != 90.0) {
            KeyValue angleValue = new KeyValue(arcToDelete.startAngleProperty(), chartStartAngleDeg);
            KeyFrame keyFrame1 = new KeyFrame(Duration.millis(animationDurationMs), angleValue);
            tl.getKeyFrames().add(keyFrame1);
        }
        tl.getKeyFrames().add(keyFrame2);
        tl.play();

        animateFadeDisappear(linePath, text);
        moveAndAnimateElements(oldAngles, slicesCount);
        return tl;
    }

    //Text & Line fading animation
    private void animateFadeDisappear(Path linePath, Text text){
        FadeTransition ftText = new FadeTransition(Duration.millis(animationDurationMs / 2), text);
        FadeTransition ftLine = new FadeTransition(Duration.millis(animationDurationMs / 2), linePath);
        ftText.setFromValue(1.0);
        ftLine.setFromValue(1.0);

        ftText.setToValue(0.0);
        ftLine.setToValue(0.0);
        ftText.play();
        ftLine.play();
    }
    private void animateFadeAppear(Path linePath, Text text){
        FadeTransition ftText = new FadeTransition(Duration.millis(animationDurationMs), text);
        FadeTransition ftLine = new FadeTransition(Duration.millis(animationDurationMs), linePath);
        ftText.setFromValue(0.0);
        ftLine.setFromValue(0.0);

        ftText.setToValue(1.0);
        ftLine.setToValue(1.0);
        ftText.play();
        ftLine.play();
    }
}