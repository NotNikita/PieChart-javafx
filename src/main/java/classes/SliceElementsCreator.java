package classes;

import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;

public class SliceElementsCreator {

    private final static Color[] palette = {
            // 1st
            Color.rgb(250, 98, 187),
            Color.rgb(100, 239, 199),
            Color.rgb(255, 170, 0),
            Color.rgb(255, 208, 0),
            Color.rgb(42, 195, 203),
            Color.rgb(154, 139, 231),
    };

    PieChart.Data sliceData;
    Path createdLine;
    Text createdText;
    Arc createdArc;

    private int centerX = 268;
    private int centerY = 258;
    private int radius = 190;
    private double animationDurationMs;
    private int[] angles;

    public SliceElementsCreator(double animationDurationMs){
        this.animationDurationMs = animationDurationMs;
    }

    public ArrayList<Object> createSliceElements(int iteration, PieChart.Data _sliceData, int[] _angles){
        this.sliceData = _sliceData;
        this.angles = _angles;

        createMiddleLine(iteration);
        createTextForSlice(iteration);
        createArc(iteration);

        ArrayList<Object> container = new ArrayList<>();
        container.add(0, createdLine);
        container.add(1, createdText);
        container.add(2, createdArc);
        return container;
    }
    private void createMiddleLine(int currentIteration){
        double[] coordinates = calculateXYofArcsMiddle(currentIteration, 'L');
        this.createdLine = new Path( // line from center to middle point of arc
                new MoveTo(centerX, centerY),
                new LineTo(coordinates[0], coordinates[1])
        );
        createdLine.setStroke(palette[currentIteration % palette.length]);
        createdLine.setStrokeWidth(5);
    }
    private void createArc(int currIteration){
        // Draw the next wedge. The start angle for the wedge
        // is angles[i].  The ending angle is angles[i+1}, so
        // the number of degrees in the wedge is
        // angles[i+1] - angles[i].
        this.createdArc = new Arc(centerX,centerY,radius,radius, 0, angles[currIteration+1] - angles[currIteration]);
        createdArc.setType(ArcType.ROUND);
        createdArc.setFill(palette[currIteration % palette.length]);
        createdArc.setStartAngle(450);

//        createdArc.lengthProperty().addListener(e->{
//            String newText = calculateNewPercentage(createdArc.getLength());
//            createdText.setText(newText + "$$");
//        });
//        createdArc.hoverProperty().addListener( e -> {
//            Label label = new Label(sliceData.getName() + " is " + sliceData.getPieValue());
//            label.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));
//            label.setTextFill(Color.SILVER);
//        });
    }
    private void createTextForSlice(int iteration) {
        double[] coordinates = calculateXYofArcsMiddle(iteration, 'T');
        double arcLength = angles[iteration+1] - angles[iteration];

        this.createdText = new Text(coordinates[0], coordinates[1], calculateNewPercentage(arcLength));
        createdText.setFill(palette[iteration % palette.length]);
        createdText.setFont(Font.font("Lato", FontWeight.BOLD, FontPosture.REGULAR, 20));
        createdText.setWrappingWidth(90);
        createdText.setTextAlignment(TextAlignment.LEFT);
        createdText.setTranslateY(5);

        // Выравнивание относительно четверти на графике
//        createdText.xProperty().addListener(e->{
//            if (createdText.getX() < centerX)
//                createdText.setTranslateX(createdText.getWrappingWidth() / -2.0);
//            else if (createdText.getX() > centerX)
//                createdText.setTranslateX(-5);
//
//            System.out.println("THIS IS LISTENER 1 " + createdText.getTranslateX());
//        });
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
}