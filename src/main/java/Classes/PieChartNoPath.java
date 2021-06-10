package Classes;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.apache.commons.math3.util.Precision;

public class PieChartNoPath {

    // If you want to change direction of chart - just make this false
    private boolean paintChartClockwise;

    private int slicesCount;     // The number of data values for the chart.
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
    private int[] angles;
    // An array to hold the angles that divide
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
    public PieChartNoPath(ObservableList<Data> data, Group root, boolean paintChartClockwise) {
        // Set data list and Group
        dataList = data;
        this.group = root;
        this.paintChartClockwise = paintChartClockwise;
        setData();
    }

    private void refreshData() {
        // Delete any class-local variables to draw the chart.
        slicesCount = 0;
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
        refreshData();
        middleLinesList = FXCollections.observableArrayList();
        arcsList = FXCollections.observableArrayList();
        labelsList = FXCollections.observableArrayList();

        calculateAngles();
    }
    private void calculateAngles(){
        this.slicesCount = dataList.size();
        angles = new int[slicesCount + 1];
        angles[0] = 0;
        angles[slicesCount] = 360;
        double totalDataSum = 0;
        for (Data node: dataList) {
            totalDataSum += node.getPieValue();
        }
        double sum = 0;
        for (int i = 1; i < slicesCount; i++) {
            sum += dataList.get(i-1).getPieValue();
            angles[i] = (int)(360*sum/totalDataSum + 0.5);
        }
    }

    public Group paint() {
        if (slicesCount == 0) {
            System.out.println("No data available in method Paint.");
            return null;
        }

        for (int i = 0; i < slicesCount; i++) {
            createSliceElements(i);
        }
        return group;
    }
    private void createSliceElements(int iteration){
        Path middleLinePath = createMiddleLine(iteration);
        Arc createdArc = createArc(iteration);
        Text sliceText = createTextForSlice(createdArc.getLength(), iteration);


        animateLineFromCenter(middleLinePath);
        animateArcByAngle(createdArc, angles[iteration], createdArc.getLength());
        animateText(sliceText);

        group.getChildren().add(middleLinePath);
        group.getChildren().add(createdArc);
        group.getChildren().add(sliceText);
    }
    private Path createMiddleLine(int currentIteration){
        double[] coordinates = calculateXYofArcsMiddle(currentIteration, 'L');
        Path middleLinePath = new Path(
                // line from center to middle point of arc
                new MoveTo(centerX, centerY),
                new LineTo(coordinates[0], coordinates[1])
        );
        middleLinePath.setStroke(palette[currentIteration % palette.length]);
        middleLinePath.setStrokeWidth(5);

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
        double[] coordinates = calculateXYofArcsMiddle(iteration, 'T');

        String rounded = String.valueOf(Precision.round(length*100 / 360, 1)) + '%';
        Text text = new Text(coordinates[0], coordinates[1], rounded);
        text.setFill(palette[iteration % palette.length]);
        text.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));
        text.setWrappingWidth(90);
        text.setTextAlignment(TextAlignment.LEFT);
        text.setTranslateX(-20);
        labelsList.add(text);
        return text;
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

        if (type =='T'){
            if (labelsList.size() == slicesCount){
                //if (result[0] < centerX) labelsList.get(iteration).setTranslateX(-55);
                //else labelsList.get(iteration).setTranslateX(-10);
            }
        }

        return result;
    }
    private double calculateMiddleLineAngle(int[] angles, int iteration){
        return (angles[iteration+1]-angles[iteration])/2.0 + angles[iteration];
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
        createSliceElements(dataList.size()-1);
        moveAndAnimateElements(oldAngles, slicesCount - 1);
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
    private void animateText(Text textToAnimate){
        FadeTransition ft = new FadeTransition(Duration.millis(2000), textToAnimate);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    // Edit arc functionality
    private void moveAndAnimateElements(int[] oldAngles, int amount){
        // We use amount because in Add\Delete operations we dont want
        // to animate last element in this func, we have others for this
        for (int j = 0; j < amount; j++) {
            moveArcByAngle(arcsList.get(j), oldAngles[j], angles[j], angles[j+1] - angles[j]);

            double[] newXYofLine = calculateXYofArcsMiddle(j,'L');
            double newMiddleAngle = calculateMiddleLineAngle(oldAngles, j);
            double[] newXYofText = calculateXYofArcsMiddle(j,'T');
            Text text = labelsList.get(j);
            //This one is hard
            if (angles[j] < oldAngles[j] || (j==0 && (oldAngles[j+1]-oldAngles[j]) > (angles[j+1]-angles[j]))){
                //clockwise
                animateLineMoving( middleLinesList.get(j), newXYofLine[0], newXYofLine[1], newMiddleAngle, true);
                animateTextMoving(text, newXYofText, angles[j+1] - angles[j], newMiddleAngle, true);
            } else {
                //counterclock
                animateLineMoving( middleLinesList.get(j), newXYofLine[0], newXYofLine[1], newMiddleAngle, false);
                animateTextMoving(text, newXYofText, angles[j+1] - angles[j], newMiddleAngle, false);
            }
        }
    }
    private void moveArcByAngle(Arc element, double startAngle, double targetAngle, double targetLength){
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
    private void animateLineMoving(Path middleLine, double xEnd, double yEnd, double startAngleDegrees, boolean clockwise){
        final double startAngleRadians = Math.toRadians(-startAngleDegrees);
        final long startNanoTime = System.nanoTime();
        // Getting our target  line
        LineTo lnTo = (LineTo) middleLine.getElements().get(1);

        Thread th = new Thread(()-> new AnimationTimer()
        {
            @Override
            public void handle(long currentNanoTime)
            {
                // T is for angle and speed at the same time
                //В зависимости от увеличения или уменьшения угла, надо менять знак
                // + по часовой, - против
                double incrementer = (System.nanoTime() - startNanoTime) / 1000000000.0;
                double t = clockwise? startAngleRadians + incrementer: startAngleRadians - incrementer;

                double x = centerX + (radius+10) * Math.cos(t);
                double y = centerY + (radius+10) * Math.sin(t);

                lnTo.setX(x);
                lnTo.setY(y);
                if ((Math.abs(x - xEnd) < 3) && (Math.abs(y - yEnd) < 3)) this.stop();
            }
        }.start());
        th.start();


    }
    private void animateTextMoving(Text text, double[] textFinalPosition, double newLength, double startAngleDegrees, boolean clockwise) {
        // Target text
        String newText = String.valueOf(Precision.round(newLength*100 / 360, 1)) + '%';
        text.setText(newText);

        final double startAngleRadians = Math.toRadians(-startAngleDegrees);
        final long startNanoTime = System.nanoTime();
        final long endNanoTime = startNanoTime + 900000000;
        Thread th = new Thread(()-> new AnimationTimer()
        {
            @Override
            public void handle(long currentNanoTime)
            {
                // T is for angle and speed at the same time
                //В зависимости от увеличения или уменьшения угла, надо менять знак
                // + по часовой, - против
                double incrementer = (currentNanoTime - startNanoTime) / 1000000000.0;
                System.out.println(incrementer);
                double t = clockwise? startAngleRadians + incrementer: startAngleRadians - incrementer;

                //TODO: по одному принципу перемещать линии и кусочки пирога, в одной параллельой транзакции
                double x = centerX + (radius+30) * Math.cos(t);
                double y = centerY + (radius+30) * Math.sin(t);

                // Выравнивание относительно четверти на графике
                if (x < centerX && y < centerY )
                {
                    text.setTranslateX(-40);
                    text.setTranslateY(5);
                }
                else if (x < centerX && y > centerY)
                {
                    text.setTranslateX(-38);
                    text.setTranslateY(9);
                }
                else if (x > centerX && y < centerY)
                {
                    text.setTranslateX(-10);
                }
                else if (x > centerX && y > centerY)
                {
                    text.setTranslateX(-10);
                    text.setTranslateY(5);
                }

                if ((Math.abs(y - centerY) < 20) && x < centerX)
                    text.setTranslateX(-35 - text.getWrappingWidth() / 10);

                text.setX(x);
                text.setY(y);
                if ((Math.abs(x - textFinalPosition[0]) < 3) && (Math.abs(y - textFinalPosition[1]) < 3)) this.stop();
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
        Text textToDelete = labelsList.get(iteration);

        middleLinesList.remove(iteration);
        arcsList.remove(iteration);
        labelsList.remove(iteration);
        animateDeleteElements(arcToDelete, lineToDelete, textToDelete, oldAngles).setOnFinished(event -> {
            group.getChildren().remove(arcToDelete);
            group.getChildren().remove(lineToDelete);
            group.getChildren().remove(textToDelete);
        });
    }
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
        moveAndAnimateElements(oldAngles, slicesCount);
        return tl;
    }
}
