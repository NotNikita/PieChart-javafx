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

    // A PieChartCanvas can display a pie chart, based on an array
    // of data passed to it in its setData() method.  There can be
    // up to 12 wedges in the pie.

    private int dataCount;     // The number of data values for the chart.
    private ObservableList<Data> dataList;
    private ObservableList<Path> middleLinesList;
    private ObservableList<Text> labelsList;
    private Group group;

    private int centerX = 355;   // Center point of circle.
    private int centerY = 300;
    private int radius = centerY - 110;
    private int[] angles;   // An array to hold the angles that divide
    // the wedges.  For convenience, this array
    // is of size dataCt + 1, and it starts with
    // 0 and ends with 360.

    private final static Color[] palette = {  // Colors for the chart.
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.CYAN
    };


    public PieChartNoPath(ObservableList<PieChart.Data> data, Group root) {
        // Set data list and Group
        dataList = data;
        this.group = root;
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

        this.dataCount = dataList.size();
        angles = new int[dataCount + 1];
        middleLinesList = FXCollections.observableArrayList();
        labelsList = FXCollections.observableArrayList();

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

            animateLineByAngle(middleLinePath);
            animateArcByAngle(createdArc, angles[i]);
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
        middleLinePath.setStroke(palette[currentIteration % palette.length]);middleLinePath.setStrokeWidth(2);

        middleLinesList.add(middleLinePath);
        return middleLinePath;
    }
    private Arc createArc(int currentIteration){
        // Draw the next wedge.  The start angle for the wedge
        // is angles[i].  The ending angle is angles[i+1}, so
        // the number of degrees in the wedge is
        // angles[i+1] - angles[i].
        Arc arc = new Arc(centerX,centerY,radius,radius, 0, angles[currentIteration+1] - angles[currentIteration]);
        //Line lineForArc = new Line(centerX,centerY,radius,)
        arc.setType(ArcType.ROUND);
        arc.setFill(palette[currentIteration % palette.length]);
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

        // Correcting position of text, because it can be burried into chart.
        if (result[0] < centerX && result[1] <= centerY){ // 2 part of coordinates
            result[0] -= 35;
            result[1] -= 10;
        }
        else if (result[0] < centerX && result[1] >= centerY && type == 'T'){ // 3 part of coordinates
            result[0] -= 45;
            result[1] += 15;
        }
        return result;
    }

    public void addNode(Data data) {
        for (Data node: dataList) {
            if (node.getName().equals(data.getName()))
                node.setPieValue( node.getPieValue() + data.getPieValue() );
        }
        setData();
    }
    public void editNode(Data data) {
        for (Data node: dataList) {
            if (node.getName().equals(data.getName()))
                node.setPieValue( data.getPieValue() );
        }
        setData();
    }
    public boolean deleteNode(String nodeName) {
        for (Data node: dataList) {
            if (node.getName().equals(nodeName)){
                dataList.remove(node);
                setData();
                return true;
            }
        }

        return false;
    }

    private void animateLineByAngle(Path middleLine){
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
    private void animateArcByAngle(Arc element, double targetAngle){
        // We want to animateArcByAngle pieSlice from 0* to another degree
        KeyValue angleValue = new KeyValue(element.startAngleProperty(), targetAngle);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(600), angleValue);
        Timeline tl = new Timeline();
        tl.getKeyFrames().add(keyFrame);
        tl.play();
    }
    private void animateText(Text textToAnimate){
        FadeTransition ft = new FadeTransition(Duration.seconds(2), textToAnimate);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}
