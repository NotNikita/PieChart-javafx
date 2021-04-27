package Classes;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.commons.math3.util.Precision;

public class PieChartNoPath {

    // If you want to change direction of chart - just make this false
    private boolean paintChartClockwise;

    private int dataCount;     // The number of data values for the chart.
    private ObservableList<Data> dataList;
    private ObservableList<Path> middleLinesList;
    private ObservableList<Arc> arcsList;
    private ObservableList<Text> labelsList;
    private Group group;

    // if parent is apMain -> (355,300, radius = centerY - 110)
    // if parent is pane   -> (268,258, radius = 190)
    private int centerX = 268;
    private int centerY = 258;
    private int radius = 190;
    private int[] angles;   // An array to hold the angles that divide
    // the wedges.  For convenience, this array
    // is of size dataCt + 1, and it starts with
    // 0 and ends with 360.

    private final static Color[] palette = {  // Colors for the chart.
            // 1st
            Color.rgb(250, 98, 187),
            Color.rgb(100, 239, 199),
            Color.rgb(255, 170, 0),
            Color.rgb(255, 208, 0),
            Color.rgb(42, 195, 203),
            Color.rgb(154, 139, 231),
    };


    public PieChartNoPath(ObservableList<PieChart.Data> data, Group root, boolean paintChartClockwise) {
        // Set data list and Group
        dataList = data;
        this.group = root;
        this.paintChartClockwise = paintChartClockwise;
        setData();
    }

    private void clearData() {
        // Delete any class-local variables to draw the chart.
        dataCount = 0;
        angles = null;
        // removing old lines,labels from group
        group.getChildren().clear();
    }
    private void setData() {
        // The data needed to draw the pie chart is computed and stored
        // in the angles array.  Note that the number of degrees
        // in the i-th wedge is 360*data[i]/dataSum, where dataSum
        // is the total of all the data values.  The cumulative angles
        // are computed, converted to ints, and stored in angles.
        clearData();

        //this.dataCount = dataList.size();
        //angles = new int[dataCount + 1];
        middleLinesList = FXCollections.observableArrayList();
        arcsList = FXCollections.observableArrayList();
        labelsList = FXCollections.observableArrayList();

        calculateAngles();
    }
    private void calculateAngles(){
        this.dataCount = dataList.size();
        angles = new int[dataCount + 1];
        angles[0] = 0;
        angles[dataCount] = 360;
        double totalDataSum = 0;
        for (Data node: dataList) {
            totalDataSum += node.getPieValue();
        }
        double sum = 0;
        for (int i = 1; i < dataCount; i++) {
            sum += dataList.get(i-1).getPieValue();
            angles[i] = (int)(360*sum/totalDataSum + 0.5);
        }
    }


    public Group paint() {
        if (dataCount == 0) {
            System.out.println("No data available in method Paint.");
            return null;
        }

        for (int i = 0; i < dataCount; i++) {
            // Creating line from center of Chart to the middle of the arc
            Path middleLinePath = createMiddleLine(i);
            // Creating arc by reverse clock moving
            Arc createdArc = createArc(i);
            // Creating text for slice
            Text sliceText = createTextForSlice(createdArc.getLength(), i);

            animateLineFromCenter(middleLinePath);
            animateArcByAngle(createdArc, angles[i], createdArc.getLength());
            animateText(sliceText);

            group.getChildren().add(middleLinePath);
            group.getChildren().add(createdArc);
            group.getChildren().add(sliceText);
        }

        return group;
    }
    private Path createMiddleLine(int currentIteration){
        double[] coordinates = calculateXYofArcsMiddle(currentIteration, 10, 'L');
        Path middleLinePath = new Path(
                // line from center to second point of arc
                new MoveTo(centerX, centerY),
                new LineTo(coordinates[0], coordinates[1])
        );
        middleLinePath.setStroke(palette[currentIteration % palette.length]);middleLinePath.setStrokeWidth(5);

        //middleLinesList.add(middleLinePath);
        try {
            middleLinesList.set(currentIteration,middleLinePath);
        } catch (IndexOutOfBoundsException e){
            middleLinesList.add(middleLinePath);
        }
        return middleLinePath;
    }
    private Arc createArc(int currentIteration){
        // Draw the next wedge.  The start angle for the wedge
        // is angles[i].  The ending angle is angles[i+1}, so
        // the number of degrees in the wedge is
        // angles[i+1] - angles[i].
        Arc arc = new Arc(centerX,centerY,radius,radius, 0, angles[currentIteration+1] - angles[currentIteration]);
        arc.setType(ArcType.ROUND);
        arc.setFill(palette[currentIteration % palette.length]);
        if (paintChartClockwise) arc.setStartAngle(360);

        arcsList.add(arc);
        return arc;
    }
    private Text createTextForSlice(double length, int iteration) {
        double[] coordinates = calculateXYofArcsMiddle(iteration, 20, 'T');

        String rounded = String.valueOf(Precision.round(length*100 / 360, 2)) + '%';
        Text text = new Text(coordinates[0], coordinates[1], rounded);
        text.setFill(palette[iteration % palette.length]);
        text.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));

        labelsList.add(text);
        return text;
    }

    private double[] calculateXYofArcsMiddle(int iteration, double overLength, char type){
        // char type: 'L' stands for LINE, 'T' for TEXT
        double[] result = new double[2];
        double angle = (angles[iteration+1]-angles[iteration])/2.0 + angles[iteration];
        double currentAngleRadians = Math.toRadians(-angle);
        result[0] = centerX + (radius + overLength)*Math.cos(currentAngleRadians);
        result[1] = centerY + (radius + overLength)*Math.sin(currentAngleRadians);

        if (type == 'L') return result;
        // Correcting position of text, because it can be burried into chart.
        if (result[0] < centerX && result[1] < centerY){ // 2 part of coordinates
            result[0] -= 49; //35
            result[1] -= 7; // 10
        }
        else if (result[0] < centerX && result[1] >= centerY){ // 3 part of coordinates
            result[0] -= 58; //45
            result[1] += 15;
        }
        return result;
    }

    public void addNode(Data data) {
        //boolean nameFoundInList = false;
        int[] oldAngles = new int[this.angles.length];
        System.arraycopy(angles, 0, oldAngles, 0, oldAngles.length);
        for (Data node: dataList) {
            if (node.getName().equals(data.getName()))
            {

                node.setPieValue( node.getPieValue() + data.getPieValue() );
                //nameFoundInList = true;
                calculateAngles();
                moveAndAnimateElements(oldAngles, dataCount);
                return;
            }
        }
        dataList.add(data);
        //if(!nameFoundInList){
        //    dataList.add(data);
        //}

        calculateAngles();
        // here add arc line and label for new data
        Path middleLinePath = createMiddleLine(dataList.size()-1);
        Arc createdArc = createArc(dataList.size()-1);
        Text sliceText = createTextForSlice(createdArc.getLength(), dataList.size()-1);
        animateLineFromCenter(middleLinePath);
        animateArcByAngle(createdArc, this.angles[dataList.size() - 1], createdArc.getLength());
        //animateArcFromCenter(createdArc, createdArc.getLength(), createdArc.getRadiusX(), createdArc.getRadiusY());
        animateText(sliceText);
        group.getChildren().add(middleLinePath);
        group.getChildren().add(createdArc);
        group.getChildren().add(sliceText);
        moveAndAnimateElements(oldAngles, dataCount - 1);
    }
    public void editNode(Data data) {
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
        moveAndAnimateElements(oldAngles, dataCount);
    }
    public void deleteNode(String nodeName) {
        // 1. Delete dataList's one selected arc (MAIN LOGIC)
        boolean nameFoundInList = false;
        int indexOfDeletedNode = 0;
        for (Data node: dataList) {
            if (node.getName().equals(nodeName)){
                dataList.remove(node);
                this.dataCount = dataList.size();
                nameFoundInList = true;
                break;
            }
            indexOfDeletedNode++;
        }
        if (!nameFoundInList) return;
        // 2. Array of old arc's angles
        int[] oldAngles = new int[this.angles.length];
        System.arraycopy(angles, 0, oldAngles, 0, oldAngles.length);
        calculateAngles();
        // 3. DELETE ANIMATION
        Path lineToDelete = middleLinesList.get(indexOfDeletedNode);
        Arc arcToDelete = arcsList.get(indexOfDeletedNode);
        Text textToDelete = labelsList.get(indexOfDeletedNode);

        middleLinesList.remove(indexOfDeletedNode);
        arcsList.remove(indexOfDeletedNode);
        labelsList.remove(indexOfDeletedNode);
        animateDeleteElements(arcToDelete, lineToDelete, textToDelete, oldAngles).setOnFinished(event -> {
            // 4. EDIT

            group.getChildren().remove(arcToDelete);
            group.getChildren().remove(lineToDelete);
            group.getChildren().remove(textToDelete);
        });
    }

    private void animateLineFromCenter(Path middleLine){
        // Getting our target  line
        LineTo lnTo = (LineTo) middleLine.getElements().get(1);
        // Setting property and its final value
        KeyValue lnToValueX = new KeyValue(lnTo.xProperty(), lnTo.getX());
        KeyValue lnToValueY = new KeyValue(lnTo.yProperty(), lnTo.getY());
        // Setting property's start value to the center of circle
        lnTo.setX(centerX);
        lnTo.setY(centerY);
        // Animating and timing
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(600), lnToValueX);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(600), lnToValueY);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame1);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }
    private void animateArcByAngle(Arc element, double targetAngle, double targetLength){
        element.setLength(0);
        // We want to animateArcByAngle pieSlice from 360* to another degree
        // and length to scale from (0 to its size)  as well
        KeyValue angleValue = new KeyValue(element.startAngleProperty(), targetAngle);
        KeyValue lengthValue = new KeyValue(element.lengthProperty(), targetLength);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(600), angleValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(600), lengthValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }
    private void animateArcFromCenter(Arc element, double targetLength, double radX, double radY){
        element.setLength(0);
        element.setRadiusX(0);
        element.setRadiusY(0);
        KeyValue radiusXValue = new KeyValue(element.radiusXProperty(), radX);
        KeyValue radiusYValue = new KeyValue(element.radiusYProperty(), radY);
        KeyValue lengthValue = new KeyValue(element.lengthProperty(), targetLength);

        KeyFrame keyFrame = new KeyFrame(Duration.millis( 1000), radiusXValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(1000), radiusYValue);
        KeyFrame keyFrame3 = new KeyFrame(Duration.millis(1000), lengthValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.getKeyFrames().add(keyFrame2);
        tl.getKeyFrames().add(keyFrame3);
        tl.play();
    }
    private void animateText(Text textToAnimate){
        FadeTransition ft = new FadeTransition(Duration.seconds(2), textToAnimate);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // Edit arc functionality
    private void moveAndAnimateElements(int[] oldAngles, int amount){
        // We use amount because in Add\Delete operations we dont want
        // to animate last element in this func, we have others for this
        for (int j = 0; j < amount; j++) {
            animateArcFromAngleToAngle(arcsList.get(j), oldAngles[j], angles[j], angles[j+1] - angles[j]);

            double[] newLineXY = calculateXYofArcsMiddle(j, 10,'L');
            animateLineMoving( middleLinesList.get(j), newLineXY[0], newLineXY[1]);

            double[] newTextXY = calculateXYofArcsMiddle(j, 20,'T');
            animateTextMoving(labelsList.get(j), newTextXY[0], newTextXY[1], angles[j+1] - angles[j]);
        }
    }
    private void animateArcFromAngleToAngle(Arc element, double startAngle, double targetAngle, double targetLength){
        //Arc already has its old length, but angle = 0
        element.setStartAngle(startAngle);
        // animating
        KeyValue angleValue = new KeyValue(element.startAngleProperty(), targetAngle);
        KeyValue lengthValue = new KeyValue(element.lengthProperty(), targetLength);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(900), angleValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(900), lengthValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }
    private void animateLineMoving(Path middleLine, double xEnd, double yEnd){
        // Getting our target  line
        LineTo lnTo = (LineTo) middleLine.getElements().get(1);
        // Setting property and its final value
        KeyValue lnToValueX = new KeyValue(lnTo.xProperty(), xEnd);
        KeyValue lnToValueY = new KeyValue(lnTo.yProperty(), yEnd);

        // Animating and timing
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(900), lnToValueX);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(900), lnToValueY);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame1);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }
    private void animateTextMoving(Text text, double xEnd, double yEnd, double newLenght) {
        // New text
        String NewText = String.valueOf(Precision.round(newLenght*100 / 360, 2)) + '%';
        text.setText(NewText);

        // Setting property and its final value
        KeyValue lnToValueX = new KeyValue(text.xProperty(), xEnd);
        KeyValue lnToValueY = new KeyValue(text.yProperty(), yEnd);

        // Animating and timing
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(900), lnToValueX);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(900), lnToValueY);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame1);
        tl.getKeyFrames().add(keyFrame2);
        tl.play();
    }

    // Delete
    private Timeline animateDeleteElements(Arc arcToDelete, Path linePath, Text text, int[] oldAngles){
        // Here, i want to animate radius and length to 0, so the slice will become tiny at the end

        KeyValue lengthValue = new KeyValue(arcToDelete.lengthProperty(), 0);
        KeyValue angleValue = new KeyValue(arcToDelete.startAngleProperty(), 360);
        LineTo actualLine  = (LineTo) linePath.getElements().get(1);
        KeyValue xValue = new KeyValue(actualLine.xProperty(), centerX + radius);
        KeyValue yValue = new KeyValue(actualLine.yProperty(), centerY);

        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(900), angleValue);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(900), lengthValue);
        KeyFrame keyFrame3 = new KeyFrame(Duration.millis(900), xValue);
        KeyFrame keyFrame4 = new KeyFrame(Duration.millis(900), yValue);

        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame1);
        tl.getKeyFrames().add(keyFrame2);
        tl.getKeyFrames().add(keyFrame3);
        tl.getKeyFrames().add(keyFrame4);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.7), text);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.play();
        tl.play();
        moveAndAnimateElements(oldAngles, dataCount);
        return tl;
    }
}
